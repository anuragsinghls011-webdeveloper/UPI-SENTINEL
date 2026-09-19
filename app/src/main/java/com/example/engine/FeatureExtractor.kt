package com.example.engine

import com.example.data.model.TransactionInput
import kotlin.math.ln

object FeatureExtractor {

    data class ExtractedFeatures(
        val vector: Map<String, Double>,
        val amount: Double,
        val receiverVerified: Double,
        val merchantVerified: Double,
        val domainRisk: Double,
        val invoiceAmountMatch: Double,
        val invoiceMerchantMatch: Double,
        val receiverNovelty: Double,
        val merchantNovelty: Double,
        val amountDeviation: Double,
        val timeAnomaly: Double,
        val velocityAnomaly: Double,
        val failedAttempts: Double,
        val transactionFrequency: Double,
        val merchantReceiverMatch: Double,
        // Enhanced ML features & Non-linear interactions
        val vpaEntropy: Double,
        val suspiciousTld: Double,
        val urgencyIndicator: Double,
        val noveltyMismatchInteraction: Double,
        val domainUnverifiedInteraction: Double,
        val nightTimeVelocityInteraction: Double,
        val urgencyAmountInteraction: Double
    )

    fun extract(
        tx: TransactionInput,
        domainRiskScore: Double,
        isDomainMerchantMatch: Boolean,
        receiverMerchantMatch: Boolean,
        invoiceAmountMatch: Boolean,
        invoiceMerchantMatch: Boolean,
        amountDeviationRatio: Double,
        velocityAnomaly: Boolean
    ): ExtractedFeatures {
        val amountNorm = (tx.amount / 100000.0).coerceIn(0.0, 5.0)
        val recVerified = if (tx.receiverVerified) 1.0 else 0.0
        val merVerified = if (tx.merchantVerified) 1.0 else 0.0
        val domRisk = domainRiskScore.coerceIn(0.0, 1.0)
        val invAmtMatch = if (invoiceAmountMatch) 1.0 else 0.0
        val invMerMatch = if (invoiceMerchantMatch) 1.0 else 0.0
        val recNovelty = if (tx.receiverFirstSeen) 1.0 else 0.0
        val merNovelty = if (tx.merchantFirstSeen) 1.0 else 0.0
        val amtDev = (amountDeviationRatio / 5.0).coerceIn(0.0, 2.0)
        val timeAnom = if (tx.unusualTimeFlag) 1.0 else 0.0
        val velAnom = if (velocityAnomaly) 1.0 else 0.0
        val failAtt = (tx.failedAttempts / 5.0).coerceIn(0.0, 1.0)
        val txnFreq = (tx.transactionFrequency / 10.0).coerceIn(0.0, 2.0)
        val merRecMatch = if (receiverMerchantMatch && isDomainMerchantMatch) 1.0 else 0.0

        // 1. VPA Shannon Entropy Calculation (detects randomly generated fraud handles)
        val vpaHandle = tx.receiverId.substringBefore("@", tx.receiverId)
        val vpaEntropyVal = calculateEntropy(vpaHandle)

        // 2. High-Risk TLD & Domain string heuristic
        val urlLower = (tx.paymentUrl.ifBlank { tx.domain }).lowercase()
        val suspiciousTldVal = if (urlLower.contains(".xyz") || urlLower.contains(".top") ||
            urlLower.contains(".click") || urlLower.contains(".live") ||
            urlLower.contains(".link") || urlLower.contains(".site") ||
            urlLower.contains("upi-") || urlLower.contains("verify-")
        ) 1.0 else 0.0

        // 3. Urgency & Social Engineering NLP keywords in note or merchant name
        val textCorpus = "${tx.description} ${tx.merchantName}".lowercase()
        val urgencyKeywords = listOf("urgent", "immediately", "block", "lottery", "cashback", "refund", "kyc", "electricity bill", "disconnected", "prize")
        val urgencyVal = if (urgencyKeywords.any { textCorpus.contains(it) }) 1.0 else 0.0

        // 4. Non-linear Interaction terms
        val noveltyMismatch = if (recNovelty > 0.5 && (invAmtMatch < 0.5 || invMerMatch < 0.5)) 1.0 else 0.0
        val domainUnverified = domRisk * (1.0 - recVerified)
        val nightTimeVelocity = timeAnom * velAnom
        val urgencyAmount = urgencyVal * (amtDev.coerceAtLeast(0.5))

        val map = mapOf(
            "amount_normalized" to amountNorm,
            "receiver_verified" to recVerified,
            "merchant_verified" to merVerified,
            "domain_risk" to domRisk,
            "invoice_amount_match" to invAmtMatch,
            "invoice_merchant_match" to invMerMatch,
            "receiver_novelty" to recNovelty,
            "merchant_novelty" to merNovelty,
            "amount_deviation" to amtDev,
            "time_anomaly" to timeAnom,
            "velocity_anomaly" to velAnom,
            "failed_attempts" to failAtt,
            "transaction_frequency" to txnFreq,
            "merchant_receiver_match" to merRecMatch,
            "vpa_entropy" to vpaEntropyVal,
            "suspicious_tld" to suspiciousTldVal,
            "urgency_indicator" to urgencyVal,
            "novelty_mismatch_interaction" to noveltyMismatch,
            "domain_unverified_interaction" to domainUnverified,
            "night_velocity_interaction" to nightTimeVelocity,
            "urgency_amount_interaction" to urgencyAmount
        )

        return ExtractedFeatures(
            vector = map,
            amount = amountNorm,
            receiverVerified = recVerified,
            merchantVerified = merVerified,
            domainRisk = domRisk,
            invoiceAmountMatch = invAmtMatch,
            invoiceMerchantMatch = invMerMatch,
            receiverNovelty = recNovelty,
            merchantNovelty = merNovelty,
            amountDeviation = amtDev,
            timeAnomaly = timeAnom,
            velocityAnomaly = velAnom,
            failedAttempts = failAtt,
            transactionFrequency = txnFreq,
            merchantReceiverMatch = merRecMatch,
            vpaEntropy = vpaEntropyVal,
            suspiciousTld = suspiciousTldVal,
            urgencyIndicator = urgencyVal,
            noveltyMismatchInteraction = noveltyMismatch,
            domainUnverifiedInteraction = domainUnverified,
            nightTimeVelocityInteraction = nightTimeVelocity,
            urgencyAmountInteraction = urgencyAmount
        )
    }

    private fun calculateEntropy(text: String): Double {
        if (text.length <= 1) return 0.0
        val freqMap = mutableMapOf<Char, Int>()
        for (c in text) freqMap[c] = (freqMap[c] ?: 0) + 1
        var entropy = 0.0
        val len = text.length.toDouble()
        for ((_, count) in freqMap) {
            val p = count / len
            entropy -= p * (ln(p) / ln(2.0))
        }
        // Normalize against max theoretical entropy (log2(len))
        val maxEntropy = ln(len) / ln(2.0)
        return if (maxEntropy > 0) (entropy / maxEntropy).coerceIn(0.0, 1.0) else 0.0
    }
}
