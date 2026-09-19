package com.example.data.repository

import com.example.data.local.AuditLogEntity
import com.example.data.local.RiskAnalysisEntity
import com.example.data.local.SentinelDao
import com.example.data.local.TransactionEntity
import com.example.data.model.AuditLogEntry
import com.example.data.model.LatencyBreakdown
import com.example.data.model.ModelBenchmark
import com.example.data.model.RiskAnalysisResult
import com.example.data.model.RiskLevel
import com.example.data.model.RiskSignal
import com.example.data.model.TransactionInput
import com.example.engine.UnifiedRiskEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

class SentinelRepository(private val dao: SentinelDao) {

    init {
        CoroutineScope(Dispatchers.IO).launch {
            seedInitialDataIfEmpty()
        }
    }

    val allAnalyses: Flow<List<RiskAnalysisResult>> = dao.getAllRiskAnalyses().map { list ->
        list.map { entityToResult(it) }
    }

    val allAuditLogs: Flow<List<AuditLogEntry>> = dao.getAllAuditLogs().map { list ->
        list.map {
            AuditLogEntry(
                id = it.id,
                eventType = it.eventType,
                actor = it.actor,
                entityId = it.entityId,
                timestamp = it.timestamp,
                metadata = it.metadata
            )
        }
    }

    val totalCount: Flow<Int> = dao.getTotalAnalysesCount()
    val lowCount: Flow<Int> = dao.getAnalysesCountByLevel(RiskLevel.LOW.name)
    val mediumCount: Flow<Int> = dao.getAnalysesCountByLevel(RiskLevel.MEDIUM.name)
    val highCount: Flow<Int> = dao.getAnalysesCountByLevel(RiskLevel.HIGH.name)
    val criticalCount: Flow<Int> = dao.getAnalysesCountByLevel(RiskLevel.CRITICAL.name)
    val averageRiskScore: Flow<Double> = dao.getAverageRiskScore().map { it ?: 0.0 }

    suspend fun analyzeAndSave(tx: TransactionInput, actor: String = "analyst@sentinel.io"): RiskAnalysisResult {
        // Run analysis via unified engine
        val result = UnifiedRiskEngine.analyzeTransaction(tx)

        // Persist transaction
        dao.insertTransaction(txToEntity(tx))

        // Persist risk analysis
        dao.insertRiskAnalysis(resultToEntity(result, tx))

        // Persist audit log
        dao.insertAuditLog(
            AuditLogEntity(
                eventType = "TRANSACTION_ANALYZED",
                actor = actor,
                entityId = tx.transactionId,
                timestamp = System.currentTimeMillis(),
                metadata = "Amount: ₹${tx.amount.toInt()}, Score: ${result.riskScore}, Level: ${result.riskLevel.name}"
            )
        )

        return result
    }

    suspend fun getAnalysisById(id: String): RiskAnalysisResult? {
        val entity = dao.getRiskAnalysisDirect(id) ?: return null
        return entityToResult(entity)
    }

    suspend fun updateReview(id: String, status: String, note: String, actor: String = "analyst@sentinel.io") {
        dao.updateReviewStatus(id, status, note)
        dao.insertAuditLog(
            AuditLogEntity(
                eventType = "TRANSACTION_REVIEWED",
                actor = actor,
                entityId = id,
                timestamp = System.currentTimeMillis(),
                metadata = "Marked as $status. Note: $note"
            )
        )
    }

    suspend fun logEvent(eventType: String, actor: String, entityId: String, metadata: String) {
        dao.insertAuditLog(
            AuditLogEntity(
                eventType = eventType,
                actor = actor,
                entityId = entityId,
                timestamp = System.currentTimeMillis(),
                metadata = metadata
            )
        )
    }

