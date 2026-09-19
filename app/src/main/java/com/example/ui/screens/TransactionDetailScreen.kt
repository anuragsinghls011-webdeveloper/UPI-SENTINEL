package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.RiskAnalysisResult
import com.example.data.model.RiskLevel
import com.example.ui.components.CircularRiskGauge
import com.example.ui.components.EvidenceSignalCard
import com.example.ui.components.RiskBadge
import com.example.ui.theme.BackgroundLight
import com.example.ui.theme.BorderLight
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
import com.example.ui.viewmodel.SentinelViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TransactionDetailScreen(
    analysis: RiskAnalysisResult,
    viewModel: SentinelViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateStr = SimpleDateFormat("dd MMM yyyy, HH:mm:ss", Locale.getDefault()).format(Date(analysis.createdAt))
    var selectedReviewStatus by remember { mutableStateOf(analysis.reviewStatus) }
    var reviewNote by remember { mutableStateOf(analysis.reviewNote) }
    var updateSuccess by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Back Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            OutlinedButton(
                onClick = onBack,
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
                Text("Back to History", fontSize = 12.sp, color = BrandNavy, fontWeight = FontWeight.Bold)
            }

            RiskBadge(level = analysis.riskLevel)
        }

        // Summary Card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            color = SurfaceWhite,
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "TRANSACTION OVERVIEW",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrandBlue,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "₹${String.format("%,.0f", analysis.amount)}",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = BrandNavy
                )
                Text(
                    text = "ID: ${analysis.transactionId} • Created: $dateStr",
                    fontSize = 11.sp,
                    color = TextMuted,
                    fontFamily = FontFamily.Monospace
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Risk Score", fontSize = 11.sp, color = TextMuted)
                        Text("${analysis.riskScore}/100", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = BrandNavy)
                    }
                    Column {
                        Text("ML Score", fontSize = 11.sp, color = TextMuted)
                        Text("${(analysis.mlScore * 100).toInt()}%", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = BrandNavy)
                    }
                    Column {
                        Text("Anomaly Score", fontSize = 11.sp, color = TextMuted)
                        Text("${(analysis.anomalyScore * 100).toInt()}%", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = BrandNavy)
                    }
                    Column {
                        Text("Confidence", fontSize = 11.sp, color = TextMuted)
                        Text("${(analysis.confidence * 100).toInt()}%", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = BrandNavy)
                    }
                }
            }
        }

        // Consult Gemini AI on this transaction
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFFEFF6FF),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBFDBFE)),
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    viewModel.openChatWithTransaction(analysis)
                    onBack()
                }
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(BrandNavy),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Consult Sentinel Gemini AI",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrandNavy
                    )
                    Text(
                        text = "Analyze this transaction's risk vector with Gemini",
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

        // Explanations Box
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = SurfaceWhite,
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "EXPLAINABLE REASONS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrandNavy,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                analysis.explanations.forEach { exp ->
                    Row(
                        modifier = Modifier.padding(vertical = 3.dp),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("•", color = BrandBlue, fontWeight = FontWeight.Bold)
                        Text(text = exp, fontSize = 13.sp, color = TextPrimary, lineHeight = 18.sp)
                    }
                }
            }
        }

        // Contributing Signals
        if (analysis.signals.isNotEmpty()) {
            Text(
                text = "CONTRIBUTING EVIDENCE SIGNALS (${analysis.signals.size})",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = BrandNavy,
                letterSpacing = 0.5.sp
            )
            analysis.signals.forEach { signal ->
                EvidenceSignalCard(signal = signal)
            }
        }

        // Analyst Review Workflow (Section 35 & 53)
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            color = SurfaceWhite,
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.RateReview,
                        contentDescription = null,
                        tint = BrandNavy,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "ANALYST / ADMIN REVIEW WORKFLOW",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrandNavy,
                        letterSpacing = 0.5.sp
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Update classification for false-positive auditing and model governance.",
                    fontSize = 11.sp,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(10.dp))
                val statuses = listOf("LEGITIMATE", "FALSE_POSITIVE", "SUSPICIOUS", "CONFIRMED_RISK")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    statuses.forEach { st ->
                        val isSel = selectedReviewStatus == st
                        FilterChip(
                            selected = isSel,
                            onClick = { selectedReviewStatus = st },
                            label = { Text(st.replace("_", " "), fontSize = 9.sp, fontWeight = FontWeight.Bold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = BrandNavy,
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = reviewNote,
                    onValueChange = { reviewNote = it },
                    placeholder = { Text("Add analyst audit note (e.g. Verified via merchant phone call)...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = textFieldColors()
                )

                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = {
                        viewModel.updateTransactionReview(analysis.transactionId, selectedReviewStatus, reviewNote)
                        updateSuccess = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandNavy),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Save Review Decision", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                if (updateSuccess) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Review status successfully recorded in MongoDB audit log.",
                        fontSize = 11.sp,
                        color = RiskLow,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Pipeline Latency
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            color = SurfaceSubtle,
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Model: ${analysis.modelVersion}", fontSize = 11.sp, color = TextMuted)
                Text("Execution Time: ${analysis.latency.totalTimeMs} ms", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BrandNavy)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}
