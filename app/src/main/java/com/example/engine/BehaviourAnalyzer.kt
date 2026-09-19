package com.example.engine

import com.example.data.model.RiskSignal
import com.example.data.model.TransactionInput

object BehaviourAnalyzer {

    data class BehaviourAnalysisResult(
        val anomalyScore: Double, // 0.0 (normal) to 1.0 (extreme anomaly)
        val isAnomalous: Boolean,
        val amountDeviationRatio: Double,
        val velocityAnomaly: Boolean,
        val signals: List<RiskSignal>
    )

    fun analyze(tx: TransactionInput): BehaviourAnalysisResult {
        val signals = mutableListOf<RiskSignal>()
        var anomalyScore = 0.0

        // 1. Amount deviation analysis
        val avg = if (tx.averageTransactionAmount > 0) tx.averageTransactionAmount else 5000.0
        val deviationRatio = tx.amount / avg

        if (deviationRatio >= 4.0) {
            anomalyScore += 0.35
            signals.add(
                RiskSignal(
                    category = "behaviour",
                    severity = "medium",
                    finding = "Transaction amount is ${String.format("%.1f", deviationRatio)}x higher than your typical average (₹${String.format("%,.0f", avg)})",
                    evidence = "Requested: ₹${String.format("%,.0f", tx.amount)} vs 90-day baseline avg: ₹${String.format("%,.0f", avg)}",
                    riskContribution = 14
                )
            )
        } else if (deviationRatio >= 2.5) {
            anomalyScore += 0.15
            signals.add(
                RiskSignal(
                    category = "behaviour",
                    severity = "low",
                    finding = "Transaction is notably higher than your standard average",
                    evidence = "${String.format("%.1f", deviationRatio)}x baseline deviation",
                    riskContribution = 8
                )
            )
        }

        // 2. Velocity analysis (rapid transactions in 24h)
        val velocityAnomaly = tx.userTransactionCount24h > 12
        if (velocityAnomaly) {
            anomalyScore += 0.25
            signals.add(
                RiskSignal(
                    category = "behaviour",
                    severity = "high",
                    finding = "Unusual payment burst: ${tx.userTransactionCount24h} transactions initiated in the last 24 hours",
                    evidence = "Velocity threshold exceeded (>12 txn/24h baseline)",
                    riskContribution = 15
                )
            )
        }

        // 3. Timing anomaly (unusual late night flag, e.g., 2 AM - 5 AM)
        if (tx.unusualTimeFlag) {
            anomalyScore += 0.15
            signals.add(
                RiskSignal(
                    category = "behaviour",
                    severity = "low",
                    finding = "Irregular transaction timestamp (outside your typical operating window)",
                    evidence = "Late-night / anomalous hour flag triggered",
                    riskContribution = 9
                )
            )
        }

        // 4. Repeated failed attempts
        if (tx.failedAttempts >= 2) {
            anomalyScore += 0.30
            signals.add(
                RiskSignal(
                    category = "behaviour",
                    severity = "high",
                    finding = "${tx.failedAttempts} prior failed authorization attempts detected immediately preceding this request",
                    evidence = "Brute-force / coercion signal flag",
                    riskContribution = 18
                )
            )
        }

        val isAnomalous = anomalyScore >= 0.40

        return BehaviourAnalysisResult(
            anomalyScore = anomalyScore.coerceIn(0.0, 1.0),
            isAnomalous = isAnomalous,
            amountDeviationRatio = deviationRatio,
            velocityAnomaly = velocityAnomaly,
            signals = signals
        )
    }
}
