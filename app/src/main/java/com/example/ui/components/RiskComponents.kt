package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.RiskLevel
import com.example.data.model.RiskSignal
import com.example.ui.theme.BorderLight
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.BrandNavy
import com.example.ui.theme.RiskCritical
import com.example.ui.theme.RiskCriticalBg
import com.example.ui.theme.RiskCriticalBorder
import com.example.ui.theme.RiskHigh
import com.example.ui.theme.RiskHighBg
import com.example.ui.theme.RiskHighBorder
import com.example.ui.theme.RiskLow
import com.example.ui.theme.RiskLowBg
import com.example.ui.theme.RiskLowBorder
import com.example.ui.theme.RiskMedium
import com.example.ui.theme.RiskMediumBg
import com.example.ui.theme.RiskMediumBorder
import com.example.ui.theme.SurfaceSubtle
import com.example.ui.theme.SurfaceWhite
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

fun getRiskColor(level: RiskLevel): Color = when (level) {
    RiskLevel.LOW -> RiskLow
    RiskLevel.MEDIUM -> RiskMedium
    RiskLevel.HIGH -> RiskHigh
    RiskLevel.CRITICAL -> RiskCritical
}

fun getRiskBgColor(level: RiskLevel): Color = when (level) {
    RiskLevel.LOW -> RiskLowBg
    RiskLevel.MEDIUM -> RiskMediumBg
    RiskLevel.HIGH -> RiskHighBg
    RiskLevel.CRITICAL -> RiskCriticalBg
}

fun getRiskBorderColor(level: RiskLevel): Color = when (level) {
    RiskLevel.LOW -> RiskLowBorder
    RiskLevel.MEDIUM -> RiskMediumBorder
    RiskLevel.HIGH -> RiskHighBorder
    RiskLevel.CRITICAL -> RiskCriticalBorder
}

@Composable
fun RiskBadge(
    level: RiskLevel,
    modifier: Modifier = Modifier,
    sizeLarge: Boolean = false
) {
    val color = getRiskColor(level)
    val bg = getRiskBgColor(level)
    val border = getRiskBorderColor(level)

    Surface(
        modifier = modifier,
        color = bg,
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, border)
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = if (sizeLarge) 14.dp else 10.dp,
                vertical = if (sizeLarge) 6.dp else 4.dp
            ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(if (sizeLarge) 10.dp else 8.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Text(
                text = level.label,
                color = color,
                fontSize = if (sizeLarge) 13.sp else 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        }
    }
}

