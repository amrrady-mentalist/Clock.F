package com.example.ui.components

import android.content.Context
import android.os.Build
import android.os.VibrationAttributes
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.DecayAnimationSpec
import androidx.compose.animation.core.calculateTargetValue
import androidx.compose.animation.core.exponentialDecay
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.ScrollScope
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.sign

private fun getVibrator(context: Context): Vibrator? {
    return try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    } catch (_: Exception) {
        null
    }
}

/**
 * Trigger an ultra-subtle, crisp tactile tick vibration via system haptics and hardware vibrator
 */
fun performTick(context: Context, haptic: HapticFeedback?) {
    try {
        haptic?.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        val vibrator = getVibrator(context)
        if (vibrator != null && vibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                // Use subtle composition primitive tick for pristine mechanical wheel feel
                try {
                    val composition = VibrationEffect.startComposition()
                        .addPrimitive(VibrationEffect.Composition.PRIMITIVE_TICK, 0.45f)
                        .compose()
                    val attributes = VibrationAttributes.Builder()
                        .setUsage(VibrationAttributes.USAGE_TOUCH)
                        .build()
                    vibrator.vibrate(composition, attributes)
                } catch (_: Exception) {
                    val effect = VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK)
                    vibrator.vibrate(effect)
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val effect = VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK)
                vibrator.vibrate(effect)
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val effect = VibrationEffect.createOneShot(8, 120)
                vibrator.vibrate(effect)
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(8)
            }
        }
    } catch (_: Exception) {}
}

fun performLockClick(context: Context, haptic: HapticFeedback?) {
    try {
        haptic?.performHapticFeedback(HapticFeedbackType.LongPress)
        val vibrator = getVibrator(context)
        if (vibrator != null && vibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                try {
                    val composition = VibrationEffect.startComposition()
                        .addPrimitive(VibrationEffect.Composition.PRIMITIVE_CLICK, 0.8f)
                        .compose()
                    val attributes = VibrationAttributes.Builder()
                        .setUsage(VibrationAttributes.USAGE_TOUCH)
                        .build()
                    vibrator.vibrate(composition, attributes)
                } catch (_: Exception) {
                    val effect = VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK)
                    vibrator.vibrate(effect)
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val effect = VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK)
                vibrator.vibrate(effect)
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val effect = VibrationEffect.createOneShot(20, 200)
                vibrator.vibrate(effect)
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(18)
            }
        }
    } catch (_: Exception) {}
}

/**
 * Custom physics-based fling behavior that smoothly spins, decelerates,
 * and gracefully routes inertia onto the forced target index with tactile feel.
 */
class ForcedPhysicsFlingBehavior(
    private val listState: LazyListState,
    private val itemHeightPx: Float,
    private val itemsCount: Int,
    private val isForceEnabled: Boolean,
    private val forcedTargetIndex: Int?,
    private val context: Context,
    private val haptic: HapticFeedback?,
    private val visibleItemsCount: Int = 5
) : FlingBehavior {

    private val decaySpec: DecayAnimationSpec<Float> = exponentialDecay(
        frictionMultiplier = 0.85f,
        absVelocityThreshold = 0.1f
    )

    override suspend fun ScrollScope.performFling(initialVelocity: Float): Float {
        val naturalProjectedDelta = decaySpec.calculateTargetValue(0f, initialVelocity)
        val centerOffset = visibleItemsCount / 2
        val currentCenterIndex = listState.firstVisibleItemIndex + centerOffset
        val currentExactCenter = currentCenterIndex + (listState.firstVisibleItemScrollOffset / itemHeightPx)

        var remainingVelocity = 0f

        if (isForceEnabled && forcedTargetIndex != null && forcedTargetIndex in 0 until itemsCount) {
            val direction = if (abs(initialVelocity) > 100f) sign(initialVelocity).toInt() else 1
            val estimatedItemsSpan = (abs(naturalProjectedDelta) / itemHeightPx).roundToInt().coerceAtLeast(3)
            val minExtraSpins = if (abs(initialVelocity) > 800f) (itemsCount * 2) else itemsCount
            val projectedVirtualTargetIndex = (currentCenterIndex + (direction * (estimatedItemsSpan + minExtraSpins)))

            val targetRemainder = forcedTargetIndex % itemsCount
            var finalTargetCenterIndex = projectedVirtualTargetIndex - (projectedVirtualTargetIndex % itemsCount) + targetRemainder
            if (direction > 0 && finalTargetCenterIndex <= currentCenterIndex) {
                finalTargetCenterIndex += itemsCount
            } else if (direction < 0 && finalTargetCenterIndex >= currentCenterIndex) {
                finalTargetCenterIndex -= itemsCount
            }

            val targetScrollIndex = (finalTargetCenterIndex - centerOffset).coerceAtLeast(0)
            val totalDistanceInItems = abs(finalTargetCenterIndex - currentCenterIndex)
            val animDurationMs = (600 + (totalDistanceInItems * 35)).coerceIn(800, 2200)

            val anim = Animatable(0f)
            var lastValue = 0f
            val physicsEasing = CubicBezierEasing(0.12f, 0.8f, 0.22f, 1.0f)
            val totalPixelDelta = (finalTargetCenterIndex - currentExactCenter) * itemHeightPx

            anim.animateTo(
                targetValue = totalPixelDelta,
                animationSpec = tween(
                    durationMillis = animDurationMs,
                    easing = physicsEasing
                )
            ) {
                val delta = value - lastValue
                scrollBy(delta)
                lastValue = value
            }

            listState.animateScrollToItem(
                index = targetScrollIndex,
                scrollOffset = 0
            )
            // Final lock tactile pulse
            performLockClick(context, haptic)
        } else {
            val naturalTargetDistance = naturalProjectedDelta
            val targetCenter = (currentExactCenter + (naturalTargetDistance / itemHeightPx)).roundToInt()
            val finalTargetFirst = (targetCenter - centerOffset).coerceAtLeast(0)

            val anim = Animatable(0f)
            var lastValue = 0f
            val totalDelta = (targetCenter - currentExactCenter) * itemHeightPx
            val animDuration = (400 + (abs(totalDelta) / itemHeightPx * 25).toInt()).coerceIn(300, 1200)

            anim.animateTo(
                targetValue = totalDelta,
                animationSpec = tween(
                    durationMillis = animDuration,
                    easing = CubicBezierEasing(0.18f, 0.7f, 0.25f, 1.0f)
                )
            ) {
                val delta = value - lastValue
                scrollBy(delta)
                lastValue = value
            }

            listState.animateScrollToItem(
                index = finalTargetFirst,
                scrollOffset = 0
            )
            performTick(context, haptic)
        }

        return remainingVelocity
    }
}

