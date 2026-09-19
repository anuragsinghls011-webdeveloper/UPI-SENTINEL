package com.example.engine

import com.example.data.model.LatencyBreakdown
import com.example.data.model.RiskAnalysisResult
import com.example.data.model.RiskLevel
import com.example.data.model.RiskSignal
import com.example.data.model.TransactionInput

object UnifiedRiskEngine {

    fun analyzeTransaction(tx: TransactionInput): RiskAnalysisResult {
        val totalStart = System.nanoTime()

        // 1. Feature extraction & sub-analyzers
        val featStart = System.nanoTime()
        val domainRes = DomainAnalyzer.analyze(tx.paymentUrl.ifBlank { tx.domain }, tx.merchantName)
        val receiverRes = ReceiverAnalyzer.analyze(tx)
        val invoiceRes = InvoiceAnalyzer.analyze(tx)
        val behaviourRes = BehaviourAnalyzer.analyze(tx)

        val features = FeatureExtractor.extract(
            tx = tx,
            domainRiskScore = domainRes.domainRiskScore,
            isDomainMerchantMatch = domainRes.merchantMatch,
            receiverMerchantMatch = receiverRes.merchantMatch,
            invoiceAmountMatch = invoiceRes.amountMatch,
            invoiceMerchantMatch = invoiceRes.merchantMatch,
            amountDeviationRatio = behaviourRes.amountDeviationRatio,
            velocityAnomaly = behaviourRes.velocityAnomaly
        )
        val featEnd = System.nanoTime()

        // 2. Deterministic Rule Engine
        val ruleStart = System.nanoTime()
        val ruleResults = RuleEngine.evaluate(
            tx = tx,
            domainRisk = domainRes.domainRiskScore,
            domainReputation = domainRes.reputation,
            receiverMerchantMatch = receiverRes.merchantMatch,
            invoiceAmountMatch = invoiceRes.amountMatch,
            invoiceMerchantMatch = invoiceRes.merchantMatch,
            amountDeviationRatio = behaviourRes.amountDeviationRatio,
            velocityAnomaly = behaviourRes.velocityAnomaly
        )
        val ruleEnd = System.nanoTime()

        // 3. Machine Learning Inference (XGBoost Ensemble)
        val mlStart = System.nanoTime()
        val mlResult = MlRiskEngine.predict(features)
        val mlEnd = System.nanoTime()

        // 4. Anomaly Detection (Isolation Forest)
        val anomStart = System.nanoTime()
        val anomResult = AnomalyDetector.detect(features)
        val anomEnd = System.nanoTime()

        // Aggregate Signals from sub-analyzers and triggered rules
        val allSignals = mutableListOf<RiskSignal>()
        allSignals.addAll(domainRes.signals)
        allSignals.addAll(receiverRes.signals)
        allSignals.addAll(invoiceRes.signals)
        allSignals.addAll(behaviourRes.signals)

        for (r in ruleResults) {
            if (r.triggered && allSignals.none { it.evidence == r.evidence }) {
                allSignals.add(
                    RiskSignal(
                        category = r.category,
                        severity = r.severity,
                        finding = r.finding,
                        evidence = r.evidence,
                        riskContribution = r.riskContribution
                    )
                )
            }
        }

        // Composite risk score calculation (0 - 100)
        // Blending ML probability (55%), Rule accumulation (30%), Anomaly detection (15%)
        val ruleScoreAccumulated = ruleResults.filter { it.triggered }.sumOf { it.riskContribution }
        val normalizedRuleScore = ruleScoreAccumulated.coerceIn(0, 100).toDouble()
        val mlScore100 = mlResult.probability * 100.0
        val anomScore100 = anomResult.anomalyScore * 100.0

        val compositeScoreDouble = (mlScore100 * 0.55) + (normalizedRuleScore * 0.30) + (anomScore100 * 0.15)
        val finalRiskScore = compositeScoreDouble.toInt().coerceIn(0, 100)
        val riskLevel = RiskLevel.fromScore(finalRiskScore)

        // Explanations generation
        val explanations = mutableListOf<String>()
        if (!receiverRes.isVerified) {
            explanations.add("Receiver VPA is unverified by bank or payment registry.")
        }
        if (!receiverRes.merchantMatch) {
            explanations.add("Payment destination is a personal account, but transaction claims to be for merchant '${tx.merchantName}'.")
        }
        if (domainRes.reputation == "SUSPICIOUS" || domainRes.domainRiskScore >= 0.40) {
            explanations.add("Payment URL routing points to an unverified or suspicious external domain.")
        }
        if (invoiceRes.invoiceExists && !invoiceRes.amountMatch) {
            explanations.add("Invoice amount (₹${String.format("%,.0f", tx.invoiceAmount)}) differs from requested transfer (₹${String.format("%,.0f", tx.amount)}).")
        }
        if (invoiceRes.invoiceExists && !invoiceRes.merchantMatch) {
            explanations.add("Invoice issuer (${tx.invoiceMerchant}) does not match designated merchant (${tx.merchantName}).")
        }
        if (behaviourRes.isAnomalous) {
            explanations.add("Transaction behaviour is anomalous compared to your historical baseline (${behaviourRes.signals.firstOrNull()?.finding ?: "Unusual payment pattern"}).")
        }
        if (explanations.isEmpty()) {
            explanations.add("Receiver and merchant credentials are fully verified.")
            explanations.add("Digital invoice and payment context match expected records.")
            explanations.add("Transaction behaviour is consistent with your standard profile.")
        }

        // Counterfactual safety actions ("What would make this transaction safer?")
        val counterfactuals = mutableListOf<String>()
        if (!receiverRes.isVerified) {
            counterfactuals.add("Request a verified Merchant QR code or registered business VPA.")
        }
        if (!invoiceRes.amountMatch || !invoiceRes.merchantMatch) {
            counterfactuals.add("Request an updated official invoice matching the exact payable amount.")
        }
        if (domainRes.domainRiskScore >= 0.30) {
            counterfactuals.add("Open the merchant's official mobile application directly instead of following an external link.")
        }
        if (behaviourRes.isAnomalous) {
            counterfactuals.add("Verify recipient identity independently via a secure out-of-band channel (e.g. phone call).")
        }
        if (counterfactuals.isEmpty()) {
            counterfactuals.add("All baseline integrity checks passed. Maintain standard vigilance for UPI PIN entry.")
        }

        // Actionable guidance
        val (actionRecommendation, actionType) = when (riskLevel) {
            RiskLevel.LOW -> Pair(
                "Transaction appears consistent with verified context. Proceed with normal awareness.",
                "PROCEED"
            )
            RiskLevel.MEDIUM -> Pair(
                "Some contextual signals require verification. Verify merchant identity before entering UPI PIN.",
                "VERIFY"
            )
            RiskLevel.HIGH -> Pair(
                "Multiple risk indicators detected. We strongly advise verifying invoice and receiver before proceeding.",
                "VERIFY"
            )
            RiskLevel.CRITICAL -> Pair(
                "Multiple high-severity risk indicators detected. DO NOT PROCEED without independent out-of-band verification.",
                "DO_NOT_PROCEED"
            )
        }

        val totalEnd = System.nanoTime()

        val latency = LatencyBreakdown(
            featureExtractionMs = ((featEnd - featStart) / 1_000_000L).coerceAtLeast(1L),
            ruleEngineMs = ((ruleEnd - ruleStart) / 1_000_000L).coerceAtLeast(1L),
            mlInferenceMs = ((mlEnd - mlStart) / 1_000_000L).coerceAtLeast(2L),
            anomalyDetectionMs = ((anomEnd - anomStart) / 1_000_000L).coerceAtLeast(1L),
            totalTimeMs = ((totalEnd - totalStart) / 1_000_000L).coerceAtLeast(5L)
        )

        return RiskAnalysisResult(
            transactionId = tx.transactionId,
            amount = tx.amount,
            riskScore = finalRiskScore,
            riskLevel = riskLevel,
            confidence = mlResult.confidence,
            mlScore = mlResult.probability,
            anomalyScore = anomResult.anomalyScore,
            signals = allSignals,
            explanations = explanations,
            counterfactuals = counterfactuals,
            recommendedAction = actionRecommendation,
            actionType = actionType,
            modelVersion = mlResult.modelVersion,
            featureVector = features.vector,
            latency = latency,
            reviewStatus = "PENDING",
            reviewNote = "",
            createdAt = System.currentTimeMillis(),
            rawTransaction = tx
        )
    }
}
