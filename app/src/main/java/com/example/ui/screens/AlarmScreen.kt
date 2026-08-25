package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.AlarmOn
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.AlarmEntity
import com.example.data.local.SecretConfigEntity
import com.example.ui.components.WheelPicker
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.DarkBg
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceHover
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.GreenAccent
import com.example.ui.theme.RedAccent
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.ui.theme.VioletAccent
import com.example.ui.viewmodel.ClockViewModel
import java.util.Locale

import com.example.ui.theme.glassBorderBrush
import com.example.ui.theme.liquidGlass
import com.example.ui.theme.GlassSurfaceDark

@Composable
fun AlarmScreen(
    viewModel: ClockViewModel,
    modifier: Modifier = Modifier
) {
    val alarms by viewModel.alarms.collectAsState()
    val showAlarmDialog by viewModel.showAlarmDialog.collectAsState()
    val editingAlarm by viewModel.editingAlarm.collectAsState()
    val ringingAlarm by viewModel.ringingAlarm.collectAsState()
    val secretConfig by viewModel.secretConfig.collectAsState()
    val isAlarmForceArmed by viewModel.isAlarmForceArmed.collectAsState()

    val primaryColor = MaterialTheme.colorScheme.primary
    val primaryMuted = MaterialTheme.colorScheme.primaryContainer

    val nextActiveAlarm = remember(alarms) {
        alarms.firstOrNull { it.isEnabled }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("alarm_screen")
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(12.dp))

                // Next Alarm Hero Banner
                if (nextActiveAlarm != null) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .liquidGlass(
                                shape = RoundedCornerShape(24.dp),
                                backgroundColor = GlassSurfaceDark,
                                borderWidth = 1.dp,
                                borderBrush = glassBorderBrush(0.4f, 0.12f, 0.04f)
                            )
                            .testTag("next_alarm_banner")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 18.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(primaryMuted, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AlarmOn,
                                    contentDescription = "Active Alarm",
                                    tint = primaryColor,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "UPCOMING ALARM",
                                    color = primaryColor,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.2.sp
                                )
                                Text(
                                    text = String.format(
                                        Locale.US,
                                        "%d:%02d %s",
                                        nextActiveAlarm.hour,
                                        nextActiveAlarm.minute,
                                        if (nextActiveAlarm.isPm) "PM" else "AM"
                                    ),
                                    color = TextPrimary,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = nextActiveAlarm.label,
                                    color = TextSecondary,
                                    fontSize = 13.sp
                                )
                            }

                            IconButton(
                                onClick = { viewModel.testRingAlarm(nextActiveAlarm) },
                                modifier = Modifier.testTag("test_ring_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Test Ring",
                                    tint = primaryColor
                                )
                            }
                        }
                    }
                }
            }

            // Alarms list
            if (alarms.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Alarm,
                            contentDescription = "No Alarms",
                            tint = TextTertiary,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No Alarms Set",
                            color = TextSecondary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Tap the + button to create a new alarm",
                            color = TextTertiary,
                            fontSize = 14.sp
                        )
                    }
                }
            } else {
                items(alarms, key = { it.id }) { alarm ->
                    AlarmCard(
                        alarm = alarm,
                        primaryColor = primaryColor,
                        onToggle = { viewModel.toggleAlarm(alarm) },
                        onEdit = { viewModel.openEditAlarm(alarm) },
                        onDelete = { viewModel.deleteAlarm(alarm) },
                        onTestRing = { viewModel.testRingAlarm(alarm) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(160.dp))
            }
        }

        // Floating Action Button to Add Alarm (Elevated comfortably above bottom navigation bar)
        FloatingActionButton(
            onClick = { viewModel.openAddAlarm() },
            containerColor = primaryColor,
            contentColor = Color.Black,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 92.dp)
                .size(60.dp)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onLongPress = {
                            // Secret gesture: Long press on FAB toggles force secretly
                            viewModel.toggleForceSecretly()
                        },
                        onTap = {
                            viewModel.openAddAlarm()
                        }
                    )
                }
                .testTag("add_alarm_fab")
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add Alarm",
                modifier = Modifier.size(30.dp)
            )
        }

        // Add/Edit Alarm Wheel Picker Dialog (With Force Engine integration!)
        if (showAlarmDialog && editingAlarm != null) {
            AlarmEditorDialog(
                alarm = editingAlarm!!,
                secretConfig = secretConfig,
                isAlarmForceArmed = isAlarmForceArmed,
                primaryColor = primaryColor,
                onDismiss = { viewModel.dismissAlarmDialog() },
                onSave = { updated -> viewModel.saveAlarm(updated) },
                onSecretColonTap = { viewModel.onSecretTriggerAttempt() }
            )
        }

        // Alarm Ringing Simulation Screen
        if (ringingAlarm != null) {
            AlarmRingingOverlay(
                alarm = ringingAlarm!!,
                primaryColor = primaryColor,
                onDismiss = { viewModel.dismissRingingAlarm() }
            )
        }
    }
}