/**
 * 3D cylindrical wheel picker with physics inertia, continuous haptic ticking, and secret force deceleration.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WheelPicker(
    items: List<String>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    visibleItemsCount: Int = 5,
    itemHeight: Dp = 48.dp,
    width: Dp = 76.dp,
    isForceEnabled: Boolean = false,
    forcedTargetIndex: Int? = null,
    forceMode: String = "MAGNETIC",
    testTag: String = "wheel_picker"
) {
    val density = LocalDensity.current
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val itemHeightPx = with(density) { itemHeight.toPx() }

    val repeatCount = 200
    val totalCount = items.size * repeatCount
    val middleOffset = (repeatCount / 2) * items.size

    val initialIndex = remember {
        middleOffset + (selectedIndex.coerceIn(0, items.size - 1))
    }

    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = (initialIndex - (visibleItemsCount / 2)).coerceAtLeast(0)
    )

    val physicsFlingBehavior = remember(isForceEnabled, forcedTargetIndex, items.size, itemHeightPx) {
        ForcedPhysicsFlingBehavior(
            listState = listState,
            itemHeightPx = itemHeightPx,
            itemsCount = items.size,
            isForceEnabled = isForceEnabled,
            forcedTargetIndex = forcedTargetIndex,
            context = context,
            haptic = haptic,
            visibleItemsCount = visibleItemsCount
        )
    }

    val centerItemIndex by remember {
        derivedStateOf {
            val firstVisible = listState.firstVisibleItemIndex
            val offset = listState.firstVisibleItemScrollOffset
            val extra = if (offset > itemHeightPx / 2) 1 else 0
            (firstVisible + (visibleItemsCount / 2) + extra) % items.size
        }
    }

    // High precision subtle ticking: monitors scroll position and fires subtle haptic tick on each item step
    LaunchedEffect(listState, itemHeightPx) {
        var previousStep: Int? = null
        snapshotFlow {
            val totalScrolledPx = (listState.firstVisibleItemIndex * itemHeightPx) + listState.firstVisibleItemScrollOffset
            (totalScrolledPx / itemHeightPx).toInt()
        }
            .distinctUntilChanged()
            .collect { step ->
                if (previousStep != null && listState.isScrollInProgress) {
                    performTick(context, haptic)
                }
                previousStep = step
            }
    }

    LaunchedEffect(listState.isScrollInProgress) {
        if (!listState.isScrollInProgress) {
            val actualIndex = (listState.firstVisibleItemIndex + (visibleItemsCount / 2)) % items.size
            if (actualIndex != selectedIndex) {
                onItemSelected(actualIndex)
            }
        }
    }

    Box(
        modifier = modifier
            .width(width)
            .height(itemHeight * visibleItemsCount)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        // Selection highlight bar using theme primary color
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(itemHeight)
                .background(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(12.dp)
                )
        )

        LazyColumn(
            state = listState,
            flingBehavior = physicsFlingBehavior,
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(0.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items(totalCount) { index ->
                val realIndex = index % items.size
                val itemText = items[realIndex]
                val isSelected = realIndex == centerItemIndex

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(itemHeight),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = itemText,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else TextSecondary.copy(alpha = 0.65f),
                        fontSize = if (isSelected) 26.sp else 20.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        fontFamily = FontFamily.Monospace,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .graphicsLayer {
                                val centerOffset = (visibleItemsCount / 2)
                                val currentFirst = listState.firstVisibleItemIndex
                                val distanceFromCenter = abs(index - (currentFirst + centerOffset))
                                alpha = when (distanceFromCenter) {
                                    0 -> 1f
                                    1 -> 0.72f
                                    2 -> 0.38f
                                    else -> 0.15f
                                }
                                scaleX = when (distanceFromCenter) {
                                    0 -> 1.1f
                                    1 -> 0.94f
                                    else -> 0.82f
                                }
                                scaleY = scaleX
                                rotationX = (distanceFromCenter * 16f) * if (index < currentFirst + centerOffset) 1f else -1f
                            }
                    )
                }
            }
        }

        // Depth perspective gradients
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(itemHeight * 1.5f)
                .align(Alignment.TopCenter)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.surface.copy(alpha = 0f)
                        )
                    )
                )
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(itemHeight * 1.5f)
                .align(Alignment.BottomCenter)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surface.copy(alpha = 0f),
                            MaterialTheme.colorScheme.surface
                        )
                    )
                )
        )
    }
}
