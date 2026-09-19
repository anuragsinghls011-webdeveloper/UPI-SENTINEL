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
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
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
import com.example.data.model.ModelBenchmark
import com.example.ui.components.ArchitectureDiagramView
import com.example.ui.theme.BackgroundLight
import com.example.ui.theme.BorderLight
import com.example.ui.theme.BrandBlue
import com.example.ui.theme.BrandNavy
import com.example.ui.theme.RiskCritical
import com.example.ui.theme.RiskLow
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceSubtle
import com.example.ui.theme.SurfaceWhite
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.SentinelViewModel

@Composable
fun ModelIntelScreen(
    viewModel: SentinelViewModel,
    modifier: Modifier = Modifier
) {
    val benchmark by viewModel.modelBenchmark.collectAsState()
    val isRetraining by viewModel.isRetraining.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "MACHINE LEARNING ARCHITECTURE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrandBlue,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Model Evaluation & Intel",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = BrandNavy
                )
            }

            Button(
                onClick = { viewModel.triggerModelRetraining() },
                enabled = !isRetraining,
                colors = ButtonDefaults.buttonColors(containerColor = BrandNavy),
                shape = RoundedCornerShape(8.dp)
            ) {
                if (isRetraining) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = Color.White
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(if (isRetraining) "Training..." else "Train Model", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Active Model Specs Card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
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
                    Text(
                        text = "ACTIVE MODEL SPECIFICATION",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted,
                        letterSpacing = 0.5.sp
                    )
                    Surface(
                        color = RiskLow.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "STATUS: DEPLOYED",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = RiskLow,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = benchmark.version,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = BrandNavy
                )
                Text(
                    text = benchmark.algorithm,
                    fontSize = 12.sp,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Dataset Size", fontSize = 10.sp, color = TextMuted)
                        Text("${benchmark.datasetSize} rows", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = BrandNavy)
                    }
                    Column {
                        Text("Feature Dim.", fontSize = 10.sp, color = TextMuted)
                        Text("${benchmark.featureCount} features", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = BrandNavy)
                    }
                    Column {
                        Text("Avg Inference", fontSize = 10.sp, color = TextMuted)
                        Text("${benchmark.avgInferenceMs} ms", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = BrandNavy)
                    }
                }
            }
        }

        // Metrics Grid (Precision, Recall, F1, ROC-AUC, PR-AUC)
        Text(
            text = "EVALUATION METRICS (TEST SPLIT)",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = BrandNavy,
            letterSpacing = 0.5.sp
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MetricPill(name = "PRECISION", value = "${(benchmark.precision * 100).toInt()}%", modifier = Modifier.weight(1f))
            MetricPill(name = "RECALL", value = "${(benchmark.recall * 100).toInt()}%", modifier = Modifier.weight(1f))
            MetricPill(name = "F1-SCORE", value = "${(benchmark.f1Score * 100).toInt()}%", modifier = Modifier.weight(1f))
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MetricPill(name = "ROC-AUC", value = String.format("%.3f", benchmark.rocAuc), modifier = Modifier.weight(1f))
            MetricPill(name = "PR-AUC", value = String.format("%.3f", benchmark.prAuc), modifier = Modifier.weight(1f))
            MetricPill(name = "INFERENCE", value = "${benchmark.avgInferenceMs}ms", modifier = Modifier.weight(1f))
        }

        // Confusion Matrix (Section 24)
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = SurfaceWhite,
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "CONFUSION MATRIX (10,000 SYNTHETIC TEST SAMPLES)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrandNavy,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MatrixCell(
                        label = "TRUE POSITIVE",
                        count = "${benchmark.truePositives}",
                        sub = "Fraud detected",
                        color = RiskLow,
                        modifier = Modifier.weight(1f)
                    )
                    MatrixCell(
                        label = "FALSE POSITIVE",
                        count = "${benchmark.falsePositives}",
                        sub = "Safe flagged risk",
                        color = RiskCritical,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MatrixCell(
                        label = "FALSE NEGATIVE",
                        count = "${benchmark.falseNegatives}",
                        sub = "Missed risk",
                        color = RiskCritical,
                        modifier = Modifier.weight(1f)
                    )
                    MatrixCell(
                        label = "TRUE NEGATIVE",
                        count = "${benchmark.trueNegatives}",
                        sub = "Safe confirmed",
                        color = RiskLow,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Feature Importance Ranking
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = SurfaceWhite,
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "FEATURE IMPORTANCE (SHAP WEIGHTS)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrandNavy,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(12.dp))

                FeatureBar(name = "Payment Domain Threat & Protocol", frac = 0.28f, pct = "28%")
                Spacer(modifier = Modifier.height(8.dp))
                FeatureBar(name = "Receiver Identity Verification", frac = 0.22f, pct = "22%")
                Spacer(modifier = Modifier.height(8.dp))
                FeatureBar(name = "Invoice Amount & Reference Match", frac = 0.18f, pct = "18%")
                Spacer(modifier = Modifier.height(8.dp))
                FeatureBar(name = "Merchant / Receiver Entity Alignment", frac = 0.14f, pct = "14%")
                Spacer(modifier = Modifier.height(8.dp))
                FeatureBar(name = "Behavioural Baseline & Velocity Spike", frac = 0.10f, pct = "10%")
                Spacer(modifier = Modifier.height(8.dp))
                FeatureBar(name = "Timing & Prior Failed Attempts", frac = 0.08f, pct = "8%")
            }
        }

        // Calibrated Risk Score Explanation Note
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = SurfaceWhite,
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "SCORE CALIBRATION & THRESHOLD POLICY",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrandNavy,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "UPI Sentinel generates a Calibrated Risk Index (0 - 100), not an uncalibrated raw probability. A score of 87 indicates a High/Critical severity index where multiple context dimensions contradict normal merchant behavior. An unusual transaction alone does not trigger high severity; only cross-signal evidence correlation elevates the score.",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    lineHeight = 17.sp
                )
            }
        }

        // Complete Architecture Diagram
        ArchitectureDiagramView()

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
fun MetricPill(
    name: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = SurfaceWhite,
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(name, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TextMuted)
            Spacer(modifier = Modifier.height(2.dp))
            Text(value, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = BrandNavy)
        }
    }
}

@Composable
fun MatrixCell(
    label: String,
    count: String,
    sub: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = SurfaceSubtle,
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(label, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TextMuted)
            Text(count, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = color)
            Text(sub, fontSize = 10.sp, color = TextSecondary)
        }
    }
}

@Composable
fun FeatureBar(
    name: String,
    frac: Float,
    pct: String
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(name, fontSize = 11.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
            Text(pct, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BrandNavy)
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { frac },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = BrandBlue,
            trackColor = SurfaceSubtle
        )
    }
}
