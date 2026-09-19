package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Dataset
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BackgroundLight
import com.example.ui.theme.BorderLight
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.BrandBlue
import com.example.ui.theme.BrandNavy
import com.example.ui.theme.RiskCritical
import com.example.ui.theme.RiskHigh
import com.example.ui.theme.RiskLow
import com.example.ui.theme.RiskMedium
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceSubtle
import com.example.ui.theme.SurfaceWhite
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

/**
 * Interactive architectural diagram representing the complete pipeline:
 * UPI TRANSACTION -> Context Analysis (Receiver, Domain, Invoice, Behaviour)
 * -> Risk Engine (ML Model + Anomaly Detection) -> RISK SCORE -> EXPLANATION -> USER ALERT
 */
@Composable
fun ArchitectureDiagramView(
    modifier: Modifier = Modifier
) {
    var selectedNode by remember { mutableStateOf<String?>("risk_engine") }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceWhite)
            .border(1.dp, BorderLight, RoundedCornerShape(16.dp))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(BrandBlue)
                )
                Text(
                    text = "CORE FEATURE ARCHITECTURE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = BrandBlue,
                    letterSpacing = 1.sp
                )
            }

            Surface(
                color = SurfaceSubtle,
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    text = "TAP NODES TO INSPECT",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 1. UPI Transaction Node
        ArchNodeCard(
            title = "UPI TRANSACTION",
            subtitle = "Intent Payload: VPA, Amount, Note, URL, Merchant Code, Timestamp",
            icon = Icons.Default.Receipt,
            accentColor = BrandNavy,
            isSelected = selectedNode == "transaction",
            onClick = { selectedNode = if (selectedNode == "transaction") null else "transaction" }
        )

        ArchFlowArrow()

        // 2. Context Analysis Container
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, BrandBlue.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
            color = SurfaceSubtle
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "CONTEXT ANALYSIS LAYER",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = BrandNavy,
                    letterSpacing = 0.8.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                // 4 Sub-Analyzers in a 2x2 grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ContextSubNode(
                        title = "Receiver Analysis",
                        tag = "VPA & KYC",
                        icon = Icons.Default.Person,
                        isSelected = selectedNode == "receiver",
                        modifier = Modifier.weight(1f),
                        onClick = { selectedNode = if (selectedNode == "receiver") null else "receiver" }
                    )
                    ContextSubNode(
                        title = "Domain Analysis",
                        tag = "URL & Threat",
                        icon = Icons.Default.Language,
                        isSelected = selectedNode == "domain",
                        modifier = Modifier.weight(1f),
                        onClick = { selectedNode = if (selectedNode == "domain") null else "domain" }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ContextSubNode(
                        title = "Invoice Analysis",
                        tag = "Match & Ref",
                        icon = Icons.Default.Description,
                        isSelected = selectedNode == "invoice",
                        modifier = Modifier.weight(1f),
                        onClick = { selectedNode = if (selectedNode == "invoice") null else "invoice" }
                    )
                    ContextSubNode(
                        title = "Behaviour Analysis",
                        tag = "Velocity & Time",
                        icon = Icons.Default.Timeline,
                        isSelected = selectedNode == "behaviour",
                        modifier = Modifier.weight(1f),
                        onClick = { selectedNode = if (selectedNode == "behaviour") null else "behaviour" }
                    )
                }
            }
        }

        ArchFlowArrow()

        // 3. Multi-Signal Risk Engine
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { selectedNode = if (selectedNode == "risk_engine") null else "risk_engine" }
                .clip(RoundedCornerShape(12.dp))
                .border(
                    if (selectedNode == "risk_engine") 2.dp else 1.dp,
                    if (selectedNode == "risk_engine") BrandBlue else BorderLight,
                    RoundedCornerShape(12.dp)
                ),
            color = if (selectedNode == "risk_engine") BrandBlue.copy(alpha = 0.05f) else SurfaceWhite
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = BrandNavy,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "MULTI-SIGNAL RISK ENGINE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = BrandNavy,
                        letterSpacing = 0.5.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    EngineBranchCard(
                        name = "ML Model",
                        spec = "XGBoost (55%)",
                        icon = Icons.Default.AutoGraph,
                        accentColor = BrandBlue,
                        modifier = Modifier.weight(1f)
                    )
                    EngineBranchCard(
                        name = "Anomaly Detection",
                        spec = "IsoForest (15%)",
                        icon = Icons.Default.Memory,
                        accentColor = RiskMedium,
                        modifier = Modifier.weight(1f)
                    )
                    EngineBranchCard(
                        name = "Rule Engine",
                        spec = "Policies (30%)",
                        icon = Icons.Default.Shield,
                        accentColor = BrandNavy,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        ArchFlowArrow()

        // 4. Risk Score Node
        ArchNodeCard(
            title = "RISK SCORE (0 - 100)",
            subtitle = "Calibrated risk level: LOW (0-29), MEDIUM (30-59), HIGH (60-84), CRITICAL (85-100)",
            icon = Icons.Default.Dataset,
            accentColor = RiskHigh,
            isSelected = selectedNode == "risk_score",
            onClick = { selectedNode = if (selectedNode == "risk_score") null else "risk_score" }
        )

        ArchFlowArrow()

        // 5. Explanation Node
        ArchNodeCard(
            title = "EXPLAINABLE AI / \"WHY IS THIS RISKY?\"",
            subtitle = "Contextual evidence, feature attribution, counterfactual safety recommendations",
            icon = Icons.Default.Lightbulb,
            accentColor = BrandBlue,
            isSelected = selectedNode == "explanation",
            onClick = { selectedNode = if (selectedNode == "explanation") null else "explanation" }
        )

        ArchFlowArrow()

        // 6. User Alert & Decision Action Node
        ArchNodeCard(
            title = "ACTIONABLE USER ALERT",
            subtitle = "Real-time pre-payment intervention: [ DO NOT PAY ] • [ VERIFY ] • [ PROCEED ]",
            icon = Icons.Default.Warning,
            accentColor = RiskCritical,
            isSelected = selectedNode == "alert",
            onClick = { selectedNode = if (selectedNode == "alert") null else "alert" }
        )

        // Inspector Details Box
        AnimatedVisibility(visible = selectedNode != null) {
            selectedNode?.let { node ->
                Spacer(modifier = Modifier.height(14.dp))
                ArchitectureNodeInspector(nodeId = node)
            }
        }
    }
}

