package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Security
import com.example.ui.viewmodel.AppTab
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.RiskAnalysisResult
import com.example.ui.theme.BackgroundLight
import com.example.ui.theme.BorderLight
import com.example.ui.theme.BorderSubtle
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

@Composable
fun AnalyzeScreen(
    viewModel: SentinelViewModel,
    onAnalysisComplete: (RiskAnalysisResult) -> Unit,
    modifier: Modifier = Modifier
) {
    val amount by viewModel.formAmount.collectAsState()
    val description by viewModel.formDescription.collectAsState()
    val receiverName by viewModel.formReceiverName.collectAsState()
    val receiverId by viewModel.formReceiverId.collectAsState()
    val receiverVerified by viewModel.formReceiverVerified.collectAsState()
    val merchantName by viewModel.formMerchantName.collectAsState()
    val merchantVerified by viewModel.formMerchantVerified.collectAsState()
    val paymentUrl by viewModel.formPaymentUrl.collectAsState()
    val domain by viewModel.formDomain.collectAsState()
    val invoiceId by viewModel.formInvoiceId.collectAsState()
    val invoiceAmount by viewModel.formInvoiceAmount.collectAsState()
    val invoiceMerchant by viewModel.formInvoiceMerchant.collectAsState()
    val userTxnCount24h by viewModel.formUserTxnCount24h.collectAsState()
    val avgTxnAmount by viewModel.formAvgTxnAmount.collectAsState()
    val receiverFirstSeen by viewModel.formReceiverFirstSeen.collectAsState()
    val failedAttempts by viewModel.formFailedAttempts.collectAsState()
    val unusualTime by viewModel.formUnusualTime.collectAsState()

    val scenarios = viewModel.getScenarios()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Top Header
        Column {
            Text(
                text = "TRANSACTION RISK ANALYSIS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = BrandBlue,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Analyze Payment Context",
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = BrandNavy
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Enter simulated transaction signals or choose a prefill preset to evaluate explainable risk.",
                fontSize = 12.sp,
                color = TextSecondary
            )
        }

        // Quick Preset & QR Scanner Card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = SurfaceWhite,
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight)
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Primary QR Scanner shortcut
                Button(
                    onClick = { viewModel.selectTab(AppTab.QR_SCAN) },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandNavy),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCodeScanner,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Scan Physical UPI QR Code (CameraX)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = "OR QUICK LOAD SCENARIO PRESETS",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted,
                    letterSpacing = 0.5.sp
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    scenarios.forEach { (name, tx) ->
                        OutlinedButton(
                            onClick = { viewModel.loadFormPreset(tx) },
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight),
                            colors = ButtonDefaults.outlinedButtonColors(containerColor = SurfaceSubtle)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = BrandBlue
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = name.replace("SCENARIO ", "S"),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = BrandNavy
                            )
                        }
                    }
                }
            }
        }

        // Section 1: Transaction Details
        FormSection(title = "1. TRANSACTION DETAILS") {
            OutlinedTextField(
                value = amount,
                onValueChange = { viewModel.formAmount.value = it },
                label = { Text("Payment Amount (INR)") },
                prefix = { Text("₹ ", fontWeight = FontWeight.Bold) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                colors = textFieldColors()
            )
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField(
                value = description,
                onValueChange = { viewModel.formDescription.value = it },
                label = { Text("Description / Purpose") },
                modifier = Modifier.fillMaxWidth(),
                colors = textFieldColors()
            )
        }

        // Section 2: Receiver Details
        FormSection(title = "2. RECEIVER DETAILS") {
            OutlinedTextField(
                value = receiverName,
                onValueChange = { viewModel.formReceiverName.value = it },
                label = { Text("Receiver Display Name") },
                modifier = Modifier.fillMaxWidth(),
                colors = textFieldColors()
            )
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField(
                value = receiverId,
                onValueChange = { viewModel.formReceiverId.value = it },
                label = { Text("Receiver UPI VPA (e.g. name@bank)") },
                modifier = Modifier.fillMaxWidth(),
                colors = textFieldColors()
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Receiver Bank/KYC Verified", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    Text("Registered merchant or verified individual", fontSize = 11.sp, color = TextMuted)
                }
                Switch(
                    checked = receiverVerified,
                    onCheckedChange = { viewModel.formReceiverVerified.value = it },
                    colors = switchColors()
                )
            }
        }

        // Section 3: Merchant Details
        FormSection(title = "3. MERCHANT DETAILS") {
            OutlinedTextField(
                value = merchantName,
                onValueChange = { viewModel.formMerchantName.value = it },
                label = { Text("Designated Merchant Name") },
                modifier = Modifier.fillMaxWidth(),
                colors = textFieldColors()
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Merchant Officially Registered", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    Text("Verified in merchant catalog", fontSize = 11.sp, color = TextMuted)
                }
                Switch(
                    checked = merchantVerified,
                    onCheckedChange = { viewModel.formMerchantVerified.value = it },
                    colors = switchColors()
                )
            }
        }

        // Section 4: Payment Link & Domain
        FormSection(title = "4. PAYMENT LINK & DOMAIN") {
            OutlinedTextField(
                value = paymentUrl,
                onValueChange = {
                    viewModel.formPaymentUrl.value = it
                    // auto extract domain
                    try {
                        val host = it.substringAfter("://").substringBefore("/")
                        if (host.isNotBlank()) viewModel.formDomain.value = host
                    } catch (_: Exception) {}
                },
                label = { Text("Checkout / Payment URL (Optional)") },
                modifier = Modifier.fillMaxWidth(),
                colors = textFieldColors()
            )
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField(
                value = domain,
                onValueChange = { viewModel.formDomain.value = it },
                label = { Text("Domain Hostname (e.g. freshmart.in)") },
                modifier = Modifier.fillMaxWidth(),
                colors = textFieldColors()
            )
        }

        // Section 5: Invoice Details
        FormSection(title = "5. INVOICE DETAILS") {
            OutlinedTextField(
                value = invoiceId,
                onValueChange = { viewModel.formInvoiceId.value = it },
                label = { Text("Invoice Number / Order ID") },
                modifier = Modifier.fillMaxWidth(),
                colors = textFieldColors()
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = invoiceAmount,
                    onValueChange = { viewModel.formInvoiceAmount.value = it },
                    label = { Text("Invoice Amount") },
                    prefix = { Text("₹ ") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    colors = textFieldColors()
                )
                OutlinedTextField(
                    value = invoiceMerchant,
                    onValueChange = { viewModel.formInvoiceMerchant.value = it },
                    label = { Text("Invoice Issuer") },
                    modifier = Modifier.weight(1f),
                    colors = textFieldColors()
                )
            }
        }

        // Section 6: Behavioural Baseline
        FormSection(title = "6. BEHAVIOURAL BASELINE") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = userTxnCount24h,
                    onValueChange = { viewModel.formUserTxnCount24h.value = it },
                    label = { Text("24h Txn Count") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    colors = textFieldColors()
                )
                OutlinedTextField(
                    value = avgTxnAmount,
                    onValueChange = { viewModel.formAvgTxnAmount.value = it },
                    label = { Text("Average Txn (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    colors = textFieldColors()
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("First Time Encountering Receiver", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    Text("Receiver novelty signal", fontSize = 11.sp, color = TextMuted)
                }
                Switch(
                    checked = receiverFirstSeen,
                    onCheckedChange = { viewModel.formReceiverFirstSeen.value = it },
                    colors = switchColors()
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Unusual Timing Flag", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    Text("Late night payment window (2 AM - 5 AM)", fontSize = 11.sp, color = TextMuted)
                }
                Switch(
                    checked = unusualTime,
                    onCheckedChange = { viewModel.formUnusualTime.value = it },
                    colors = switchColors()
                )
            }
        }

        // Privacy Guarantee Banner
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            color = SurfaceSubtle,
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = BrandNavy,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "Privacy Guarantee: No UPI PIN, OTP, or banking password is ever requested or accessed.",
                    fontSize = 11.sp,
                    color = TextSecondary,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Action Button
        Button(
            onClick = { viewModel.submitAnalysis(onComplete = onAnalysisComplete) },
            colors = ButtonDefaults.buttonColors(containerColor = BrandNavy),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Security,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = Color.White
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "EVALUATE TRANSACTION RISK",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
fun FormSection(
    title: String,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = SurfaceWhite,
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight),
        shadowElevation = 0.5.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = BrandNavy,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
fun textFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = BrandBlue,
    unfocusedBorderColor = BorderLight,
    focusedLabelColor = BrandBlue,
    unfocusedLabelColor = TextSecondary,
    focusedTextColor = TextPrimary,
    unfocusedTextColor = TextPrimary
)

@Composable
fun switchColors() = SwitchDefaults.colors(
    checkedThumbColor = Color.White,
    checkedTrackColor = BrandBlue,
    uncheckedThumbColor = Color.White,
    uncheckedTrackColor = SurfaceSubtle,
    uncheckedBorderColor = BorderLight
)
