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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AddAlert
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.RiskAnalysisResult
import com.example.data.model.RiskLevel
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
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceSubtle
import com.example.ui.theme.SurfaceWhite
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.AppTab
import com.example.ui.viewmodel.SentinelViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    viewModel: SentinelViewModel,
    onNavigateToDetail: (RiskAnalysisResult) -> Unit,
    modifier: Modifier = Modifier
) {
    val totalCount by viewModel.totalCount.collectAsState()
    val lowCount by viewModel.lowCount.collectAsState()
    val mediumCount by viewModel.mediumCount.collectAsState()
    val highCount by viewModel.highCount.collectAsState()
    val criticalCount by viewModel.criticalCount.collectAsState()
    val avgScore by viewModel.averageRiskScore.collectAsState()
    val analyses by viewModel.allAnalyses.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(6.dp))
            // Hero Platform Banner
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = SurfaceWhite,
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight),
                shadowElevation = 0.5.dp
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "REAL-TIME FRAUD INTELLIGENCE",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = BrandBlue,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Understand the Risk Behind Every Payment",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = BrandNavy
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(BrandBlue.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = BrandBlue,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Context-aware decision support: We analyze receiver authenticity, merchant alignment, domain threat, invoice consistency, and behavioural deviations before UPI PIN entry.",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        lineHeight = 17.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { viewModel.selectTab(AppTab.QR_SCAN) },
                            colors = ButtonDefaults.buttonColors(containerColor = BrandNavy),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1.2f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.QrCodeScanner,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Scan QR", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = { viewModel.selectTab(AppTab.ANALYZE) },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight)
                        ) {
                            Icon(
                                imageVector = Icons.Default.FlashOn,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = BrandNavy
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Analyze", fontSize = 13.sp, color = BrandNavy, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = { viewModel.selectTab(AppTab.DEMO) },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = BrandNavy
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Demo", fontSize = 13.sp, color = BrandNavy, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Top Metrics Grid
        item {
            Text(
                text = "SYSTEM METRICS (LIVE DATABASE)",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextMuted,
                letterSpacing = 0.8.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricTile(
                        title = "TOTAL ANALYSES",
                        value = "$totalCount",
                        sub = "Stored in MongoDB",
                        icon = Icons.Default.TrendingUp,
                        accentColor = BrandNavy,
                        modifier = Modifier.weight(1f)
                    )
                    MetricTile(
                        title = "AVG RISK SCORE",
                        value = "${avgScore.toInt()} / 100",
                        sub = "Ensemble composite",
                        icon = Icons.Default.Security,
                        accentColor = BrandBlue,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    RiskStatSmall(
                        level = "LOW",
                        count = lowCount,
                        color = RiskLow,
                        modifier = Modifier.weight(1f)
                    )
                    RiskStatSmall(
                        level = "MEDIUM",
                        count = mediumCount,
                        color = RiskMedium,
                        modifier = Modifier.weight(1f)
                    )
                    RiskStatSmall(
                        level = "HIGH",
                        count = highCount,
                        color = RiskHigh,
                        modifier = Modifier.weight(1f)
                    )
                    RiskStatSmall(
                        level = "CRITICAL",
                        count = criticalCount,
                        color = RiskCritical,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Live Risk Distribution Visual Bar
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = SurfaceWhite,
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "RISK LEVEL DISTRIBUTION",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrandNavy,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    val totalSafe = if (totalCount > 0) totalCount else 1
                    val lowFrac = lowCount.toFloat() / totalSafe
                    val medFrac = mediumCount.toFloat() / totalSafe
                    val highFrac = highCount.toFloat() / totalSafe
                    val critFrac = criticalCount.toFloat() / totalSafe

                    // Proportional Distribution Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp))
                    ) {
                        if (lowCount > 0) Box(modifier = Modifier.weight(lowFrac).fillMaxSize().background(RiskLow))
                        if (mediumCount > 0) Box(modifier = Modifier.weight(medFrac).fillMaxSize().background(RiskMedium))
                        if (highCount > 0) Box(modifier = Modifier.weight(highFrac).fillMaxSize().background(RiskHigh))
                        if (criticalCount > 0) Box(modifier = Modifier.weight(critFrac).fillMaxSize().background(RiskCritical))
                        if (totalCount == 0) Box(modifier = Modifier.weight(1f).fillMaxSize().background(SurfaceSubtle))
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Low: $lowCount", fontSize = 11.sp, color = RiskLow, fontWeight = FontWeight.SemiBold)
                        Text("Med: $mediumCount", fontSize = 11.sp, color = RiskMedium, fontWeight = FontWeight.SemiBold)
                        Text("High: $highCount", fontSize = 11.sp, color = RiskHigh, fontWeight = FontWeight.SemiBold)
                        Text("Crit: $criticalCount", fontSize = 11.sp, color = RiskCritical, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        // Recent Analyses Feed
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "RECENT ANALYSES",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrandNavy,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = "View All (${analyses.size})",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrandBlue,
                    modifier = Modifier.clickable { viewModel.selectTab(AppTab.TRANSACTIONS) }
                )
            }
        }

        if (analyses.isEmpty()) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = SurfaceWhite,
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No analyses recorded yet",
                            fontSize = 14.sp,
                            color = TextMuted,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        } else {
            items(analyses.take(6)) { analysis ->
                RecentTransactionCard(
                    analysis = analysis,
                    onClick = { onNavigateToDetail(analysis) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun MetricTile(
    title: String,
    value: String,
    sub: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = SurfaceWhite,
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted,
                    letterSpacing = 0.5.sp
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = accentColor
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = BrandNavy
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = sub,
                fontSize = 11.sp,
                color = TextSecondary
            )
        }
    }
}

@Composable
fun RiskStatSmall(
    level: String,
    count: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = SurfaceWhite,
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "$count",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = level,
                fontSize = 9.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextMuted
            )
        }
    }
}

@Composable
fun RecentTransactionCard(
    analysis: RiskAnalysisResult,
    onClick: () -> Unit
) {
    val dateStr = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()).format(Date(analysis.createdAt))

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        color = SurfaceWhite,
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "₹${String.format("%,.0f", analysis.amount)}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = BrandNavy
                    )
                    RiskBadge(level = analysis.riskLevel)
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = analysis.explanations.firstOrNull() ?: "Standard contextual evaluation",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${analysis.transactionId} • $dateStr",
                    fontSize = 10.sp,
                    color = TextMuted
                )
            }

            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "${analysis.riskScore}/100",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = getRiskColor(analysis.riskLevel)
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