@Composable
fun SeverityChip(severity: String, modifier: Modifier = Modifier) {
    val (color, bg) = when (severity.lowercase()) {
        "critical" -> Pair(RiskCritical, RiskCriticalBg)
        "high" -> Pair(RiskHigh, RiskHighBg)
        "medium" -> Pair(RiskMedium, RiskMediumBg)
        else -> Pair(RiskLow, RiskLowBg)
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bg)
            .border(0.5.dp, color.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(
            text = severity.uppercase(),
            color = color,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun CircularRiskGauge(
    score: Int,
    level: RiskLevel,
    confidence: Double,
    modifier: Modifier = Modifier,
    size: Dp = 140.dp
) {
    val progress = (score / 100f).coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 900),
        label = "gaugeProgress"
    )

    val riskColor = getRiskColor(level)
    val trackColor = Color(0xFFE2E8F0)

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(size),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(size)) {
                val strokeWidth = 14.dp.toPx()
                // Track arc (240 degrees from 150 to 390)
                drawArc(
                    color = trackColor,
                    startAngle = 150f,
                    sweepAngle = 240f,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
                // Active risk arc
                if (animatedProgress > 0.01f) {
                    drawArc(
                        color = riskColor,
                        startAngle = 150f,
                        sweepAngle = 240f * animatedProgress,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "$score",
                    fontSize = 38.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimary
                )
                Text(
                    text = "OUT OF 100",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextMuted,
                    letterSpacing = 1.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))
        RiskBadge(level = level, sizeLarge = true)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Model Confidence: ${(confidence * 100).toInt()}%",
            fontSize = 12.sp,
            color = TextSecondary,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun EvidenceSignalCard(
    signal: RiskSignal,
    modifier: Modifier = Modifier,
    itemNumber: Int? = null
) {
    val categoryIcon = when (signal.category.lowercase()) {
        "receiver" -> Icons.Default.Info
        "merchant" -> Icons.Default.Security
        "domain" -> Icons.Default.Warning
        "invoice" -> Icons.Default.Info
        else -> Icons.Default.Warning
    }

    val layerName = when (signal.category.lowercase()) {
        "receiver" -> "Layer 1 • Receiver & Payee Verification"
        "merchant" -> "Layer 1 • Merchant Registry & Alignment"
        "domain" -> "Layer 2 • Payment Domain & Gateway Security"
        "invoice" -> "Layer 3 • Invoice Cross-Reference & Billing"
        "behaviour" -> "Layer 4 • Behaviour & Velocity Baseline"
        "anomaly" -> "Intelligence Layer • Isolation Forest Outlier"
        else -> "Context Engine Signal"
    }

    val isSafeSignal = signal.severity.lowercase() == "low" && signal.riskContribution == 0

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = SurfaceWhite,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (signal.severity.lowercase() == "critical") RiskCritical.copy(alpha = 0.4f)
            else if (signal.severity.lowercase() == "high") RiskHigh.copy(alpha = 0.4f)
            else BorderLight
        ),
        shadowElevation = 0.5.dp
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (itemNumber != null) {
                        Surface(
                            shape = CircleShape,
                            color = BrandNavy,
                            modifier = Modifier.size(22.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "$itemNumber",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    Icon(
                        imageVector = categoryIcon,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = BrandNavy
                    )
                    Column {
                        Text(
                            text = signal.category.replaceFirstChar { it.uppercase() },
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrandNavy,
                            letterSpacing = 0.3.sp
                        )
                        Text(
                            text = layerName,
                            fontSize = 10.sp,
                            color = TextMuted
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    SeverityChip(severity = signal.severity)
                    Text(
                        text = if (isSafeSignal) "0 pts (Pass)" else "+${signal.riskContribution} pts",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSafeSignal) RiskLow else getRiskColor(
                            when (signal.severity.lowercase()) {
                                "critical" -> RiskLevel.CRITICAL
                                "high" -> RiskLevel.HIGH
                                "medium" -> RiskLevel.MEDIUM
                                else -> RiskLevel.LOW
                            }
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = signal.finding,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
                lineHeight = 18.sp
            )

            if (signal.evidence.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    color = SurfaceSubtle,
                    shape = RoundedCornerShape(6.dp),
                    border = androidx.compose.foundation.BorderStroke(0.5.dp, BorderLight),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                        Text(
                            text = "AI EVIDENCE EXTRACTED",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMuted,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = signal.evidence,
                            fontSize = 11.sp,
                            color = TextSecondary,
                            fontFamily = FontFamily.Monospace,
                            lineHeight = 15.sp
                        )
                    }
                }
            }
        }
    }
}

private data class ActionGuidanceConfig(
    val color: Color,
    val bg: Color,
    val border: Color,
    val icon: ImageVector,
    val title: String
)

@Composable
fun ActionableGuidanceCard(
    actionType: String,
    recommendation: String,
    modifier: Modifier = Modifier,
    onDoNotPay: (() -> Unit)? = null,
    onViewDetails: (() -> Unit)? = null,
    onProceedWithCaution: (() -> Unit)? = null
) {
    val config = when (actionType) {
        "DO_NOT_PROCEED" -> ActionGuidanceConfig(
            RiskCritical,
            RiskCriticalBg,
            RiskCriticalBorder,
            Icons.Default.Warning,
            "ACTION RECOMMENDED: DO NOT PROCEED"
        )
        "VERIFY" -> ActionGuidanceConfig(
            RiskHigh,
            RiskHighBg,
            RiskHighBorder,
            Icons.Default.Info,
            "ACTION RECOMMENDED: VERIFY DETAILS"
        )
        else -> ActionGuidanceConfig(
            RiskLow,
            RiskLowBg,
            RiskLowBorder,
            Icons.Default.CheckCircle,
            "ACTION RECOMMENDED: SAFE TO PROCEED"
        )
    }
    val color = config.color
    val bg = config.bg
    val border = config.border
    val icon = config.icon
    val title = config.title

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = bg,
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, border)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = title,
                    color = color,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.5.sp
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = recommendation,
                fontSize = 13.sp,
                color = TextPrimary,
                lineHeight = 19.sp,
                fontWeight = FontWeight.Normal
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Actionable Buttons
            if (actionType == "DO_NOT_PROCEED" || actionType == "VERIFY") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { onDoNotPay?.invoke() },
                        colors = ButtonDefaults.buttonColors(containerColor = RiskCritical),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1.1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Block,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp),
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("DO NOT PAY", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    OutlinedButton(
                        onClick = { onViewDetails?.invoke() },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, border)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Visibility,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp),
                            tint = color
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("VIEW DETAILS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = color)
                    }
                }

                if (onProceedWithCaution != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    TextButton(
                        onClick = { onProceedWithCaution.invoke() },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text(
                            text = "I know this receiver • Proceed with acknowledgment",
                            fontSize = 11.sp,
                            color = TextMuted,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            } else {
                Button(
                    onClick = { onProceedWithCaution?.invoke() },
                    colors = ButtonDefaults.buttonColors(containerColor = RiskLow),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("SAFE TO PROCEED TO UPI PIN", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Decision support only. UPI Sentinel does not intercept bank communication or UPI PIN entry.",
                fontSize = 10.sp,
                color = TextMuted,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
            )
        }
    }
}

private data class Tuple5<A, B, C, D, E>(
    val a: A, val b: B, val c: C, val d: D, val e: E
)