@Composable
private fun ArchFlowArrow() {
    Column(
        modifier = Modifier.padding(vertical = 3.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.ArrowDownward,
            contentDescription = null,
            tint = BrandBlue.copy(alpha = 0.7f),
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
private fun ArchNodeCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .clip(RoundedCornerShape(10.dp))
            .border(
                if (isSelected) 2.dp else 1.dp,
                if (isSelected) accentColor else BorderLight,
                RoundedCornerShape(10.dp)
            ),
        color = if (isSelected) accentColor.copy(alpha = 0.06f) else SurfaceWhite
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(18.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrandNavy
                )
                Text(
                    text = subtitle,
                    fontSize = 10.sp,
                    color = TextSecondary,
                    lineHeight = 14.sp
                )
            }
        }
    }
}

@Composable
private fun ContextSubNode(
    title: String,
    tag: String,
    icon: ImageVector,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .clickable(onClick = onClick)
            .clip(RoundedCornerShape(8.dp))
            .border(
                if (isSelected) 1.5.dp else 1.dp,
                if (isSelected) BrandBlue else BorderLight,
                RoundedCornerShape(8.dp)
            ),
        color = if (isSelected) BrandBlue.copy(alpha = 0.08f) else SurfaceWhite
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = BrandNavy,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = title,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrandNavy
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = tag,
                fontSize = 9.sp,
                color = TextMuted
            )
        }
    }
}

@Composable
private fun EngineBranchCard(
    name: String,
    spec: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .border(1.dp, BorderSubtle, RoundedCornerShape(8.dp)),
        color = SurfaceSubtle
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = name,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = BrandNavy,
                textAlign = TextAlign.Center
            )
            Text(
                text = spec,
                fontSize = 9.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
        }
    }
}

private data class NodeInspectorDetails(
    val title: String,
    val role: String,
    val inputs: String,
    val outputs: String,
    val insight: String
)

