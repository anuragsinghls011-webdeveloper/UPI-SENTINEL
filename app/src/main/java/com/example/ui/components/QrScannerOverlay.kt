package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BrandBlue
import com.example.ui.theme.BrandNavy
import com.example.ui.theme.RiskLow

@Composable
fun QrScannerOverlay(
    isTorchEnabled: Boolean,
    onToggleTorch: () -> Unit,
    onSwitchCamera: () -> Unit,
    onOpenPresets: () -> Unit,
    onManualInput: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "scannerLaser")
    val laserProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laserPosition"
    )

    val boxSizeDp = 260.dp

    Box(
        modifier = modifier
            .fillMaxSize()
            .graphicsLayer { alpha = 0.99f }
    ) {
        // Dark vignette overlay with cutout for scanning window
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val boxSizePx = boxSizeDp.toPx()
            val left = (canvasWidth - boxSizePx) / 2f
            val top = (canvasHeight - boxSizePx) / 2f - 40.dp.toPx()

            // Outer dark mask
            drawRect(
                color = Color.Black.copy(alpha = 0.65f),
                size = size
            )

            // Transparent cutout window for viewfinder
            drawRoundRect(
                color = Color.Transparent,
                topLeft = Offset(left, top),
                size = Size(boxSizePx, boxSizePx),
                cornerRadius = CornerRadius(16.dp.toPx(), 16.dp.toPx()),
                blendMode = BlendMode.Clear
            )

            // Viewfinder Corner Brackets
            val cornerLength = 32.dp.toPx()
            val strokeWidth = 4.dp.toPx()
            val bracketColor = Color(0xFF38BDF8) // Bright Cyan Blue

            // Top-Left Corner
            val tlPath = Path().apply {
                moveTo(left, top + cornerLength)
                lineTo(left, top + 16.dp.toPx())
                quadraticTo(left, top, left + 16.dp.toPx(), top)
                lineTo(left + cornerLength, top)
            }
            drawPath(tlPath, bracketColor, style = Stroke(strokeWidth, cap = StrokeCap.Round))

            // Top-Right Corner
            val trPath = Path().apply {
                moveTo(left + boxSizePx - cornerLength, top)
                lineTo(left + boxSizePx - 16.dp.toPx(), top)
                quadraticTo(left + boxSizePx, top, left + boxSizePx, top + 16.dp.toPx())
                lineTo(left + boxSizePx, top + cornerLength)
            }
            drawPath(trPath, bracketColor, style = Stroke(strokeWidth, cap = StrokeCap.Round))

            // Bottom-Left Corner
            val blPath = Path().apply {
                moveTo(left, top + boxSizePx - cornerLength)
                lineTo(left, top + boxSizePx - 16.dp.toPx())
                quadraticTo(left, top + boxSizePx, left + 16.dp.toPx(), top + boxSizePx)
                lineTo(left + cornerLength, top + boxSizePx)
            }
            drawPath(blPath, bracketColor, style = Stroke(strokeWidth, cap = StrokeCap.Round))

            // Bottom-Right Corner
            val brPath = Path().apply {
                moveTo(left + boxSizePx - cornerLength, top + boxSizePx)
                lineTo(left + boxSizePx - 16.dp.toPx(), top + boxSizePx)
                quadraticTo(left + boxSizePx, top + boxSizePx, left + boxSizePx, top + boxSizePx - 16.dp.toPx())
                lineTo(left + boxSizePx, top + boxSizePx - cornerLength)
            }
            drawPath(brPath, bracketColor, style = Stroke(strokeWidth, cap = StrokeCap.Round))

            // Animated Laser Scanning Line
            val laserY = top + (boxSizePx * laserProgress)
            drawLine(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color(0xFF38BDF8).copy(alpha = 0.9f),
                        Color.White,
                        Color(0xFF38BDF8).copy(alpha = 0.9f),
                        Color.Transparent
                    )
                ),
                start = Offset(left + 8.dp.toPx(), laserY),
                end = Offset(left + boxSizePx - 8.dp.toPx(), laserY),
                strokeWidth = 3.dp.toPx()
            )
        }

        // Top Header Info
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 28.dp, start = 20.dp, end = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.Black.copy(alpha = 0.6f),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(RiskLow)
                    )
                    Text(
                        text = "CameraX Computer Vision Active",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Point camera at any UPI QR code",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.85f)
            )
        }

        // Viewfinder center label
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(top = 220.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Align UPI QR code inside square",
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                color = Color.White.copy(alpha = 0.7f)
            )
        }

        // Bottom Controls Bar
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            color = Color.Black.copy(alpha = 0.75f),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.15f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Torch toggle
                IconButton(
                    onClick = onToggleTorch,
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = if (isTorchEnabled) Color(0xFFF59E0B) else Color.White.copy(alpha = 0.12f)
                    )
                ) {
                    Icon(
                        imageVector = if (isTorchEnabled) Icons.Default.FlashOn else Icons.Default.FlashOff,
                        contentDescription = "Toggle Torch",
                        tint = if (isTorchEnabled) Color.Black else Color.White
                    )
                }

                // Switch camera (front/back)
                IconButton(
                    onClick = onSwitchCamera,
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = Color.White.copy(alpha = 0.12f)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Cameraswitch,
                        contentDescription = "Switch Camera",
                        tint = Color.White
                    )
                }

                // Sample UPI QR Presets (vital for simulator & quick testing)
                FilledTonalIconButton(
                    onClick = onOpenPresets,
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = BrandBlue,
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCode,
                        contentDescription = "Sample QR Presets"
                    )
                }

                // Manual Input option
                IconButton(
                    onClick = onManualInput,
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = Color.White.copy(alpha = 0.12f)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Keyboard,
                        contentDescription = "Manual UPI Input",
                        tint = Color.White
                    )
                }
            }
        }
    }
}
