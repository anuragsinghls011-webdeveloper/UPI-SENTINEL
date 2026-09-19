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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.RiskLevel
import com.example.ui.components.ArchitectureDiagramView
import com.example.ui.components.CircularRiskGauge
import com.example.ui.components.RiskBadge
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
import com.example.ui.viewmodel.SentinelViewModel

@Composable
fun PresentationModeScreen(
    viewModel: SentinelViewModel,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val step by viewModel.presentationStep.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SurfaceWhite)
            .padding(20.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top Bar
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
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(RiskLow)
                )
                Text(
                    text = "PRESENTATION MODE • STEP ${step + 1} OF 6",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrandBlue,
                    letterSpacing = 1.sp
                )
            }

            IconButton(onClick = onClose) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Exit Presentation",
                    tint = BrandNavy
                )
            }
        }

        // Slide Content
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            when (step) {
                0 -> PresentationSlide1()
                1 -> PresentationSlide2(viewModel)
                2 -> PresentationSlide3(viewModel)
                3 -> PresentationSlide4()
                4 -> PresentationSlideArchitecture()
                5 -> PresentationSlide5()
            }
        }

        // Bottom Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = { viewModel.prevPresentationStep() },
                enabled = step > 0,
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Previous", fontWeight = FontWeight.Bold)
            }

            // Step dots
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                for (i in 0 until 6) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (i == step) BrandBlue else SurfaceSubtle)
                    )
                }
            }

            Button(
                onClick = {
                    if (step < 5) {
                        viewModel.nextPresentationStep()
                    } else {
                        onClose()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = BrandNavy),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = if (step < 5) "Next Step" else "Complete Demo",
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
fun PresentationSlide1() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.padding(16.dp)
    ) {
        Text(
            text = "UPI SENTINEL",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = BrandBlue,
            letterSpacing = 2.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "The Traditional Fraud Blindspot",
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            color = BrandNavy,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = "Conventional fraud rules look at fixed thresholds: 'Is ₹40,000 suspicious?'.\n\nLooking only at the amount is a critical blindspot. Real payments depend on dynamic context: Who is receiving? Where was the link clicked? Is there an invoice? How unusual is this behaviour?",
            fontSize = 14.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )
        Spacer(modifier = Modifier.height(24.dp))
        Surface(
            color = SurfaceSubtle,
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight)
        ) {
            Text(
                text = "\"DETECT • EXPLAIN • PROTECT\"",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = BrandNavy,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                letterSpacing = 1.sp
            )
        }
    }
}

@Composable
fun PresentationSlide2(viewModel: SentinelViewModel) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.padding(16.dp)
    ) {
        Text(
            text = "CASE A: SAFE TRANSACTION",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = RiskLow,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "₹40,000 Payment to Verified Merchant",
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold,
            color = BrandNavy,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))

        Surface(
            shape = RoundedCornerShape(14.dp),
            color = BackgroundLight,
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RiskBadge(level = RiskLevel.LOW, sizeLarge = true)
                    Text("Risk Score: 12 / 100", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = RiskLow)
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text("• Receiver: FreshMart Retail (Bank & NPCI Verified)", fontSize = 13.sp, color = TextPrimary)
                Text("• Invoice: INV-2026-9042 matches exactly (₹40,000)", fontSize = 13.sp, color = TextPrimary)
                Text("• Domain: freshmart.in (HTTPS, Verified Merchant Gateway)", fontSize = 13.sp, color = TextPrimary)
                Text("• Behaviour: Within normal baseline transaction profile", fontSize = 13.sp, color = TextPrimary)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = "RESULT: LOW RISK (Safe to proceed)",
            fontSize = 14.sp,
            fontWeight = FontWeight.ExtraBold,
            color = RiskLow
        )
    }
}