@Composable
private fun ArchitectureNodeInspector(nodeId: String) {
    val details = when (nodeId) {
        "transaction" -> NodeInspectorDetails(
            "Node: UPI Transaction Input",
            "Captures payment intent before UPI PIN authorization (via QR or deep link).",
            "Payee VPA (pa), Payee Name (pn), Amount (am), Note (tn), Merchant Code (mc), URL (url)",
            "Normalized TransactionInput data object",
            "Zero banking credentials (no UPI PIN, no OTP) are ever captured."
        )
        "receiver" -> NodeInspectorDetails(
            "Layer 1: Receiver & Merchant Verification",
            "Verifies bank registry status, entity alignment, and novelty.",
            "Receiver VPA, Receiver Name, Claimed Merchant, Account Type (INDIVIDUAL / MERCHANT)",
            "isVerified, merchantMatch, isNovel, receiverScore",
            "Crucial: An unknown receiver does NOT automatically mean fraud; it is a weighted signal combined with others."
        )
        "domain" -> NodeInspectorDetails(
            "Layer 2: Suspicious URL / Domain Analysis",
            "Evaluates transport encryption, domain reputation, TLD risk, and typosquatting.",
            "Payment link URL / domain string, expected merchant name",
            "isHttps, domainRiskScore, reputation, merchantDomainMatch",
            "Catches phishing payment gateways (e.g. upi-verify.xyz claiming to be an electricity board)."
        )
        "invoice" -> NodeInspectorDetails(
            "Layer 3: Invoice Verification & Mismatch",
            "Cross-references billing records against the requested transfer amount.",
            "Invoice ID, Billed Amount, Invoice Issuer Merchant",
            "amountMatch, merchantMatch, consistencyScore",
            "Example: Invoice states ₹4,000 for FreshMart, but payment requests ₹40,000 to an unknown receiver."
        )
        "behaviour" -> NodeInspectorDetails(
            "Layer 4: Behavioural & Transaction Pattern Analysis",
            "Benchmarks transaction against user's historical 90-day baseline.",
            "Amount vs average, 24h transaction velocity, time-of-day (e.g. 2-5 AM), prior failed attempts",
            "amountDeviationRatio, velocityAnomaly, isAnomalous, anomalyScore",
            "Detects sudden deviations (e.g. ₹40k transfer at 3 AM from a user typically paying ₹500-₹2,000)."
        )
        "risk_engine" -> NodeInspectorDetails(
            "Intelligence Layer: Multi-Signal Risk Engine",
            "Combines supervised ML, unsupervised anomaly detection, and deterministic policies.",
            "32-dimensional engineered feature vector from all 4 context layers",
            "Composite Risk Score (0-100), Model Confidence, Anomaly Distance",
            "Blends: XGBoost (55%) + Deterministic Rules (30%) + Isolation Forest (15%)."
        )
        "risk_score" -> NodeInspectorDetails(
            "Output: Machine Learning Risk Scoring",
            "Empirical calibrated risk index (0 to 100).",
            "Ensemble composite risk calculation",
            "Risk Level: LOW (0-29), MEDIUM (30-59), HIGH (60-84), CRITICAL (85-100)",
            "Calibrated score: 87/100 represents a high risk index, not an uncalibrated raw probability."
        )
        "explanation" -> NodeInspectorDetails(
            "Explainable AI: \"Why is this risky?\"",
            "Generates human-readable evidence points and counterfactual recommendations.",
            "Triggered rules, SHAP feature attributions, context mismatch flags",
            "Executive summary bullet points, 'What would make this safer?' suggestions",
            "Provides reasoning context to ensure transparency rather than an opaque black box."
        )
        "alert" -> NodeInspectorDetails(
            "Decision Support: Actionable User Alert",
            "Presents pre-payment warnings before UPI PIN authorization.",
            "Final risk level, primary risk drivers, recommended action",
            "Action Guidance: [ DO NOT PAY ] • [ VERIFY DETAILS ] • [ SAFE TO PROCEED ]",
            "False-positive aware: Allows user override with safety acknowledgment, learning from user feedback."
        )
        else -> NodeInspectorDetails(
            "System Architecture",
            "End-to-end contextual fraud detection pipeline.",
            "Context signals",
            "Risk scores and user alerts",
            "Same amount. Different context. Different risk."
        )
    }
    val title = details.title
    val role = details.role
    val inputs = details.inputs
    val outputs = details.outputs
    val insight = details.insight

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = SurfaceSubtle,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, BrandBlue.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                color = BrandNavy
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = role,
                fontSize = 11.sp,
                color = TextSecondary,
                lineHeight = 15.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            Text("• Inputs: $inputs", fontSize = 10.sp, color = TextPrimary)
            Text("• Output: $outputs", fontSize = 10.sp, color = TextPrimary)
            Spacer(modifier = Modifier.height(6.dp))
            Surface(
                color = SurfaceWhite,
                shape = RoundedCornerShape(6.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight)
            ) {
                Text(
                    text = "Key Takeaway: $insight",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = BrandBlue,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                )
            }
        }
    }
}
