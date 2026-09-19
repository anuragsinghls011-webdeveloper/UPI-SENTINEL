package com.example.qr

import android.graphics.ImageFormat
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.MultiFormatReader
import com.google.zxing.NotFoundException
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.common.HybridBinarizer
import java.nio.ByteBuffer

/**
 * CameraX ImageAnalysis.Analyzer that decodes 2D Barcodes (QR Codes)
 * in real-time from camera frames using ZXing.
 */
class CameraXQrAnalyzer(
    private val onQrDetected: (String) -> Unit
) : ImageAnalysis.Analyzer {

    private val reader = MultiFormatReader().apply {
        val hints = mapOf<DecodeHintType, Any>(
            DecodeHintType.POSSIBLE_FORMATS to listOf(BarcodeFormat.QR_CODE),
            DecodeHintType.TRY_HARDER to true,
            DecodeHintType.CHARACTER_SET to "UTF-8"
        )
        setHints(hints)
    }

    @Volatile
    private var isPaused: Boolean = false

    @Volatile
    private var lastScannedText: String? = null

    @Volatile
    private var lastScannedTimestamp: Long = 0L

    fun pauseScanning() {
        isPaused = true
    }

    fun resumeScanning() {
        isPaused = false
        lastScannedText = null
        lastScannedTimestamp = 0L
    }

    override fun analyze(image: ImageProxy) {
        if (isPaused) {
            image.close()
            return
        }

        val currentTime = System.currentTimeMillis()
        // Debounce if same QR detected in the last 1.5 seconds
        if (currentTime - lastScannedTimestamp < 1500L) {
            image.close()
            return
        }

        try {
            val format = image.format
            if (format == ImageFormat.YUV_420_888 || format == ImageFormat.YUV_422_888 || format == ImageFormat.YUV_444_888) {
                val plane = image.planes[0]
                val buffer = plane.buffer
                val bytes = buffer.toByteArray()
                val width = image.width
                val height = image.height

                val source = PlanarYUVLuminanceSource(
                    bytes,
                    width,
                    height,
                    0,
                    0,
                    width,
                    height,
                    false
                )

                val binaryBitmap = BinaryBitmap(HybridBinarizer(source))

                try {
                    val result = reader.decodeWithState(binaryBitmap)
                    val text = result.text
                    if (!text.isNullOrBlank()) {
                        lastScannedText = text
                        lastScannedTimestamp = currentTime
                        onQrDetected(text)
                    }
                } catch (_: NotFoundException) {
                    // Normal behavior when no barcode is in frame
                } catch (_: Exception) {
                    // Frame unreadable or corrupt
                } finally {
                    reader.reset()
                }
            }
        } catch (_: Exception) {
            // Error processing image frame
        } finally {
            image.close()
        }
    }

    private fun ByteBuffer.toByteArray(): ByteArray {
        rewind()
        val data = ByteArray(remaining())
        get(data)
        return data
    }
}
