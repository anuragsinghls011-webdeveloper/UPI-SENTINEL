package com.example.engine

import com.example.data.model.RiskSignal
import com.example.data.model.TransactionInput
import kotlin.math.abs

object InvoiceAnalyzer {

    data class InvoiceAnalysisResult(
        val invoiceExists: Boolean,
        val amountMatch: Boolean,
        val merchantMatch: Boolean,
        val consistencyScore: Double, // 1.0 = fully consistent, 0.0 = severe mismatch
        val signals: List<RiskSignal>
    )

    fun analyze(tx: TransactionInput): InvoiceAnalysisResult {
        val signals = mutableListOf<RiskSignal>()
        val invoiceExists = tx.invoiceId.isNotBlank() || tx.invoiceAmount > 0.0

        if (!invoiceExists) {
            // High value transactions (> ₹20,000) without invoice context get an advisory signal
            if (tx.amount >= 20000.0 && tx.transactionType == "P2M") {
                signals.add(
                    RiskSignal(
                        category = "invoice",
                        severity = "low",
                        finding = "High-value commercial transaction without attached invoice or order reference",
                        evidence = "Amount ₹${String.format("%,.0f", tx.amount)} with no digital invoice identifier",
                        riskContribution = 8
                    )
                )
            }
            return InvoiceAnalysisResult(
                invoiceExists = false,
                amountMatch = true,
                merchantMatch = true,
                consistencyScore = 0.85,
                signals = signals
            )
        }

        var amountMatch = true
        var merchantMatch = true
        var score = 1.0

        // Amount comparison
        val diff = abs(tx.amount - tx.invoiceAmount)
        if (diff > 1.0) { // More than 1 rupee mismatch
            amountMatch = false
            score -= 0.45
            signals.add(
                RiskSignal(
                    category = "invoice",
                    severity = "high",
                    finding = "Invoice amount mismatch: Requested payment (₹${String.format("%,.0f", tx.amount)}) does not match billed invoice (₹${String.format("%,.0f", tx.invoiceAmount)})",
                    evidence = "Variance of ₹${String.format("%,.0f", diff)} detected between transaction and invoice #${tx.invoiceId}",
                    riskContribution = 18
                )
            )
        }

        // Merchant comparison
        if (tx.invoiceMerchant.isNotBlank() && tx.merchantName.isNotBlank()) {
            val iClean = tx.invoiceMerchant.lowercase().replace(Regex("[^a-z0-9]"), "")
            val mClean = tx.merchantName.lowercase().replace(Regex("[^a-z0-9]"), "")
            if (!iClean.contains(mClean) && !mClean.contains(iClean)) {
                merchantMatch = false
                score -= 0.40
                signals.add(
                    RiskSignal(
                        category = "invoice",
                        severity = "high",
                        finding = "Invoice merchant discrepancy: Bill issued by '${tx.invoiceMerchant}', but payment directed to '${tx.merchantName}'",
                        evidence = "Issuer mismatch for invoice #${tx.invoiceId}",
                        riskContribution = 16
                    )
                )
            }
        }

        return InvoiceAnalysisResult(
            invoiceExists = true,
            amountMatch = amountMatch,
            merchantMatch = merchantMatch,
            consistencyScore = score.coerceIn(0.0, 1.0),
            signals = signals
        )
    }
}