@Composable
fun PresentationSlide3(viewModel: SentinelViewModel) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.padding(16.dp)
    ) {
        Text(
            text = "CASE B: SUSPICIOUS TRANSACTION",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = RiskCritical,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "₹40,000 with Inconsistent Context",
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold,
            color = BrandNavy,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))

        Surface(
            shape = RoundedCornerShape(14.dp),
            color = BackgroundLight,
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RiskBadge(level = RiskLevel.HIGH, sizeLarge = true)
                    Text("Risk Score: 87 / 100", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = RiskHigh)
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text("• Receiver: Unverified individual account (ramesh.k9182@ybl)", fontSize = 13.sp, color = RiskHigh)
                Text("• Invoice: Mismatch! Billed ₹4,000, requested ₹40,000", fontSize = 13.sp, color = RiskHigh)
                Text("• Domain: Insecure upi-pay-verify.xyz (Suspicious gateway)", fontSize = 13.sp, color = RiskHigh)
                Text("• Behaviour: High 24h burst velocity & anomalous late-night timing", fontSize = 13.sp, color = RiskHigh)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = "RESULT: HIGH / CRITICAL RISK (Action: Do Not Proceed)",
            fontSize = 14.sp,
            fontWeight = FontWeight.ExtraBold,
            color = RiskCritical
        )
    }
}

@Composable
fun PresentationSlide4() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.padding(16.dp)
    ) {
        Text(
            text = "THE CORE ARCHITECTURAL INSIGHT",
            fontSize = 12.sp,
            fontWeight = FontWeight.ExtraBold,
            color = BrandBlue,
            letterSpacing = 1.5.sp
        )
        Spacer(modifier = Modifier.height(16.dp))

        Surface(
            color = SurfaceSubtle,
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, BrandBlue.copy(alpha = 0.4f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "SAME AMOUNT\n(₹40,000)",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = BrandNavy,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "+",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrandBlue,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
                Text(
                    text = "DIFFERENT CONTEXT\n(Receiver • Domain • Invoice • Velocity)",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "=",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrandBlue,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
                Text(
                    text = "DIFFERENT RISK\n(12 / 100 vs 87 / 100)",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = RiskCritical,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "The amount is identical. The contextual signals are different. Therefore, the risk assessment must be different.",
            fontSize = 13.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            lineHeight = 18.sp
        )
    }
}

@Composable
fun PresentationSlideArchitecture() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "SYSTEM ARCHITECTURE PIPELINE",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = BrandBlue,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "From Transaction to User Alert",
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold,
            color = BrandNavy,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Multi-signal synthesis: Receiver + Domain + Invoice + Behaviour -> ML (XGBoost) + Anomaly (IsoForest) -> Explainable Alert.",
            fontSize = 12.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            lineHeight = 16.sp
        )
        Spacer(modifier = Modifier.height(14.dp))

        ArchitectureDiagramView()
    }
}

@Composable
fun PresentationSlide5() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "EXPLAINABILITY, DECISION SUPPORT & PRIVACY",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = BrandBlue,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Beyond Black-Box Detection",
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold,
            color = BrandNavy,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(14.dp))

        Surface(
            shape = RoundedCornerShape(12.dp),
            color = SurfaceSubtle,
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Column {
                    Text("1. EXPLAINABLE REASONING (\"Why is this risky?\")", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = BrandNavy)
                    Text("Replaces opaque score '87' with clear evidence: unverified receiver, suspicious domain, mismatched invoice, anomalous velocity.", fontSize = 11.sp, color = TextSecondary, lineHeight = 15.sp)
                }

                Column {
                    Text("2. ACTIONABLE USER ALERT ([ DO NOT PAY ] / [ VERIFY ])", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = RiskCritical)
                    Text("Empowers users to abort risky payments BEFORE entering their UPI PIN. Provides clear counterfactual guidance on what would make it safe.", fontSize = 11.sp, color = TextSecondary, lineHeight = 15.sp)
                }

                Column {
                    Text("3. FALSE-POSITIVE AWARENESS", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = RiskHigh)
                    Text("\"Har unusual transaction fraud nahi hoti.\" Prevents locking legitimate users out through multi-signal correlation and user override acknowledgment.", fontSize = 11.sp, color = TextSecondary, lineHeight = 15.sp)
                }

                Column {
                    Text("4. ZERO-ACCESS PRIVACY INTEGRITY", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = BrandBlue)
                    Text("Zero UPI PIN, Zero OTP, Zero bank passwords captured. Fully on-device metadata evaluation complying with NPCI privacy principles.", fontSize = 11.sp, color = TextSecondary, lineHeight = 15.sp)
                }
            }
        }
    }
}
