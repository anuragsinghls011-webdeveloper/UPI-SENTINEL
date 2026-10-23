package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * 1. Business Intent: Stated order/invoice intent issued by partner platform
 */
@JsonClass(generateAdapter = true)
data class BusinessIntent(
    val orderId: String,
    val platformName: String, // e.g. "Amazon India", "Swiggy", "B2B Invoice Hub", "Zomato"
    val expectedAmount: Double,
    val currency: String = "INR",
    val expectedPayeeVpa: String,
    val expectedMerchantName: String,
    val paymentPurpose: String, // "ECOMMERCE_ORDER", "UTILITY_BILL", "SERVICES", "SUBSCRIPTION"
    val orderCreatedAt: Long = System.currentTimeMillis(),
    val expiresAt: Long = System.currentTimeMillis() + 900_000L // 15 mins default validity
)

/**
 * Actual payment request presented via QR / Link / UPI Intent
 */
@JsonClass(generateAdapter = true)
data class PaymentRequestIntent(
    val actualPayeeVpa: String,
    val actualAmount: Double,
    val actualPayeeName: String,
    val rawQrOrLink: String = "",
    val paymentDirection: String = "PAY_DEBIT", // "PAY_DEBIT" vs dangerous "COLLECT_CREDIT"
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * 2. Intent Integrity Discrepancy Analysis
 */
@JsonClass(generateAdapter = true)
data class IntentIntegrityCheck(
    val amountMatched: Boolean,
    val amountDelta: Double,
    val payeeVpaMatched: Boolean,
    val payeeNameMatched: Boolean,
    val directionSafe: Boolean, // False if malicious "COLLECT" request disguised as a payment
    val qrLinkTampered: Boolean,
    val discrepancySummary: List<String>
)

/**
 * 3. Action Optimization: Contextual intervention recommendation
 */
enum class IntentAction(val label: String, val badgeColor: String) {
    ALLOW("ALLOW - SAFE", "Green"),
    ADDITIONAL_VERIFICATION("ADDITIONAL OTP / BIOMETRIC VERIFY", "Yellow"),
    MANUAL_REVIEW("MANUAL AGENT REVIEW", "Orange"),
    STOP_AND_VERIFY("STOP & VERIFY - BLOCKED", "Red")
}

@JsonClass(generateAdapter = true)
data class IntentBridgeEvaluation(
    val evaluationId: String,
    val businessIntent: BusinessIntent,
    val paymentRequest: PaymentRequestIntent,
    val integrity: IntentIntegrityCheck,
    val compositeRiskScore: Int, // 0 - 100
    val recommendedAction: IntentAction,
    val actionReason: String,
    val partnerPermissions: List<String> = listOf("DIRECT_BLOCK", "WARN_USER", "REQUIRE_STEP_UP"),
    val timestamp: Long = System.currentTimeMillis(),
    var actualOutcome: OutcomeFeedback? = null
)

/**
 * 4. Continuous Learning: Post-transaction outcome loop
 */
enum class OutcomeStatus(val displayName: String) {
    GENUINE_COMPLETED("Genuine Payment Completed"),
    CONFIRMED_FRAUD("Confirmed Fraud / Scammer Reported"),
    DELIVERY_VERIFIED("Order Delivered & Verified"),
    DISPUTE_FILED("Chargeback / Dispute Filed"),
    USER_CANCELLED_SUSPICIOUS("User Aborted Due to Warning")
}

@JsonClass(generateAdapter = true)
data class OutcomeFeedback(
    val feedbackId: String,
    val evaluationId: String,
    val outcome: OutcomeStatus,
    val notes: String = "",
    val feedbackTimestamp: Long = System.currentTimeMillis()
)

/**
 * -------------------------------------------------------------
 * Continuous Mutation & Immune Learning Models (Adversarial Testing)
 * -------------------------------------------------------------
 */

@JsonClass(generateAdapter = true)
data class FraudMutation(
    val mutationId: String,
    val mutationType: String, // "RECIPIENT_SWAP", "MICRO_SPLIT_AMOUNT", "TIME_OF_DAY_SHIFT", "DEVICE_FINGERPRINT_ALTER", "COLLECT_INVOICE_DISGUISE"
    val description: String,
    val mutatedPaymentRequest: PaymentRequestIntent,
    val defencePenetrated: Boolean = false, // True if existing defence evaluated as ALLOW (missed attack)
    val testScore: Int = 0,
    val testAction: IntentAction = IntentAction.ALLOW
)

@JsonClass(generateAdapter = true)
data class DefencePatch(
    val patchId: String,
    val candidateRuleName: String,
    val conditionLogic: String,
    val severityOverride: IntentAction,
    val falsePositiveImpact: String, // e.g. "<0.02% estimated FP rate on genuine retail orders"
    val appliedStatus: Boolean = true
)

@JsonClass(generateAdapter = true)
data class RegressionTestVector(
    val regressionId: String,
    val incidentOrigin: String,
    val attackVectorName: String,
    val variantCount: Int,
    val patchRef: String,
    val isRegressionPassing: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

@JsonClass(generateAdapter = true)
data class AdversarialImmuneSession(
    val incidentName: String,
    val seedFraud: PaymentRequestIntent,
    val mutationsGenerated: List<FraudMutation>,
    val vulnerabilitiesFound: Int,
    val generatedPatch: DefencePatch?,
    val regressionVectorSaved: RegressionTestVector?
)
