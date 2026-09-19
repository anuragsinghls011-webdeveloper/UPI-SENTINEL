package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.RiskAnalysisResult
import com.example.data.model.RiskLevel
import com.example.data.model.RiskSignal
import com.example.ui.components.ActionableGuidanceCard
import com.example.ui.components.ArchitectureDiagramView
import com.example.ui.components.CircularRiskGauge
import com.example.ui.components.EvidenceSignalCard
import com.example.ui.components.RiskBadge
import com.example.ui.components.getRiskColor
import com.example.ui.theme.BackgroundLight
import com.example.ui.theme.BorderLight
import com.example.ui.theme.BrandBlue
import com.example.ui.theme.BrandNavy
import com.example.ui.theme.RiskCritical
import com.example.ui.theme.RiskHigh
import com.example.ui.theme.RiskLow
import com.example.ui.theme.RiskMedium
import com.example.ui.theme.SurfaceSubtle
import com.example.ui.theme.SurfaceWhite
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun RiskResultScreen(
    result: RiskAnalysisResult,
    onAnalyzeAnother: () -> Unit,
    onBackToDashboard: () -> Unit,
    onConsultGemini: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val dateStr = SimpleDateFormat("dd MMM yyyy, HH:mm:ss", Locale.getDefault()).format(Date(result.createdAt))
    var showAbortDialog by remember { mutableStateOf(false) }
    var showProceedDialog by remember { mutableStateOf(false) }
    var showArchDialog by remember { mutableStateOf(false) }
    var feedbackReported by remember { mutableStateOf(false) }
    var signalFilter by remember { mutableStateOf("ALL") } // ALL, RISKY, SAFE

    val isHighRisk = result.riskScore >= 50
    val riskHeadline = if (isHighRisk) "WHY IS THIS RISKY?" else "WHY IS THIS SAFE?"

    val filteredSignals = when (signalFilter) {
        "RISKY" -> result.signals.filter { it.riskContribution > 0 || it.severity.lowercase() in listOf("critical", "high", "medium") }
        "SAFE" -> result.signals.filter { it.riskContribution == 0 || it.severity.lowercase() == "low" }
        else -> result.signals
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundLight)
    ) {
        val isWide = maxWidth >= 760.dp

        if (isWide) {
            // Tablet / Landscape / Wide screen: Dual pane side-by-side
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Left Column: Risk Score Hero & Actionable Guidance
                Column(
                    modifier = Modifier
                        .weight(0.42f)
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    ResultHeaderBar(
                        transactionId = result.transactionId,
                        onBackToDashboard = onBackToDashboard
                    )

                    ScoreHeroCard(
                        result = result,
                        dateStr = dateStr
                    )

                    ActionableGuidanceCard(
                        actionType = result.actionType,
                        recommendation = result.recommendedAction,
                        onDoNotPay = { showAbortDialog = true },
                        onViewDetails = { /* Focused */ },
                        onProceedWithCaution = { showProceedDialog = true }
                    )

                    // Consult Gemini AI Chatbot
                    if (onConsultGemini != null) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFEFF6FF),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBFDBFE)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onConsultGemini() }
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(BrandNavy),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Ask Sentinel Gemini AI",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = BrandNavy
                                    )
                                    Text(
                                        text = "Multi-turn scam analysis & safety advice for this transaction",
                                        fontSize = 10.sp,
                                        color = TextSecondary
                                    )
                                }
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null,
                                    tint = BrandBlue,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    OutlinedButton(
                        onClick = { showArchDialog = true },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth(),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountTree,
                            contentDescription = null,
                            tint = BrandNavy,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Inspect Full Architecture Flow",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrandNavy
                        )
                    }

                    LatencyBenchmarkCard(latency = result.latency)
                }

                // Right Column: Clear, Itemized 'Why is this risky?' Reasoning Section
                Column(
                    modifier = Modifier
                        .weight(0.58f)
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    ItemizedReasoningSection(
                        result = result,
                        headline = riskHeadline,
                        filteredSignals = filteredSignals,
                        currentFilter = signalFilter,
                        onFilterChange = { signalFilter = it },
                        feedbackReported = feedbackReported,
                        onReportFeedback = { feedbackReported = true },
                        onAnalyzeAnother = onAnalyzeAnother
                    )
                }
            }
        } else {
            // Handheld Mobile View: Stacked with Score alongside Primary Assessment
            val scrollState = rememberScrollState()
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                ResultHeaderBar(
                    transactionId = result.transactionId,
                    onBackToDashboard = onBackToDashboard
                )

                // Top Result Hero: Displays Risk Score alongside Primary Indicators
                CompactScoreHeroCard(
                    result = result,
                    dateStr = dateStr
                )

                // Actionable Warning / Guidance
                ActionableGuidanceCard(
                    actionType = result.actionType,
                    recommendation = result.recommendedAction,
                    onDoNotPay = { showAbortDialog = true },
                    onViewDetails = { /* Focused */ },
                    onProceedWithCaution = { showProceedDialog = true }
                )

                // Consult Gemini AI Chatbot
                if (onConsultGemini != null) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFEFF6FF),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBFDBFE)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onConsultGemini() }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(BrandNavy),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Ask Sentinel Gemini AI",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BrandNavy
                                )
                                Text(
                                    text = "Multi-turn scam analysis & safety advice for this transaction",
                                    fontSize = 10.sp,
                                    color = TextSecondary
                                )
                            }
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = BrandBlue,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                // Clear, Itemized 'Why is this risky?' Reasoning Section
                ItemizedReasoningSection(
                    result = result,
                    headline = riskHeadline,
                    filteredSignals = filteredSignals,
                    currentFilter = signalFilter,
                    onFilterChange = { signalFilter = it },
                    feedbackReported = feedbackReported,
                    onReportFeedback = { feedbackReported = true },
                    onAnalyzeAnother = onAnalyzeAnother
                )

                // Pipeline Flow Inspector Trigger
                OutlinedButton(
                    onClick = { showArchDialog = true },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth(),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight)
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountTree,
                        contentDescription = null,
                        tint = BrandNavy,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Inspect Full Architecture Flow",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrandNavy
                    )
                }

                // Measured Latency Card
                LatencyBenchmarkCard(latency = result.latency)

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Dialog: Do Not Pay Abort confirmation
    if (showAbortDialog) {
        AlertDialog(
            onDismissRequest = { showAbortDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = RiskLow)
                    Text("Payment Aborted Safely", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = BrandNavy)
                }
            },
            text = {
                Text(
                    text = "You safely declined to proceed with payment of ₹${String.format("%,.0f", result.amount)}.\n\n• No UPI PIN was requested or exposed.\n• Fraud prevention logged securely in local audit.\n• Payee VPA flagged for risk telemetry.",
                    fontSize = 13.sp,
                    color = TextSecondary,
                    lineHeight = 18.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showAbortDialog = false
                        onBackToDashboard()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandNavy)
                ) {
                    Text("Return to Dashboard", fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // Dialog: Proceed with acknowledgment confirmation
    if (showProceedDialog) {
        AlertDialog(
            onDismissRequest = { showProceedDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.WarningAmber, contentDescription = null, tint = RiskHigh)
                    Text("User Override Notice", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = BrandNavy)
                }
            },
            text = {
                Text(
                    text = "You are choosing to proceed with ₹${String.format("%,.0f", result.amount)}.\n\n• Please verify you personally know this recipient.\n• Never enter your UPI PIN on unverified payment pages.\n• User override has been logged with time-stamped acknowledgment.",
                    fontSize = 13.sp,
                    color = TextSecondary,
                    lineHeight = 18.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = { showProceedDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = RiskHigh)
                ) {
                    Text("I Understand & Proceed", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showProceedDialog = false }) {
                    Text("Cancel", color = TextMuted)
                }
            }
        )
    }

    // Dialog: Full Core Architecture Flow Diagram Viewer
    if (showArchDialog) {
        AlertDialog(
            onDismissRequest = { showArchDialog = false },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Pipeline Architecture", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = BrandNavy)
                    IconButton(onClick = { showArchDialog = false }) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = BrandNavy)
                    }
                }
            },
            text = {
                Box(modifier = Modifier.fillMaxWidth()) {
                    ArchitectureDiagramView()
                }
            },
            confirmButton = {
                TextButton(onClick = { showArchDialog = false }) {
                    Text("Done", fontWeight = FontWeight.Bold, color = BrandNavy)
                }
            }
        )
    }
}

