package com.example

import com.example.data.model.RiskLevel
import com.example.data.model.TransactionInput
import com.example.engine.DomainAnalyzer
import com.example.engine.InvoiceAnalyzer
import com.example.engine.UnifiedRiskEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SentinelRiskEngineTest {

    @Test
    fun testSafeTransactionProducesLowRisk() {
        val safeTx = TransactionInput(
            transactionId = "TXN-SAFE-001",
            amount = 40000.0,
            description = "Monthly groceries and home essentials",
            receiverName = "FreshMart Retail Private Limited",
            receiverId = "freshmart@okaxis",
            receiverVerified = true,
            merchantName = "FreshMart Retail",
            merchantVerified = true,
            paymentUrl = "https://freshmart.in/pay",
            domain = "freshmart.in",
            invoiceId = "INV-2026-9042",
            invoiceAmount = 40000.0,
            invoiceMerchant = "FreshMart Retail",
            userTransactionCount24h = 2,
            averageTransactionAmount = 38000.0,
            receiverFirstSeen = false,
            failedAttempts = 0,
            unusualTimeFlag = false
        )

        val result = UnifiedRiskEngine.analyzeTransaction(safeTx)

        assertEquals("TXN-SAFE-001", result.transactionId)
        assertEquals(RiskLevel.LOW, result.riskLevel)
        assertTrue("Risk score should be low for verified transaction", result.riskScore < 30)
        assertEquals("PROCEED", result.actionType)
        assertTrue(result.explanations.isNotEmpty())
        assertTrue(result.counterfactuals.isNotEmpty())
    }

    @Test
    fun testSuspiciousTransactionProducesHighRisk() {
        val suspTx = TransactionInput(
            transactionId = "TXN-SUSP-002",
            amount = 40000.0,
            description = "Urgent electricity bill verification",
            receiverName = "Unknown Individual",
            receiverId = "ramesh.k9182@ybl",
            receiverVerified = false,
            merchantName = "Tata Power DDL",
            merchantVerified = false,
            paymentUrl = "http://upi-pay-verify.xyz/pay",
            domain = "upi-pay-verify.xyz",
            invoiceId = "BILL-84920",
            invoiceAmount = 4000.0, // Invoice mismatch!
            invoiceMerchant = "Tata Power",
            userTransactionCount24h = 14,
            averageTransactionAmount = 1500.0,
            receiverFirstSeen = true,
            failedAttempts = 3,
            unusualTimeFlag = true
        )

        val result = UnifiedRiskEngine.analyzeTransaction(suspTx)

        assertEquals("TXN-SUSP-002", result.transactionId)
        assertTrue("Risk score should be high or critical for suspicious transaction", result.riskScore >= 60)
        assertTrue(result.riskLevel == RiskLevel.HIGH || result.riskLevel == RiskLevel.CRITICAL)
        assertTrue(result.actionType == "DO_NOT_PROCEED" || result.actionType == "VERIFY")
        assertTrue(result.signals.isNotEmpty())
    }

    @Test
    fun testCoreInsight_SameAmount_DifferentContext_DifferentRisk() {
        val safeTx = TransactionInput(
            transactionId = "TXN-SAME-SAFE",
            amount = 40000.0,
            receiverName = "FreshMart Retail",
            receiverId = "freshmart@okaxis",
            receiverVerified = true,
            merchantName = "FreshMart Retail",
            merchantVerified = true,
            paymentUrl = "https://freshmart.in",
            domain = "freshmart.in",
            invoiceId = "INV-1",
            invoiceAmount = 40000.0,
            invoiceMerchant = "FreshMart Retail",
            userTransactionCount24h = 1,
            averageTransactionAmount = 35000.0,
            receiverFirstSeen = false
        )

        val suspTx = TransactionInput(
            transactionId = "TXN-SAME-SUSP",
            amount = 40000.0,
            receiverName = "Unknown Receiver",
            receiverId = "unknown@fake",
            receiverVerified = false,
            merchantName = "Tata Power",
            merchantVerified = false,
            paymentUrl = "http://upi-pay-verify.xyz",
            domain = "upi-pay-verify.xyz",
            invoiceId = "INV-2",
            invoiceAmount = 4000.0,
            invoiceMerchant = "Tata Power",
            userTransactionCount24h = 12,
            averageTransactionAmount = 2000.0,
            receiverFirstSeen = true
        )

        val safeResult = UnifiedRiskEngine.analyzeTransaction(safeTx)
        val suspResult = UnifiedRiskEngine.analyzeTransaction(suspTx)

        assertEquals(safeTx.amount, suspTx.amount, 0.001)
        assertTrue("Safe score should be significantly lower than suspicious score", safeResult.riskScore < suspResult.riskScore)
        assertTrue("Difference in risk score should be at least 30 points", (suspResult.riskScore - safeResult.riskScore) >= 30)
    }

    @Test
    fun testDomainAnalyzer() {
        val suspiciousResult = DomainAnalyzer.analyze("http://upi-pay-verify.xyz/verify", "Tata Power")
        assertTrue(suspiciousResult.domainRiskScore > 0.4)
        assertFalse(suspiciousResult.isHttps)

        val legitimateResult = DomainAnalyzer.analyze("https://freshmart.in/checkout", "FreshMart Retail")
        assertEquals(0.0, legitimateResult.domainRiskScore, 0.01)
        assertTrue(legitimateResult.isHttps)
    }

    @Test
    fun testInvoiceAnalyzerMismatch() {
        val txWithMismatch = TransactionInput(
            transactionId = "TXN-INV-TEST",
            amount = 35000.0,
            invoiceId = "INV-123",
            invoiceAmount = 3500.0, // 10x difference
            invoiceMerchant = "Croma",
            merchantName = "Croma"
        )
        val mismatch = InvoiceAnalyzer.analyze(txWithMismatch)
        assertFalse(mismatch.amountMatch)
        assertTrue(mismatch.signals.any { it.category == "invoice" })
    }
}
