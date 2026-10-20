"""
Canonical Transaction Feature Schema for UPI Sentinel.
Shared across Frontend, Node.js Orchestrator, FastAPI ML Service, and Offline Training.
"""

from typing import Optional, Dict, Any, List
from pydantic import BaseModel, Field

# 24 core features used in preprocessing & XGBoost + IsolationForest
NUMERICAL_FEATURES = [
    "amount",
    "merchant_reputation_score",
    "recipient_risk_score",
    "recipient_transaction_count",
    "domain_reputation_score",
    "invoice_amount",
    "hour_of_day",
    "user_avg_amount",
    "user_amount_deviation",
    "user_transaction_frequency",
    "behavioral_deviation_score",
    "location_deviation_score",
    "historical_transaction_count",
    "historical_failed_count",
    "historical_flagged_count"
]

BOOLEAN_FEATURES = [
    "merchant_verified",
    "merchant_mismatch",
    "recipient_known",
    "suspicious_domain",
    "shortened_url",
    "invoice_present",
    "invoice_match",
    "unusual_time",
    "new_device",
    "unusual_location"
]

CATEGORICAL_FEATURES = [
    "transaction_type"  # e.g., P2P, P2M, QR_DYNAMIC, QR_STATIC, INTENT
]

ALL_MODEL_FEATURES = NUMERICAL_FEATURES + BOOLEAN_FEATURES + CATEGORICAL_FEATURES

class TransactionInput(BaseModel):
    transaction_id: str = Field(..., description="Unique transaction ID")
    user_id: Optional[str] = Field("user_default", description="User identifier")
    amount: float = Field(..., ge=0.0, description="Amount in INR")
    currency: str = Field("INR", description="Currency code")
    transaction_type: str = Field("P2M", description="P2P, P2M, QR_DYNAMIC, QR_STATIC, INTENT")

    # Merchant signals
    merchant_verified: bool = Field(False, description="Whether merchant KYC/VPA is verified by bank/NPCI")
    merchant_reputation_score: float = Field(50.0, ge=0.0, le=100.0, description="Merchant trust score 0-100")
    merchant_mismatch: bool = Field(False, description="VPA DBA name does not match legal entity name")

    # Recipient / UPI ID signals
    recipient_known: bool = Field(False, description="Has user transacted with recipient previously")
    recipient_risk_score: float = Field(0.0, ge=0.0, le=100.0, description="Global blacklist / abuse score 0-100")
    recipient_transaction_count: int = Field(0, ge=0, description="Historical interactions with this recipient")

    # Domain / URL signals
    domain_reputation_score: float = Field(80.0, ge=0.0, le=100.0, description="Web/domain reputation score 0-100")
    suspicious_domain: bool = Field(False, description="Domain matches known quishing/phishing regex/feed")
    shortened_url: bool = Field(False, description="Bitly, tinyurl, or suspicious redirect link")

    # Invoice signals
    invoice_present: bool = Field(False, description="Invoice document or digital receipt provided")
    invoice_amount: float = Field(0.0, ge=0.0, description="Amount stated on invoice")
    invoice_match: bool = Field(True, description="Invoice amount matches transaction amount within tolerance")

    # Timing signals
    hour_of_day: int = Field(12, ge=0, le=23, description="Hour transaction initiated")
    unusual_time: bool = Field(False, description="Outside user's normal active hours (e.g. 2 AM - 5 AM)")

    # User Behaviour signals
    user_avg_amount: float = Field(1500.0, ge=0.0, description="User 90-day moving average transaction amount")
    user_amount_deviation: float = Field(1.0, ge=0.0, description="Ratio of amount to user_avg_amount")
    user_transaction_frequency: float = Field(2.5, ge=0.0, description="Average transactions per day")
    behavioral_deviation_score: float = Field(0.1, ge=0.0, le=1.0, description="Isolation behavioral deviation")

    # Device / Location signals
    new_device: bool = Field(False, description="First time transaction from this device fingerprint")
    unusual_location: bool = Field(False, description="Geo-location outside usual cluster")
    location_deviation_score: float = Field(0.0, ge=0.0, le=1.0, description="Distance deviation score")

    # History
    historical_transaction_count: int = Field(50, ge=0, description="User's total completed transactions")
    historical_failed_count: int = Field(0, ge=0, description="Recent failed transactions")
    historical_flagged_count: int = Field(0, ge=0, description="Transactions flagged as disputed")
