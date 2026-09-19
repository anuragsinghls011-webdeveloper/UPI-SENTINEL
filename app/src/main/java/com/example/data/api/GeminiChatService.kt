package com.example.data.api

import android.util.Log
import com.example.BuildConfig
import com.example.data.model.ChatMessage
import com.example.data.model.ChatSender
import com.example.data.model.GeminiModelChoice
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object GeminiChatService {

    private const val TAG = "GeminiChatService"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models"

    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()
    }

    private val SYSTEM_INSTRUCTION = """
        You are UPI Sentinel AI, an expert real-time financial fraud defense and digital payment safety assistant.
        Your mission is to help users analyze UPI transactions, decode payment risks, and protect their money against modern payment fraud.

        Core Domain Knowledge:
        1. QR Code Attacks: Malicious QR replacement (quishing), dynamic QR tampering, embedded phishing links, ghost merchant VPAs.
        2. VPA Masquerading: Unverified personal handles (e.g., tech.refund.agent@okhdfcbank) mimicking official company accounts.
        3. Invoice Mismatches: Invoices generated with different amounts or corporate names than the actual UPI payee.
        4. UPI Collect Request Scams: Fraudsters sending collect requests while promising refunds/lottery; explain that RECEIVING money NEVER requires a UPI PIN.
        5. Phishing URLs: Subdomains like 'verify-paytm.top' or 'hdfc-kyc-update.xyz'.
        6. Incident Escalation: Remind victims to immediately call national cyber helpline 1930 and file on cybercrime.gov.in, plus contact their bank to freeze the transaction within the golden hour (first 2-3 hours).

        Behavior Rules:
        - NEVER ask for, store, or accept UPI PINs, ATM cards, CVVs, or OTPs.
        - Emphasize that entering a UPI PIN always DEBITS money, never credits.
        - Give clear, calm, bulleted, actionable guidance.
        - Adapt your depth based on the selected mode:
          * Flash: Concise, direct, balanced advice.
          * Pro: In-depth forensic breakdown, legal recourse steps, and systemic risk analysis.
          * Flash Lite: Ultra-fast, immediate safety check summary.
    """.trimIndent()

    suspend fun sendMessage(
        history: List<ChatMessage>,
        newMessage: String,
        modelChoice: GeminiModelChoice,
        transactionContext: String? = null
    ): Result<String> = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Throwable) {
            ""
        }

        // Validate API Key: if missing or placeholder, use intelligent local fallback
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY" || apiKey == "YOUR_GEMINI_API_KEY") {
            Log.d(TAG, "Gemini API key is placeholder or empty, utilizing local Sentinel Cyber Intelligence fallback")
            return@withContext Result.success(generateLocalAdvisory(newMessage, transactionContext, modelChoice))
        }

        try {
            val url = "$BASE_URL/${modelChoice.modelId}:generateContent?key=$apiKey"

            // Construct contents array with multi-turn conversation history
            val contentsArray = JSONArray()

            // Optional transaction context injection into initial exchange
            if (!transactionContext.isNullOrBlank()) {
                val contextTurn = JSONObject().apply {
                    put("role", "user")
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", "Transaction Context to reference:\n$transactionContext")
                        })
                    })
                }
                val ackTurn = JSONObject().apply {
                    put("role", "model")
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", "Understood. I have absorbed this transaction's context, risk score, and signals. How can I assist you with this transaction?")
                        })
                    })
                }
                contentsArray.put(contextTurn)
                contentsArray.put(ackTurn)
            }

            // Add previous history turns
            for (msg in history) {
                if (msg.isError) continue
                val role = when (msg.sender) {
                    ChatSender.USER -> "user"
                    ChatSender.MODEL -> "model"
                    ChatSender.SYSTEM -> continue
                }
                val turn = JSONObject().apply {
                    put("role", role)
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply { put("text", msg.text) })
                    })
                }
                contentsArray.put(turn)
            }

            // Append current user message
            val currentTurn = JSONObject().apply {
                put("role", "user")
                put("parts", JSONArray().apply {
                    put(JSONObject().apply { put("text", newMessage) })
                })
            }
            contentsArray.put(currentTurn)

            // Construct root JSON
            val requestJson = JSONObject().apply {
                put("contents", contentsArray)

                // System Instruction
                put("systemInstruction", JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply { put("text", SYSTEM_INSTRUCTION) })
                    })
                })

                // Generation Config
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.7)
                    put("topP", 0.95)
                    put("topK", 40)
                })
            }

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = requestJson.toString().toRequestBody(mediaType)

            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            val responseBodyString = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                Log.e(TAG, "Gemini API Error: Code ${response.code}, Body: $responseBodyString")
                // Parse error details if possible
                val errorMsg = try {
                    val errJson = JSONObject(responseBodyString)
                    errJson.optJSONObject("error")?.optString("message")
                        ?: "HTTP ${response.code}: ${response.message}"
                } catch (_: Exception) {
                    "HTTP ${response.code}: ${response.message}"
                }

                // If error is related to key or quota, fall back to local advisory with clear notice
                if (response.code == 400 || response.code == 403 || response.code == 404) {
                    val local = generateLocalAdvisory(newMessage, transactionContext, modelChoice)
                    return@withContext Result.success(
                        "⚠️ Note: Cloud Gemini API returned ($errorMsg). Falling back to built-in Sentinel Cyber Intelligence engine:\n\n$local"
                    )
                }

                return@withContext Result.failure(Exception("Gemini API error ($errorMsg)"))
            }

            // Parse response
            val root = JSONObject(responseBodyString)
            val candidates = root.optJSONArray("candidates")
            val firstCandidate = candidates?.optJSONObject(0)
            val content = firstCandidate?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            val textPart = parts?.optJSONObject(0)?.optString("text")

            if (textPart.isNullOrBlank()) {
                val fallback = generateLocalAdvisory(newMessage, transactionContext, modelChoice)
                return@withContext Result.success(fallback)
            }

            Result.success(textPart)
        } catch (e: Exception) {
            Log.e(TAG, "Gemini network exception", e)
            val local = generateLocalAdvisory(newMessage, transactionContext, modelChoice)
            Result.success("⚠️ Network connection notice (${e.localizedMessage ?: "Offline"}). Providing instant analysis via Sentinel Cyber Intelligence engine:\n\n$local")
        }
    }

    /**
     * Built-in expert rulebook providing high-fidelity, immediate domain responses
     * when offline or when using placeholder keys in development mode.
     */
    private fun generateLocalAdvisory(
        query: String,
        context: String?,
        model: GeminiModelChoice
    ): String {
        val q = query.lowercase()

        val contextPrefix = if (!context.isNullOrBlank()) {
            "🔍 **Context Analyzed**: Transaction data integrated.\n\n"
        } else ""

        return when {
            q.contains("pin") || q.contains("receive money") || q.contains("collect request") -> {
                contextPrefix + """
                    🛡️ **CRITICAL UPI RULE: Receiving Money NEVER Requires a UPI PIN**

                    - **Golden Rule**: If anyone asks you to enter your UPI PIN, scan a QR code, or click an SMS link to **receive** a refund, cashback, or prize, **IT IS 100% A FRAUD**.
                    - **Collect Request Trap**: Fraudsters initiate a "Payment Request" (Collect Request) and tell you to "Accept and enter PIN to claim money". Doing so instantly debits your bank account!
                    - **Recommended Action**: Reject the request in your UPI app immediately and block the sender.
                """.trimIndent()
            }

            q.contains("qr") || q.contains("scan") || q.contains("quishing") -> {
                contextPrefix + """
                    📷 **UPI QR Code Safety & Quishing Defense**

                    - **Static vs Dynamic QR**: Physical QR stickers at merchants can be pasted over by fraudsters with their personal QR code.
                    - **Payee Name Verification**: Always check the name shown on your payment confirmation screen before entering your PIN. If you are buying from "FreshMart" but the screen says "Sunil Kumar (Personal VPA)", do not pay!
                    - **URL in QR**: If scanning a QR code opens a web browser link instead of triggering your UPI app, cancel immediately. Legitimate merchant QRs route directly to UPI intents (`upi://pay`).
                """.trimIndent()
            }

            q.contains("invoice") || q.contains("mismatch") || q.contains("bill") -> {
                contextPrefix + """
                    📄 **Invoice Mismatch Scams Explained**

                    - **The Attack Pattern**: Scammers send an altered PDF or bill quoting ₹5,000, but the payment request or link directs ₹50,000 to an unrelated account.
                    - **Dual-Verification Check**: Always cross-reference the invoice total with the amount displayed on your UPI authorization prompt.
                    - **Tax & Registered Name**: Legitimate B2B/B2C vendors will have registered GSTIN/CIN merchant VPAs, not `@ybl` or `@paytm` personal accounts.
                """.trimIndent()
            }

            q.contains("report") || q.contains("scam") || q.contains("fraud") || q.contains("stolen") || q.contains("cybercrime") -> {
                contextPrefix + """
                    🚨 **Emergency Action Plan: If You Sent Money to a Fraudster**

                    1. **Call 1930 Immediately**: Dial the National Cyber Crime Helpline (1930) operated by the Ministry of Home Affairs. If reported within the **Golden Hour (first 2 hours)**, police can instruct banks to freeze the fraudster's wallet or account before cash-out.
                    2. **Lodge Online Complaint**: Submit screenshots, transaction reference numbers (UTR), and chat logs on **[cybercrime.gov.in](https://cybercrime.gov.in)**.
                    3. **Contact Your Bank**: Request a transaction reversal/recall and ask them to register an official chargeback dispute under NPCI guidelines.
                    4. **Report VPA in UPI App**: Mark the recipient VPA as "Fraud / Phishing" directly within GPay, PhonePe, or Paytm.
                """.trimIndent()
            }

            q.contains("score") || q.contains("why") || q.contains("explain") -> {
                contextPrefix + """
                    📊 **How UPI Sentinel ML Model Calculates Risk**

                    Sentinel uses an ensemble of:
                    1. **Calibrated XGBoost (55%)**: Evaluates 21 statistical features including payee verification, amount deviation, domain TLD, and receiver novelty.
                    2. **Deterministic Rules (30%)**: Evaluates binary red lines (e.g. personal account claiming to be merchant, unverified external payment links).
                    3. **Isolation Forest (15%)**: Measures behavioral anomalies against your personal spending baseline.

                    A score above **75** indicates multiple corroborating high-severity indicators. Never proceed without out-of-band identity verification!
                """.trimIndent()
            }

            else -> {
                val modelTag = when (model) {
                    GeminiModelChoice.PRO -> "Gemini 3.1 Pro (Deep Forensic Model)"
                    GeminiModelChoice.FLASH_LITE -> "Gemini 3.1 Flash Lite (Fast Model)"
                    GeminiModelChoice.FLASH -> "Gemini 3.5 Flash (Balanced Model)"
                }

                contextPrefix + """
                    🤖 **UPI Sentinel Security Advisor ($modelTag)**

                    I am here to protect your digital payments. Here are key precautions for UPI safety:

                    1. **Pre-Payment Verification**: Check that receiver name and VPA match the official business entity.
                    2. **Domain Integrity**: Never follow external links sent via SMS, WhatsApp, or Telegram for payment resolution.
                    3. **Zero-Trust for PIN Requests**: PIN is strictly for debiting your funds, never for refunds, KYC verification, or prize winnings.
                    4. **Emergency Contact**: In case of fraudulent debits, dial **1930** immediately.

                    *Ask me about specific transactions, QR verification, invoice checks, or how to dispute unauthorized debits!*
                """.trimIndent()
            }
        }
    }
}