@Composable
fun AlarmCard(
    alarm: AlarmEntity,
    primaryColor: Color,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onTestRing: () -> Unit
) {
    val dayNames = listOf("M", "T", "W", "T", "F", "S", "S")
    val activeDays = remember(alarm.daysOfWeek) {
        if (alarm.daysOfWeek.isBlank()) emptySet()
        else alarm.daysOfWeek.split(",").mapNotNull { it.trim().toIntOrNull() }.toSet()
    }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier
            .fillMaxWidth()
            .liquidGlass(
                shape = RoundedCornerShape(24.dp),
                backgroundColor = if (alarm.isEnabled) GlassSurfaceDark else GlassSurfaceDark.copy(alpha = 0.5f),
                borderWidth = 1.dp,
                borderBrush = glassBorderBrush(
                    if (alarm.isEnabled) 0.35f else 0.15f,
                    0.1f,
                    0.03f
                )
            )
            .clip(RoundedCornerShape(24.dp))
            .clickable { onEdit() }
            .testTag("alarm_card_${alarm.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = String.format(Locale.US, "%d:%02d", alarm.hour, alarm.minute),
                            color = if (alarm.isEnabled) TextPrimary else TextTertiary,
                            fontSize = 38.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (alarm.isPm) "PM" else "AM",
                            color = if (alarm.isEnabled) primaryColor else TextTertiary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = alarm.label,
                        color = if (alarm.isEnabled) TextSecondary else TextTertiary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Switch(
                    checked = alarm.isEnabled,
                    onCheckedChange = { onToggle() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.Black,
                        checkedTrackColor = primaryColor,
                        uncheckedThumbColor = TextTertiary,
                        uncheckedTrackColor = DarkSurfaceVariant
                    ),
                    modifier = Modifier.testTag("alarm_switch_${alarm.id}")
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Day recurrence badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    for (i in 1..7) {
                        val isSelected = activeDays.contains(i)
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .background(
                                    color = if (isSelected && alarm.isEnabled) primaryColor.copy(alpha = 0.2f)
                                    else if (isSelected) DarkSurfaceVariant
                                    else Color.Transparent,
                                    shape = CircleShape
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected && alarm.isEnabled) primaryColor
                                    else if (isSelected) TextTertiary
                                    else DarkSurfaceVariant,
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = dayNames[i - 1],
                                color = if (isSelected && alarm.isEnabled) primaryColor
                                else if (isSelected) TextSecondary
                                else TextTertiary,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                Row {
                    IconButton(
                        onClick = onTestRing,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Test Alarm",
                            tint = AmberAccent,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp).testTag("delete_alarm_${alarm.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Alarm",
                            tint = TextTertiary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * The Alarm Editor with the interactive 3D Wheel Pickers and Secret Force Engine!
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AlarmEditorDialog(
    alarm: AlarmEntity,
    secretConfig: SecretConfigEntity?,
    isAlarmForceArmed: Boolean = true,
    primaryColor: Color,
    onDismiss: () -> Unit,
    onSave: (AlarmEntity) -> Unit,
    onSecretColonTap: () -> Unit
) {
    val hoursList = remember { (1..12).map { it.toString() } }
    val minutesList = remember { (0..59).map { String.format(Locale.US, "%02d", it) } }
    val amPmList = remember { listOf("AM", "PM") }

    var selectedHourIndex by remember {
        mutableIntStateOf(hoursList.indexOf(alarm.hour.toString()).coerceAtLeast(0))
    }
    var selectedMinuteIndex by remember {
        mutableIntStateOf(alarm.minute.coerceIn(0, 59))
    }
    var selectedAmPmIndex by remember {
        mutableIntStateOf(if (alarm.isPm) 1 else 0)
    }

    var label by remember { mutableStateOf(alarm.label) }
    var ringtone by remember { mutableStateOf(alarm.ringtone) }
    var vibrate by remember { mutableStateOf(alarm.vibrate) }

    // Active recurrence days (1=Mon..7=Sun)
    val dayNames = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    var selectedDays by remember {
        mutableStateOf(
            if (alarm.daysOfWeek.isBlank()) setOf(1, 2, 3, 4, 5)
            else alarm.daysOfWeek.split(",").mapNotNull { it.trim().toIntOrNull() }.toSet()
        )
    }

    // Force engine parameters
    val isForceActive = remember(secretConfig?.isForceEnabled, secretConfig?.alarmForceTriggerType, isAlarmForceArmed) {
        if (secretConfig?.isForceEnabled != true) false
        else when (secretConfig.alarmForceTriggerType) {
            "PROXIMITY_WAVE", "VOLUME_BUTTON" -> isAlarmForceArmed
            else -> true // "ALWAYS"
        }
    }
    val forcedHourTarget = remember(secretConfig) {
        secretConfig?.forcedHour?.let { h ->
            val h12 = if (h > 12) h - 12 else if (h == 0) 12 else h
            hoursList.indexOf(h12.toString()).takeIf { it >= 0 }
        }
    }
    val forcedMinuteTarget = remember(secretConfig) {
        secretConfig?.forcedMinute?.coerceIn(0, 59)
    }
    val forcedAmPmTarget = remember(secretConfig) {
        if (secretConfig?.forcedIsPm == true) 1 else 0
    }
    val forceMode = secretConfig?.forceMode ?: "MAGNETIC"

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (alarm.id == 0L) "Set Alarm" else "Edit Alarm",
                    color = TextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                // Secret subtle trigger area: Tapping the icon 3 times opens hidden settings
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onDoubleTap = { onSecretColonTap() },
                                onLongPress = { onSecretColonTap() }
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Alarm,
                        contentDescription = null,
                        tint = TextTertiary
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // The 3D Wheel Pickers (Hours : Minutes AM/PM)
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkBg),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp, horizontal = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Hour Wheel Picker
                        WheelPicker(
                            items = hoursList,
                            selectedIndex = selectedHourIndex,
                            onItemSelected = { selectedHourIndex = it },
                            width = 72.dp,
                            isForceEnabled = isForceActive,
                            forcedTargetIndex = forcedHourTarget,
                            forceMode = forceMode,
                            testTag = "hour_wheel_picker"
                        )

                        // Colon separator (Hidden tap trigger also supported here)
                        Text(
                            text = ":",
                            color = primaryColor,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .pointerInput(Unit) {
                                    detectTapGestures(
                                        onDoubleTap = { onSecretColonTap() },
                                        onLongPress = { onSecretColonTap() }
                                    )
                                }
                                .testTag("colon_separator")
                        )

                        // Minute Wheel Picker
                        WheelPicker(
                            items = minutesList,
                            selectedIndex = selectedMinuteIndex,
                            onItemSelected = { selectedMinuteIndex = it },
                            width = 72.dp,
                            isForceEnabled = isForceActive,
                            forcedTargetIndex = forcedMinuteTarget,
                            forceMode = forceMode,
                            testTag = "minute_wheel_picker"
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        // AM/PM Wheel Picker
                        WheelPicker(
                            items = amPmList,
                            selectedIndex = selectedAmPmIndex,
                            onItemSelected = { selectedAmPmIndex = it },
                            width = 64.dp,
                            isForceEnabled = isForceActive,
                            forcedTargetIndex = forcedAmPmTarget,
                            forceMode = forceMode,
                            testTag = "ampm_wheel_picker"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Alarm Label Input
                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    label = { Text("Alarm Name", color = TextSecondary) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = primaryColor,
                        unfocusedBorderColor = DarkSurfaceVariant,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("alarm_label_input")
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Recurrence Day Chips
                Text(
                    text = "REPEAT DAYS",
                    color = TextTertiary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.align(Alignment.Start)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    for (i in 1..7) {
                        val isSelected = selectedDays.contains(i)
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isSelected) primaryColor else DarkSurfaceVariant
                                )
                                .clickable {
                                    selectedDays = if (isSelected) {
                                        selectedDays - i
                                    } else {
                                        selectedDays + i
                                    }
                                }
                                .testTag("day_chip_$i"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = dayNames[i - 1].take(1),
                                color = if (isSelected) Color.Black else TextSecondary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Vibrate Toggle Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkSurfaceVariant)
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Vibration,
                            contentDescription = "Vibrate",
                            tint = VioletAccent,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (isForceActive) "Vibrate." else "Vibrate",
                            color = TextPrimary,
                            fontSize = 14.sp
                        )
                    }

                    Switch(
                        checked = vibrate,
                        onCheckedChange = { vibrate = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.Black,
                            checkedTrackColor = primaryColor
                        )
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalHour = hoursList[selectedHourIndex].toInt()
                    val finalMinute = selectedMinuteIndex
                    val finalIsPm = selectedAmPmIndex == 1
                    val daysCsv = selectedDays.sorted().joinToString(",")

                    onSave(
                        alarm.copy(
                            hour = finalHour,
                            minute = finalMinute,
                            isPm = finalIsPm,
                            label = label.ifBlank { "Alarm" },
                            daysOfWeek = daysCsv,
                            vibrate = vibrate,
                            isEnabled = true
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = primaryColor,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("save_alarm_button")
            ) {
                Text("Save", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}

@Composable
fun AlarmRingingOverlay(
    alarm: AlarmEntity,
    primaryColor: Color,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg.copy(alpha = 0.95f))
            .testTag("alarm_ringing_overlay"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .background(primaryColor.copy(alpha = 0.15f), CircleShape)
                    .border(2.dp, primaryColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.NotificationsActive,
                    contentDescription = "Alarm Ringing",
                    tint = primaryColor,
                    modifier = Modifier.size(54.dp)
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = String.format(Locale.US, "%d:%02d %s", alarm.hour, alarm.minute, if (alarm.isPm) "PM" else "AM"),
                color = TextPrimary,
                fontSize = 52.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = alarm.label,
                color = TextSecondary,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(48.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.weight(1f).height(56.dp)
                ) {
                    Text("Snooze (10m)", color = TextPrimary, fontSize = 16.sp)
                }

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = RedAccent),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.weight(1f).height(56.dp).testTag("dismiss_alarm_ring_button")
                ) {
                    Text("Dismiss", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
