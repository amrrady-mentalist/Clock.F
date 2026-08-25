package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.RedAccent
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextTertiary
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun AnalogClockView(
    hour: Int,
    minute: Int,
    second: Float,
    modifier: Modifier = Modifier,
    size: Dp = 240.dp,
    showSeconds: Boolean = true,
    accentColor: Color = MaterialTheme.colorScheme.primary,
    testTag: String = "analog_clock"
) {
    Box(
        modifier = modifier
            .size(size)
            .aspectRatio(1f)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(12.dp)) {
            val center = Offset(this.size.width / 2, this.size.height / 2)
            val radius = this.size.width / 2

            // Liquid Glass Dial Background
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0x33FFFFFF),
                        DarkSurfaceVariant.copy(alpha = 0.75f),
                        DarkSurface.copy(alpha = 0.95f)
                    ),
                    center = center - Offset(0f, radius * 0.3f),
                    radius = radius * 1.3f
                ),
                radius = radius,
                center = center
            )

            // Outer Liquid Glass Specular Bezel Rim
            drawCircle(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.45f),
                        Color.White.copy(alpha = 0.12f),
                        Color.White.copy(alpha = 0.04f)
                    ),
                    startY = center.y - radius,
                    endY = center.y + radius
                ),
                radius = radius - 1.dp.toPx(),
                center = center,
                style = Stroke(width = 1.5.dp.toPx())
            )

            // Hour tick marks & markers
            for (i in 0 until 60) {
                val isHour = i % 5 == 0
                val angleInRad = (i * 6f) * (PI.toFloat() / 180f)
                val lineLength = if (isHour) 14.dp.toPx() else 6.dp.toPx()
                val strokeWidth = if (isHour) 3.dp.toPx() else 1.2.dp.toPx()
                val strokeColor = if (isHour) accentColor.copy(alpha = 0.85f) else TextTertiary.copy(alpha = 0.4f)

                val startX = center.x + (radius - 12.dp.toPx() - lineLength) * cos(angleInRad)
                val startY = center.y + (radius - 12.dp.toPx() - lineLength) * sin(angleInRad)
                val endX = center.x + (radius - 12.dp.toPx()) * cos(angleInRad)
                val endY = center.y + (radius - 12.dp.toPx()) * sin(angleInRad)

                drawLine(
                    color = strokeColor,
                    start = Offset(startX, startY),
                    end = Offset(endX, endY),
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Round
                )
            }

            // 12, 3, 6, 9 numeral indicators in Roboto font
            val textPaint = android.graphics.Paint().apply {
                color = android.graphics.Color.WHITE
                textSize = 13.5.dp.toPx()
                textAlign = android.graphics.Paint.Align.CENTER
                typeface = android.graphics.Typeface.create("sans-serif", android.graphics.Typeface.NORMAL)
                isAntiAlias = true
            }

            val numerals = listOf("12" to 270f, "3" to 0f, "6" to 90f, "9" to 180f)
            for ((num, deg) in numerals) {
                val rad = deg * (PI.toFloat() / 180f)
                val textRadius = radius - 36.dp.toPx()
                val x = center.x + textRadius * cos(rad)
                val y = center.y + textRadius * sin(rad) + 5.dp.toPx()
                drawContext.canvas.nativeCanvas.drawText(num, x, y, textPaint)
            }

            // Hour Hand
            val hourAngle = (hour % 12 + minute / 60f + second / 3600f) * 30f
            rotate(degrees = hourAngle, pivot = center) {
                drawLine(
                    color = TextPrimary,
                    start = center - Offset(0f, -12.dp.toPx()),
                    end = center - Offset(0f, radius * 0.52f),
                    strokeWidth = 6.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }

            // Minute Hand
            val minuteAngle = (minute + second / 60f) * 6f
            rotate(degrees = minuteAngle, pivot = center) {
                drawLine(
                    color = accentColor,
                    start = center - Offset(0f, -16.dp.toPx()),
                    end = center - Offset(0f, radius * 0.74f),
                    strokeWidth = 4.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }

            // Second Hand (Clean minimal white)
            if (showSeconds) {
                val secondAngle = second * 6f
                rotate(degrees = secondAngle, pivot = center) {
                    drawLine(
                        color = Color.White,
                        start = center - Offset(0f, -22.dp.toPx()),
                        end = center - Offset(0f, radius * 0.85f),
                        strokeWidth = 1.8.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                    // Small counter-balance tail circle
                    drawCircle(
                        color = Color.White,
                        radius = 3.5.dp.toPx(),
                        center = center - Offset(0f, -20.dp.toPx())
                    )
                }
            }

            // Center Pin / Hub
            drawCircle(
                color = Color.White,
                radius = 4.5.dp.toPx(),
                center = center
            )
            drawCircle(
                color = Color.Black,
                radius = 2.dp.toPx(),
                center = center
            )
        }
    }
}