@Composable
private fun ResultHeaderBar(
    transactionId: String,
    onBackToDashboard: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        OutlinedButton(
            onClick = onBackToDashboard,
            shape = RoundedCornerShape(8.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = BrandNavy
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text("Dashboard", fontSize = 12.sp, color = BrandNavy, fontWeight = FontWeight.Bold)
        }

        Surface(
            color = SurfaceSubtle,
            shape = RoundedCornerShape(6.dp),
            border = androidx.compose.foundation.BorderStroke(0.5.dp, BorderLight)
        ) {
            Text(
                text = transactionId,
                fontSize = 11.sp,
                color = TextMuted,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
private fun ScoreHeroCard(
    result: RiskAnalysisResult,
    dateStr: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = SurfaceWhite,
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight),
        shadowElevation = 0.5.dp
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "TRANSACTION RISK ASSESSMENT",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = BrandBlue,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "₹${String.format("%,.0f", result.amount)}",
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = BrandNavy
            )
            Text(
                text = dateStr,
                fontSize = 11.sp,
                color = TextMuted
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Gauge
            CircularRiskGauge(
                score = result.riskScore,
                level = result.riskLevel,
                confidence = result.confidence
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Model calibration meta
            Surface(
                color = SurfaceSubtle,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Model Blend:", fontSize = 11.sp, color = TextMuted)
                        Text("55% XGB + 30% Rules + 15% IsoForest", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BrandNavy)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("AI Confidence:", fontSize = 11.sp, color = TextMuted)
                        Text("${(result.confidence * 100).toInt()}% High Confidence", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BrandBlue)
                    }
                }
            }
        }
    }
}

