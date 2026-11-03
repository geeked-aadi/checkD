package com.example.eventattendance.presentation.scanner.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.eventattendance.presentation.theme.MonochromeWhite

@Composable
fun ScannerOverlay(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "LaserTransition")
    val laserYPercent by infiniteTransition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "LaserScan"
    )

    Box(modifier = modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height

            // Square viewfinder frame in the center
            val boxSize = canvasWidth * 0.70f
            val left = (canvasWidth - boxSize) / 2f
            val top = (canvasHeight - boxSize) / 2.3f
            val cornerLength = boxSize * 0.18f
            val cornerRadius = 24.dp.toPx()
            val strokeWidth = 5.dp.toPx()

            // Draw semi-transparent dark background vignette
            drawRect(
                color = Color.Black.copy(alpha = 0.5f),
                size = size
            )

            // Punch out transparent cutout for viewfinder
            val cutoutPath = Path().apply {
                addRoundRect(
                    RoundRect(
                        left = left,
                        top = top,
                        right = left + boxSize,
                        bottom = top + boxSize,
                        cornerRadius = CornerRadius(cornerRadius, cornerRadius)
                    )
                )
            }
            drawPath(
                path = cutoutPath,
                color = Color.Transparent,
                blendMode = BlendMode.Clear
            )

            // Draw crisp white rounded corner brackets matching the reference
            val bracketColor = Color.White
            val stroke = Stroke(width = strokeWidth, cap = StrokeCap.Round)

            // Top-Left Corner
            drawPath(
                path = Path().apply {
                    moveTo(left, top + cornerLength)
                    lineTo(left, top + cornerRadius)
                    quadraticTo(left, top, left + cornerRadius, top)
                    lineTo(left + cornerLength, top)
                },
                color = bracketColor,
                style = stroke
            )

            // Top-Right Corner
            drawPath(
                path = Path().apply {
                    moveTo(left + boxSize - cornerLength, top)
                    lineTo(left + boxSize - cornerRadius, top)
                    quadraticTo(left + boxSize, top, left + boxSize, top + cornerRadius)
                    lineTo(left + boxSize, top + cornerLength)
                },
                color = bracketColor,
                style = stroke
            )

            // Bottom-Left Corner
            drawPath(
                path = Path().apply {
                    moveTo(left, top + boxSize - cornerLength)
                    lineTo(left, top + boxSize - cornerRadius)
                    quadraticTo(left, top + boxSize, left + cornerRadius, top + boxSize)
                    lineTo(left + cornerLength, top + boxSize)
                },
                color = bracketColor,
                style = stroke
            )

            // Bottom-Right Corner
            drawPath(
                path = Path().apply {
                    moveTo(left + boxSize - cornerLength, top + boxSize)
                    lineTo(left + boxSize - cornerRadius, top + boxSize)
                    quadraticTo(left + boxSize, top + boxSize, left + boxSize, top + boxSize - cornerRadius)
                    lineTo(left + boxSize, top + boxSize - cornerLength)
                },
                color = bracketColor,
                style = stroke
            )

            // Draw animated scan line
            val currentLaserY = top + (boxSize * laserYPercent)
            val laserBrush = Brush.horizontalGradient(
                colors = listOf(
                    Color.Transparent,
                    Color.White.copy(alpha = 0.9f),
                    Color.White,
                    Color.White.copy(alpha = 0.9f),
                    Color.Transparent
                ),
                startX = left + 10f,
                endX = left + boxSize - 10f
            )

            drawLine(
                brush = laserBrush,
                start = Offset(left + 14f, currentLaserY),
                end = Offset(left + boxSize - 14f, currentLaserY),
                strokeWidth = 3.5.dp.toPx(),
                cap = StrokeCap.Round
            )
        }

        // Instruction Text below the viewfinder
        Text(
            text = "Scan participant QR code",
            color = MonochromeWhite,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier
                .align(Alignment.Center)
                .padding(top = 260.dp)
        )
    }
}
