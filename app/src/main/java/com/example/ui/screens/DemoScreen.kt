package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Slideshow
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.RiskAnalysisResult
import com.example.data.model.RiskLevel
import com.example.data.model.TransactionInput
import com.example.ui.components.RiskBadge
import com.example.ui.components.getRiskColor
import com.example.ui.theme.BackgroundLight
import com.example.ui.theme.BorderLight
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.BrandBlue
import com.example.ui.theme.BrandNavy
import com.example.ui.theme.RiskCritical
import com.example.ui.theme.RiskHigh
import com.example.ui.theme.RiskLow
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceSubtle
import com.example.ui.theme.SurfaceWhite
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.SentinelViewModel

@Composable
fun DemoScreen(
    viewModel: SentinelViewModel,
    onViewAnalysisDetail: (RiskAnalysisResult) -> Unit,
    modifier: Modifier = Modifier
) {
    val scenarios = viewModel.getScenarios()
    val isPresentationMode by viewModel.isPresentationMode.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "VALIDATION DEMO",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrandBlue,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Contextual Risk Scenarios",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = BrandNavy
                )
            }

            Button(
                onClick = { viewModel.setPresentationMode(true) },
                colors = ButtonDefaults.buttonColors(containerColor = BrandNavy),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Slideshow,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Presentation Mode", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        // The Iconic Visual Core Insight Box: Same Amount + Different Context = Different Risk
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = SurfaceWhite,
            border = androidx.compose.foundation.BorderStroke(1.5.dp, BrandBlue.copy(alpha = 0.3f)),
            shadowElevation = 1.dp
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CompareArrows,
                        contentDescription = null,
                        tint = BrandBlue,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "CORE PRODUCT INSIGHT",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = BrandBlue,
                        letterSpacing = 1.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Same Amount. Different Context. Different Risk.",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = BrandNavy
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Traditional threshold systems blindspot: Looking only at ₹40,000 misses the underlying contextual signals entirely.",
                    fontSize = 12.sp,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Comparative Cards Side-by-Side / Stacked
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Transaction A: Safe ₹40,000
                    ComparisonTile(
                        tag = "TRANSACTION A",
                        amount = "₹40,000",
                        receiver = "FreshMart Retail",
                        points = listOf(
                            "Verified Merchant",
                            "Matching Invoice",
                            "Expected Behaviour",
                            "Trusted Domain"
                        ),
                        riskLevel = RiskLevel.LOW,
                        score = "12/100",
                        accentColor = RiskLow,
                        modifier = Modifier.weight(1f),
                        onRun = {
                            val safeTx = scenarios.first { it.first.contains("Safe") }.second
                            viewModel.runScenario(safeTx, onFinished = onViewAnalysisDetail)
                        }
                    )

                    // Transaction B: Suspicious ₹40,000
                    ComparisonTile(
                        tag = "TRANSACTION B",
                        amount = "₹40,000",
                        receiver = "Unknown Individual",
                        points = listOf(
                            "Unverified Receiver",
                            "Suspicious Domain (.xyz)",
                            "Invoice Mismatch",
                            "Unusual Frequency"
                        ),
                        riskLevel = RiskLevel.HIGH,
                        score = "87/100",
                        accentColor = RiskHigh,
                        modifier = Modifier.weight(1f),
                        onRun = {
                            val suspTx = scenarios.first { it.first.contains("Suspicious") }.second
                            viewModel.runScenario(suspTx, onFinished = onViewAnalysisDetail)
                        }
                    )
                }
            }
        }

        // Scenarios List
        Text(
            text = "ONE-CLICK TEST SCENARIOS",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = BrandNavy,
            letterSpacing = 0.5.sp
        )

        scenarios.forEachIndexed { idx, (title, tx) ->
            ScenarioCard(
                index = idx + 1,
                title = title,
                tx = tx,
                onRun = {
                    viewModel.runScenario(tx, onFinished = onViewAnalysisDetail)
                }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
fun ComparisonTile(
    tag: String,
    amount: String,
    receiver: String,
    points: List<String>,
    riskLevel: RiskLevel,
    score: String,
    accentColor: Color,
    onRun: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = SurfaceSubtle,
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = tag,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = TextMuted,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = amount,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color = BrandNavy
            )
            Text(
                text = receiver,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = TextSecondary,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(8.dp))
            points.forEach { pt ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(vertical = 1.5.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(4.dp)
                            .clip(CircleShape)
                            .background(accentColor)
                    )
                    Text(
                        text = pt,
                        fontSize = 10.sp,
                        color = TextPrimary,
                        maxLines = 1
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                RiskBadge(level = riskLevel)
                Text(
                    text = score,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = accentColor
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = onRun,
                colors = ButtonDefaults.buttonColors(containerColor = BrandNavy),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(32.dp)
            ) {
                Text("Analyze Now", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ScenarioCard(
    index: Int,
    title: String,
    tx: TransactionInput,
    onRun: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = SurfaceWhite,
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight),
        shadowElevation = 0.5.dp
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
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
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(BrandNavy),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$index",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Text(
                        text = title,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrandNavy
                    )
                }

                Button(
                    onClick = onRun,
                    colors = ButtonDefaults.buttonColors(containerColor = BrandNavy),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Run", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Amount: ₹${String.format("%,.0f", tx.amount)} • Receiver: ${tx.receiverName} • Domain: ${tx.domain.ifBlank { "N/A" }}",
                fontSize = 11.sp,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = tx.description,
                fontSize = 11.sp,
                color = TextMuted
            )
        }
    }
}
