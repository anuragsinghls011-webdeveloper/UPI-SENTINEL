package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.BorderLight
import com.example.ui.theme.BrandBlue
import com.example.ui.theme.BrandNavy
import com.example.ui.theme.RiskLow
import com.example.ui.theme.SurfaceSubtle
import com.example.ui.theme.SurfaceWhite
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

val ANALYSIS_STEPS = listOf(
    "Extracting transaction features & normalization",
    "Checking receiver registration & VPA verification",
    "Checking merchant credentials & entity alignment",
    "Analyzing payment URL & domain reputation",
    "Verifying invoice consistency & reference matching",
    "Analyzing behavioural baseline & velocity patterns",
    "Running calibrated XGBoost risk model",
    "Running Isolation Forest anomaly detector",
    "Synthesizing explainable decision-support output"
)

@Composable
fun AnalysisProgressDialog(
    currentStepIndex: Int,
    onDismissRequest: () -> Unit = {}
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = SurfaceWhite,
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight),
            shadowElevation = 8.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 3.dp,
                        color = BrandBlue
                    )
                    Text(
                        text = "Analyzing Transaction...",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrandNavy
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Running contextual fraud intelligence pipeline",
                    fontSize = 12.sp,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(18.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ANALYSIS_STEPS.forEachIndexed { index, stepName ->
                        val isDone = index < currentStepIndex
                        val isCurrent = index == currentStepIndex
                        val isPending = index > currentStepIndex

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when {
                                            isDone -> RiskLow
                                            isCurrent -> BrandBlue
                                            else -> SurfaceSubtle
                                        }
                                    )
                                    .border(
                                        1.dp,
                                        if (isPending) BorderLight else Color.Transparent,
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isDone) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(12.dp)
                                    )
                                } else if (isCurrent) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(12.dp),
                                        strokeWidth = 2.dp,
                                        color = Color.White
                                    )
                                } else {
                                    Text(
                                        text = "${index + 1}",
                                        fontSize = 9.sp,
                                        color = TextMuted,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Text(
                                text = stepName,
                                fontSize = 12.sp,
                                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                color = when {
                                    isDone -> TextPrimary
                                    isCurrent -> BrandBlue
                                    else -> TextMuted
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
