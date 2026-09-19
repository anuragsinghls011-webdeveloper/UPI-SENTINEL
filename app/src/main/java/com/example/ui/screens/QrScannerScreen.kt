package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.model.RiskAnalysisResult
import com.example.engine.UnifiedRiskEngine
import com.example.qr.CameraXQrAnalyzer
import com.example.qr.UpiIntentDetails
import com.example.qr.UpiQrParser
import com.example.ui.components.CameraXPreviewView
import com.example.ui.components.ManualUpiInputDialog
import com.example.ui.components.QrPresetsDialog
import com.example.ui.components.QrScannedResultSheet
import com.example.ui.components.QrScannerOverlay
import com.example.ui.theme.BackgroundLight
import com.example.ui.theme.BorderLight
import com.example.ui.theme.BrandBlue
import com.example.ui.theme.BrandNavy
import com.example.ui.theme.RiskLow
import com.example.ui.theme.SurfaceSubtle
import com.example.ui.theme.SurfaceWhite
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.SentinelViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QrScannerScreen(
    viewModel: SentinelViewModel,
    onViewFullAnalysis: (RiskAnalysisResult) -> Unit,
    onNavigateToAnalyzeForm: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    var isTorchEnabled by remember { mutableStateOf(false) }
    var useFrontCamera by remember { mutableStateOf(false) }
    var showPresetsDialog by remember { mutableStateOf(false) }
    var showManualInputDialog by remember { mutableStateOf(false) }

    // Scanned state
    var capturedIntent by remember { mutableStateOf<UpiIntentDetails?>(null) }
    var realTimeRiskResult by remember { mutableStateOf<RiskAnalysisResult?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showResultSheet by remember { mutableStateOf(false) }

    // CameraX Analyzer instance
    val analyzer = remember {
        CameraXQrAnalyzer { rawQrText ->
            val parsed = UpiQrParser.parse(rawQrText)
            val txInput = parsed.toTransactionInput()
            val assessment = UnifiedRiskEngine.analyzeTransaction(txInput)

            capturedIntent = parsed
            realTimeRiskResult = assessment
            showResultSheet = true
        }
    }

    // Process a selected or manually provided raw UPI URI
    fun processRawUpi(rawText: String) {
        val parsed = UpiQrParser.parse(rawText)
        val txInput = parsed.toTransactionInput()
        val assessment = UnifiedRiskEngine.analyzeTransaction(txInput)

        capturedIntent = parsed
        realTimeRiskResult = assessment
        showResultSheet = true
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        if (hasCameraPermission) {
            // Live CameraX Feed
            CameraXPreviewView(
                analyzer = analyzer,
                isTorchEnabled = isTorchEnabled,
                useFrontCamera = useFrontCamera,
                modifier = Modifier.fillMaxSize()
            )

            // Scanning Overlay
            QrScannerOverlay(
                isTorchEnabled = isTorchEnabled,
                onToggleTorch = { isTorchEnabled = !isTorchEnabled },
                onSwitchCamera = { useFrontCamera = !useFrontCamera },
                onOpenPresets = { showPresetsDialog = true },
                onManualInput = { showManualInputDialog = true },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // Camera Permission Rationale & Simulator fallback
            CameraPermissionRationale(
                onRequestPermission = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                onOpenPresets = { showPresetsDialog = true },
                onManualInput = { showManualInputDialog = true }
            )
        }

        // Dialogs
        if (showPresetsDialog) {
            QrPresetsDialog(
                onDismiss = { showPresetsDialog = false },
                onSelectPreset = { uri ->
                    processRawUpi(uri)
                }
            )
        }

        if (showManualInputDialog) {
            ManualUpiInputDialog(
                onDismiss = { showManualInputDialog = false },
                onSubmitUri = { uri ->
                    processRawUpi(uri)
                }
            )
        }

        // Scanned Result Bottom Sheet
        if (showResultSheet && capturedIntent != null && realTimeRiskResult != null) {
            QrScannedResultSheet(
                intent = capturedIntent!!,
                analysisResult = realTimeRiskResult!!,
                sheetState = sheetState,
                onDismiss = {
                    showResultSheet = false
                    analyzer.resumeScanning()
                },
                onViewFullAnalysis = { result ->
                    showResultSheet = false
                    analyzer.resumeScanning()
                    onViewFullAnalysis(result)
                },
                onLoadIntoForm = { details ->
                    showResultSheet = false
                    analyzer.resumeScanning()
                    val tx = details.toTransactionInput()
                    viewModel.loadFormPreset(tx)
                    onNavigateToAnalyzeForm()
                },
                onScanAnother = {
                    showResultSheet = false
                    capturedIntent = null
                    realTimeRiskResult = null
                    analyzer.resumeScanning()
                }
            )
        }
    }
}

@Composable
private fun CameraPermissionRationale(
    onRequestPermission: () -> Unit,
    onOpenPresets: () -> Unit,
    onManualInput: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
            shape = RoundedCornerShape(20.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(BrandNavy),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "CameraX Scanner",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = BrandNavy
                    )
                    Text(
                        text = "Camera Permission Required",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = BrandBlue
                    )
                }

                Text(
                    text = "UPI Sentinel uses CameraX to scan physical UPI QR codes and immediately extract payee VPAs, payment URLs, invoice numbers, and merchant codes for real-time fraud assessment.",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    lineHeight = 17.sp
                )

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = SurfaceSubtle,
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = RiskLow, modifier = Modifier.size(16.dp))
                        Text(
                            text = "100% On-device frame analysis • Zero external image transmission",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = BrandNavy
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Button(
                    onClick = onRequestPermission,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandNavy),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                        Text("Enable Camera Access", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onOpenPresets,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.QrCode, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Test Presets", fontSize = 11.sp)
                    }

                    OutlinedButton(
                        onClick = onManualInput,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Paste Intent", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}
