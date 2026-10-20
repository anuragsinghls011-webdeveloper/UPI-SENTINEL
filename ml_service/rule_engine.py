"""
Deterministic Rule Engine for UPI Sentinel.
Evaluates explicit fraud signals, blacklists, domain safety, and invoice consistency.
Each rule returns structured evidence for explainability.
"""

from typing import List, Dict, Any
from schema import TransactionInput

class RuleResult:
    def __init__(self, rule_id: str, triggered: bool, severity: str, message: str, evidence: Dict[str, Any], signal_type: str):
        self.rule_id = rule_id
        self.triggered = triggered
        self.severity = severity  # "CRITICAL", "HIGH", "MEDIUM", "LOW", "SAFE"
        self.message = message
        self.evidence = evidence
        self.signal_type = signal_type

    def to_dict(self) -> Dict[str, Any]:
        return {
            "rule_id": self.rule_id,
            "triggered": self.triggered,
            "severity": self.severity,
            "message": self.message,
            "evidence": self.evidence,
            "type": self.signal_type
        }

class RuleEngine:
    """
    Deterministic rule engine running ahead of and in parallel with ML.
    Never conceals rule decisions inside the frontend or blackbox.
    """

    def evaluate(self, txn: TransactionInput) -> List[RuleResult]:
        results: List[RuleResult] = []

        # 1. Suspicious / Phishing Domain Rule
        if txn.suspicious_domain or txn.domain_reputation_score < 30.0:
            results.append(RuleResult(
                rule_id="RULE_DOMAIN_PHISHING_001",
                triggered=True,
                severity="CRITICAL",
                message="Payment link or QR domain has severe phishing/quishing risk indicators",
                evidence={
                    "suspicious_domain": txn.suspicious_domain,
                    "domain_reputation_score": txn.domain_reputation_score
                },
                signal_type="domain"
            ))

        # 2. Shortened / Obfuscated URL
        if txn.shortened_url:
            results.append(RuleResult(
                rule_id="RULE_URL_SHORTENER_002",
                triggered=True,
                severity="HIGH",
                message="Payment intent disguised via URL shortener or obfuscated redirect",
                evidence={"shortened_url": True},
                signal_type="domain"
            ))

        # 3. Merchant Legal Name Mismatch (Tampering / Impersonation)
        if txn.merchant_mismatch:
            results.append(RuleResult(
                rule_id="RULE_MERCHANT_MISMATCH_003",
                triggered=True,
                severity="CRITICAL",
                message="Merchant display identity conflicts with registered banking VPA name",
                evidence={"merchant_mismatch": True, "merchant_verified": txn.merchant_verified},
                signal_type="merchant"
            ))

        # 4. Unverified Merchant with Low Reputation
        if not txn.merchant_verified and txn.merchant_reputation_score < 40.0 and txn.transaction_type in ["P2M", "QR_STATIC", "QR_DYNAMIC"]:
            results.append(RuleResult(
                rule_id="RULE_MERCHANT_UNVERIFIED_004",
                triggered=True,
                severity="HIGH",
                message="Merchant is not bank-verified and carries low historical trust score",
                evidence={
                    "merchant_verified": False,
                    "merchant_reputation_score": txn.merchant_reputation_score
                },
                signal_type="merchant"
            ))

        # 5. Invoice Mismatch / Reverse Billing Exploit
        if txn.invoice_present and not txn.invoice_match:
            diff = abs(txn.amount - txn.invoice_amount)
            results.append(RuleResult(
                rule_id="RULE_INVOICE_MISMATCH_005",
                triggered=True,
                severity="CRITICAL",
                message=f"Transaction amount (₹{txn.amount:,.2f}) does not match stated invoice amount (₹{txn.invoice_amount:,.2f})",
                evidence={
                    "transaction_amount": txn.amount,
                    "invoice_amount": txn.invoice_amount,
                    "difference": diff
                },
                signal_type="invoice"
            ))

        # 6. High Recipient Blacklist / Abuse Score
        if txn.recipient_risk_score >= 60.0:
            results.append(RuleResult(
                rule_id="RULE_RECIPIENT_RISK_006",
                triggered=True,
                severity="HIGH",
                message="Recipient VPA has been flagged in cybercrime/dispute intelligence databases",
                evidence={"recipient_risk_score": txn.recipient_risk_score},
                signal_type="recipient"
            ))

        # 7. Midnight Account Drain Pattern (Unusual time + High deviation + New device)
        if txn.unusual_time and txn.new_device and txn.user_amount_deviation > 3.0:
            results.append(RuleResult(
                rule_id="RULE_UNUSUAL_HOURS_DRAIN_007",
                triggered=True,
                severity="CRITICAL",
                message="High-value midnight transaction initiated from an unrecognized device",
                evidence={
                    "hour_of_day": txn.hour_of_day,
                    "new_device": True,
                    "amount_deviation": txn.user_amount_deviation
                },
                signal_type="behaviour"
            ))

        # 8. Severe Behavioral Deviation
        if txn.behavioral_deviation_score >= 0.70:
            results.append(RuleResult(
                rule_id="RULE_BEHAVIOURAL_ANOMALY_008",
                triggered=True,
                severity="HIGH",
                message="Transaction spending velocity severely deviates from user 90-day baseline",
                evidence={"behavioral_deviation_score": txn.behavioral_deviation_score},
                signal_type="behaviour"
            ))

        # 9. Legitimate Safety Confirmation Rules
        if txn.merchant_verified and txn.invoice_match and txn.invoice_present and txn.recipient_known:
            results.append(RuleResult(
                rule_id="RULE_TRUSTED_CONTEXT_009",
                triggered=True,
                severity="SAFE",
                message="Verified merchant with matching verified invoice and established history",
                evidence={"verified": True, "invoice_match": True},
                signal_type="safety"
            ))

        return results
