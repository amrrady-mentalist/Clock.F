package com.example.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// Apple Liquid Glass Design Colors
val GlassBase = Color(0x28FFFFFF)
val GlassSurface = Color(0x18FFFFFF)
val GlassSurfaceElevated = Color(0x26FFFFFF)
val GlassSurfaceDark = Color(0xCC0D0D12)
val GlassSurfaceUltraDark = Color(0xF207070A)

val GlassBorderLight = Color(0x4DFFFFFF)
val GlassBorderMuted = Color(0x1AFFFFFF)
val GlassBorderSubtle = Color(0x12FFFFFF)

// Specular edge gradient for glossy liquid glass look
fun glassBorderBrush(
    startAlpha: Float = 0.35f,
    midAlpha: Float = 0.12f,
    endAlpha: Float = 0.04f
): Brush = Brush.linearGradient(
    colors = listOf(
        Color.White.copy(alpha = startAlpha),
        Color.White.copy(alpha = midAlpha),
        Color.White.copy(alpha = endAlpha)
    )
)

// Liquid Glass modifier
fun Modifier.liquidGlass(
    shape: Shape = RoundedCornerShape(24.dp),
    backgroundColor: Color = Color(0x1AFFFFFF),
    borderWidth: Dp = 1.dp,
    borderBrush: Brush = glassBorderBrush()
): Modifier = this
    .clip(shape)
    .background(
        brush = Brush.verticalGradient(
            colors = listOf(
                backgroundColor.copy(alpha = (backgroundColor.alpha * 1.3f).coerceAtMost(0.95f)),
                backgroundColor.copy(alpha = (backgroundColor.alpha * 0.7f).coerceAtLeast(0.05f))
            )
        )
    )
    .border(width = borderWidth, brush = borderBrush, shape = shape)

@Composable
fun LiquidGlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(24.dp),
    backgroundColor: Color = Color(0x1AFFFFFF),
    borderWidth: Dp = 1.dp,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .liquidGlass(
                shape = shape,
                backgroundColor = backgroundColor,
                borderWidth = borderWidth
            ),
        content = content
    )
}