    // Demo Scenarios Preset generator
    fun getDemoScenarios(): List<Pair<String, TransactionInput>> {
        return listOf(
            Pair(
                "SCENARIO 1: Safe ₹40,000",
                TransactionInput(
                    transactionId = "TXN-DEMO-SAFE-40K",
                    amount = 40000.0,
                    description = "Monthly Grocery Stock & Supplies",
                    receiverName = "FreshMart Retail Private Limited",
                    receiverId = "freshmart@okaxis",
                    receiverType = "MERCHANT",
                    receiverVerified = true,
                    merchantName = "FreshMart Retail",
                    merchantId = "MER-FMR-9821",
                    merchantVerified = true,
                    paymentUrl = "https://freshmart.in/checkout/pay",
                    domain = "freshmart.in",
                    invoiceId = "INV-2026-9042",
                    invoiceAmount = 40000.0,
                    invoiceMerchant = "FreshMart Retail",
                    invoiceReference = "TXN-DEMO-SAFE-40K",
                    userTransactionCount24h = 2,
                    averageTransactionAmount = 38000.0,
                    previousTransactionAmount = 35000.0,
                    receiverFirstSeen = false,
                    merchantFirstSeen = false,
                    failedAttempts = 0,
                    transactionFrequency = 1.2,
                    unusualTimeFlag = false
                )
            ),
            Pair(
                "SCENARIO 2: Suspicious ₹40,000",
                TransactionInput(
                    transactionId = "TXN-DEMO-SUSP-40K",
                    amount = 40000.0,
                    description = "Urgent Cashback Claim Deposit",
                    receiverName = "Ramesh Kumar Sharma (Personal)",
                    receiverId = "ramesh.k9182@ybl",
                    receiverType = "INDIVIDUAL",
                    receiverVerified = false,
                    merchantName = "FreshMart Retail",
                    merchantId = "MER-UNVERIFIED-33",
                    merchantVerified = false,
                    paymentUrl = "http://upi-pay-verify.xyz/lottery-claim?id=40000",
                    domain = "upi-pay-verify.xyz",
                    invoiceId = "INV-FAKE-001",
                    invoiceAmount = 4000.0,
                    invoiceMerchant = "MegaRewards Portal",
                    invoiceReference = "REF-UNMATCHED-999",
                    userTransactionCount24h = 16,
                    averageTransactionAmount = 3500.0,
                    previousTransactionAmount = 2500.0,
                    receiverFirstSeen = true,
                    merchantFirstSeen = true,
                    failedAttempts = 3,
                    transactionFrequency = 7.5,
                    unusualTimeFlag = true
                )
            ),
            Pair(
                "SCENARIO 3: High-Value Legitimate (₹1,50,000)",
                TransactionInput(
                    transactionId = "TXN-DEMO-HIGH-150K",
                    amount = 150000.0,
                    description = "Annual Electronics & Laptop Upgrade",
                    receiverName = "Reliance Digital Stores",
                    receiverId = "reliancedigital@hdfcbank",
                    receiverType = "MERCHANT",
                    receiverVerified = true,
                    merchantName = "Reliance Digital",
                    merchantId = "MER-REL-4412",
                    merchantVerified = true,
                    paymentUrl = "https://reliancedigital.in/pay",
                    domain = "reliancedigital.in",
                    invoiceId = "INV-REL-88912",
                    invoiceAmount = 150000.0,
                    invoiceMerchant = "Reliance Digital",
                    invoiceReference = "TXN-DEMO-HIGH-150K",
                    userTransactionCount24h = 1,
                    averageTransactionAmount = 65000.0,
                    previousTransactionAmount = 42000.0,
                    receiverFirstSeen = false,
                    merchantFirstSeen = false,
                    failedAttempts = 0,
                    transactionFrequency = 0.5,
                    unusualTimeFlag = false
                )
            ),
            Pair(
                "SCENARIO 4: New Receiver (Contextual signal, not fraud)",
                TransactionInput(
                    transactionId = "TXN-DEMO-NEW-REC-1200",
                    amount = 1200.0,
                    description = "Shared Dinner Split",
                    receiverName = "Aman Verma",
                    receiverId = "aman.verma@okhdfcbank",
                    receiverType = "INDIVIDUAL",
                    receiverVerified = true,
                    merchantName = "",
                    merchantId = "",
                    merchantVerified = true,
                    paymentUrl = "",
                    domain = "",
                    invoiceId = "",
                    invoiceAmount = 0.0,
                    invoiceMerchant = "",
                    invoiceReference = "",
                    userTransactionCount24h = 3,
                    averageTransactionAmount = 1500.0,
                    previousTransactionAmount = 1100.0,
                    receiverFirstSeen = true,
                    merchantFirstSeen = false,
                    failedAttempts = 0,
                    transactionFrequency = 1.0,
                    unusualTimeFlag = false
                )
            ),
            Pair(
                "SCENARIO 5: Behavioural Anomaly (Late Night Burst)",
                TransactionInput(
                    transactionId = "TXN-DEMO-ANOM-NIGHT",
                    amount = 55000.0,
                    description = "Late night transfer spike",
                    receiverName = "Apollo Pharmacy",
                    receiverId = "apollo@icici",
                    receiverType = "MERCHANT",
                    receiverVerified = true,
                    merchantName = "Apollo Pharmacy",
                    merchantId = "MER-APOLLO-102",
                    merchantVerified = true,
                    paymentUrl = "https://apollopharmacy.in",
                    domain = "apollopharmacy.in",
                    invoiceId = "INV-APO-9912",
                    invoiceAmount = 55000.0,
                    invoiceMerchant = "Apollo Pharmacy",
                    invoiceReference = "TXN-DEMO-ANOM-NIGHT",
                    userTransactionCount24h = 14,
                    averageTransactionAmount = 4500.0,
                    previousTransactionAmount = 3500.0,
                    receiverFirstSeen = false,
                    merchantFirstSeen = false,
                    failedAttempts = 2,
                    transactionFrequency = 6.0,
                    unusualTimeFlag = true
                )
            )
        )
    }

