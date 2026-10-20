package com.example
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import com.example.ui.components.ClipboardSafetyBanner
import com.example.ui.screens.CybercrimeIntelScreen
import com.example.ui.screens.PrivacyShieldScreen
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.WarningAmber

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import com.example.ui.screens.QrScannerScreen
import com.example.ui.screens.GeminiChatScreen
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.RiskAnalysisResult
import com.example.ui.components.AnalysisProgressDialog
import com.example.ui.screens.AnalyzeScreen
import com.example.ui.screens.AuditLogsScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.DemoScreen
import com.example.ui.screens.ModelIntelScreen
import com.example.ui.screens.PresentationModeScreen
import com.example.ui.screens.RiskResultScreen
import com.example.ui.screens.SecurityScopeScreen
import com.example.ui.screens.TransactionDetailScreen
import com.example.ui.screens.TransactionHistoryScreen
import com.example.ui.theme.BackgroundLight
import com.example.ui.theme.BorderLight
import com.example.ui.theme.BrandBlue
import com.example.ui.theme.BrandNavy
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.RiskLow
import com.example.ui.theme.SurfaceSubtle
import com.example.ui.theme.SurfaceWhite
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.AppTab
import com.example.ui.viewmodel.SentinelViewModel
import com.example.ui.viewmodel.UserRole

