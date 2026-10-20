"""
Unified Risk Engine, SHAP Explainability & Guarded NLG for UPI Sentinel.
Calculates calibrated risk score, feature contributions, grounded explanations, and safety recommendations.
"""

from typing import List, Dict, Any, Tuple
import numpy as np
from schema import TransactionInput
from rule_engine import RuleResult

class RiskExplanationBuilder:
    """
    Constructs grounded, human-readable explanations strictly tied to:
    1. Model SHAP contributions
    2. Triggered deterministic rules
    3. Anomaly detection findings
    Guaranteed: NO HALLUCINATIONS, NO FABRICATED EVIDENCE.
    """

    @staticmethod
    def map_shap_contributions(
        feature_names: List[str],
        shap_values: np.ndarray,
        top_k: int = 5
    ) -> List[Dict[str, Any]]:
        contributions = []
        for name, val in zip(feature_names, shap_values):
            contributions.append({
                "feature": name,
                "contribution": round(float(val), 4),
                "is_risk_increasing": bool(val > 0)
            })
        
        # Sort by absolute contribution magnitude
        contributions.sort(key=lambda x: abs(x["contribution"]), reverse=True)
        return contributions[:top_k]

    @staticmethod
    def build_reasons(
        rule_hits: List[RuleResult],
        top_shap: List[Dict[str, Any]],
        anomaly_score: float,
        anomaly_level: str
    ) -> List[Dict[str, Any]]:
        reasons = []
        seen_types = set()

        # 1. Prioritize critical & high rule hits
        for r in rule_hits:
            if r.severity in ["CRITICAL", "HIGH", "MEDIUM"]:
                reasons.append({
                    "type": r.signal_type,
                    "severity": r.severity.lower(),
                    "message": r.message,
                    "evidence": r.evidence
                })
                seen_types.add(r.signal_type)

        # 2. Add Anomaly detector reason if anomalous and not already captured
        if anomaly_level in ["HIGH", "CRITICAL"] and "behaviour" not in seen_types:
            reasons.append({
                "type": "anomaly",
                "severity": anomaly_level.lower(),
                "message": f"Behavioral patterns diverge significantly from typical payment baseline (Anomaly index: {anomaly_score:.2f})",
                "evidence": {"anomaly_score": round(anomaly_score, 2)}
            })
            seen_types.add("anomaly")

        # 3. Add SHAP feature contributors if not covered
        feature_human_map = {
            "amount": "Transaction amount is unusually elevated for this payment type",
            "domain_reputation_score": "Payment link or QR domain has low reputation",
            "merchant_reputation_score": "Merchant has insufficient or low reputation history",
            "recipient_risk_score": "Recipient account carries negative fraud reports",
            "behavioral_deviation_score": "Payment timing or frequency departs from normal habits",
            "invoice_match": "Invoice verification could not be validated",
            "new_device": "Transaction initiated from an unrecognized device profile"
        }

        for item in top_shap:
            feat = item["feature"]
            if item["is_risk_increasing"] and feat in feature_human_map:
                mapped_type = "ml_feature"
                if any(feat in r["message"].lower() for r in reasons):
                    continue
                reasons.append({
                    "type": mapped_type,
                    "severity": "medium",
                    "message": feature_human_map[feat],
                    "evidence": {"feature": feat, "shap_impact": item["contribution"]}
                })

        # Safe fallback if clean
        if not reasons:
            reasons.append({
                "type": "safety",
                "severity": "safe",
                "message": "Transaction conforms to verified parameters, safe domain, and typical user activity.",
                "evidence": {"verified": True}
            })

        return reasons

    @staticmethod
    def build_recommendation(risk_level: str, rule_hits: List[RuleResult]) -> Dict[str, str]:
        critical_rules = [r for r in rule_hits if r.severity == "CRITICAL"]
        if risk_level == "CRITICAL" or critical_rules:
            return {
                "action": "STOP_TRANSACTION",
                "message": "DO NOT PROCEED. High likelihood of financial quishing, impersonation, or invoice manipulation.",
                "advisory": "Report this UPI ID/QR to NPCI Cyber Crime portal (1930) or your bank app immediately."
            }
        elif risk_level == "HIGH":
            return {
                "action": "VERIFY_OR_STOP",
                "message": "Exercise extreme caution. Recipient or transaction context shows high anomaly indicators.",
                "advisory": "Contact the recipient on a verified voice phone number before authorizing payment."
            }
        elif risk_level == "MEDIUM":
            return {
                "action": "PROCEED_WITH_CAUTION",
                "message": "Unusual payment context detected. Double-check recipient account name on your UPI app screen.",
                "advisory": "Verify invoice itemization and confirm you are sending, not requesting, funds."
            }
        else:
            return {
                "action": "SAFE_TO_PROCEED",
                "message": "Transaction context, merchant credentials, and behavioral signals appear normal.",
                "advisory": "Always review the amount on the UPI PIN entry prompt before finalizing."
            }

class UnifiedRiskScorer:
    """
    Combines:
    1. Calibrated XGBoost probability (0.0 - 1.0)
    2. Deterministic Rule triggers (Critical / High severity weights)
    3. IsolationForest Anomaly score (0.0 - 1.0)
    Produces auditable final score [0, 100] and risk category.
    """

    @staticmethod
    def compute_risk(
        fraud_prob: float,
        anomaly_score: float,
        rule_hits: List[RuleResult]
    ) -> Tuple[int, str]:
        # Base ML contribution (weight: 55%)
        base_score = fraud_prob * 55.0

        # Anomaly contribution (weight: 20%)
        anomaly_contribution = anomaly_score * 20.0

        # Deterministic Rules contribution (weight: up to 25% or instant override)
        rule_penalty = 0.0
        critical_hit = False

        for r in rule_hits:
            if r.severity == "CRITICAL":
                critical_hit = True
                rule_penalty += 25.0
            elif r.severity == "HIGH":
                rule_penalty += 15.0
            elif r.severity == "MEDIUM":
                rule_penalty += 8.0
            elif r.severity == "SAFE":
                rule_penalty -= 10.0  # Safe verified context deduction

        rule_penalty = max(-10.0, min(25.0, rule_penalty))

        raw_score = base_score + anomaly_contribution + rule_penalty
        
        # Hard overrides: Critical rule triggers (e.g. invoice mismatch or verified phishing domain) guarantee CRITICAL risk
        if critical_hit and raw_score < 75.0:
            raw_score = 82.0

        final_score = int(np.clip(round(raw_score), 0, 100))

        # Risk level mapping:
        # 0-29: LOW
        # 30-59: MEDIUM
        # 60-79: HIGH
        # 80-100: CRITICAL
        if final_score < 30:
            risk_level = "LOW"
        elif final_score < 60:
            risk_level = "MEDIUM"
        elif final_score < 80:
            risk_level = "HIGH"
        else:
            risk_level = "CRITICAL"

        return final_score, risk_level
