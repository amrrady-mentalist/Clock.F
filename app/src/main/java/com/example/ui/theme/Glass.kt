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

// Apple Optical & Thick Slab Glass Palette
val GlassBase = Color(0x28FFFFFF)
val GlassSurface = Color(0x14FFFFFF)
val GlassSurfaceElevated = Color(0x22FFFFFF)
val GlassSurfaceDark = Color(0xD9101015)       // Deep optical smoky glass
val GlassSurfacePill = Color(0xE614141C)       // High-density floating capsule glass
val GlassSurfaceUltraDark = Color(0xF209090D)

val GlassBorderLight = Color(0x59FFFFFF)
val GlassBorderMuted = Color(0x1FFFFFFF)
val GlassBorderSubtle = Color(0x0FFFFFFF)

/**
 * Specular edge gradient for Apple-style thick slab glass.
 * Simulates directional light hitting the top-left chamfer/bevel edge with high refraction,
 * and diffusing toward the shadowed bottom-right edge.
 */
fun glassBorderBrush(
    startAlpha: Float = 0.55f,
    midAlpha: Float = 0.18f,
    endAlpha: Float = 0.05f
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
 * Creates a thick slab glass look with simulated refraction, specular highlight top rim,
 * and ambient edge depth.
 */
fun Modifier.appleThickGlass(
    shape: Shape = RoundedCornerShape(24.dp),
    backgroundColor: Color = GlassSurfaceDark,
    borderWidth: Dp = 1.2.dp,
    borderBrush: Brush = glassBorderBrush(0.55f, 0.16f, 0.06f),
    highlightAlpha: Float = 0.35f
): Modifier = this
    .clip(shape)
    .background(
        brush = Brush.linearGradient(
            colors = listOf(
                backgroundColor.copy(alpha = (backgroundColor.alpha * 1.15f).coerceAtMost(0.98f)),
                backgroundColor,
                backgroundColor.copy(alpha = (backgroundColor.alpha * 0.88f).coerceAtLeast(0.1f))
            ),
            start = Offset(0f, 0f),
            end = Offset(0f, Float.POSITIVE_INFINITY)
        )
    )
    .drawBehind {
        // Specular Top Rim Refraction Glint (Apple Slab Glass light dispersion)
        if (highlightAlpha > 0f) {
            val highlightHeight = 1.5.dp.toPx()
            drawRect(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.05f),
                        Color.White.copy(alpha = highlightAlpha),
                        Color.White.copy(alpha = highlightAlpha * 0.8f),
                        Color.White.copy(alpha = 0.05f)
                    )
                ),
                topLeft = Offset(0f, 0f),
                size = Size(size.width, highlightHeight)
            )
        }
    }
    .border(width = borderWidth, brush = borderBrush, shape = shape)

// Backward compatible liquidGlass modifier using updated thick optical glass system
fun Modifier.liquidGlass(
    shape: Shape = RoundedCornerShape(24.dp),
    backgroundColor: Color = GlassSurfaceDark,
    borderWidth: Dp = 1.dp,
    borderBrush: Brush = glassBorderBrush(0.50f, 0.15f, 0.05f)
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

