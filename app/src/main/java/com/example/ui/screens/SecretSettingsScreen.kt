package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Password
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.SecretConfigEntity
import com.example.ui.components.WheelPicker
import com.example.ui.theme.AccentTheme
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.DarkBg
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.DividerColor
import com.example.ui.theme.RedAccent
import com.example.ui.theme.SecretAura
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.ui.viewmodel.ClockViewModel
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SecretSettingsDialog(
    viewModel: ClockViewModel,
    onDismiss: () -> Unit
) {
    val secretConfig by viewModel.secretConfig.collectAsState()

    var isForceEnabled by remember(secretConfig) {
        mutableStateOf(secretConfig?.isForceEnabled ?: false)
    }

    var forcedHour by remember(secretConfig) {
        mutableIntStateOf(secretConfig?.forcedHour ?: 7)
    }

    var forcedMinute by remember(secretConfig) {
        mutableIntStateOf(secretConfig?.forcedMinute ?: 30)
    }

    var forcedIsPm by remember(secretConfig) {
        mutableStateOf(secretConfig?.forcedIsPm ?: false)
    }

    var isStopwatchForceEnabled by remember(secretConfig) {
        mutableStateOf(secretConfig?.isStopwatchForceEnabled ?: false)
    }

    var forcedStopwatchCentiseconds by remember(secretConfig) {
        mutableIntStateOf(secretConfig?.forcedStopwatchCentiseconds ?: 37)
    }

    var stopwatchForceTriggerStopCount by remember(secretConfig) {
        mutableIntStateOf(secretConfig?.stopwatchForceTriggerStopCount ?: 1)
    }

    var selectedAccentName by remember(secretConfig) {
        mutableStateOf(secretConfig?.accentColorTheme ?: AccentTheme.WHITE.name)
    }

    var pinCode by remember(secretConfig) {
        mutableStateOf(secretConfig?.secretPin ?: "1234")
    }

    var isPinProtected by remember(secretConfig) {
        mutableStateOf(secretConfig?.isPinRequired ?: false)
    }

    var alarmForceTriggerType by remember(secretConfig) {
        mutableStateOf(secretConfig?.alarmForceTriggerType ?: "ALWAYS")
    }

    val hoursList = remember { (1..12).map { it.toString() } }
    val minutesList = remember { (0..59).map { String.format(Locale.US, "%02d", it) } }
    val amPmList = remember { listOf("AM", "PM") }
    val centisecondsList = remember { (0..99).map { String.format(Locale.US, "%02d", it) } }

    val hourIndex = hoursList.indexOf((if (forcedHour > 12) forcedHour - 12 else if (forcedHour == 0) 12 else forcedHour).toString()).coerceAtLeast(0)
    val minuteIndex = forcedMinute.coerceIn(0, 59)
    val amPmIndex = if (forcedIsPm) 1 else 0
    val centisecondIndex = forcedStopwatchCentiseconds.coerceIn(0, 99)

    val primaryColor = MaterialTheme.colorScheme.primary

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .background(Color.White.copy(alpha = 0.12f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Settings,
                        contentDescription = "Settings",
                        tint = TextPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Settings",
                        color = TextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Preferences & Options",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Section 1: Color Customization
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkBg),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Palette,
                                contentDescription = "Accent Theme",
                                tint = primaryColor,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "APP ACCENT COLOR",
                                color = TextTertiary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.2.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Grid / Flow of color palette choices
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            AccentTheme.entries.forEach { themeOption ->
                                val isSelected = selectedAccentName == themeOption.name
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(
                                            if (isSelected) themeOption.primary.copy(alpha = 0.22f)
                                            else DarkSurfaceVariant
                                        )
                                        .border(
                                            width = if (isSelected) 2.dp else 1.dp,
                                            color = if (isSelected) themeOption.primary else Color.Transparent,
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .clickable {
                                            selectedAccentName = themeOption.name
                                        }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                        .testTag("color_chip_${themeOption.name.lowercase()}"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(14.dp)
                                                .background(themeOption.primary, CircleShape)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = themeOption.title,
                                            color = if (isSelected) themeOption.primary else TextSecondary,
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                        if (isSelected) {
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Selected",
                                                tint = themeOption.primary,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Section 2: Master Force Toggle
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isForceEnabled) SecretAura.copy(alpha = 0.15f) else DarkBg
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Master Force Engine",
                                color = TextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Wheel picker scrolls naturally from current time, then decelerates to hit forced number",
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        }

                        Switch(
                            checked = isForceEnabled,
                            onCheckedChange = { isForceEnabled = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.Black,
                                checkedTrackColor = SecretAura
                            ),
                            modifier = Modifier.testTag("master_force_switch")
                        )
                    }
                }

                // Section 3: Force Target Picker
                AnimatedVisibility(visible = isForceEnabled) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = DarkBg),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "FORCED TARGET TIME",
                                color = TextTertiary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.2.sp
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // 3D Wheel Pickers for setting the forced number
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(DarkSurface)
                                    .padding(vertical = 10.dp, horizontal = 6.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                WheelPicker(
                                    items = hoursList,
                                    selectedIndex = hourIndex,
                                    onItemSelected = { forcedHour = hoursList[it].toInt() },
                                    width = 64.dp,
                                    testTag = "forced_hour_picker"
                                )

                                Text(":", color = SecretAura, fontSize = 28.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp))

                                WheelPicker(
                                    items = minutesList,
                                    selectedIndex = minuteIndex,
                                    onItemSelected = { forcedMinute = it },
                                    width = 64.dp,
                                    testTag = "forced_minute_picker"
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                WheelPicker(
                                    items = amPmList,
                                    selectedIndex = amPmIndex,
                                    onItemSelected = { forcedIsPm = it == 1 },
                                    width = 56.dp,
                                    testTag = "forced_ampm_picker"
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Target: ${if (forcedHour == 0) 12 else forcedHour}:${String.format(Locale.US, "%02d", forcedMinute)} ${if (forcedIsPm) "PM" else "AM"}",
                                color = SecretAura,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(16.dp))
                            HorizontalDivider(color = DividerColor.copy(alpha = 0.5f))
                            Spacer(modifier = Modifier.height(14.dp))

                            // ALARM SCROLL FORCE TRIGGERS (ALWAYS, PROXIMITY WAVE, VOLUME BUTTON)
                            Text(
                                text = "ALARM FORCE TRIGGER",
                                color = TextTertiary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.2.sp
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            val triggerOptions = listOf(
                                Triple("ALWAYS", "Always Active", "Continuous force on all scrolls"),
                                Triple("PROXIMITY_WAVE", "Proximity Wave", "Wave hand over top sensor to arm"),
                                Triple("VOLUME_BUTTON", "Volume Button", "Click Vol Up/Down to silently arm")
                            )

                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                triggerOptions.forEach { (typeKey, title, desc) ->
                                    val isSelected = alarmForceTriggerType == typeKey
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(if (isSelected) SecretAura.copy(alpha = 0.22f) else DarkSurfaceVariant)
                                            .border(
                                                width = if (isSelected) 1.5.dp else 1.dp,
                                                color = if (isSelected) SecretAura else Color(0x1AFFFFFF),
                                                shape = RoundedCornerShape(12.dp)
                                            )
                                            .clickable { alarmForceTriggerType = typeKey }
                                            .padding(horizontal = 14.dp, vertical = 10.dp)
                                            .testTag("alarm_trigger_${typeKey.lowercase()}"),
                                        contentAlignment = Alignment.CenterStart
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = title,
                                                    color = if (isSelected) SecretAura else TextPrimary,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Text(
                                                    text = desc,
                                                    color = if (isSelected) TextPrimary.copy(alpha = 0.9f) else TextSecondary,
                                                    fontSize = 11.sp
                                                )
                                            }
                                            if (isSelected) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(22.dp)
                                                        .background(SecretAura, CircleShape),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Check,
                                                        contentDescription = "Selected",
                                                        tint = Color.Black,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Section 3b: Stopwatch Hundredths Force
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isStopwatchForceEnabled) AmberAccent.copy(alpha = 0.15f) else DarkBg
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Timer,
                                        contentDescription = "Stopwatch Force",
                                        tint = AmberAccent,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Stopwatch 1/100s Force",
                                        color = TextPrimary,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "When paused, the hundredths of a second (.00–.99) will stop exactly on your forced number",
                                    color = TextSecondary,
                                    fontSize = 12.sp
                                )
                            }

                            Switch(
                                checked = isStopwatchForceEnabled,
                                onCheckedChange = { isStopwatchForceEnabled = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.Black,
                                    checkedTrackColor = AmberAccent
                                ),
                                modifier = Modifier.testTag("stopwatch_force_switch")
                            )
                        }

                        AnimatedVisibility(visible = isStopwatchForceEnabled) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 14.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "FORCED HUNDREDTHS (.00 - .99)",
                                    color = TextTertiary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.2.sp
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                // Wheel Picker for centiseconds (00-99)
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(DarkSurface)
                                        .padding(vertical = 8.dp, horizontal = 20.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = ".",
                                        color = TextPrimary,
                                        fontSize = 32.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(end = 4.dp)
                                    )

                                    WheelPicker(
                                        items = centisecondsList,
                                        selectedIndex = centisecondIndex,
                                        onItemSelected = { forcedStopwatchCentiseconds = it },
                                        width = 72.dp,
                                        testTag = "forced_stopwatch_centiseconds_picker"
                                    )

                                    Text(
                                        text = "s",
                                        color = TextSecondary,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.padding(start = 6.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = "Stopwatch Prediction: .${String.format(Locale.US, "%02d", forcedStopwatchCentiseconds)}s",
                                    color = TextPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                // Quick presets for common prediction numbers
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally)
                                ) {
                                    listOf(0, 14, 27, 37, 42, 50, 73, 88).forEach { preset ->
                                        val isSelected = forcedStopwatchCentiseconds == preset
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(if (isSelected) Color.White.copy(alpha = 0.25f) else DarkSurfaceVariant)
                                                .border(
                                                    width = 1.dp,
                                                    color = if (isSelected) Color.White else Color.Transparent,
                                                    shape = RoundedCornerShape(8.dp)
                                                )
                                                .clickable { forcedStopwatchCentiseconds = preset }
                                                .padding(horizontal = 7.dp, vertical = 4.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = ".${String.format(Locale.US, "%02d", preset)}",
                                                color = if (isSelected) Color.White else TextSecondary,
                                                fontSize = 11.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(18.dp))
                                HorizontalDivider(color = DividerColor.copy(alpha = 0.5f))
                                Spacer(modifier = Modifier.height(14.dp))

                                // Trigger on Nth stop
                                Text(
                                    text = "TRIGGER AFTER STOP COUNT",
                                    color = TextTertiary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.2.sp
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                val stopTriggers = listOf(
                                    1 to "1st Stop",
                                    2 to "2nd Stop",
                                    3 to "3rd Stop",
                                    4 to "4th Stop",
                                    5 to "5th Stop",
                                    0 to "Every Stop"
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally)
                                ) {
                                    stopTriggers.take(3).forEach { (count, label) ->
                                        val isSelected = stopwatchForceTriggerStopCount == count
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(if (isSelected) Color.White.copy(alpha = 0.25f) else DarkSurfaceVariant)
                                                .border(
                                                    width = if (isSelected) 1.5.dp else 0.dp,
                                                    color = if (isSelected) Color.White else Color.Transparent,
                                                    shape = RoundedCornerShape(10.dp)
                                                )
                                                .clickable { stopwatchForceTriggerStopCount = count }
                                                .padding(vertical = 8.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = label,
                                                color = if (isSelected) Color.White else TextPrimary,
                                                fontSize = 12.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally)
                                ) {
                                    stopTriggers.drop(3).forEach { (count, label) ->
                                        val isSelected = stopwatchForceTriggerStopCount == count
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(if (isSelected) Color.White.copy(alpha = 0.25f) else DarkSurfaceVariant)
                                                .border(
                                                    width = if (isSelected) 1.5.dp else 0.dp,
                                                    color = if (isSelected) Color.White else Color.Transparent,
                                                    shape = RoundedCornerShape(10.dp)
                                                )
                                                .clickable { stopwatchForceTriggerStopCount = count }
                                                .padding(vertical = 8.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = label,
                                                color = if (isSelected) Color.White else TextPrimary,
                                                fontSize = 12.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                val triggerExplanation = when (stopwatchForceTriggerStopCount) {
                                    1 -> "Forces on the 1st stop (Immediate standard force)."
                                    2 -> "Forces on the 2nd stop. The 1st stop runs 100% naturally so the spectator can test the stopwatch first!"
                                    3 -> "Forces on the 3rd stop. Stops #1 and #2 run naturally without forcing."
                                    4 -> "Forces on the 4th stop. Stops #1 through #3 run naturally."
                                    5 -> "Forces on the 5th stop. Stops #1 through #4 run naturally."
                                    else -> "Forces on every single pause/stop."
                                }

                                Text(
                                    text = triggerExplanation,
                                    color = TextSecondary,
                                    fontSize = 11.sp,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }

                // Section 4: Stealth & Security Protection
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkBg),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "PIN Lock Protection",
                                    color = TextPrimary,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Require passcode to access secret settings",
                                    color = TextSecondary,
                                    fontSize = 12.sp
                                )
                            }

                            Switch(
                                checked = isPinProtected,
                                onCheckedChange = { isPinProtected = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.Black,
                                    checkedTrackColor = Color.White
                                ),
                                modifier = Modifier.testTag("pin_protection_switch")
                            )
                        }

                        if (isPinProtected) {
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = pinCode,
                                onValueChange = { if (it.length <= 6) pinCode = it },
                                label = { Text("Passcode (4-6 digits)") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = AmberAccent,
                                    unfocusedBorderColor = DarkSurfaceVariant,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().testTag("pin_input_field")
                            )
                        }
                    }
                }

                // Secret Access Instructions
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkBg),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Info",
                            tint = TextTertiary,
                            modifier = Modifier.size(18.dp).padding(top = 2.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Stealth shortcuts:\n• Long-press top header title (Opens Settings)\n• Double-tap colon (:) in Alarm Time Editor\n• Long-press Stopwatch display to toggle 1/100s force\n• Long-press '+' FAB in Alarms to toggle alarm force",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val current = secretConfig ?: SecretConfigEntity()
                    viewModel.updateSecretConfig(
                        current.copy(
                            isForceEnabled = isForceEnabled,
                            forcedHour = forcedHour,
                            forcedMinute = forcedMinute,
                            forcedIsPm = forcedIsPm,
                            alarmForceTriggerType = alarmForceTriggerType,
                            isStopwatchForceEnabled = isStopwatchForceEnabled,
                            forcedStopwatchCentiseconds = forcedStopwatchCentiseconds,
                            stopwatchForceTriggerStopCount = stopwatchForceTriggerStopCount,
                            forceMode = "MAGNETIC",
                            accentColorTheme = selectedAccentName,
                            secretPin = pinCode,
                            isPinRequired = isPinProtected
                        )
                    )
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = primaryColor,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("save_secret_settings_button")
            ) {
                Text("Apply Settings", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = TextSecondary)
            }
        }
    )
}

@Composable
fun SecretPinPromptDialog(
    onDismiss: () -> Unit,
    onSubmitPin: (String) -> Unit
) {
    var enteredPin by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    val primaryColor = MaterialTheme.colorScheme.primary

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.Lock,
                    contentDescription = "Passcode Lock",
                    tint = TextPrimary
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Enter Passcode",
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column {
                Text(
                    text = "Please enter the passcode to access Settings.",
                    color = TextSecondary,
                    fontSize = 13.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = enteredPin,
                    onValueChange = {
                        if (it.length <= 6) {
                            enteredPin = it
                            isError = false
                        }
                    },
                    singleLine = true,
                    isError = isError,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = primaryColor,
                        unfocusedBorderColor = DarkSurfaceVariant,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("secret_pin_prompt_input")
                )

                if (isError) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Incorrect passcode",
                        color = RedAccent,
                        fontSize = 12.sp
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (enteredPin.isBlank()) {
                        isError = true
                    } else {
                        onSubmitPin(enteredPin)
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = primaryColor,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("submit_pin_button")
            ) {
                Text("Unlock", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}
