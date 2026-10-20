package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CloudAnalysisRequest(
    @Json(name = "transaction") val transaction: CloudTransactionDto
)

@JsonClass(generateAdapter = true)
data class CloudTransactionDto(
    @Json(name = "transaction_id") val transactionId: String,
    @Json(name = "user_id") val userId: String = "user_default",
    @Json(name = "amount") val amount: Double,
    @Json(name = "currency") val currency: String = "INR",
    @Json(name = "transaction_type") val transactionType: String = "P2M",
    @Json(name = "merchant_verified") val merchantVerified: Boolean = false,
    @Json(name = "merchant_reputation_score") val merchantReputationScore: Double = 50.0,
    @Json(name = "merchant_mismatch") val merchantMismatch: Boolean = false,
    @Json(name = "recipient_known") val recipientKnown: Boolean = false,
    @Json(name = "recipient_risk_score") val recipientRiskScore: Double = 0.0,
    @Json(name = "recipient_transaction_count") val recipientTransactionCount: Int = 0,
    @Json(name = "domain_reputation_score") val domainReputationScore: Double = 80.0,
    @Json(name = "suspicious_domain") val suspiciousDomain: Boolean = false,
    @Json(name = "shortened_url") val shortenedUrl: Boolean = false,
    @Json(name = "invoice_present") val invoicePresent: Boolean = false,
    @Json(name = "invoice_amount") val invoiceAmount: Double = 0.0,
    @Json(name = "invoice_match") val invoiceMatch: Boolean = true,
    @Json(name = "hour_of_day") val hourOfDay: Int = 12,
    @Json(name = "unusual_time") val unusualTime: Boolean = false,
    @Json(name = "user_avg_amount") val userAvgAmount: Double = 1500.0,
    @Json(name = "user_amount_deviation") val userAmountDeviation: Double = 1.0,
    @Json(name = "user_transaction_frequency") val userTransactionFrequency: Double = 2.5,
    @Json(name = "behavioral_deviation_score") val behavioralDeviationScore: Double = 0.1,
    @Json(name = "new_device") val newDevice: Boolean = false,
    @Json(name = "unusual_location") val unusualLocation: Boolean = false,
    @Json(name = "location_deviation_score") val locationDeviationScore: Double = 0.0,
    @Json(name = "historical_transaction_count") val historicalTransactionCount: Int = 50,
    @Json(name = "historical_failed_count") val historicalFailedCount: Int = 0,
    @Json(name = "historical_flagged_count") val historicalFlaggedCount: Int = 0
)

@JsonClass(generateAdapter = true)
data class CloudAnalysisResponse(
    @Json(name = "transaction_id") val transactionId: String,
    @Json(name = "risk_score") val riskScore: Int,
    @Json(name = "risk_level") val riskLevel: String,
    @Json(name = "fraud_probability") val fraudProbability: Double,
    @Json(name = "anomaly") val anomaly: CloudAnomalyDto? = null,
    @Json(name = "top_shap_features") val topShapFeatures: List<CloudShapFeatureDto> = emptyList(),
    @Json(name = "reasons") val reasons: List<CloudReasonDto> = emptyList(),
    @Json(name = "recommendation") val recommendation: CloudRecommendationDto? = null,
    @Json(name = "model_version") val modelVersion: String = "xgb-v1.0.0",
    @Json(name = "processing_time_ms") val processingTimeMs: Double = 0.0
)

@JsonClass(generateAdapter = true)
data class CloudAnomalyDto(
    @Json(name = "score") val score: Double,
    @Json(name = "level") val level: String
)

@JsonClass(generateAdapter = true)
data class CloudShapFeatureDto(
    @Json(name = "feature") val feature: String,
    @Json(name = "contribution") val contribution: Double,
    @Json(name = "is_risk_increasing") val isRiskIncreasing: Boolean
)

@JsonClass(generateAdapter = true)
data class CloudReasonDto(
    @Json(name = "type") val type: String,
    @Json(name = "severity") val severity: String,
    @Json(name = "message") val message: String
)

@JsonClass(generateAdapter = true)
data class CloudRecommendationDto(
    @Json(name = "action") val action: String,
    @Json(name = "message") val message: String,
    @Json(name = "advisory") val advisory: String? = null
)

@JsonClass(generateAdapter = true)
data class CybercrimeThreatRecord(
    val vpa: String,
    val threatCategory: String, // "PHISHING", "INVESTMENT_SCAM", "LOTTERY_FRAUD", "MULE_ACCOUNT"
    val reportCount: Int,
    val source: String, // "I4C", "1930_HELPLINE", "COMMUNITY"
    val lastReportedDate: String,
    val isConfirmedFraud: Boolean
)
