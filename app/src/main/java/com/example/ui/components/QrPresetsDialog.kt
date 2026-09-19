package com.example.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.qr.QrCodeGenerator
import com.example.ui.theme.BackgroundLight
import com.example.ui.theme.BorderLight
import com.example.ui.theme.BrandBlue
import com.example.ui.theme.BrandNavy
import com.example.ui.theme.RiskCritical
import com.example.ui.theme.RiskHigh
import com.example.ui.theme.RiskLow
import com.example.ui.theme.RiskMedium
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceSubtle
import com.example.ui.theme.SurfaceWhite
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

data class UpiQrPreset(
    val id: String,
    val title: String,
    val expectedRisk: String,
    val riskColor: Color,
    val description: String,
    val upiUri: String
)

val SAMPLE_UPI_QR_PRESETS = listOf(
    UpiQrPreset(
        id = "SAFE_RETAIL",
        title = "FreshMart Retail (Safe ₹40,000)",
        expectedRisk = "LOW RISK (12/100)",
        riskColor = RiskLow,
        description = "Legitimate grocery store checkout, verified merchant handle, official HTTPS payment link.",
        upiUri = "upi://pay?pa=freshmart@okaxis&pn=FreshMart%20Retail%20Private%20Limited&am=40000.00&cu=INR&tn=Monthly%20groceries%20supplies&mc=5411&tr=INV-2026-9042&url=https://freshmart.in/pay"
    ),
    UpiQrPreset(
        id = "TATA_BILL_SCAM",
        title = "Electricity Bill Urgent Scam (₹40,000)",
        expectedRisk = "CRITICAL RISK (87/100)",
        riskColor = RiskCritical,
        description = "Impersonates utility company, but uses personal VPA (ramesh.k@ybl) and insecure HTTP .xyz URL.",
        upiUri = "upi://pay?pa=ramesh.k9182@ybl&pn=Tata%20Power%20DDL%20Support&am=40000.00&cu=INR&tn=URGENT%20Electricity%20Bill%20Verification&mc=4900&tr=BILL-84920&url=http://electricity-bill-pay-verify.xyz/pay"
    ),
    UpiQrPreset(
        id = "LOTTERY_CASHBACK",
        title = "Telegram Bonus Lottery (₹15,000)",
        expectedRisk = "CRITICAL RISK (92/100)",
        riskColor = RiskCritical,
        description = "Fake task refund lure, unverified handle, high-risk .site domain, urgency manipulation.",
        upiUri = "upi://pay?pa=cashback.rewards98@paytm&pn=Official%20Telegram%20Earn&am=15000.00&cu=INR&tn=Claim%20Double%20Bonus%20Lottery&url=http://telegram-rewards-claim.site"
    ),
    UpiQrPreset(
        id = "INVOICE_MISMATCH",
        title = "Invoice Amount Mismatch (₹35,000)",
        expectedRisk = "HIGH RISK (64/100)",
        riskColor = RiskHigh,
        description = "Billed for ₹3,500 item, but QR encodes ₹35,000 (10x inflated charge attempt).",
        upiUri = "upi://pay?pa=croma.retail@icici&pn=Croma%20Electronics&am=35000.00&cu=INR&tn=Electronics%20Purchase&mc=5732&tr=INV-CROMA-3500&url=https://croma.com/bill"
    ),
    UpiQrPreset(
        id = "NEW_BENEFICIARY",
        title = "First-Seen P2P Transfer (₹25,000)",
        expectedRisk = "MEDIUM RISK (44/100)",
        riskColor = RiskMedium,
        description = "High-value transfer to an unverified individual VPA seen for the first time.",
        upiUri = "upi://pay?pa=unknown.stranger@oksbi&pn=Individual%20Account&am=25000.00&cu=INR&tn=Urgent%20Transfer"
    )
)

@Composable
fun QrPresetsDialog(
    onDismiss: () -> Unit,
    onSelectPreset: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var previewingPreset by remember { mutableStateOf<UpiQrPreset?>(null) }
    var generatedBitmap by remember { mutableStateOf<Bitmap?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(20.dp),
            color = SurfaceWhite
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
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
                            text = "SIMULATE / TEST UPI QR",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrandBlue,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Choose Sample Preset",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = BrandNavy
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Text(
                    text = "Test real-time context-aware detection with ready-made UPI intents representing safe merchants and active fraud vectors.",
                    fontSize = 12.sp,
                    color = TextSecondary
                )

                // List of Presets
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(360.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(SAMPLE_UPI_QR_PRESETS) { preset ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = SurfaceSubtle),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = preset.title,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = BrandNavy,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(preset.riskColor.copy(alpha = 0.12f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = preset.expectedRisk,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = preset.riskColor
                                        )
                                    }
                                }

                                Text(
                                    text = preset.description,
                                    fontSize = 11.sp,
                                    color = TextSecondary,
                                    lineHeight = 15.sp
                                )

                                Spacer(modifier = Modifier.height(2.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = {
                                            previewingPreset = preset
                                            generatedBitmap = QrCodeGenerator.generateQrBitmap(preset.upiUri, sizePx = 400)
                                        },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Show QR", fontSize = 11.sp)
                                    }

                                    Button(
                                        onClick = {
                                            onSelectPreset(preset.upiUri)
                                            onDismiss()
                                        },
                                        modifier = Modifier.weight(1.2f),
                                        colors = ButtonDefaults.buttonColors(containerColor = BrandNavy),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Scan This", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // QR Image Preview Dialog (allows pointing another phone or inspecting barcode)
    previewingPreset?.let { preset ->
        AlertDialog(
            onDismissRequest = {
                previewingPreset = null
                generatedBitmap = null
            },
            title = {
                Text(preset.title, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    generatedBitmap?.let { bmp ->
                        Box(
                            modifier = Modifier
                                .size(220.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White)
                                .border(1.dp, BorderLight, RoundedCornerShape(12.dp))
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                bitmap = bmp.asImageBitmap(),
                                contentDescription = "QR Code Preview",
                                modifier = Modifier.size(200.dp)
                            )
                        }
                    }

                    Text(
                        text = "You can scan this QR code with another camera or tap 'Assess Immediately'.",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val uri = preset.upiUri
                        previewingPreset = null
                        onSelectPreset(uri)
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandNavy)
                ) {
                    Text("Assess Immediately")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { previewingPreset = null }) {
                    Text("Back")
                }
            }
        )
    }
}
