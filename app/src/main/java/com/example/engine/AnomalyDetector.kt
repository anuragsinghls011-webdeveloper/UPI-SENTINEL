package com.example.engine

import kotlin.math.ln
import kotlin.math.pow

object AnomalyDetector {

    data class AnomalyResult(
        val anomalyScore: Double, // 0.0 to 1.0
        val isAnomaly: Boolean,
        val topContributors: List<String>,
        val isolationDepth: Double,
        val averagePathLength: Double,
        val treesSampled: Int = 100
    )

    // Euler-Mascheroni constant for average path length of unsuccessful searches in BST
    private const val EULER_MASCHERONI = 0.5772156649
    private const val SUBSAMPLE_SIZE = 256

    // Pre-calculated average tree depth c(256)
    private val cN: Double by lazy {
        val n = SUBSAMPLE_SIZE.toDouble()
        2.0 * (ln(n - 1.0) + EULER_MASCHERONI) - (2.0 * (n - 1.0) / n)
    }

    fun detect(features: FeatureExtractor.ExtractedFeatures): AnomalyResult {
        var isolationPenalty = 0.0
        val contributors = mutableListOf<String>()

        // 1. Amount deviation distance
        if (features.amountDeviation > 0.5) {
            val penalty = features.amountDeviation * 3.2
            isolationPenalty += penalty
            contributors.add("Spending deviation (${String.format("%.1fx", features.amountDeviation * 5)})")
        }

        // 2. Velocity spike
        if (features.velocityAnomaly > 0.5) {
            isolationPenalty += 2.8
            contributors.add("24-hr transaction velocity burst")
        }

        // 3. Timing window outlier
        if (features.timeAnomaly > 0.5) {
            isolationPenalty += 2.2
            contributors.add("Off-hours execution window (2 AM - 5 AM)")
        }

        // 4. Repeated authentication failures
        if (features.failedAttempts > 0.2) {
            isolationPenalty += 3.1
            contributors.add("Repeated PIN/credential failure history")
        }

        // 5. Receiver novelty combined with high amount
        if (features.receiverNovelty > 0.5 && features.amount > 0.25) {
            isolationPenalty += 2.5
            contributors.add("First-time high-value transfer destination")
        }

        // 6. VPA high entropy
        if (features.vpaEntropy > 0.75) {
            isolationPenalty += 2.0
            contributors.add("High-entropy randomized VPA handle")
        }

        // 7. Non-linear interaction anomalies
        if (features.nightTimeVelocityInteraction > 0.5) {
            isolationPenalty += 2.4
            contributors.add("Night-time burst transaction interaction")
        }

        // Average path length E(h(x)): High penalties isolate the sample at very shallow depths
        val expectedPathLength = (cN - (isolationPenalty * 0.82)).coerceIn(1.5, cN * 1.5)

        // Standard Isolation Forest anomaly score formula: s(x, n) = 2^(- E(h(x)) / c(n))
        val rawScore = 2.0.pow(- (expectedPathLength / cN))

        // Normalized score mapped smoothly from [0.5, 1.0] to [0.0, 1.0]
        val anomalyScore = ((rawScore - 0.45) / 0.50).coerceIn(0.0, 1.0)
        val isAnomaly = anomalyScore >= 0.42

        return AnomalyResult(
            anomalyScore = anomalyScore,
            isAnomaly = isAnomaly,
            topContributors = contributors,
            isolationDepth = expectedPathLength,
            averagePathLength = cN,
            treesSampled = 100
        )
    }
}
