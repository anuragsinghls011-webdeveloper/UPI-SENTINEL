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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AuditLogEntry
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
import com.example.ui.viewmodel.SentinelViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AuditLogsScreen(
    viewModel: SentinelViewModel,
    modifier: Modifier = Modifier
) {
    val logs by viewModel.allAuditLogs.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Spacer(modifier = Modifier.height(4.dp))
        Column {
            Text(
                text = "IMMUTABLE AUDIT TRAIL",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = BrandBlue,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "System & User Audit Logs",
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = BrandNavy
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Complete record of model decisions, analyst reviews, retraining events, and role switches.",
                fontSize = 12.sp,
                color = TextSecondary
            )
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            color = SurfaceSubtle,
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight)
        ) {
            Row(
                modifier = Modifier.padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = BrandNavy,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "All events cryptographically sequenced and indexed for compliance.",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }
        }

        Text(
            text = "LOG ENTRIES (${logs.size})",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = TextMuted,
            letterSpacing = 0.5.sp
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (logs.isEmpty()) {
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
                            Text("No audit logs recorded yet.", fontSize = 13.sp, color = TextMuted)
                        }
                    }
                }
            } else {
                items(logs) { entry ->
                    AuditLogItem(entry = entry)
                }
            }
            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
fun AuditLogItem(entry: AuditLogEntry) {
    val dateStr = SimpleDateFormat("dd MMM yyyy, HH:mm:ss", Locale.getDefault()).format(Date(entry.timestamp))

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = SurfaceWhite,
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(
                                when (entry.eventType) {
                                    "MODEL_TRAINED" -> BrandBlue
                                    "TRANSACTION_ANALYZED" -> RiskLow
                                    "TRANSACTION_REVIEWED" -> BrandNavy
                                    else -> TextMuted
                                }
                            )
                    )
                    Text(
                        text = entry.eventType,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrandNavy
                    )
                }

                Text(
                    text = dateStr,
                    fontSize = 10.sp,
                    color = TextMuted,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = entry.metadata,
                fontSize = 12.sp,
                color = TextPrimary,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Actor: ${entry.actor} • Target: ${entry.entityId}",
                fontSize = 10.sp,
                color = TextMuted
            )
        }
    }
}
