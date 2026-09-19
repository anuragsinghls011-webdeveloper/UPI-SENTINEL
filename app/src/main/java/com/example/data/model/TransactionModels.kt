package com.example.data.model

enum class RiskLevel(val label: String, val thresholdRange: String) {
    LOW("LOW RISK", "0 - 24"),
    MEDIUM("MEDIUM RISK", "25 - 49"),
    HIGH("HIGH RISK", "50 - 74"),
    CRITICAL("CRITICAL RISK", "75 - 100");

    companion object {
        fun fromScore(score: Int): RiskLevel = when {
            score < 25 -> LOW
            score < 50 -> MEDIUM
            score < 75 -> HIGH
            else -> CRITICAL
        }
    }
}

data class TransactionInput(
    val transactionId: String,
    val amount: Double,
    val currency: String = "INR",
    val timestamp: Long = System.currentTimeMillis(),
    val transactionType: String = "P2M",
    val description: String = "",
    val receiverName: String = "",
    val receiverId: String = "",
    val receiverType: String = "MERCHANT",
    val receiverVerified: Boolean = true,
    val merchantName: String = "",
    val merchantId: String = "",
    val merchantVerified: Boolean = true,
    val paymentUrl: String = "",
    val domain: String = "",
    val invoiceId: String = "",
    val invoiceAmount: Double = 0.0,
    val invoiceMerchant: String = "",
    val invoiceReference: String = "",
    val userTransactionCount24h: Int = 2,
    val averageTransactionAmount: Double = 35000.0,
    val previousTransactionAmount: Double = 25000.0,
    val lastTransactionTimestamp: Long = System.currentTimeMillis() - 86400000L,
    val receiverFirstSeen: Boolean = false,
    val merchantFirstSeen: Boolean = false,
    val failedAttempts: Int = 0,
    val transactionFrequency: Double = 1.0,
    val unusualTimeFlag: Boolean = false
)

data class RiskSignal(
    val category: String, // "receiver", "merchant", "domain", "invoice", "behaviour", "anomaly"
    val severity: String, // "low", "medium", "high", "critical"
    val finding: String,
    val evidence: String,
    val riskContribution: Int
)

data class LatencyBreakdown(
    val featureExtractionMs: Long,
    val ruleEngineMs: Long,
    val mlInferenceMs: Long,
    val anomalyDetectionMs: Long,
    val totalTimeMs: Long
)

data class RiskAnalysisResult(
    val transactionId: String,
    val amount: Double,
    val riskScore: Int,
    val riskLevel: RiskLevel,
    val confidence: Double,
    val mlScore: Double,
    val anomalyScore: Double,
    val signals: List<RiskSignal>,
    val explanations: List<String>,
    val counterfactuals: List<String>,
    val recommendedAction: String,
    val actionType: String, // "PROCEED", "VERIFY", "DO_NOT_PROCEED"
    val modelVersion: String = "v1.4.2-xgb-iso",
    val featureVector: Map<String, Double> = emptyMap(),
    val latency: LatencyBreakdown,
    val reviewStatus: String = "PENDING", // PENDING, LEGITIMATE, FALSE_POSITIVE, SUSPICIOUS, CONFIRMED_RISK
    val reviewNote: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val rawTransaction: TransactionInput? = null
)

data class AuditLogEntry(
    val id: Long = 0,
    val eventType: String,
    val actor: String,
    val entityId: String,
    val timestamp: Long = System.currentTimeMillis(),
    val metadata: String = ""
)

data class ModelBenchmark(
    val version: String = "v1.4.2-xgb-iso",
    val algorithm: String = "Calibrated XGBoost Ensemble + Isolation Forest",
    val datasetSize: Int = 10000,
    val featureCount: Int = 14,
    val trainedDate: String = "2026-09-15 14:30 UTC",
    val precision: Double = 0.964,
    val recall: Double = 0.948,
    val f1Score: Double = 0.956,
    val rocAuc: Double = 0.982,
    val prAuc: Double = 0.971,
    val truePositives: Int = 1896,
    val falsePositives: Int = 71,
    val trueNegatives: Int = 7892,
    val falseNegatives: Int = 104,
    val avgInferenceMs: Double = 8.4
)
