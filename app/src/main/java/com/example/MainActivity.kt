package com.example

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.HourglassBottom
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.screens.AlarmScreen
import com.example.ui.screens.ClockScreen
import com.example.ui.screens.SecretPinPromptDialog
import com.example.ui.screens.SecretSettingsDialog
import com.example.ui.screens.StopwatchScreen
import com.example.ui.screens.TimerScreen
import com.example.ui.theme.AccentTheme
import com.example.ui.theme.ClockTheme
import com.example.ui.theme.DarkBg
import com.example.ui.theme.DarkBgGradientBottom
import com.example.ui.theme.DarkBgGradientTop
import com.example.ui.theme.GlassSurfaceDark
import com.example.ui.theme.GlassSurfaceElevated
import com.example.ui.theme.GlassSurfacePill
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.ui.theme.appleThickGlass
import com.example.ui.theme.glassBorderBrush
import com.example.ui.theme.liquidGlass
import com.example.ui.viewmodel.ClockViewModel

data class NavTab(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

class MainActivity : ComponentActivity(), SensorEventListener {
    internal val viewModel: ClockViewModel by viewModels()
    private var sensorManager: SensorManager? = null
    private var proximitySensor: Sensor? = null
    private var isNear = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize Proximity Sensor for magic wave trigger
        try {
            sensorManager = getSystemService(Context.SENSOR_SERVICE) as? SensorManager
            proximitySensor = sensorManager?.getDefaultSensor(Sensor.TYPE_PROXIMITY)
        } catch (_: Exception) {}

        setContent {
            val secretConfig by viewModel.secretConfig.collectAsState()
            val accentTheme = remember(secretConfig?.accentColorTheme) {
                secretConfig?.accentColorTheme?.let { name ->
                    try {
                        AccentTheme.valueOf(name)
                    } catch (_: Exception) {
                        AccentTheme.WHITE
                    }
                } ?: AccentTheme.WHITE
            }

            ClockTheme(accent = accentTheme) {
                MainClockApp(viewModel = viewModel)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        proximitySensor?.let { sensor ->
            sensorManager?.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager?.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null || event.sensor.type != Sensor.TYPE_PROXIMITY) return
        val distance = event.values.firstOrNull() ?: return
        val maxRange = event.sensor.maximumRange
        val near = if (maxRange <= 1.0f) {
            distance < 0.5f
        } else {
            distance < minOf(maxRange, 4.0f) && distance < maxRange
        }
        
        // Detect state transition from far to near (hand wave over proximity sensor)
        if (near && !isNear) {
            viewModel.onProximityWaveDetected()
        }
        isNear = near
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    /**
     * Intercept volume button presses without changing volume slider
     */
    private fun isVolumeTriggerConfigured(): Boolean {
        val secretConfig = viewModel.secretConfig.value ?: return false
        val isAlarmVolume = secretConfig.alarmForceTriggerType in listOf("VOLUME_BUTTON", "WAVE_OR_VOLUME")
        val isStopwatchVolume = secretConfig.stopwatchForceTriggerType in listOf("VOLUME_BUTTON", "WAVE_OR_VOLUME")
        return isAlarmVolume || isStopwatchVolume
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (event.keyCode == KeyEvent.KEYCODE_VOLUME_UP || event.keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            if (isVolumeTriggerConfigured()) {
                if (event.action == KeyEvent.ACTION_DOWN && event.repeatCount == 0) {
                    viewModel.onVolumeButtonTriggered()
                }
                // Return true to prevent system volume HUD from popping up or altering volume
                return true
            }
        }
        return super.dispatchKeyEvent(event)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            if (isVolumeTriggerConfigured()) {
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            if (isVolumeTriggerConfigured()) {
                return true
            }
        }
        return super.onKeyUp(keyCode, event)
    }
}

@Composable
fun MainClockApp(viewModel: ClockViewModel) {
    val selectedTab by viewModel.selectedTab.collectAsState()
    val showSecretSettings by viewModel.showSecretSettings.collectAsState()
    val showPinPrompt by viewModel.showPinPrompt.collectAsState()
    val secretConfig by viewModel.secretConfig.collectAsState()
    val isAlarmForceArmed by viewModel.isAlarmForceArmed.collectAsState()
    val isStopwatchForceArmed by viewModel.isStopwatchForceArmed.collectAsState()

    var dotsTapCount by remember { mutableIntStateOf(0) }
    var lastDotsTapTime by remember { mutableLongStateOf(0L) }

    val primaryColor = MaterialTheme.colorScheme.primary

    val tabs = listOf(
        NavTab("Clock", Icons.Filled.Schedule, Icons.Outlined.Schedule),
        NavTab("Alarm", Icons.Filled.Alarm, Icons.Outlined.Alarm),
        NavTab("Stopwatch", Icons.Filled.Timer, Icons.Outlined.Timer),
        NavTab("Timer", Icons.Filled.HourglassBottom, Icons.Outlined.HourglassBottom)
    )

    // Pure Pitch Black OLED Canvas with Crisp Optical Glass Elements
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Optical Frosted Glass Top Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onLongPress = { viewModel.onSecretTriggerAttempt() },
                                    onDoubleTap = { viewModel.onSecretTriggerAttempt() }
                                )
                            }
                            .padding(vertical = 4.dp, horizontal = 6.dp)
                            .testTag("app_top_header")
                    ) {
                        Text(
                            text = tabs[selectedTab].title,
                            color = TextPrimary,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = (-0.5).sp
                        )

                        // If force is active/armed, stealth indicator
                        val isForceActiveOnScreen = when (selectedTab) {
                            1 -> secretConfig?.isForceEnabled == true || isAlarmForceArmed
                            2 -> secretConfig?.isStopwatchForceEnabled == true || isStopwatchForceArmed
                            else -> (secretConfig?.isForceEnabled == true) || (secretConfig?.isStopwatchForceEnabled == true) || isAlarmForceArmed || isStopwatchForceArmed
                        }

                        if (isForceActiveOnScreen) {
                            Spacer(modifier = Modifier.width(8.dp))
                            val isArmed = when (selectedTab) {
                                1 -> if (secretConfig?.alarmForceTriggerType == "ALWAYS") secretConfig?.isForceEnabled == true else isAlarmForceArmed
                                2 -> if (secretConfig?.stopwatchForceTriggerType == "ALWAYS") secretConfig?.isStopwatchForceEnabled == true else isStopwatchForceArmed
                                else -> (if (secretConfig?.alarmForceTriggerType == "ALWAYS") secretConfig?.isForceEnabled == true else isAlarmForceArmed) ||
                                        (if (secretConfig?.stopwatchForceTriggerType == "ALWAYS") secretConfig?.isStopwatchForceEnabled == true else isStopwatchForceArmed)
                            }
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(
                                        color = if (isArmed) primaryColor.copy(alpha = 0.85f) else TextTertiary.copy(alpha = 0.35f),
                                        shape = CircleShape
                                    )
                            )
                        }
                    }

                    // Discreet stealth options button (Triple-tap to open Settings)
                    IconButton(
                        onClick = {
                            val now = System.currentTimeMillis()
                            if (now - lastDotsTapTime <= 800L) {
                                dotsTapCount += 1
                            } else {
                                dotsTapCount = 1
                            }
                            lastDotsTapTime = now

                            if (dotsTapCount >= 3) {
                                dotsTapCount = 0
                                viewModel.onSecretTriggerAttempt()
                            }
                        },
                        modifier = Modifier
                            .size(40.dp)
                            .appleThickGlass(
                                shape = CircleShape,
                                backgroundColor = Color(0x1AFFFFFF),
                                borderWidth = 1.dp,
                                borderBrush = glassBorderBrush(0.40f, 0.12f, 0.04f),
                                highlightAlpha = 0.25f
                            )
                            .testTag("secret_settings_entry_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Options",
                            tint = TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Main Screen Content Area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                AnimatedContent(
                    targetState = selectedTab,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "TabContent"
                ) { targetTab ->
                    when (targetTab) {
                        0 -> ClockScreen(viewModel = viewModel)
                        1 -> AlarmScreen(viewModel = viewModel)
                        2 -> StopwatchScreen(viewModel = viewModel)
                        3 -> TimerScreen(viewModel = viewModel)
                    }
                }
            }

            Spacer(modifier = Modifier.height(72.dp))
        }

        // Apple Thick Slab Glass Floating Capsule Bottom Navigation Bar
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(start = 20.dp, end = 20.dp, bottom = 12.dp)
                .fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 16.dp,
                        shape = RoundedCornerShape(36.dp),
                        spotColor = Color.Black.copy(alpha = 0.85f),
                        ambientColor = Color.Black.copy(alpha = 0.95f)
                    )
                    .appleThickGlass(
                        shape = RoundedCornerShape(36.dp),
                        backgroundColor = GlassSurfacePill,
                        borderWidth = 1.25.dp,
                        borderBrush = glassBorderBrush(0.65f, 0.22f, 0.08f),
                        highlightAlpha = 0.45f
                    )
                    .padding(horizontal = 8.dp, vertical = 7.dp)
                    .testTag("bottom_nav_bar")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    tabs.forEachIndexed { index, tab ->
                        val isSelected = selectedTab == index
                        val iconColor by animateColorAsState(
                            targetValue = if (isSelected) Color.White else TextTertiary,
                            label = "IconColor"
                        )
                        val textColor by animateColorAsState(
                            targetValue = if (isSelected) TextPrimary else TextTertiary,
                            label = "TextColor"
                        )

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(22.dp))
                                .then(
                                    if (isSelected) {
                                        Modifier.appleThickGlass(
                                            shape = RoundedCornerShape(22.dp),
                                            backgroundColor = Color(0x38FFFFFF),
                                            borderWidth = 1.dp,
                                            borderBrush = glassBorderBrush(0.55f, 0.18f, 0.06f),
                                            highlightAlpha = 0.30f
                                        )
                                    } else {
                                        Modifier
                                    }
                                )
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    viewModel.selectTab(index)
                                }
                                .padding(horizontal = 14.dp, vertical = 7.dp)
                                .testTag("nav_tab_${tab.title.lowercase()}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                    contentDescription = tab.title,
                                    tint = iconColor,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = tab.title,
                                    color = textColor,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Secret Hidden Force Settings Dialog
    if (showSecretSettings) {
        SecretSettingsDialog(
            viewModel = viewModel,
            onDismiss = { viewModel.dismissSecretSettings() }
        )
    }

    // Secret PIN Prompt Dialog
    if (showPinPrompt) {
        SecretPinPromptDialog(
            onDismiss = { viewModel.dismissPinPrompt() },
            onSubmitPin = { pin ->
                viewModel.verifySecretPin(pin)
            }
        )
    }
}
