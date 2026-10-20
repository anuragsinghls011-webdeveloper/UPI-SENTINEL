"""
FastAPI Microservice for UPI Sentinel Risk & Anomaly Analysis.
Exposes low-latency endpoints (<300ms) with:
- Dynamic XGBoost inference
- Isolation Forest anomaly detection
- Deterministic Rule Engine
- SHAP feature attribution
- Grounded explainability & safety advisories
"""

import os
import time
import json
import joblib
import numpy as np
import pandas as pd
from typing import Dict, Any, List, Optional
from fastapi import FastAPI, HTTPException, Request
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel

from schema import TransactionInput, ALL_MODEL_FEATURES
from rule_engine import RuleEngine
from risk_engine import UnifiedRiskScorer, RiskExplanationBuilder

app = FastAPI(
    title="UPI Sentinel ML & Risk Engine",
    description="Context-Aware UPI Fraud Risk & Anomaly Scoring Service",
    version="1.0.0"
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Global artifacts loaded at startup
ARTIFACTS_DIR = "ml_service/artifacts"
preprocessor = None
xgb_model = None
isolation_forest = None
explainer = None
rule_engine = RuleEngine()
feature_names = []
model_metadata = {}

@app.on_event("startup")
def load_ml_artifacts():
    global preprocessor, xgb_model, isolation_forest, explainer, feature_names, model_metadata
    print(f"Loading ML models and artifacts from {ARTIFACTS_DIR}...")
    try:
        preprocessor = joblib.load(os.path.join(ARTIFACTS_DIR, "preprocessor_v1.joblib"))
        xgb_model = joblib.load(os.path.join(ARTIFACTS_DIR, "xgb_model_v1.joblib"))
        isolation_forest = joblib.load(os.path.join(ARTIFACTS_DIR, "isolation_forest_v1.joblib"))
        explainer = joblib.load(os.path.join(ARTIFACTS_DIR, "shap_explainer_v1.joblib"))
        
        with open(os.path.join(ARTIFACTS_DIR, "feature_metadata.json"), "r") as f:
            model_metadata = json.load(f)
            feature_names = model_metadata["processed_feature_names"]
        print("ML Artifacts and SHAP TreeExplainer loaded successfully!")
    except Exception as e:
        print(f"ERROR: Failed to load ML artifacts: {e}")

class AnalyzeRequest(BaseModel):
    transaction: TransactionInput

class FeedbackRequest(BaseModel):
    transaction_id: str
    user_action: str  # "PAID", "ABORTED", "REPORTED_FRAUD", "MARKED_SAFE"
    user_notes: Optional[str] = None

# In-memory store for feedback & metrics (persisted to disk)
FEEDBACK_LOG = []
INFERENCE_METRICS = {
    "total_requests": 0,
    "fraud_detected_count": 0,
    "total_latency_ms": 0.0
}

@app.get("/health")
def health():
    return {
        "status": "HEALTHY",
        "service": "upi-sentinel-ml",
        "timestamp": time.time()
    }

@app.get("/api/v1/model/health")
def model_health():
    is_ready = all([preprocessor, xgb_model, isolation_forest, explainer])
    return {
        "status": "UP" if is_ready else "DEGRADED",
        "models_loaded": {
            "preprocessor": preprocessor is not None,
            "xgboost": xgb_model is not None,
            "isolation_forest": isolation_forest is not None,
            "shap_explainer": explainer is not None
        }
    }

@app.get("/api/v1/model/version")
def model_version():
    return {
        "model_version": "xgb-v1.0.0",
        "anomaly_version": "iforest-v1.0.0",
        "feature_schema_version": "transaction-v1",
        "explainer_version": "shap-tree-v1"
    }

@app.get("/api/v1/metrics")
def get_metrics():
    avg_latency = (
        INFERENCE_METRICS["total_latency_ms"] / max(1, INFERENCE_METRICS["total_requests"])
    )
    metrics_path = os.path.join(ARTIFACTS_DIR, "metrics.json")
    offline_metrics = {}
    if os.path.exists(metrics_path):
        with open(metrics_path, "r") as f:
            offline_metrics = json.load(f)

    return {
        "live_metrics": {
            "total_inspections": INFERENCE_METRICS["total_requests"],
            "fraud_flagged_count": INFERENCE_METRICS["fraud_detected_count"],
            "average_latency_ms": round(avg_latency, 2),
            "feedback_received_count": len(FEEDBACK_LOG)
        },
        "model_evaluation_metrics": offline_metrics
    }

@app.post("/api/v1/feedback")
def submit_feedback(fb: FeedbackRequest):
    record = {
        "transaction_id": fb.transaction_id,
        "user_action": fb.user_action,
        "user_notes": fb.user_notes,
        "timestamp": time.time()
    }
    FEEDBACK_LOG.append(record)
    with open("ml_service/artifacts/feedback_records.jsonl", "a") as f:
        f.write(json.dumps(record) + "\n")
    return {"status": "SUCCESS", "message": "Feedback recorded for model retraining cycle."}

@app.post("/api/v1/risk/analyze")
def analyze_transaction(request: AnalyzeRequest):
    t0 = time.time()
    txn = request.transaction

    # 1. Deterministic Rule Engine Inspection
    rule_hits = rule_engine.evaluate(txn)

    # 2. Preprocess Single Transaction Record
    txn_dict = txn.dict()
    df_single = pd.DataFrame([txn_dict])
    
    try:
        X_proc = preprocessor.transform(df_single[ALL_MODEL_FEATURES])
    except Exception as e:
        raise HTTPException(status_code=400, detail=f"Feature preprocessing error: {str(e)}")

    # 3. Supervised XGBoost Inference
    pred_prob = float(xgb_model.predict_proba(X_proc)[0, 1])

    # 4. Unsupervised Isolation Forest Anomaly Detection
    anomaly_raw = float(isolation_forest.decision_function(X_proc)[0])
    # Normalize score: lower decision function means higher anomaly
    norm_anomaly_score = float(np.clip(1.0 - (anomaly_raw + 0.25) / 0.5, 0.0, 1.0))
    if norm_anomaly_score >= 0.70:
        anomaly_level = "CRITICAL" if norm_anomaly_score >= 0.85 else "HIGH"
    elif norm_anomaly_score >= 0.45:
        anomaly_level = "MEDIUM"
    else:
        anomaly_level = "LOW"

    # 5. SHAP TreeExplainer Attribution
    shap_vals = explainer.shap_values(X_proc)[0]
    top_shap = RiskExplanationBuilder.map_shap_contributions(feature_names, shap_vals, top_k=5)

    # 6. Unified Context Risk Scoring
    risk_score, risk_level = UnifiedRiskScorer.compute_risk(
        fraud_prob=pred_prob,
        anomaly_score=norm_anomaly_score,
        rule_hits=rule_hits
    )

    # 7. Grounded Reasons & Recommendations
    reasons = RiskExplanationBuilder.build_reasons(
        rule_hits=rule_hits,
        top_shap=top_shap,
        anomaly_score=norm_anomaly_score,
        anomaly_level=anomaly_level
    )
    recommendation = RiskExplanationBuilder.build_recommendation(risk_level, rule_hits)

    processing_time_ms = round((time.time() - t0) * 1000, 2)

    # Update telemetry
    INFERENCE_METRICS["total_requests"] += 1
    if risk_score >= 60:
        INFERENCE_METRICS["fraud_detected_count"] += 1
    INFERENCE_METRICS["total_latency_ms"] += processing_time_ms

    return {
        "transaction_id": txn.transaction_id,
        "risk_score": risk_score,
        "risk_level": risk_level,
        "fraud_probability": round(pred_prob, 4),
        "anomaly": {
            "score": round(norm_anomaly_score, 4),
            "level": anomaly_level
        },
        "rule_hits": [r.to_dict() for r in rule_hits],
        "top_shap_features": top_shap,
        "reasons": reasons,
        "recommendation": recommendation,
        "model_version": "xgb-v1.0.0",
        "explanation_version": "v1.0",
        "processing_time_ms": processing_time_ms
    }

if __name__ == "__main__":
    import uvicorn
    uvicorn.run("main:app", host="0.0.0.0", port=8000, reload=False)
