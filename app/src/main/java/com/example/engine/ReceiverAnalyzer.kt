package com.example.engine

import com.example.data.model.RiskSignal
import com.example.data.model.TransactionInput

object ReceiverAnalyzer {

    data class ReceiverAnalysisResult(
        val receiverScore: Double, // 0.0 (safe) to 1.0 (risky)
        val isVerified: Boolean,
        val isNovel: Boolean,
        val merchantMatch: Boolean,
        val signals: List<RiskSignal>
    )

    fun analyze(tx: TransactionInput): ReceiverAnalysisResult {
        val signals = mutableListOf<RiskSignal>()
        var score = 0.0

        val isVerified = tx.receiverVerified
        val isNovel = tx.receiverFirstSeen

        if (!isVerified) {
            score += 0.35
            signals.add(
                RiskSignal(
                    category = "receiver",
                    severity = "medium",
                    finding = "Receiver VPA (${tx.receiverId.ifBlank { tx.receiverName }}) is not NPCI/Bank verified",
                    evidence = "Unverified individual/merchant entity flag",
                    riskContribution = 15
                )
            )
        }

        if (isNovel) {
            score += 0.20
            signals.add(
                RiskSignal(
                    category = "receiver",
                    severity = "low",
                    finding = "First-time transfer: You have never transacted with this receiver before",
                    evidence = "Receiver novelty flag active (first interaction timestamp)",
                    riskContribution = 10
                )
            )
        }

        // Check consistency between merchant and receiver
        val merchantMatch = if (tx.merchantName.isNotBlank() && tx.receiverName.isNotBlank()) {
            val mClean = tx.merchantName.lowercase().replace(Regex("[^a-z0-9]"), "")
            val rClean = tx.receiverName.lowercase().replace(Regex("[^a-z0-9]"), "")
            mClean.contains(rClean) || rClean.contains(mClean) || tx.receiverType != "INDIVIDUAL"
        } else true

        if (!merchantMatch) {
            score += 0.30
            signals.add(
                RiskSignal(
                    category = "receiver",
                    severity = "high",
                    finding = "Receiver entity mismatch: Claimed merchant is '${tx.merchantName}', but receiver is individual '${tx.receiverName}'",
                    evidence = "Expected verified merchant account, but destination is personal account (${tx.receiverId})",
                    riskContribution = 18
                )
            )
        }

        return ReceiverAnalysisResult(
            receiverScore = score.coerceIn(0.0, 1.0),
            isVerified = isVerified,
            isNovel = isNovel,
            merchantMatch = merchantMatch,
            signals = signals
        )
    }
}