    private suspend fun seedInitialDataIfEmpty() {
        val existing = dao.getTransactionById("TXN-DEMO-SAFE-40K")
        if (existing == null) {
            val scenarios = getDemoScenarios()
            for ((_, tx) in scenarios) {
                analyzeAndSave(tx, actor = "system@sentinel.io")
            }
            dao.insertAuditLog(
                AuditLogEntity(
                    eventType = "SYSTEM_SEEDED",
                    actor = "system@sentinel.io",
                    entityId = "SYSTEM",
                    timestamp = System.currentTimeMillis(),
                    metadata = "Initialized 5 representative UPI Sentinel validation scenarios in database"
                )
            )
        }
    }

    // Mapping helpers
    private fun txToEntity(tx: TransactionInput): TransactionEntity {
        return TransactionEntity(
            transactionId = tx.transactionId,
            amount = tx.amount,
            currency = tx.currency,
            timestamp = tx.timestamp,
            transactionType = tx.transactionType,
            description = tx.description,
            receiverName = tx.receiverName,
            receiverId = tx.receiverId,
            receiverType = tx.receiverType,
            receiverVerified = tx.receiverVerified,
            merchantName = tx.merchantName,
            merchantId = tx.merchantId,
            merchantVerified = tx.merchantVerified,
            paymentUrl = tx.paymentUrl,
            domain = tx.domain,
            invoiceId = tx.invoiceId,
            invoiceAmount = tx.invoiceAmount,
            invoiceMerchant = tx.invoiceMerchant,
            invoiceReference = tx.invoiceReference,
            userTransactionCount24h = tx.userTransactionCount24h,
            averageTransactionAmount = tx.averageTransactionAmount,
            previousTransactionAmount = tx.previousTransactionAmount,
            lastTransactionTimestamp = tx.lastTransactionTimestamp,
            receiverFirstSeen = tx.receiverFirstSeen,
            merchantFirstSeen = tx.merchantFirstSeen,
            failedAttempts = tx.failedAttempts,
            transactionFrequency = tx.transactionFrequency,
            unusualTimeFlag = tx.unusualTimeFlag
        )
    }

