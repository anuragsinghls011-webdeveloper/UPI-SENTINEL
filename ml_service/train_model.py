"""
Training Pipeline for UPI Sentinel Fraud Detection.
Trains:
1. ColumnTransformer Preprocessor (Handles Numerical Scaling, Boolean Imputation, Categorical One-Hot)
2. Supervised XGBoost Classifier with scale_pos_weight
3. Unsupervised IsolationForest for Behavioural & Context Anomaly Detection
4. SHAP TreeExplainer for Explainable AI
Persists all artifacts to ml_service/artifacts/
"""

import os
import json
import time
import joblib
import numpy as np
import pandas as pd
from sklearn.model_selection import train_test_split
from sklearn.preprocessing import StandardScaler, OneHotEncoder
from sklearn.compose import ColumnTransformer
from sklearn.ensemble import IsolationForest
from sklearn.metrics import (
    precision_score, recall_score, f1_score, roc_auc_score,
    average_precision_score, confusion_matrix, classification_report
)
import xgboost as xgb
import shap

from schema import NUMERICAL_FEATURES, BOOLEAN_FEATURES, CATEGORICAL_FEATURES, ALL_MODEL_FEATURES

def train_upi_sentinel_pipeline(
    data_path: str = "ml_service/data/v1/synthetic_transactions.csv",
    artifacts_dir: str = "ml_service/artifacts"
):
    print(f"--- [Phase 1] Loading and Validating Data from {data_path} ---")
    df = pd.read_csv(data_path)
    print(f"Total records: {len(df)}")
    
    X = df[ALL_MODEL_FEATURES].copy()
    y = df["fraud_label"].values

    # Train / Validation / Test split (70 / 15 / 15) with stratification
    X_train_val, X_test, y_train_val, y_test = train_test_split(
        X, y, test_size=0.15, random_state=42, stratify=y
    )
    X_train, X_val, y_train, y_val = train_test_split(
        X_train_val, y_train_val, test_size=0.17647, random_state=42, stratify=y_train_val
    )

    print(f"Train split: {len(X_train)} (Frauds: {y_train.sum()})")
    print(f"Val split:   {len(X_val)} (Frauds: {y_val.sum()})")
    print(f"Test split:  {len(X_test)} (Frauds: {y_test.sum()})")

    # --- [Phase 2] Build Scikit-Learn Preprocessing Pipeline ---
    print("\n--- [Phase 2] Fitting Preprocessor ---")
    preprocessor = ColumnTransformer(
        transformers=[
            ("num", StandardScaler(), NUMERICAL_FEATURES),
            ("bool", "passthrough", BOOLEAN_FEATURES),
            ("cat", OneHotEncoder(handle_unknown="ignore", sparse_output=False), CATEGORICAL_FEATURES)
        ],
        remainder="drop"
    )

    X_train_proc = preprocessor.fit_transform(X_train)
    X_val_proc = preprocessor.transform(X_val)
    X_test_proc = preprocessor.transform(X_test)

    # Get transformed feature names for SHAP
    cat_feature_names = list(preprocessor.named_transformers_["cat"].get_feature_names_out(CATEGORICAL_FEATURES))
    processed_feature_names = NUMERICAL_FEATURES + BOOLEAN_FEATURES + cat_feature_names

    # --- [Phase 3] Train Isolation Forest Anomaly Detector ---
    print("\n--- [Phase 3] Training Behavioural Isolation Forest ---")
    # Train anomaly detector primarily on legitimate baseline behaviour
    X_train_legit = X_train_proc[y_train == 0]
    isolation_forest = IsolationForest(
        n_estimators=100,
        contamination=0.08,
        random_state=42,
        n_jobs=-1
    )
    isolation_forest.fit(X_train_legit)

    # --- [Phase 4] Train XGBoost Supervised Classifier ---
    print("\n--- [Phase 4] Training XGBoost Classifier with Imbalance Weighting ---")
    neg_count = (y_train == 0).sum()
    pos_count = (y_train == 1).sum()
    scale_pos_weight = neg_count / max(1, pos_count)
    print(f"Calculated scale_pos_weight: {scale_pos_weight:.2f}")

    xgb_model = xgb.XGBClassifier(
        n_estimators=150,
        max_depth=5,
        learning_rate=0.08,
        subsample=0.85,
        colsample_bytree=0.85,
        scale_pos_weight=scale_pos_weight,
        eval_metric=["logloss", "aucpr"],
        random_state=42,
        n_jobs=-1
    )

    xgb_model.fit(
        X_train_proc, y_train,
        eval_set=[(X_val_proc, y_val)],
        verbose=False
    )

    # --- [Phase 5] Evaluation on Unseen Test Set ---
    print("\n--- [Phase 5] Evaluating Model on Unseen Test Set ---")
    y_test_pred_prob = xgb_model.predict_proba(X_test_proc)[:, 1]
    y_test_pred = (y_test_pred_prob >= 0.50).astype(int)

    pr_auc = average_precision_score(y_test, y_test_pred_prob)
    roc_auc = roc_auc_score(y_test, y_test_pred_prob)
    precision = precision_score(y_test, y_test_pred)
    recall = recall_score(y_test, y_test_pred)
    f1 = f1_score(y_test, y_test_pred)
    cm = confusion_matrix(y_test, y_test_pred).tolist()

    print(f"Primary Metric (PR-AUC): {pr_auc:.4f}")
    print(f"ROC-AUC:                 {roc_auc:.4f}")
    print(f"Precision:               {precision:.4f}")
    print(f"Recall:                  {recall:.4f}")
    print(f"F1-Score:                {f1:.4f}")
    print(f"Confusion Matrix [TN, FP], [FN, TP]: {cm}")

    # Evaluate Anomaly Detection Scores on Test Set
    anomaly_raw_scores = isolation_forest.decision_function(X_test_proc)
    # Convert decision function to normalized anomaly score [0, 1] where 1 is highly anomalous
    test_anomaly_scores = 1.0 - (anomaly_raw_scores - anomaly_raw_scores.min()) / (anomaly_raw_scores.max() - anomaly_raw_scores.min() + 1e-6)

    # --- [Phase 6] SHAP Explainer Initialization ---
    print("\n--- [Phase 6] Building SHAP TreeExplainer ---")
    # TreeExplainer runs natively fast on XGBoost trees
    explainer = shap.TreeExplainer(xgb_model)

    # --- [Phase 7] Save Artifacts ---
    print(f"\n--- [Phase 7] Persisting Production Artifacts to {artifacts_dir} ---")
    os.makedirs(artifacts_dir, exist_ok=True)

    joblib.dump(preprocessor, os.path.join(artifacts_dir, "preprocessor_v1.joblib"))
    joblib.dump(xgb_model, os.path.join(artifacts_dir, "xgb_model_v1.joblib"))
    joblib.dump(isolation_forest, os.path.join(artifacts_dir, "isolation_forest_v1.joblib"))
    joblib.dump(explainer, os.path.join(artifacts_dir, "shap_explainer_v1.joblib"))

    # Feature metadata
    feature_metadata = {
        "raw_features": ALL_MODEL_FEATURES,
        "numerical_features": NUMERICAL_FEATURES,
        "boolean_features": BOOLEAN_FEATURES,
        "categorical_features": CATEGORICAL_FEATURES,
        "processed_feature_names": processed_feature_names,
        "model_version": "xgb-v1.0.0",
        "anomaly_version": "iforest-v1.0.0",
        "schema_version": "transaction-v1",
        "explainer_version": "shap-tree-v1"
    }
    with open(os.path.join(artifacts_dir, "feature_metadata.json"), "w") as f:
        json.dump(feature_metadata, f, indent=2)

    # Metrics report
    metrics_report = {
        "evaluation_dataset_size": len(X_test),
        "fraud_count_in_test": int(y_test.sum()),
        "pr_auc": round(float(pr_auc), 4),
        "roc_auc": round(float(roc_auc), 4),
        "precision": round(float(precision), 4),
        "recall": round(float(recall), 4),
        "f1_score": round(float(f1), 4),
        "confusion_matrix": cm,
        "classification_report": classification_report(y_test, y_test_pred, output_dict=True)
    }
    with open(os.path.join(artifacts_dir, "metrics.json"), "w") as f:
        json.dump(metrics_report, f, indent=2)

    print("Pipeline training, evaluation, and artifact serialization completed successfully!")
    return metrics_report

if __name__ == "__main__":
    train_upi_sentinel_pipeline()
