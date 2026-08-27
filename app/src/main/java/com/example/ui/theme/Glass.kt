package com.example.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// Apple Optical & Frosted Slab Glass Palette
val GlassBase = Color(0x28FFFFFF)
val GlassSurface = Color(0x14FFFFFF)
val GlassSurfaceElevated = Color(0x22FFFFFF)
val GlassSurfaceDark = Color(0xC7131722)       // Frosted optical smoky glass (~78% opacity)
val GlassSurfacePill = Color(0xC7131722)       // Frosted slab capsule glass matching bottom bar
val GlassSurfaceChip = Color(0xC7131722)       // Frosted button & chip glass
val GlassSurfaceUltraDark = Color(0xD90A0D14)

val GlassBorderLight = Color(0x66FFFFFF)
val GlassBorderMuted = Color(0x24FFFFFF)
val GlassBorderSubtle = Color(0x12FFFFFF)

/**
 * Specular edge gradient for Apple-style liquid slab glass.
 * Directional subtle light reflection across the perimeter.
 */
fun glassBorderBrush(
    startAlpha: Float = 0.60f,
    midAlpha: Float = 0.20f,
    endAlpha: Float = 0.08f
): Brush = Brush.linearGradient(
    colors = listOf(
        Color.White.copy(alpha = startAlpha),
        Color.White.copy(alpha = midAlpha),
        Color.White.copy(alpha = endAlpha)
    ),
    start = Offset(0f, 0f),
    end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
)

/**
 * Apple-style liquid glass modifier matching the clean bottom navigation bar.
 * Uses a uniform frosted glass background with a directional specular border.
 */
fun Modifier.appleThickGlass(
    shape: Shape = RoundedCornerShape(24.dp),
    backgroundColor: Color = GlassSurfacePill,
    borderWidth: Dp = 1.2.dp,
    borderBrush: Brush = glassBorderBrush(0.60f, 0.20f, 0.08f),
    highlightAlpha: Float = 0f
): Modifier = this
    .clip(shape)
    .background(color = backgroundColor, shape = shape)
    .border(width = borderWidth, brush = borderBrush, shape = shape)

// Liquid glass modifier alias
fun Modifier.liquidGlass(
    shape: Shape = RoundedCornerShape(24.dp),
    backgroundColor: Color = GlassSurfacePill,
    borderWidth: Dp = 1.2.dp,
    borderBrush: Brush = glassBorderBrush(0.60f, 0.20f, 0.08f)
): Modifier = this.appleThickGlass(
    shape = shape,
    backgroundColor = backgroundColor,
    borderWidth = borderWidth,
    borderBrush = borderBrush
)

@Composable
fun LiquidGlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(24.dp),
    backgroundColor: Color = GlassSurfaceDark,
    borderWidth: Dp = 1.2.dp,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .appleThickGlass(
                shape = shape,
                backgroundColor = backgroundColor,
                borderWidth = borderWidth
            ),
        content = content
    )
}

