package com.example.engine

import com.example.data.model.RiskSignal
import java.net.URI
import java.util.Locale

object DomainAnalyzer {

    private val SAFE_DOMAINS = setOf(
        "phonepe.com", "paytm.com", "google.com", "amazonpay.in",
        "swiggy.com", "zomato.com", "tatacliq.com", "flipkart.com",
        "irctc.co.in", "hdfcbank.com", "icicibank.com", "sbi.co.in",
        "freshmart.in", "blinkit.com", "zepto.in", "bigbasket.com"
    )

    private val SUSPICIOUS_DOMAINS = setOf(
        "upi-pay-verify.xyz", "lottery-reward-claim.top", "free-kyc-update-bank.in",
        "instant-cashback-upi.site", "quick-refund-claim.cc", "secure-upi-portal.link",
        "fastpay-auth.club", "telecom-reward-upi.info", "gift-claim-online.xyz"
    )

    private val SUSPICIOUS_KEYWORDS = listOf(
        "lottery", "cashback", "refund", "kyc", "verify-pin", "urgent",
        "reward", "bonus", "free-gift", "claim-now", "update-bank"
    )

    data class DomainAnalysisResult(
        val hostname: String,
        val isHttps: Boolean,
        val reputation: String, // "SAFE", "SUSPICIOUS", "UNKNOWN"
        val domainRiskScore: Double, // 0.0 to 1.0
        val signals: List<RiskSignal>,
        val merchantMatch: Boolean
    )

    fun analyze(url: String, expectedMerchant: String): DomainAnalysisResult {
        if (url.isBlank()) {
            return DomainAnalysisResult(
                hostname = "N/A (Direct UPI VPA)",
                isHttps = true,
                reputation = "SAFE",
                domainRiskScore = 0.0,
                signals = emptyList(),
                merchantMatch = true
            )
        }

        val signals = mutableListOf<RiskSignal>()
        var isHttps = false
        var host = ""
        var domainRisk = 0.0

        try {
            val normalizedUrl = if (!url.startsWith("http://") && !url.startsWith("https://")) {
                "https://$url"
            } else url

            val uri = URI(normalizedUrl)
            isHttps = uri.scheme?.lowercase(Locale.ROOT) == "https"
            host = uri.host?.lowercase(Locale.ROOT) ?: ""

            if (!isHttps) {
                domainRisk += 0.35
                signals.add(
                    RiskSignal(
                        category = "domain",
                        severity = "high",
                        finding = "Payment link does not use secure HTTPS encryption",
                        evidence = "Scheme is HTTP instead of HTTPS ($url)",
                        riskContribution = 18
                    )
                )
            }

            // Check IP based hostname
            if (host.matches(Regex("^\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}$"))) {
                domainRisk += 0.50
                signals.add(
                    RiskSignal(
                        category = "domain",
                        severity = "critical",
                        finding = "Payment URL points directly to raw numeric IP address",
                        evidence = "IP host detected: $host",
                        riskContribution = 24
                    )
                )
            }

            // Punycode check
            if (host.contains("xn--")) {
                domainRisk += 0.40
                signals.add(
                    RiskSignal(
                        category = "domain",
                        severity = "high",
                        finding = "Internationalized Domain (Punycode) detected — possible homograph deception",
                        evidence = "Punycode prefix found in $host",
                        riskContribution = 20
                    )
                )
            }

            // Known reputation check
            val rootDomain = extractRootDomain(host)
            var reputation = "UNKNOWN"

            if (SAFE_DOMAINS.contains(rootDomain) || SAFE_DOMAINS.contains(host)) {
                reputation = "SAFE"
                domainRisk = maxOf(0.0, domainRisk - 0.20)
            } else if (SUSPICIOUS_DOMAINS.contains(rootDomain) || SUSPICIOUS_DOMAINS.contains(host)) {
                reputation = "SUSPICIOUS"
                domainRisk += 0.60
                signals.add(
                    RiskSignal(
                        category = "domain",
                        severity = "critical",
                        finding = "Domain flagged in prototype suspicious threat database",
                        evidence = "Domain $host matches known fraudulent gateway patterns",
                        riskContribution = 25
                    )
                )
            } else {
                reputation = "UNKNOWN"
                domainRisk += 0.15
            }

            // Suspicious keywords in URL
            val matchedKeywords = SUSPICIOUS_KEYWORDS.filter { url.lowercase(Locale.ROOT).contains(it) }
            if (matchedKeywords.isNotEmpty()) {
                domainRisk += 0.30
                signals.add(
                    RiskSignal(
                        category = "domain",
                        severity = "medium",
                        finding = "Payment URL contains promotional / high-urgency keywords",
                        evidence = "Keywords found: ${matchedKeywords.joinToString(", ")}",
                        riskContribution = 14
                    )
                )
            }

            // Merchant domain alignment
            val merchantMatch = if (expectedMerchant.isNotBlank()) {
                val cleanMerchant = expectedMerchant.lowercase(Locale.ROOT).replace(Regex("[^a-z0-9]"), "")
                val cleanHost = host.replace(Regex("[^a-z0-9]"), "")
                cleanHost.contains(cleanMerchant) || cleanMerchant.contains(cleanHost) || reputation == "SAFE"
            } else true

            if (!merchantMatch && host.isNotBlank()) {
                domainRisk += 0.25
                signals.add(
                    RiskSignal(
                        category = "domain",
                        severity = "medium",
                        finding = "Payment domain does not appear affiliated with merchant '$expectedMerchant'",
                        evidence = "Expected affiliation with '$expectedMerchant' but routing via '$host'",
                        riskContribution = 15
                    )
                )
            }

            return DomainAnalysisResult(
                hostname = host,
                isHttps = isHttps,
                reputation = reputation,
                domainRiskScore = domainRisk.coerceIn(0.0, 1.0),
                signals = signals,
                merchantMatch = merchantMatch
            )

        } catch (e: Exception) {
            return DomainAnalysisResult(
                hostname = "Invalid URL ($url)",
                isHttps = false,
                reputation = "SUSPICIOUS",
                domainRiskScore = 0.50,
                signals = listOf(
                    RiskSignal(
                        category = "domain",
                        severity = "medium",
                        finding = "Malformed or unparseable payment URL",
                        evidence = e.localizedMessage ?: "Invalid URI syntax",
                        riskContribution = 15
                    )
                ),
                merchantMatch = false
            )
        }
    }

    private fun extractRootDomain(hostname: String): String {
        val parts = hostname.split(".")
        return if (parts.size >= 2) {
            parts.takeLast(2).joinToString(".")
        } else hostname
    }
}
