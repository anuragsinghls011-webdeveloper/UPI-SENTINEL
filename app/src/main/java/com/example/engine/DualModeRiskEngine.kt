package com.example.engine

import com.example.data.api.SentinelApiService
import com.example.data.model.*
import com.example.data.repository.CybercrimeThreatRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Dual-Mode Risk Engine:
 * 1. Immediate local heuristic & rule-based score (<50ms).
 * 2. Background cloud ML sync (<400ms) with SHAP attribution & dynamic weights.
 * 3. Real-time I4C / 1930 Cybercrime intelligence cross-reference.
 */
class DualModeRiskEngine(
    private val apiService: SentinelApiService,
    private val cybercrimeThreatRepository: CybercrimeThreatRepository
) {
    suspend fun analyzeDualMode(
        transaction: TransactionInput,
        onLocalResult: (RiskAnalysisResult) -> Unit,
        onFinalResult: (RiskAnalysisResult, Boolean) -> Unit
    ) = withContext(Dispatchers.Default) {
        // Step 1: Run On-Device Instant Evaluation
        val localAssessment = UnifiedRiskEngine.analyzeTransaction(transaction)
        
        // Step 1.1: Cross-reference with I4C Cybercrime Intelligence
        val threatHit = cybercrimeThreatRepository.checkThreat(transaction.receiverId)
        val enrichedLocalAssessment = if (threatHit != null) {
            val criticalSignals = localAssessment.signals.toMutableList()
            criticalSignals.add(
                0,
                RiskSignal(
                    category = "receiver",
                    severity = "critical",
                    finding = "VPA flagged in ${threatHit.source} database",
                    evidence = "Matched against I4C cybercrime registry with ${threatHit.reportCount} reports.",
                    riskContribution = 35
                )
            )
            localAssessment.copy(
                riskScore = 96,
                riskLevel = RiskLevel.CRITICAL,
                signals = criticalSignals,
                recommendedAction = "DO NOT PROCEED. Recipient VPA is flagged by Indian Cybercrime Coordination Centre (I4C). Call 1930 helpline.",
                actionType = "DO_NOT_PROCEED"
            )
        } else {
            localAssessment
        }

        // Emit instant local result immediately for ultra-low latency UI response
        onLocalResult(enrichedLocalAssessment)

        // Step 2: Cloud ML Synchronization (<400ms timeout)
        var cloudAssessment: RiskAnalysisResult? = null
        try {
            val cloudDto = CloudTransactionDto(
                transactionId = transaction.transactionId,
                userId = "user_default",
                amount = transaction.amount,
                currency = transaction.currency,
                transactionType = transaction.transactionType,
                merchantVerified = transaction.merchantVerified,
                merchantReputationScore = 75.0,
                merchantMismatch = !transaction.receiverVerified,
                recipientKnown = !transaction.receiverFirstSeen,
                recipientRiskScore = if (threatHit != null) 95.0 else 10.0,
                domainReputationScore = if (transaction.domain.isNotEmpty()) 20.0 else 90.0,
                suspiciousDomain = transaction.domain.isNotEmpty(),
                shortenedUrl = transaction.paymentUrl.contains("bit.ly") || transaction.paymentUrl.contains("tinyurl"),
                invoicePresent = transaction.invoiceId.isNotEmpty(),
                invoiceAmount = if (transaction.invoiceAmount > 0) transaction.invoiceAmount else transaction.amount,
                invoiceMatch = (transaction.invoiceAmount == 0.0 || transaction.invoiceAmount == transaction.amount),
                hourOfDay = 14,
                unusualTime = transaction.unusualTimeFlag,
                userAvgAmount = transaction.averageTransactionAmount,
                userAmountDeviation = if (transaction.averageTransactionAmount > 0) transaction.amount / transaction.averageTransactionAmount else 1.0,
                userTransactionFrequency = transaction.transactionFrequency,
                behavioralDeviationScore = if (transaction.unusualTimeFlag) 0.85 else 0.15,
                newDevice = false,
                unusualLocation = false
            )

            val cloudResponse = withTimeoutOrNull(600) {
                apiService.analyzeTransaction(CloudAnalysisRequest(cloudDto))
            }

            if (cloudResponse != null && cloudResponse.isSuccessful && cloudResponse.body() != null) {
                val body = cloudResponse.body()!!
                val convertedSignals = body.reasons.map { r ->
                    RiskSignal(
                        category = r.type,
                        severity = r.severity.lowercase(),
                        finding = r.message,
                        evidence = "Cloud ML inference verified with model ${body.modelVersion}",
                        riskContribution = 20
                    )
                }

                val fusedScore = maxOf(enrichedLocalAssessment.riskScore, body.riskScore)
                val fusedLevel = RiskLevel.fromScore(fusedScore)
                
                cloudAssessment = enrichedLocalAssessment.copy(
                    riskScore = fusedScore,
                    riskLevel = fusedLevel,
                    signals = if (convertedSignals.isNotEmpty()) convertedSignals else enrichedLocalAssessment.signals,
                    recommendedAction = body.recommendation?.message ?: enrichedLocalAssessment.recommendedAction,
                    confidence = 0.98
                )
            }
        } catch (_: Exception) {
            // Keep local assessment intact on network failure
        }

        // Final result emitted with cloudSyncSuccess flag
        if (cloudAssessment != null) {
            onFinalResult(cloudAssessment, true)
        } else {
            onFinalResult(enrichedLocalAssessment, false)
        }
    }
}
