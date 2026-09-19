package com.example.ui.components

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.RiskAnalysisResult
import com.example.qr.UpiIntentDetails
import com.example.ui.theme.BackgroundLight
import com.example.ui.theme.BorderLight
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
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QrScannedResultSheet(
    intent: UpiIntentDetails,
    analysisResult: RiskAnalysisResult,
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onViewFullAnalysis: (RiskAnalysisResult) -> Unit,
    onLoadIntoForm: (UpiIntentDetails) -> Unit,
    onScanAnother: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("en", "IN"))

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = SurfaceWhite,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Bar with Real-Time Risk Badge
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
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(BrandNavy),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCode,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "CAPTURED UPI INTENT",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrandBlue,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Real-Time Assessment",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = BrandNavy
                        )
                    }
                }

                RiskBadge(level = analysisResult.riskLevel, sizeLarge = true)
            }

            // Real-Time Risk Gauge and Guidance
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceSubtle),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CircularRiskGauge(
                        score = analysisResult.riskScore,
                        level = analysisResult.riskLevel,
                        confidence = analysisResult.confidence,
                        size = 130.dp
                    )

                    ActionableGuidanceCard(
                        actionType = analysisResult.actionType,
                        recommendation = analysisResult.recommendedAction
                    )
                }
            }

            // Parsed UPI Intent Attributes
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "PARSED PAYMENT ATTRIBUTES",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrandNavy,
                        letterSpacing = 0.5.sp
                    )

                    // Amount
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Requested Amount", fontSize = 12.sp, color = TextSecondary)
                        val formattedAmt = intent.amount?.let { currencyFormatter.format(it) } ?: "Open / Dynamic"
                        Text(
                            text = formattedAmt,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = BrandNavy
                        )
                    }

                    // Payee VPA
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Payee VPA (pa)", fontSize = 12.sp, color = TextSecondary)
                        Text(
                            text = intent.payeeVpa.ifBlank { "N/A" },
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = BrandBlue
                        )
                    }

                    // Payee Name
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Payee Name (pn)", fontSize = 12.sp, color = TextSecondary)
                        Text(
                            text = intent.payeeName.ifBlank { "N/A" },
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                    }

                    // Transaction Note
                    if (intent.transactionNote.isNotBlank()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Note / Purpose (tn)", fontSize = 12.sp, color = TextSecondary)
                            Text(
                                text = intent.transactionNote,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Normal,
                                color = TextPrimary
                            )
                        }
                    }

                    // Payment URL / Invoice Domain
                    if (intent.paymentUrl.isNotBlank()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Payment URL (url)", fontSize = 12.sp, color = TextSecondary)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                val isHttp = intent.paymentUrl.startsWith("http://", ignoreCase = true)
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(if (isHttp) RiskCritical.copy(alpha = 0.1f) else RiskLow.copy(alpha = 0.1f))
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = if (isHttp) "HTTP INSECURE" else "HTTPS",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isHttp) RiskCritical else RiskLow
                                    )
                                }
                                Text(
                                    text = intent.extractedDomain.ifBlank { intent.paymentUrl.take(25) },
                                    fontSize = 11.sp,
                                    color = TextPrimary
                                )
                            }
                        }
                    }

                    // Reference ID / Merchant Code
                    if (intent.merchantCode.isNotBlank() || intent.transactionRef.isNotBlank()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("MCC / Ref ID", fontSize = 12.sp, color = TextSecondary)
                            Text(
                                text = "${intent.merchantCode.ifBlank { "None" }} • ${intent.transactionRef.ifBlank { "N/A" }}",
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                        }
                    }
                }
            }

            // Top Signals Detected
            if (analysisResult.signals.isNotEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "DETECTED CONTEXTUAL SIGNALS (${analysisResult.signals.size})",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrandNavy,
                            letterSpacing = 0.5.sp
                        )

                        analysisResult.signals.take(3).forEach { sig ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                val dotColor = when (sig.severity.lowercase()) {
                                    "critical" -> RiskCritical
                                    "high" -> RiskHigh
                                    else -> RiskLow
                                }
                                Box(
                                    modifier = Modifier
                                        .padding(top = 4.dp)
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(dotColor)
                                )
                                Column {
                                    Text(
                                        text = sig.finding,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = BrandNavy
                                    )
                                    Text(
                                        text = sig.evidence,
                                        fontSize = 11.sp,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Execution latency chip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Bolt,
                    contentDescription = null,
                    tint = BrandBlue,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "QR captured & contextually evaluated in ${analysisResult.latency.totalTimeMs}ms",
                    fontSize = 11.sp,
                    color = TextMuted
                )
            }

            // Actions: Inspect Full Analysis, Load into Form, Scan Another
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        onDismiss()
                        onViewFullAnalysis(analysisResult)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandNavy),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("View Full Explainable Report", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            onDismiss()
                            onLoadIntoForm(intent)
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                            Text("Edit in Form", fontSize = 12.sp)
                        }
                    }

                    OutlinedButton(
                        onClick = {
                            onDismiss()
                            onScanAnother()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                            Text("Scan Again", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}
