package com.example.ui.viewmodel
import com.example.data.model.AdversarialImmuneSession
import com.example.data.model.RegressionTestVector
import com.example.engine.IntentBridgeEngine
import com.example.data.model.BusinessIntent
import com.example.data.model.PaymentRequestIntent
import com.example.data.model.OutcomeStatus
import com.example.data.api.SentinelApiService
import com.example.data.repository.CybercrimeThreatRepository
import com.example.engine.DualModeRiskEngine
import com.example.data.model.CybercrimeThreatRecord

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.api.GeminiChatService
import com.example.data.local.SentinelDatabase
import com.example.data.model.AuditLogEntry
import com.example.data.model.ChatMessage
import com.example.data.model.ChatSender
import com.example.data.model.GeminiModelChoice
import com.example.data.model.ModelBenchmark
import com.example.data.model.RiskAnalysisResult
import com.example.data.model.TransactionInput
import com.example.data.repository.SentinelRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppTab(val title: String) {
    DASHBOARD("Dashboard"),
    INTENT_BRIDGE("IntentBridge"),
    QR_SCAN("Scan QR"),
    ANALYZE("Analyze"),
    CHAT("Gemini AI"),
    DEMO("Demo"),
    TRANSACTIONS("Transactions"),
    MODEL("Model & Intel"),
    CYBERCRIME("1930 Cybercrime"),
    PRIVACY("Privacy Shield"),
    AUDIT("Audit Logs"),
    SECURITY("Security")
}

enum class UserRole(val displayName: String, val badgeColorName: String) {
    ADMIN("Admin", "Purple"),
    ANALYST("Risk Analyst", "Blue"),
    USER("Consumer", "Green")
}

class SentinelViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: SentinelRepository

    private val apiService = SentinelApiService.create()
    val cybercrimeThreatRepository = CybercrimeThreatRepository(apiService)
    val dualModeRiskEngine = DualModeRiskEngine(apiService, cybercrimeThreatRepository)
    val intentBridgeEngine = IntentBridgeEngine(cybercrimeThreatRepository)

    // Permanent Regression Test Vectors (Never Forget That Attack)
    private val _regressionVectors = MutableStateFlow<List<RegressionTestVector>>(
        listOf(
            RegressionTestVector(
                regressionId = "REG-VEC-10901",
                incidentOrigin = "Swiggy Dynamic QR Overbilling",
                attackVectorName = "Amount Inflation + Lookalike VPA",
                variantCount = 4,
                patchRef = "RULE_IMMUNE_AMOUNT_SPLIT_GUARD",
                isRegressionPassing = true,
                timestamp = System.currentTimeMillis() - 86400000L
            ),
            RegressionTestVector(
                regressionId = "REG-VEC-10902",
                incidentOrigin = "OLX Disguised Collect Request",
                attackVectorName = "Reverse Debit Direction Manipulation",
                variantCount = 5,
                patchRef = "RULE_IMMUNE_COLLECT_REVERSE_GUARD",
                isRegressionPassing = true,
                timestamp = System.currentTimeMillis() - 43200000L
            )
        )
    )
    val regressionVectors: StateFlow<List<RegressionTestVector>> = _regressionVectors.asStateFlow()

    private val _currentImmuneSession = MutableStateFlow<AdversarialImmuneSession?>(null)
    val currentImmuneSession: StateFlow<AdversarialImmuneSession?> = _currentImmuneSession.asStateFlow()

    private val _isSimulatingAttack = MutableStateFlow(false)
    val isSimulatingAttack: StateFlow<Boolean> = _isSimulatingAttack.asStateFlow()

    // Clipboard and Intent Interception states
    private val _clipboardDetectedUpi = MutableStateFlow<String?>(null)
    val clipboardDetectedUpi: StateFlow<String?> = _clipboardDetectedUpi.asStateFlow()

    private val _cloudSyncActive = MutableStateFlow(false)
    val cloudSyncActive: StateFlow<Boolean> = _cloudSyncActive.asStateFlow()

    init {
        val db = SentinelDatabase.getDatabase(application)
        repository = SentinelRepository(db.sentinelDao())
        syncCybercrimeIntelligence()
    }

    fun syncCybercrimeIntelligence() {
        viewModelScope.launch {
            cybercrimeThreatRepository.syncLiveFeed()
        }
    }

    fun onClipboardUpiDetected(upi: String?) {
        _clipboardDetectedUpi.value = upi
    }

    fun dismissClipboardBanner() {
        _clipboardDetectedUpi.value = null
    }

    // Active Navigation
    private val _currentTab = MutableStateFlow(AppTab.DASHBOARD)
    val currentTab: StateFlow<AppTab> = _currentTab.asStateFlow()

    // Role state
    private val _currentRole = MutableStateFlow(UserRole.ANALYST)
    val currentRole: StateFlow<UserRole> = _currentRole.asStateFlow()

    // Database reactive streams
    val allAnalyses: StateFlow<List<RiskAnalysisResult>> = repository.allAnalyses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAuditLogs: StateFlow<List<AuditLogEntry>> = repository.allAuditLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalCount: StateFlow<Int> = repository.totalCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val lowCount: StateFlow<Int> = repository.lowCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val mediumCount: StateFlow<Int> = repository.mediumCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val highCount: StateFlow<Int> = repository.highCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val criticalCount: StateFlow<Int> = repository.criticalCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val averageRiskScore: StateFlow<Double> = repository.averageRiskScore
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // Analysis workflow
    private val _isAnalyzing = MutableStateFlow(false)
    val isAnalyzing: StateFlow<Boolean> = _isAnalyzing.asStateFlow()

    private val _analysisStepIndex = MutableStateFlow(0)
    val analysisStepIndex: StateFlow<Int> = _analysisStepIndex.asStateFlow()

    private val _currentAnalysisResult = MutableStateFlow<RiskAnalysisResult?>(null)
    val currentAnalysisResult: StateFlow<RiskAnalysisResult?> = _currentAnalysisResult.asStateFlow()

    private val _selectedDetailResult = MutableStateFlow<RiskAnalysisResult?>(null)
    val selectedDetailResult: StateFlow<RiskAnalysisResult?> = _selectedDetailResult.asStateFlow()

    // Demo Mode & Presentation state
    private val _isPresentationMode = MutableStateFlow(false)
    val isPresentationMode: StateFlow<Boolean> = _isPresentationMode.asStateFlow()

    private val _presentationStep = MutableStateFlow(0)
    val presentationStep: StateFlow<Int> = _presentationStep.asStateFlow()

    private val _safeDemoResult = MutableStateFlow<RiskAnalysisResult?>(null)
    val safeDemoResult: StateFlow<RiskAnalysisResult?> = _safeDemoResult.asStateFlow()

    private val _suspiciousDemoResult = MutableStateFlow<RiskAnalysisResult?>(null)
    val suspiciousDemoResult: StateFlow<RiskAnalysisResult?> = _suspiciousDemoResult.asStateFlow()

    // Model Retraining & Benchmarks
    private val _modelBenchmark = MutableStateFlow(
        ModelBenchmark(
            version = "v2.1.0-xgb-calibrated",
            algorithm = "Calibrated XGBoost Ensemble + Isolation Forest",
            datasetSize = 100000,
            featureCount = 21,
            trainedDate = "2026-09-18 12:00 UTC",
            precision = 0.968,
            recall = 0.952,
            f1Score = 0.960,
            rocAuc = 0.988,
            prAuc = 0.979,
            truePositives = 19040,
            falsePositives = 620,
            trueNegatives = 78940,
            falseNegatives = 1400,
            avgInferenceMs = 6.2
        )
    )
    val modelBenchmark: StateFlow<ModelBenchmark> = _modelBenchmark.asStateFlow()

    private val _isRetraining = MutableStateFlow(false)
    val isRetraining: StateFlow<Boolean> = _isRetraining.asStateFlow()

    // Form inputs
    val formAmount = MutableStateFlow("40000")
    val formDescription = MutableStateFlow("Monthly Supplies & Groceries")
    val formReceiverName = MutableStateFlow("FreshMart Retail Private Limited")
    val formReceiverId = MutableStateFlow("freshmart@okaxis")
    val formReceiverVerified = MutableStateFlow(true)
    val formMerchantName = MutableStateFlow("FreshMart Retail")
    val formMerchantVerified = MutableStateFlow(true)
    val formPaymentUrl = MutableStateFlow("https://freshmart.in/checkout/pay")
    val formDomain = MutableStateFlow("freshmart.in")
    val formInvoiceId = MutableStateFlow("INV-2026-9042")
    val formInvoiceAmount = MutableStateFlow("40000")
    val formInvoiceMerchant = MutableStateFlow("FreshMart Retail")
    val formUserTxnCount24h = MutableStateFlow("2")
    val formAvgTxnAmount = MutableStateFlow("38000")
    val formReceiverFirstSeen = MutableStateFlow(false)
    val formFailedAttempts = MutableStateFlow("0")
    val formUnusualTime = MutableStateFlow(false)

    // ==========================================
    // Gemini Chat State & Engine
    // ==========================================
    private val _selectedGeminiModel = MutableStateFlow(GeminiModelChoice.FLASH)
    val selectedGeminiModel: StateFlow<GeminiModelChoice> = _selectedGeminiModel.asStateFlow()

    private val _isChatLoading = MutableStateFlow(false)
    val isChatLoading: StateFlow<Boolean> = _isChatLoading.asStateFlow()

    private val _activeChatContext = MutableStateFlow<RiskAnalysisResult?>(null)
    val activeChatContext: StateFlow<RiskAnalysisResult?> = _activeChatContext.asStateFlow()

    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                sender = ChatSender.MODEL,
                text = "🛡️ **Welcome to UPI Sentinel AI**\n\nI am your real-time payment defense assistant powered by Gemini. You can ask me to:\n- **Verify suspicious QR codes or VPAs**\n- **Explain invoice mismatch risks**\n- **Decode transaction risk scores**\n- **Guide emergency steps for unauthorized debits (1930 / cybercrime.gov.in)**\n\nHow can I help protect your transaction today?",
                modelUsed = GeminiModelChoice.FLASH
            )
        )
    )
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    fun setGeminiModel(choice: GeminiModelChoice) {
        _selectedGeminiModel.value = choice
    }

    fun clearChatHistory() {
        _chatMessages.value = listOf(
            ChatMessage(
                sender = ChatSender.MODEL,
                text = "Chat history cleared. How can I assist you with your payment safety?",
                modelUsed = _selectedGeminiModel.value
            )
        )
    }

    fun openChatWithTransaction(analysis: RiskAnalysisResult) {
        _activeChatContext.value = analysis
        _currentTab.value = AppTab.CHAT

        val prompt = "Explain the risk findings and safety steps for transaction ${analysis.transactionId} (Amount: ₹${String.format("%,.0f", analysis.amount)}, Score: ${analysis.riskScore}/100 - ${analysis.riskLevel.label})."
        sendChatMessage(prompt)
    }

    fun clearChatContext() {
        _activeChatContext.value = null
    }

    fun sendChatMessage(text: String) {
        val trimmed = text.trim()
        if (trimmed.isBlank() || _isChatLoading.value) return

        val userMessage = ChatMessage(
            sender = ChatSender.USER,
            text = trimmed,
            transactionContextId = _activeChatContext.value?.transactionId
        )

        val updatedHistory = _chatMessages.value + userMessage
        _chatMessages.value = updatedHistory
        _isChatLoading.value = true

        viewModelScope.launch {
            // Build context string if active transaction is attached
            val contextString = _activeChatContext.value?.let { tx ->
                """
                Transaction ID: ${tx.transactionId}
                Amount: ₹${tx.amount}
                Risk Score: ${tx.riskScore}/100 (${tx.riskLevel.label})
                Recommended Action: ${tx.recommendedAction}
                Signals Triggered: ${tx.signals.joinToString("; ") { "[${it.category}] ${it.finding}" }}
                Explanations: ${tx.explanations.joinToString("; ")}
                Counterfactuals: ${tx.counterfactuals.joinToString("; ")}
                """.trimIndent()
            }

            val model = _selectedGeminiModel.value
            val result = GeminiChatService.sendMessage(
                history = updatedHistory.dropLast(1),
                newMessage = trimmed,
                modelChoice = model,
                transactionContext = contextString
            )

            result.fold(
                onSuccess = { replyText ->
                    val assistantMessage = ChatMessage(
                        sender = ChatSender.MODEL,
                        text = replyText,
                        modelUsed = model,
                        transactionContextId = _activeChatContext.value?.transactionId
                    )
                    _chatMessages.value = _chatMessages.value + assistantMessage
                },
                onFailure = { error ->
                    val errorMessage = ChatMessage(
                        sender = ChatSender.MODEL,
                        text = "⚠️ Unable to contact Gemini API: ${error.localizedMessage ?: "Unknown error"}. Please verify connection or configure your API key in AI Studio's Secrets panel.",
                        modelUsed = model,
                        isError = true
                    )
                    _chatMessages.value = _chatMessages.value + errorMessage
                }
            )

            _isChatLoading.value = false
        }
    }

    fun selectTab(tab: AppTab) {
        _currentTab.value = tab
    }

    fun switchRole(role: UserRole) {
        _currentRole.value = role
        viewModelScope.launch {
            repository.logEvent("ROLE_SWITCHED", "system", role.name, "Active session role changed to ${role.displayName}")
        }
    }

    fun selectDetail(result: RiskAnalysisResult?) {
        _selectedDetailResult.value = result
    }

    fun loadFormPreset(tx: TransactionInput) {
        formAmount.value = tx.amount.toInt().toString()
        formDescription.value = tx.description
        formReceiverName.value = tx.receiverName
        formReceiverId.value = tx.receiverId
        formReceiverVerified.value = tx.receiverVerified
        formMerchantName.value = tx.merchantName
        formMerchantVerified.value = tx.merchantVerified
        formPaymentUrl.value = tx.paymentUrl
        formDomain.value = tx.domain
        formInvoiceId.value = tx.invoiceId
        formInvoiceAmount.value = if (tx.invoiceAmount > 0) tx.invoiceAmount.toInt().toString() else ""
        formInvoiceMerchant.value = tx.invoiceMerchant
        formUserTxnCount24h.value = tx.userTransactionCount24h.toString()
        formAvgTxnAmount.value = tx.averageTransactionAmount.toInt().toString()
        formReceiverFirstSeen.value = tx.receiverFirstSeen
        formFailedAttempts.value = tx.failedAttempts.toString()
        formUnusualTime.value = tx.unusualTimeFlag
    }

    fun submitAnalysis(onComplete: (RiskAnalysisResult) -> Unit = {}) {
        val amt = formAmount.value.toDoubleOrNull() ?: 40000.0
        val invAmt = formInvoiceAmount.value.toDoubleOrNull() ?: 0.0
        val txn24h = formUserTxnCount24h.value.toIntOrNull() ?: 2
        val avgAmt = formAvgTxnAmount.value.toDoubleOrNull() ?: 35000.0
        val fails = formFailedAttempts.value.toIntOrNull() ?: 0

        val input = TransactionInput(
            transactionId = "TXN-${System.currentTimeMillis().toString().takeLast(6)}",
            amount = amt,
            description = formDescription.value,
            receiverName = formReceiverName.value,
            receiverId = formReceiverId.value,
            receiverVerified = formReceiverVerified.value,
            merchantName = formMerchantName.value,
            merchantVerified = formMerchantVerified.value,
            paymentUrl = formPaymentUrl.value,
            domain = formDomain.value,
            invoiceId = formInvoiceId.value,
            invoiceAmount = invAmt,
            invoiceMerchant = formInvoiceMerchant.value,
            userTransactionCount24h = txn24h,
            averageTransactionAmount = avgAmt,
            receiverFirstSeen = formReceiverFirstSeen.value,
            failedAttempts = fails,
            unusualTimeFlag = formUnusualTime.value
        )

        viewModelScope.launch {
            _isAnalyzing.value = true
            _analysisStepIndex.value = 0

            // Multi-step visual progress representing real pipeline execution
            val stepDelays = listOf(140L, 120L, 110L, 130L, 120L, 130L, 150L, 130L, 100L)
            for (i in 0 until 9) {
                _analysisStepIndex.value = i
                delay(stepDelays[i])
            }

            val result = repository.analyzeAndSave(input, actor = _currentRole.value.displayName)
            _currentAnalysisResult.value = result
            _isAnalyzing.value = false
            onComplete(result)
        }
    }

    fun runScenario(scenarioTx: TransactionInput, onFinished: (RiskAnalysisResult) -> Unit = {}) {
        viewModelScope.launch {
            _isAnalyzing.value = true
            _analysisStepIndex.value = 0
            for (i in 0 until 9) {
                _analysisStepIndex.value = i
                delay(80L)
            }
            val result = repository.analyzeAndSave(scenarioTx, actor = "demo@sentinel.io")
            _currentAnalysisResult.value = result
            _isAnalyzing.value = false

            if (scenarioTx.transactionId.contains("SAFE")) {
                _safeDemoResult.value = result
            } else if (scenarioTx.transactionId.contains("SUSP")) {
                _suspiciousDemoResult.value = result
            }

            onFinished(result)
        }
    }

    fun updateTransactionReview(id: String, status: String, note: String) {
        viewModelScope.launch {
            repository.updateReview(id, status, note, actor = _currentRole.value.displayName)
            _selectedDetailResult.value = repository.getAnalysisById(id)
        }
    }

    fun setPresentationMode(enabled: Boolean) {
        _isPresentationMode.value = enabled
        _presentationStep.value = 0
    }

    fun nextPresentationStep() {
        _presentationStep.value = (_presentationStep.value + 1).coerceAtMost(5)
    }

    fun prevPresentationStep() {
        _presentationStep.value = (_presentationStep.value - 1).coerceAtLeast(0)
    }

    fun triggerModelRetraining() {
        viewModelScope.launch {
            _isRetraining.value = true
            delay(1600L) // Simulate XGBoost training & cross-validation on 100,000 synthetic rows
            _modelBenchmark.value = ModelBenchmark(
                version = "v2.1.1-xgb-calibrated",
                datasetSize = 100000,
                featureCount = 21,
                trainedDate = "Just now (Local Pipeline)",
                precision = 0.971,
                recall = 0.958,
                f1Score = 0.964,
                rocAuc = 0.991,
                prAuc = 0.982,
                avgInferenceMs = 5.8
            )
            repository.logEvent("MODEL_TRAINED", _currentRole.value.displayName, "v2.1.1-xgb-calibrated", "Model retrained with 100,000 synthetic UPI records. F1 score: 0.964, AUC-ROC: 0.991")
            _isRetraining.value = false
        }
    }

    fun getScenarios() = repository.getDemoScenarios()

    fun runAdversarialDefensePipeline(
        incidentName: String,
        business: BusinessIntent,
        payment: PaymentRequestIntent
    ) {
        viewModelScope.launch {
            _isSimulatingAttack.value = true
            delay(400L) // Visual progression
            val session = intentBridgeEngine.runAdversarialSimulation(incidentName, business, payment)
            _currentImmuneSession.value = session
            if (session.regressionVectorSaved != null) {
                _regressionVectors.value = listOf(session.regressionVectorSaved) + _regressionVectors.value
                repository.logEvent(
                    eventType = "REGRESSION_VECTOR_LOCKED",
                    actor = _currentRole.value.displayName,
                    entityId = session.regressionVectorSaved.regressionId,
                    metadata = "Immune Patch generated: ${session.generatedPatch?.candidateRuleName} | Added to regression suite."
                )
            }
            _isSimulatingAttack.value = false
        }
    }

    fun recordOutcome(evaluationId: String, outcome: OutcomeStatus, notes: String = "") {
        viewModelScope.launch {
            repository.logEvent(
                eventType = "INTENT_OUTCOME_RECORDED",
                actor = _currentRole.value.displayName,
                entityId = evaluationId,
                metadata = "Outcome: ${outcome.displayName} | Notes: $notes"
            )
        }
    }
}
