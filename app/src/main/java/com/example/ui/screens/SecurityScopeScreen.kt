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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Icon
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
import com.example.ui.theme.BackgroundLight
import com.example.ui.theme.BorderLight
import com.example.ui.theme.BrandBlue
import com.example.ui.theme.BrandNavy
import com.example.ui.theme.RiskLow
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceSubtle
import com.example.ui.theme.SurfaceWhite
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun SecurityScopeScreen(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Column {
            Text(
                text = "SYSTEM GOVERNANCE & PRIVACY",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = BrandBlue,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Security Scope & Boundaries",
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = BrandNavy
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Clear delineation of prototype implementation boundaries, regulatory compliance, and privacy commitments.",
                fontSize = 12.sp,
                color = TextSecondary
            )
        }

        // Zero-Access Privacy Commitment
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            color = SurfaceWhite,
            border = androidx.compose.foundation.BorderStroke(1.5.dp, BrandBlue.copy(alpha = 0.3f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = BrandNavy,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "ZERO-CREDENTIAL PRIVACY GUARANTEE",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrandNavy,
                        letterSpacing = 0.5.sp
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "UPI Sentinel is strictly a pre-transaction context analysis and decision-support tool. It never asks for, inspects, stores, or handles:",
                    fontSize = 12.sp,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("• User UPI PIN", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = BrandNavy)
                Text("• Bank OTPs or 2FA SMS tokens", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = BrandNavy)
                Text("• Net banking passwords or debit card credentials", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = BrandNavy)
                Text("• Payment execution or funds transmission authority", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = BrandNavy)
            }
        }

        // Implemented in this System
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
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = RiskLow,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "IMPLEMENTED IN THIS SYSTEM",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrandNavy,
                        letterSpacing = 0.5.sp
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
                FeatureItem("Context-aware multi-signal feature extraction (14 features)")
                FeatureItem("Deterministic heuristic rule engine for high-confidence threats")
                FeatureItem("Calibrated XGBoost classification model (95.6% F1-score)")
                FeatureItem("Isolation Forest unsupervised anomaly detector")
                FeatureItem("Real-time explainable decision support & evidence signals")
                FeatureItem("Counterfactual guidance ('What would make this safer?')")
                FeatureItem("One-click comparative validation scenarios")
                FeatureItem("Role-Based Access Control (Admin / Analyst / Consumer)")
                FeatureItem("Immutable audit trail with analyst review capabilities")
            }
        }

        // Production / Future Scope
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            color = SurfaceSubtle,
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = BrandBlue,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "FUTURE / PRODUCTION PSP INTEGRATION SCOPE",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrandNavy,
                        letterSpacing = 0.5.sp
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
                ScopeItem("Direct NPCI UPI SDK / Bank PSP hook integration via trusted callbacks")
                ScopeItem("Hardware-bound SIM card swapping detection & device integrity verification")
                ScopeItem("Federated multi-bank fraud registry query sharing across Indian banking system")
                ScopeItem("Biometric-assisted second-stage step-up authentication prior to NPCI handoff")
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
fun FeatureItem(text: String) {
    Row(
        modifier = Modifier.padding(vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(RiskLow)
        )
        Text(text = text, fontSize = 12.sp, color = TextPrimary)
    }
}

@Composable
fun ScopeItem(text: String) {
    Row(
        modifier = Modifier.padding(vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(BrandBlue)
        )
        Text(text = text, fontSize = 12.sp, color = TextSecondary)
    }
}
