package com.example.data.model

enum class ChatSender {
    USER,
    MODEL,
    SYSTEM
}

enum class GeminiModelChoice(
    val modelId: String,
    val displayName: String,
    val badge: String,
    val description: String
) {
    FLASH(
        modelId = "gemini-3.5-flash",
        displayName = "Gemini 3.5 Flash",
        badge = "Standard / General",
        description = "General financial defense & multi-turn UPI scam analysis"
    ),
    PRO(
        modelId = "gemini-3.1-pro-preview",
        displayName = "Gemini 3.1 Pro",
        badge = "Complex Forensics",
        description = "Advanced reasoning, fraud tracing, legal recovery & cyber forensics"
    ),
    FLASH_LITE(
        modelId = "gemini-3.1-flash-lite-preview",
        displayName = "Gemini 3.1 Flash Lite",
        badge = "Ultra Fast",
        description = "Lightweight, low-latency instant checks during active transactions"
    )
}

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val sender: ChatSender,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val modelUsed: GeminiModelChoice? = null,
    val isError: Boolean = false,
    val transactionContextId: String? = null
)