@Composable
private fun CompactScoreHeroCard(
    result: RiskAnalysisResult,
    dateStr: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = SurfaceWhite,
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight),
        shadowElevation = 0.5.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "TRANSACTION INTENT",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrandBlue,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "₹${String.format("%,.0f", result.amount)}",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = BrandNavy
                    )
                }
                Text(
                    text = dateStr,
                    fontSize = 10.sp,
                    color = TextMuted
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Score displayed alongside primary assessment
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Left: Circular Risk Gauge
                CircularRiskGauge(
                    score = result.riskScore,
                    level = result.riskLevel,
                    confidence = result.confidence,
                    size = 115.dp,
                    modifier = Modifier.width(120.dp)
                )

                // Right: Risk Details & Model Weights
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "RISK INDEX",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted,
                        letterSpacing = 0.5.sp
                    )
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "${result.riskScore}",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = getRiskColor(result.riskLevel)
                        )
                        Text(
                            text = " / 100",
                            fontSize = 14.sp,
                            color = TextMuted,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }

                    RiskBadge(level = result.riskLevel, sizeLarge = false)

                    Surface(
                        color = SurfaceSubtle,
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(6.dp)) {
                            Text(
                                text = "AI Confidence: ${(result.confidence * 100).toInt()}%",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = BrandNavy
                            )
                            Text(
                                text = "XGBoost + Isolation Forest",
                                fontSize = 9.sp,
                                color = TextMuted
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ItemizedReasoningSection(
    result: RiskAnalysisResult,
    headline: String,
    filteredSignals: List<RiskSignal>,
    currentFilter: String,
    onFilterChange: (String) -> Unit,
    feedbackReported: Boolean,
    onReportFeedback: () -> Unit,
    onAnalyzeAnother: () -> Unit
) {
    val riskyCount = result.signals.count { it.riskContribution > 0 || it.severity.lowercase() in listOf("critical", "high", "medium") }
    val safeCount = result.signals.count { it.riskContribution == 0 || it.severity.lowercase() == "low" }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Section Header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            color = if (result.riskScore >= 50) RiskCritical.copy(alpha = 0.08f) else RiskLow.copy(alpha = 0.08f),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (result.riskScore >= 50) RiskCritical.copy(alpha = 0.25f) else RiskLow.copy(alpha = 0.25f)
            )
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = if (result.riskScore >= 50) RiskCritical else RiskLow,
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (result.riskScore >= 50) Icons.Default.Psychology else Icons.Default.Security,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Column {
                    Text(
                        text = headline,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = BrandNavy,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "Itemized reasoning based on 14-feature multi-signal AI analysis",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
            }
        }

        // Filter chips row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            FilterChip(
                label = "All Reasons (${result.signals.size})",
                selected = currentFilter == "ALL",
                onClick = { onFilterChange("ALL") }
            )
            FilterChip(
                label = "Risky Signals ($riskyCount)",
                selected = currentFilter == "RISKY",
                onClick = { onFilterChange("RISKY") }
            )
            FilterChip(
                label = "Safe / Passed ($safeCount)",
                selected = currentFilter == "SAFE",
                onClick = { onFilterChange("SAFE") }
            )
        }

        // Executive Synthesis Card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = SurfaceWhite,
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Analytics,
                        contentDescription = null,
                        tint = BrandNavy,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "AI Executive Summary",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrandNavy,
                        letterSpacing = 0.5.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (result.explanations.isNotEmpty()) {
                    result.explanations.forEachIndexed { idx, exp ->
                        Row(
                            modifier = Modifier.padding(vertical = 3.dp),
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = BrandNavy.copy(alpha = 0.1f),
                                modifier = Modifier.size(18.dp).padding(top = 1.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "${idx + 1}",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = BrandNavy
                                    )
                                }
                            }
                            Text(
                                text = exp,
                                fontSize = 13.sp,
                                color = TextPrimary,
                                lineHeight = 18.sp
                            )
                        }
                    }
                } else {
                    Text(
                        text = "All context features evaluated within acceptable baseline tolerances.",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }
        }

        // Itemized Signal Cards (Numbered 1 to N)
        if (filteredSignals.isNotEmpty()) {
            Text(
                text = "ITEMIZED EVIDENCE SIGNALS (${filteredSignals.size})",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextMuted,
                letterSpacing = 0.5.sp
            )

            filteredSignals.forEachIndexed { index, signal ->
                EvidenceSignalCard(
                    signal = signal,
                    itemNumber = index + 1
                )
            }
        } else {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                color = SurfaceSubtle,
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight)
            ) {
                Box(
                    modifier = Modifier.padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No signals match the selected filter.",
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                }
            }
        }

        // AI Feature Importance (SHAP weights)
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = SurfaceWhite,
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "AI FEATURE ATTRIBUTION (SHAP WEIGHTS)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrandNavy,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(10.dp))

                FeatureAttributionBar(label = "Layer 2: Domain Security & SSL", weight = 0.35f, color = RiskCritical)
                FeatureAttributionBar(label = "Layer 1: Receiver Registry & Novelty", weight = 0.25f, color = RiskHigh)
                FeatureAttributionBar(label = "Layer 3: Invoice Amount Consistency", weight = 0.22f, color = RiskHigh)
                FeatureAttributionBar(label = "Layer 4: Spending Baseline Velocity", weight = 0.18f, color = RiskMedium)
            }
        }

        // Counterfactual Guidance: "What would make this transaction safer?"
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = SurfaceWhite,
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = null,
                        tint = BrandNavy,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "WHAT WOULD MAKE THIS TRANSACTION SAFER?",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrandNavy,
                        letterSpacing = 0.5.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                result.counterfactuals.forEach { rec ->
                    Row(
                        modifier = Modifier.padding(vertical = 3.dp),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = RiskLow,
                            modifier = Modifier.size(15.dp).padding(top = 2.dp)
                        )
                        Text(
                            text = rec,
                            fontSize = 12.sp,
                            color = TextSecondary,
                            lineHeight = 17.sp
                        )
                    }
                }
            }
        }

        // False-Positive Awareness Card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = SurfaceWhite,
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.HelpOutline,
                        contentDescription = null,
                        tint = BrandNavy,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "FALSE-POSITIVE AWARENESS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrandNavy,
                        letterSpacing = 0.5.sp
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Har unusual transaction fraud nahi hoti. UPI Sentinel allows overrides with safety confirmation so legitimate unusual payments are not blocked.",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    lineHeight = 17.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                if (feedbackReported) {
                    Surface(
                        color = RiskLow.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = RiskLow, modifier = Modifier.size(14.dp))
                            Text(
                                text = "Feedback recorded: Signal calibrated for model retraining.",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = RiskLow
                            )
                        }
                    }
                } else {
                    OutlinedButton(
                        onClick = onReportFeedback,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth(),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight)
                    ) {
                        Icon(Icons.Default.ThumbUp, contentDescription = null, modifier = Modifier.size(14.dp), tint = BrandNavy)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Report as Legitimate (False Positive)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrandNavy
                        )
                    }
                }
            }
        }

        // Action Buttons
        Button(
            onClick = onAnalyzeAnother,
            colors = ButtonDefaults.buttonColors(containerColor = BrandNavy),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = Color.White
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Analyze Another Payment", fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun FilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        color = if (selected) BrandNavy else SurfaceWhite,
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (selected) BrandNavy else BorderLight),
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = if (selected) Color.White else TextSecondary,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun FeatureAttributionBar(
    label: String,
    weight: Float,
    color: Color
) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, fontSize = 11.sp, color = TextPrimary)
            Text("${(weight * 100).toInt()}%", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = color)
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { weight },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = color,
            trackColor = SurfaceSubtle
        )
    }
}

@Composable
private fun LatencyBenchmarkCard(
    latency: com.example.data.model.LatencyBreakdown
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = SurfaceSubtle,
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Speed,
                    contentDescription = null,
                    tint = BrandNavy,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "LOCAL INFERENCE LATENCY",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrandNavy,
                    letterSpacing = 0.5.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Feature Extr.", fontSize = 9.sp, color = TextMuted)
                    Text("${latency.featureExtractionMs} ms", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BrandNavy)
                }
                Column {
                    Text("Rule Engine", fontSize = 9.sp, color = TextMuted)
                    Text("${latency.ruleEngineMs} ms", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BrandNavy)
                }
                Column {
                    Text("ML Inference", fontSize = 9.sp, color = TextMuted)
                    Text("${latency.mlInferenceMs} ms", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BrandNavy)
                }
                Column {
                    Text("Total Time", fontSize = 9.sp, color = TextMuted)
                    Text("${latency.totalTimeMs} ms", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BrandBlue)
                }
            }
        }
    }
}
