package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
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
fun TransactionHistoryScreen(
    viewModel: SentinelViewModel,
    onSelectTransaction: (RiskAnalysisResult) -> Unit,
    modifier: Modifier = Modifier
) {
    val analyses by viewModel.allAnalyses.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("ALL") }

    val filteredList = analyses.filter { item ->
        val matchesQuery = searchQuery.isBlank() ||
                item.transactionId.contains(searchQuery, ignoreCase = true) ||
                item.explanations.any { it.contains(searchQuery, ignoreCase = true) }

        val matchesFilter = when (selectedFilter) {
            "LOW" -> item.riskLevel == RiskLevel.LOW
            "MEDIUM" -> item.riskLevel == RiskLevel.MEDIUM
            "HIGH" -> item.riskLevel == RiskLevel.HIGH
            "CRITICAL" -> item.riskLevel == RiskLevel.CRITICAL
            else -> true
        }

        matchesQuery && matchesFilter
    }

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
                text = "TRANSACTION AUDIT REPOSITORY",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = BrandBlue,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Transaction Risk History",
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = BrandNavy
            )
        }

        // Search Field
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search by Transaction ID or keyword...") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(18.dp)
                )
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            colors = textFieldColors()
        )

        // Filter Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val filters = listOf("ALL", "LOW", "MEDIUM", "HIGH", "CRITICAL")
            filters.forEach { f ->
                val isSelected = selectedFilter == f
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedFilter = f },
                    label = { Text(f, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = BrandNavy,
                        selectedLabelColor = Color.White,
                        containerColor = SurfaceWhite,
                        labelColor = TextSecondary
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isSelected,
                        borderColor = BorderLight,
                        selectedBorderColor = BrandNavy
                    )
                )
            }
        }

        Text(
            text = "SHOWING ${filteredList.size} RECORDED TRANSACTIONS",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = TextMuted,
            letterSpacing = 0.5.sp
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (filteredList.isEmpty()) {
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
                            Text("No transactions match the selected criteria.", fontSize = 13.sp, color = TextMuted)
                        }
                    }
                }
            } else {
                items(filteredList) { analysis ->
                    TransactionRowCard(
                        analysis = analysis,
                        onClick = { onSelectTransaction(analysis) }
                    )
                }
            }
            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
fun TransactionRowCard(
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
                    Text(
                        text = "₹${String.format("%,.0f", analysis.amount)}",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = BrandNavy
                    )
                    RiskBadge(level = analysis.riskLevel)
                }

                Text(
                    text = "${analysis.riskScore}/100",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = getRiskColor(analysis.riskLevel)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = analysis.explanations.firstOrNull() ?: "Standard contextual evaluation",
                fontSize = 12.sp,
                color = TextSecondary,
                maxLines = 2,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${analysis.transactionId} • $dateStr",
                    fontSize = 10.sp,
                    color = TextMuted,
                    fontFamily = FontFamily.Monospace
                )

                if (analysis.reviewStatus != "PENDING") {
                    Surface(
                        color = SurfaceSubtle,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "STATUS: ${analysis.reviewStatus}",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrandNavy,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}
