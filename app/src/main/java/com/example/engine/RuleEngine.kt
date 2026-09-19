package com.example.engine

import com.example.data.model.RiskSignal
import com.example.data.model.TransactionInput

object RuleEngine {

    data class RuleResult(
        val ruleId: String,
        val category: String,
        val severity: String,
        val finding: String,
        val evidence: String,
        val riskContribution: Int,
        val triggered: Boolean
    )

    fun evaluate(
        tx: TransactionInput,
        domainRisk: Double,
        domainReputation: String,
        receiverMerchantMatch: Boolean,
        invoiceAmountMatch: Boolean,
        invoiceMerchantMatch: Boolean,
        amountDeviationRatio: Double,
        velocityAnomaly: Boolean
    ): List<RuleResult> {
        val results = mutableListOf<RuleResult>()

        // RULE-001: Unverified receiver
        results.add(
            RuleResult(
                ruleId = "RULE-001",
                category = "receiver",
                severity = "medium",
                finding = "Unverified receiver entity",
                evidence = "Receiver VPA (${tx.receiverId}) has not completed banking KYC/Merchant verification",
                riskContribution = 12,
                triggered = !tx.receiverVerified
            )
        )

        // RULE-002: Receiver/merchant mismatch
        results.add(
            RuleResult(
                ruleId = "RULE-002",
                category = "receiver",
                severity = "high",
                finding = "Receiver entity does not match stated merchant",
                evidence = "Stated merchant is '${tx.merchantName}', but payment is routing to individual '${tx.receiverName}'",
                riskContribution = 18,
                triggered = !receiverMerchantMatch && tx.merchantName.isNotBlank() && tx.receiverName.isNotBlank()
            )
        )

        // RULE-003: Suspicious domain
        results.add(
            RuleResult(
                ruleId = "RULE-003",
                category = "domain",
                severity = "critical",
                finding = "Payment gateway domain flagged as suspicious or insecure",
                evidence = "Domain '${tx.domain.ifBlank { tx.paymentUrl }}' reputation is $domainReputation (risk score: ${(domainRisk * 100).toInt()}%)",
                riskContribution = 22,
                triggered = domainRisk >= 0.40 || domainReputation == "SUSPICIOUS"
            )
        )

        // RULE-004: Invoice amount mismatch
        val hasInvoice = tx.invoiceId.isNotBlank() || tx.invoiceAmount > 0
        results.add(
            RuleResult(
                ruleId = "RULE-004",
                category = "invoice",
                severity = "high",
                finding = "Payment amount does not match invoice bill amount",
                evidence = "Transaction amount is ₹${String.format("%,.0f", tx.amount)} while invoice #${tx.invoiceId} states ₹${String.format("%,.0f", tx.invoiceAmount)}",
                riskContribution = 16,
                triggered = hasInvoice && !invoiceAmountMatch
            )
        )

        // RULE-005: Invoice merchant mismatch
        results.add(
            RuleResult(
                ruleId = "RULE-005",
                category = "invoice",
                severity = "high",
                finding = "Invoice issuer differs from payment merchant",
                evidence = "Invoice issued by '${tx.invoiceMerchant}' while payment merchant is '${tx.merchantName}'",
                riskContribution = 15,
                triggered = hasInvoice && !invoiceMerchantMatch && tx.invoiceMerchant.isNotBlank()
            )
        )

        // RULE-006: Unusual amount
        results.add(
            RuleResult(
                ruleId = "RULE-006",
                category = "behaviour",
                severity = "medium",
                finding = "Payment amount significantly deviates from personal baseline",
                evidence = "Amount ₹${String.format("%,.0f", tx.amount)} is ${String.format("%.1f", amountDeviationRatio)}x your average payment baseline",
                riskContribution = 14,
                triggered = amountDeviationRatio >= 3.0
            )
        )

        // RULE-007: New receiver
        results.add(
            RuleResult(
                ruleId = "RULE-007",
                category = "receiver",
                severity = "low",
                finding = "First-time payment to this receiver",
                evidence = "No prior transaction record for receiver VPA in your historical log",
                riskContribution = 10,
                triggered = tx.receiverFirstSeen
            )
        )

        // RULE-008: Unusual transaction frequency
        results.add(
            RuleResult(
                ruleId = "RULE-008",
                category = "behaviour",
                severity = "medium",
                finding = "High transaction frequency / burst velocity detected",
                evidence = "User initiated ${tx.userTransactionCount24h} transactions in past 24 hours",
                riskContribution = 12,
                triggered = velocityAnomaly || tx.userTransactionCount24h > 10
            )
        )

        // RULE-009: Unusual transaction timing
        results.add(
            RuleResult(
                ruleId = "RULE-009",
                category = "behaviour",
                severity = "low",
                finding = "Transaction initiated during anomalous night-time window",
                evidence = "Timestamp falls outside standard diurnal activity profile (2 AM - 5 AM)",
                riskContribution = 8,
                triggered = tx.unusualTimeFlag
            )
        )

        // RULE-010: Multiple simultaneous risk indicators
        val activePriorRules = results.count { it.triggered }
        results.add(
            RuleResult(
                ruleId = "RULE-010",
                category = "rule",
                severity = "critical",
                finding = "Compound risk: Multiple independent risk factors triggered simultaneously",
                evidence = "$activePriorRules contextual risk signals active in a single transaction",
                riskContribution = 15,
                triggered = activePriorRules >= 3
            )
        )

        return results
    }
}
