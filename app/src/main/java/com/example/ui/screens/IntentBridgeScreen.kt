package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.SentinelViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IntentBridgeScreen(
    viewModel: SentinelViewModel,
    modifier: Modifier = Modifier
) {
    var selectedScenarioIndex by remember { mutableStateOf(1) } // Default to tampered attack
    var currentEvaluation by remember { mutableStateOf<IntentBridgeEvaluation?>(null) }
    var outcomeRecorded by remember { mutableStateOf<OutcomeStatus?>(null) }

    val immuneSession by viewModel.currentImmuneSession.collectAsState()
    val isSimulating by viewModel.isSimulatingAttack.collectAsState()
    val regressionVectors by viewModel.regressionVectors.collectAsState()

    // Preset test scenarios demonstrating Intent Integrity
    val scenarios = remember {
        listOf(
            Triple(
                "Genuine E-Commerce Order",
                BusinessIntent(
                    orderId = "AMZ-IND-884920",
                    platformName = "Amazon India",
                    expectedAmount = 4599.00,
                    expectedPayeeVpa = "amazonpay.merchant@hdfcbank",
                    expectedMerchantName = "Amazon Seller Services Pvt Ltd",
                    paymentPurpose = "ECOMMERCE_ORDER"
                ),
                PaymentRequestIntent(
                    actualPayeeVpa = "amazonpay.merchant@hdfcbank",
                    actualAmount = 4599.00,
                    actualPayeeName = "Amazon Seller Services",
                    paymentDirection = "PAY_DEBIT"
                )
            ),
            Triple(
                "Tampered QR (Amount Inflated + VPA Swapped)",
                BusinessIntent(
                    orderId = "SWIGGY-FOOD-1049",
                    platformName = "Swiggy Delivery",
                    expectedAmount = 420.00,
                    expectedPayeeVpa = "swiggy.orders@icici",
                    expectedMerchantName = "Swiggy UPI",
                    paymentPurpose = "ECOMMERCE_ORDER"
                ),
                PaymentRequestIntent(
                    actualPayeeVpa = "delivery.boy.refund@paytm",
                    actualAmount = 4200.00,
                    actualPayeeName = "Fast Delivery Refunds",
                    rawQrOrLink = "upi://pay?pa=delivery.boy.refund@paytm&am=4200&pn=FastDelivery",
                    paymentDirection = "PAY_DEBIT"
                )
            ),
            Triple(
                "Reverse Collect Request Scam (Disguised Approval)",
                BusinessIntent(
                    orderId = "OLX-BUYER-PAY-993",
                    platformName = "OLX Marketplace",
                    expectedAmount = 15000.00,
                    expectedPayeeVpa = "buyer.escrow@axis",
                    expectedMerchantName = "OLX Trust Pay",
                    paymentPurpose = "SERVICES"
                ),
                PaymentRequestIntent(
                    actualPayeeVpa = "refund.support@ybl",
                    actualAmount = 15000.00,
                    actualPayeeName = "UPI Refund Centre",
                    paymentDirection = "COLLECT_CREDIT"
                )
            )
        )
    }

    LaunchedEffect(selectedScenarioIndex) {
        val scenario = scenarios[selectedScenarioIndex]
        currentEvaluation = viewModel.intentBridgeEngine.evaluate(scenario.second, scenario.third)
        outcomeRecorded = null
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Card
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = BrandNavy),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(BrandBlue, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.CompareArrows, contentDescription = null, tint = Color.White)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("IntentBridge Protocol", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
                            Text("Intent Integrity • Action Optimization • Adversarial Immune Loop", color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        "Validates business order intent against actual payment requests, simulates safe adversarial mutations, generates auto-defence patches, and locks variants into permanent regression tests.",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )
                }
            }
        }

        // 1. Scenario Selector
        item {
            Text("1. Select Incident / Stated Business Order:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                scenarios.forEachIndexed { index, triple ->
                    FilterChip(
                        selected = selectedScenarioIndex == index,
                        onClick = { selectedScenarioIndex = index },
                        label = {
                            Text(
                                when (index) {
                                    0 -> "Genuine Order"
                                    1 -> "Tampered QR"
                                    else -> "Collect Scam"
                                },
                                fontSize = 11.sp
                            )
                        }
                    )
                }
            }
        }

        // Comparison Matrix: Expected Intent vs Actual Request
        currentEvaluation?.let { eval ->
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                    modifier = Modifier.border(1.dp, BorderLight, RoundedCornerShape(14.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Intent vs Payment Comparison", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("STATED BUSINESS INTENT", fontSize = 10.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                                Text("Platform: ${eval.businessIntent.platformName}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                Text("Order ID: ${eval.businessIntent.orderId}", fontSize = 11.sp, color = TextSecondary)
                                Text("Amount: ₹${String.format("%,.2f", eval.businessIntent.expectedAmount)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BrandNavy)
                                Text("Payee: ${eval.businessIntent.expectedPayeeVpa}", fontSize = 11.sp, color = TextSecondary)
                            }

                            Divider(
                                modifier = Modifier
                                    .height(100.dp)
                                    .width(1.dp)
                                    .padding(horizontal = 8.dp),
                                color = BorderLight
                            )

                            Column(modifier = Modifier.weight(1f)) {
                                Text("ACTUAL PAYMENT REQUEST", fontSize = 10.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                                Text("Payee: ${eval.paymentRequest.actualPayeeName}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                Text("Amount: ₹${String.format("%,.2f", eval.paymentRequest.actualAmount)}", fontSize = 12.sp, fontWeight = FontWeight.Bold,
                                    color = if (eval.integrity.amountMatched) RiskLow else RiskHigh)
                                Text("VPA: ${eval.paymentRequest.actualPayeeVpa}", fontSize = 11.sp,
                                    color = if (eval.integrity.payeeVpaMatched) TextSecondary else RiskHigh)
                                Text("Direction: ${eval.paymentRequest.paymentDirection}", fontSize = 11.sp,
                                    color = if (eval.integrity.directionSafe) TextSecondary else RiskCritical)
                            }
                        }
                    }
                }
            }

            // 3. Recommended Action Optimization
            item {
                val actionColor = when (eval.recommendedAction) {
                    IntentAction.ALLOW -> RiskLow
                    IntentAction.ADDITIONAL_VERIFICATION -> RiskMedium
                    IntentAction.MANUAL_REVIEW -> Color(0xFFF57C00)
                    IntentAction.STOP_AND_VERIFY -> RiskCritical
                }

                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = actionColor.copy(alpha = 0.12f)),
                    modifier = Modifier.border(1.5.dp, actionColor, RoundedCornerShape(14.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "ACTION OPTIMIZATION RECOMMENDATION",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = actionColor
                            )
                            Badge(containerColor = actionColor) {
                                Text(
                                    "Risk Score: ${eval.compositeRiskScore}/100",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            eval.recommendedAction.label,
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp,
                            color = actionColor
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            eval.actionReason,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 17.sp
                        )

                        if (eval.integrity.discrepancySummary.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Divergence Signals:", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            eval.integrity.discrepancySummary.forEach { d ->
                                Row(modifier = Modifier.padding(vertical = 2.dp), verticalAlignment = Alignment.Top) {
                                    Icon(Icons.Default.ArrowRight, contentDescription = null, tint = actionColor, modifier = Modifier.size(16.dp))
                                    Text(d, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }

            // =========================================================================
            // CONTINUOUS MUTATION & ADVERSARIAL IMMUNITY SECTION
            // =========================================================================
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                    modifier = Modifier.border(1.dp, BrandBlue.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Adversarial Defence & Mutation Engine", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Text("Mutate incident • Attack defences • Generate patch • Lock regression", fontSize = 11.sp, color = TextSecondary)
                            }
                            Icon(Icons.Default.SecurityUpdateGood, contentDescription = null, tint = BrandBlue)
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = {
                                val currentScenario = scenarios[selectedScenarioIndex]
                                viewModel.runAdversarialDefensePipeline(
                                    incidentName = currentScenario.first,
                                    business = currentScenario.second,
                                    payment = currentScenario.third
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = BrandNavy),
                            shape = RoundedCornerShape(10.dp),
                            enabled = !isSimulating
                        ) {
                            if (isSimulating) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Text("Simulating Adversarial Mutations...")
                            } else {
                                Icon(Icons.Default.BugReport, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Generate Mutations & Attack Own Defence", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Display Mutation Results & Defence Patch
                        immuneSession?.let { session ->
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("2. Generated Synthetic Mutations & Attack Audit:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(8.dp))

                            session.mutationsGenerated.forEach { mut ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (mut.defencePenetrated) RiskHigh.copy(alpha = 0.12f) else RiskLow.copy(alpha = 0.08f)
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(mut.mutationType.replace('_', ' '), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                            Text(mut.description, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Badge(containerColor = if (mut.defencePenetrated) RiskHigh else RiskLow) {
                                            Text(
                                                if (mut.defencePenetrated) "PENETRATED (Missed)" else "BLOCKED (${mut.testScore}/100)",
                                                color = Color.White,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }

                            // 4. Candidate Defence Patch
                            session.generatedPatch?.let { patch ->
                                Spacer(modifier = Modifier.height(14.dp))
                                Card(
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Build, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("4. Candidate Defence Patch Generated", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(patch.candidateRuleName, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = BrandNavy)
                                        Text("Logic: ${patch.conditionLogic}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("False-Positive Impact: ${patch.falsePositiveImpact}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = RiskLow)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 5. Never Forget That Attack (Permanent Regression Test Vectors)
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceSubtle),
                    modifier = Modifier.border(1.dp, BorderLight, RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LockClock, contentDescription = null, tint = BrandBlue, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("5. Permanent Regression Suite ('Never Forget')", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Text(
                            "All historical fraud incidents & synthesized variants are locked here. During future model retraining, these vectors automatically ensure zero vulnerability relapse.",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        regressionVectors.forEach { reg ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(reg.incidentOrigin, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        Text("Vectors: ${reg.attackVectorName} (${reg.variantCount} variants)", fontSize = 11.sp, color = TextMuted)
                                        Text("Lock Ref: ${reg.patchRef}", fontSize = 10.sp, color = BrandBlue)
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = RiskLow.copy(alpha = 0.15f)
                                    ) {
                                        Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Verified, contentDescription = null, tint = RiskLow, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("PASSING", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = RiskLow)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Outcome Learning Loop
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceSubtle),
                    modifier = Modifier.border(1.dp, BorderLight, RoundedCornerShape(14.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Record Real-World Outcome Resolution",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        if (outcomeRecorded != null) {
                            Surface(
                                color = RiskLow.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = RiskLow, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "Learned Outcome: ${outcomeRecorded?.displayName}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = RiskLow
                                    )
                                }
                            }
                        } else {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = {
                                        outcomeRecorded = OutcomeStatus.GENUINE_COMPLETED
                                        viewModel.recordOutcome(eval.evaluationId, OutcomeStatus.GENUINE_COMPLETED)
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = BrandBlue)
                                ) {
                                    Text("Genuine Completed", fontSize = 11.sp)
                                }

                                OutlinedButton(
                                    onClick = {
                                        outcomeRecorded = OutcomeStatus.CONFIRMED_FRAUD
                                        viewModel.recordOutcome(eval.evaluationId, OutcomeStatus.CONFIRMED_FRAUD)
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Confirmed Fraud", fontSize = 11.sp, color = RiskHigh)
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}