    private fun resultToEntity(res: RiskAnalysisResult, tx: TransactionInput): RiskAnalysisEntity {
        val signalsArr = JSONArray()
        for (s in res.signals) {
            val obj = JSONObject()
            obj.put("category", s.category)
            obj.put("severity", s.severity)
            obj.put("finding", s.finding)
            obj.put("evidence", s.evidence)
            obj.put("riskContribution", s.riskContribution)
            signalsArr.put(obj)
        }

        val explArr = JSONArray(res.explanations)
        val countArr = JSONArray(res.counterfactuals)

        val featObj = JSONObject()
        for ((k, v) in res.featureVector) {
            featObj.put(k, v)
        }

        val latObj = JSONObject()
        latObj.put("featureExtractionMs", res.latency.featureExtractionMs)
        latObj.put("ruleEngineMs", res.latency.ruleEngineMs)
        latObj.put("mlInferenceMs", res.latency.mlInferenceMs)
        latObj.put("anomalyDetectionMs", res.latency.anomalyDetectionMs)
        latObj.put("totalTimeMs", res.latency.totalTimeMs)

        return RiskAnalysisEntity(
            transactionId = res.transactionId,
            amount = res.amount,
            riskScore = res.riskScore,
            riskLevel = res.riskLevel.name,
            confidence = res.confidence,
            mlScore = res.mlScore,
            anomalyScore = res.anomalyScore,
            receiverName = tx.receiverName,
            merchantName = tx.merchantName,
            domain = tx.domain.ifBlank { tx.paymentUrl },
            signalsJson = signalsArr.toString(),
            explanationsJson = explArr.toString(),
            counterfactualsJson = countArr.toString(),
            recommendedAction = res.recommendedAction,
            actionType = res.actionType,
            modelVersion = res.modelVersion,
            featureVectorJson = featObj.toString(),
            latencyJson = latObj.toString(),
            reviewStatus = res.reviewStatus,
            reviewNote = res.reviewNote,
            createdAt = res.createdAt
        )
    }

    private fun entityToResult(entity: RiskAnalysisEntity): RiskAnalysisResult {
        val signals = mutableListOf<RiskSignal>()
        try {
            val arr = JSONArray(entity.signalsJson)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                signals.add(
                    RiskSignal(
                        category = obj.optString("category", "general"),
                        severity = obj.optString("severity", "medium"),
                        finding = obj.optString("finding", ""),
                        evidence = obj.optString("evidence", ""),
                        riskContribution = obj.optInt("riskContribution", 10)
                    )
                )
            }
        } catch (_: Exception) {}

        val explanations = mutableListOf<String>()
        try {
            val arr = JSONArray(entity.explanationsJson)
            for (i in 0 until arr.length()) {
                explanations.add(arr.getString(i))
            }
        } catch (_: Exception) {}

        val counterfactuals = mutableListOf<String>()
        try {
            val arr = JSONArray(entity.counterfactualsJson)
            for (i in 0 until arr.length()) {
                counterfactuals.add(arr.getString(i))
            }
        } catch (_: Exception) {}

        var latency = LatencyBreakdown(2, 2, 4, 2, 10)
        try {
            val obj = JSONObject(entity.latencyJson)
            latency = LatencyBreakdown(
                featureExtractionMs = obj.optLong("featureExtractionMs", 2),
                ruleEngineMs = obj.optLong("ruleEngineMs", 2),
                mlInferenceMs = obj.optLong("mlInferenceMs", 4),
                anomalyDetectionMs = obj.optLong("anomalyDetectionMs", 2),
                totalTimeMs = obj.optLong("totalTimeMs", 10)
            )
        } catch (_: Exception) {}

        return RiskAnalysisResult(
            transactionId = entity.transactionId,
            amount = entity.amount,
            riskScore = entity.riskScore,
            riskLevel = RiskLevel.valueOf(entity.riskLevel),
            confidence = entity.confidence,
            mlScore = entity.mlScore,
            anomalyScore = entity.anomalyScore,
            signals = signals,
            explanations = explanations,
            counterfactuals = counterfactuals,
            recommendedAction = entity.recommendedAction,
            actionType = entity.actionType,
            modelVersion = entity.modelVersion,
            featureVector = emptyMap(),
            latency = latency,
            reviewStatus = entity.reviewStatus,
            reviewNote = entity.reviewNote,
            createdAt = entity.createdAt
        )
    }
}
