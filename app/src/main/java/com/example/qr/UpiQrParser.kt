package com.example.qr

import android.net.Uri
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

object UpiQrParser {

    /**
     * Parses a scanned QR string into structured UPI Intent details.
     * Supports standard NPCI UPI URIs:
     * upi://pay?pa=freshmart@okaxis&pn=FreshMart%20Retail&mc=5411&am=40000.00&cu=INR&url=https://freshmart.in
     */
    fun parse(rawContent: String): UpiIntentDetails {
        val trimmed = rawContent.trim()
        if (trimmed.isEmpty()) {
            return UpiIntentDetails(
                rawQrContent = rawContent,
                isValidUpi = false,
                validationIssues = listOf("QR code contains empty payload")
            )
        }

        // Normalize scheme if needed
        val isUpiScheme = trimmed.startsWith("upi://pay", ignoreCase = true) ||
                trimmed.startsWith("upi://", ignoreCase = true) ||
                trimmed.contains("pa=")

        val params = mutableMapOf<String, String>()

        try {
            // Parse query parameters
            val queryString = when {
                trimmed.contains("?") -> trimmed.substringAfter("?")
                trimmed.contains("pa=") -> trimmed
                else -> ""
            }

            if (queryString.isNotEmpty()) {
                val pairs = queryString.split("&")
                for (pair in pairs) {
                    val idx = pair.indexOf("=")
                    if (idx > 0) {
                        val key = URLDecoder.decode(pair.substring(0, idx), StandardCharsets.UTF_8.name())
                        val value = URLDecoder.decode(pair.substring(idx + 1), StandardCharsets.UTF_8.name())
                        params[key.lowercase()] = value
                    }
                }
            }
        } catch (e: Exception) {
            // Fallback parameter extraction
            val regex = Regex("([a-zA-Z0-9_-]+)=([^&]*)")
            regex.findAll(trimmed).forEach { match ->
                params[match.groupValues[1].lowercase()] = match.groupValues[2]
            }
        }

        val payeeVpa = params["pa"] ?: ""
        val payeeName = params["pn"] ?: ""
        val amountStr = params["am"]
        val amount = amountStr?.toDoubleOrNull()
        val currency = params["cu"] ?: "INR"
        val transactionNote = params["tn"] ?: ""
        val merchantCode = params["mc"] ?: ""
        val transactionRef = params["tr"] ?: ""
        val transactionId = params["tid"] ?: ""
        val paymentUrl = params["url"] ?: ""
        val initiationMode = params["mode"] ?: ""
        val organizationId = params["orgid"] ?: ""
        val signature = params["sign"] ?: ""

        val issues = mutableListOf<String>()
        val flags = mutableListOf<String>()

        // Validation checks
        val isValidUpi = isUpiScheme && payeeVpa.isNotBlank() && payeeVpa.contains("@")

        if (!isValidUpi) {
            if (payeeVpa.isBlank()) {
                issues.add("Missing required Payee VPA ('pa') parameter")
            } else if (!payeeVpa.contains("@")) {
                issues.add("Malformed UPI VPA handle ('$payeeVpa')")
            }
            if (!isUpiScheme) {
                issues.add("Non-standard UPI protocol scheme")
            }
        }

        // Suspicious heuristics checks on payload
        if (paymentUrl.isNotBlank() && paymentUrl.startsWith("http://", ignoreCase = true)) {
            issues.add("Unencrypted HTTP payment reference URL detected: $paymentUrl")
            flags.add("INSECURE_PROTOCOL")
        }

        if (paymentUrl.contains(".xyz") || paymentUrl.contains(".top") || paymentUrl.contains(".site")) {
            issues.add("High-risk domain extension detected in payment URL: $paymentUrl")
            flags.add("SUSPICIOUS_TLD")
        }

        if (payeeName.contains("Electricity", ignoreCase = true) ||
            payeeName.contains("Tata Power", ignoreCase = true) ||
            payeeName.contains("BSES", ignoreCase = true) ||
            payeeName.contains("Police", ignoreCase = true)
        ) {
            if (merchantCode.isBlank() && (payeeVpa.endsWith("@ybl") || payeeVpa.endsWith("@paytm") || payeeVpa.endsWith("@oksbi"))) {
                issues.add("Entity claims utility identity but operates via personal P2P handle ($payeeVpa)")
                flags.add("MERCHANT_MISMATCH")
            }
        }

        if (transactionNote.contains("urgent", ignoreCase = true) ||
            transactionNote.contains("lottery", ignoreCase = true) ||
            transactionNote.contains("double", ignoreCase = true) ||
            transactionNote.contains("verify", ignoreCase = true)
        ) {
            flags.add("PSYCHOLOGICAL_URGENCY")
        }

        return UpiIntentDetails(
            rawQrContent = trimmed,
            isValidUpi = isValidUpi,
            payeeVpa = payeeVpa,
            payeeName = payeeName,
            amount = amount,
            currency = currency,
            transactionNote = transactionNote,
            merchantCode = merchantCode,
            transactionRef = transactionRef,
            transactionId = transactionId,
            paymentUrl = paymentUrl,
            initiationMode = initiationMode,
            organizationId = organizationId,
            signature = signature,
            isSigned = signature.isNotBlank(),
            isDynamicQr = amount != null && amount > 0,
            validationIssues = issues,
            flags = flags
        )
    }

    /**
     * Builds a standard formatted UPI Intent URI string
     */
    fun buildUpiUri(
        pa: String,
        pn: String,
        am: Double? = null,
        cu: String = "INR",
        tn: String? = null,
        mc: String? = null,
        tr: String? = null,
        url: String? = null
    ): String {
        val uriBuilder = StringBuilder("upi://pay?")
        uriBuilder.append("pa=").append(Uri.encode(pa))
        uriBuilder.append("&pn=").append(Uri.encode(pn))
        if (am != null) {
            uriBuilder.append("&am=").append(String.format("%.2f", am))
        }
        uriBuilder.append("&cu=").append(cu)
        if (!tn.isNullOrBlank()) {
            uriBuilder.append("&tn=").append(Uri.encode(tn))
        }
        if (!mc.isNullOrBlank()) {
            uriBuilder.append("&mc=").append(Uri.encode(mc))
        }
        if (!tr.isNullOrBlank()) {
            uriBuilder.append("&tr=").append(Uri.encode(tr))
        }
        if (!url.isNullOrBlank()) {
            uriBuilder.append("&url=").append(Uri.encode(url))
        }
        return uriBuilder.toString()
    }
}