class MainActivity : ComponentActivity() {
    private val viewModel: SentinelViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        handleIncomingUpiIntent(intent)
        setContent {
            MyApplicationTheme {
                SentinelMainApp(viewModel = viewModel)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        checkClipboardForUpi()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIncomingUpiIntent(intent)
    }

    private fun handleIncomingUpiIntent(intent: Intent?) {
        if (intent == null || intent.data == null) return
        val uri = intent.data ?: return
        if (uri.scheme == "upi" && (uri.host == "pay" || uri.host == "mandate")) {
            val pa = uri.getQueryParameter("pa") ?: ""
            val pn = uri.getQueryParameter("pn") ?: ""
            val am = uri.getQueryParameter("am") ?: "0"
            
            viewModel.formReceiverId.value = pa
            viewModel.formReceiverName.value = pn
            viewModel.formAmount.value = am
            viewModel.formDescription.value = "Intercepted payment intent via upi://${uri.host}"
            viewModel.selectTab(AppTab.ANALYZE)
        }
    }

    private fun checkClipboardForUpi() {
        try {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager ?: return
            if (clipboard.hasPrimaryClip()) {
                val clipData = clipboard.primaryClip
                if (clipData != null && clipData.itemCount > 0) {
                    val text = clipData.getItemAt(0)?.text?.toString()?.trim() ?: ""
                    val isUpiId = text.matches(Regex("^[a-zA-Z0-9._-]{2,256}@[a-zA-Z]{2,64}$"))
                    val isUpiUri = text.startsWith("upi://pay", ignoreCase = true)
                    if (isUpiId || isUpiUri) {
                        viewModel.onClipboardUpiDetected(text)
                    }
                }
            }
        } catch (_: Exception) {}
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SentinelMainApp(viewModel: SentinelViewModel) {
    val currentTab by viewModel.currentTab.collectAsState()
    val currentRole by viewModel.currentRole.collectAsState()
    val isAnalyzing by viewModel.isAnalyzing.collectAsState()
    val analysisStep by viewModel.analysisStepIndex.collectAsState()
    val selectedDetail by viewModel.selectedDetailResult.collectAsState()
    val clipboardUpi by viewModel.clipboardDetectedUpi.collectAsState()
    val currentAnalysis by viewModel.currentAnalysisResult.collectAsState()
    val isPresentationMode by viewModel.isPresentationMode.collectAsState()

    var showResultScreen by remember { mutableStateOf(false) }
    var activeResultForScreen by remember { mutableStateOf<RiskAnalysisResult?>(null) }
    var roleMenuExpanded by remember { mutableStateOf(false) }

    // Multi-stage progress overlay dialog
    if (isAnalyzing) {
        AnalysisProgressDialog(currentStepIndex = analysisStep)
    }

    if (isPresentationMode) {
        PresentationModeScreen(
            viewModel = viewModel,
            onClose = { viewModel.setPresentationMode(false) }
        )
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(BrandNavy),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "UPI SENTINEL",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = BrandNavy,
                                    letterSpacing = 0.5.sp
                                )
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(RiskLow)
                                )
                            }
                            Text(
                                text = "Context-Aware Fraud Intelligence",
                                fontSize = 10.sp,
                                color = TextMuted
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.selectTab(AppTab.CHAT) },
                        modifier = Modifier.padding(end = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Gemini AI Chat",
                            tint = if (currentTab == AppTab.CHAT) BrandBlue else BrandNavy,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(
                        onClick = { viewModel.selectTab(AppTab.QR_SCAN) },
                        modifier = Modifier.padding(end = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = "Scan UPI QR Code",
                            tint = BrandNavy,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    // Role Switcher Dropdown (Admin / Analyst / Consumer)
                    Box {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = SurfaceSubtle,
                            border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight),
                            modifier = Modifier.clickable { roleMenuExpanded = true }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(BrandBlue)
                                )
                                Text(
                                    text = currentRole.displayName,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BrandNavy
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = roleMenuExpanded,
                            onDismissRequest = { roleMenuExpanded = false }
                        ) {
                            UserRole.entries.forEach { role ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(role.displayName, fontWeight = FontWeight.Bold)
                                            Text(
                                                text = when (role) {
                                                    UserRole.ADMIN -> "Full governance, retraining & system config"
                                                    UserRole.ANALYST -> "Transaction audits & risk investigations"
                                                    UserRole.USER -> "Consumer protection & pre-payment checks"
                                                },
                                                fontSize = 10.sp,
                                                color = TextSecondary
                                            )
                                        }
                                    },
                                    onClick = {
                                        viewModel.switchRole(role)
                                        roleMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SurfaceWhite
                )
            )
        },
        bottomBar = {
            if (selectedDetail == null && !showResultScreen) {
                NavigationBar(
                    containerColor = SurfaceWhite,
                    tonalElevation = 4.dp
                ) {
                    val items = listOf(
                        NavDestination(AppTab.DASHBOARD, Icons.Default.Home, "Overview"),
                        NavDestination(AppTab.QR_SCAN, Icons.Default.QrCodeScanner, "Scan QR"),
                        NavDestination(AppTab.ANALYZE, Icons.Default.FlashOn, "Analyze"),
                        NavDestination(AppTab.CHAT, Icons.Default.AutoAwesome, "Gemini AI"),
                        NavDestination(AppTab.DEMO, Icons.Default.PlayCircleOutline, "Demo"),
                        NavDestination(AppTab.TRANSACTIONS, Icons.Default.ReceiptLong, "History"),
                        NavDestination(AppTab.MODEL, Icons.Default.AutoGraph, "Model"),
                        NavDestination(AppTab.AUDIT, Icons.Default.History, "Audit"),
                        NavDestination(AppTab.SECURITY, Icons.Default.Lock, "Scope")
                    )

                    items.forEach { dest ->
                        val isSelected = currentTab == dest.tab
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { viewModel.selectTab(dest.tab) },
                            icon = {
                                Icon(
                                    imageVector = dest.icon,
                                    contentDescription = dest.label,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            label = {
                                Text(
                                    text = dest.label,
                                    fontSize = 9.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.White,
                                selectedTextColor = BrandNavy,
                                indicatorColor = BrandNavy,
                                unselectedIconColor = TextMuted,
                                unselectedTextColor = TextMuted
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(BackgroundLight)
        ) {
            when {
                // Showing active analysis result
                showResultScreen && activeResultForScreen != null -> {
                    RiskResultScreen(
                        result = activeResultForScreen!!,
                        onAnalyzeAnother = {
                            showResultScreen = false
                            activeResultForScreen = null
                            viewModel.selectTab(AppTab.ANALYZE)
                        },
                        onBackToDashboard = {
                            showResultScreen = false
                            activeResultForScreen = null
                            viewModel.selectTab(AppTab.DASHBOARD)
                        },
                        onConsultGemini = {
                            val target = activeResultForScreen
                            showResultScreen = false
                            activeResultForScreen = null
                            if (target != null) {
                                viewModel.openChatWithTransaction(target)
                            } else {
                                viewModel.selectTab(AppTab.CHAT)
                            }
                        }
                    )
                }

                // Showing specific transaction detail
                selectedDetail != null -> {
                    TransactionDetailScreen(
                        analysis = selectedDetail!!,
                        viewModel = viewModel,
                        onBack = { viewModel.selectDetail(null) }
                    )
                }

                // Main navigation tabs
                else -> {
                    AnimatedContent(
                        targetState = currentTab,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label = "tabTransition"
                    ) { tab ->
                        when (tab) {
                            AppTab.DASHBOARD -> DashboardScreen(
                                viewModel = viewModel,
                                onNavigateToDetail = { result ->
                                    viewModel.selectDetail(result)
                                }
                            )

                            AppTab.QR_SCAN -> QrScannerScreen(
                                viewModel = viewModel,
                                onViewFullAnalysis = { result ->
                                    activeResultForScreen = result
                                    showResultScreen = true
                                },
                                onNavigateToAnalyzeForm = {
                                    viewModel.selectTab(AppTab.ANALYZE)
                                }
                            )

                            AppTab.ANALYZE -> AnalyzeScreen(
                                viewModel = viewModel,
                                onAnalysisComplete = { result ->
                                    activeResultForScreen = result
                                    showResultScreen = true
                                }
                            )

                            AppTab.CHAT -> GeminiChatScreen(
                                viewModel = viewModel
                            )

                            AppTab.DEMO -> DemoScreen(
                                viewModel = viewModel,
                                onViewAnalysisDetail = { result ->
                                    activeResultForScreen = result
                                    showResultScreen = true
                                }
                            )

                            AppTab.TRANSACTIONS -> TransactionHistoryScreen(
                                viewModel = viewModel,
                                onSelectTransaction = { result ->
                                    viewModel.selectDetail(result)
                                }
                            )

                            AppTab.MODEL -> ModelIntelScreen(viewModel = viewModel)
                            AppTab.CYBERCRIME -> CybercrimeIntelScreen(
                                viewModel = viewModel,
                                onBack = { viewModel.selectTab(AppTab.DASHBOARD) }
                            )
                            AppTab.PRIVACY -> PrivacyShieldScreen(
                                onBack = { viewModel.selectTab(AppTab.DASHBOARD) }
                            )
                            AppTab.AUDIT -> AuditLogsScreen(viewModel = viewModel)
                            AppTab.SECURITY -> SecurityScopeScreen()
                        }
                    }
                }
            }
        }
    }
}

private data class NavDestination(
    val tab: AppTab,
    val icon: ImageVector,
    val label: String
)
