package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.CircularProgressTimer
import com.example.ui.components.WheelPicker
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.DarkBg
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.RedAccent
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.ui.viewmodel.ClockViewModel
import java.util.Locale

import com.example.ui.theme.glassBorderBrush
import com.example.ui.theme.appleThickGlass
import com.example.ui.theme.liquidGlass
import com.example.ui.theme.GlassSurfaceDark
import com.example.ui.theme.GlassSurfaceElevated

@Composable
fun TimerScreen(
    viewModel: ClockViewModel,
    modifier: Modifier = Modifier
) {
    val totalSeconds by viewModel.timerTotalSeconds.collectAsState()
    val remainingSeconds by viewModel.timerRemainingSeconds.collectAsState()
    val isRunning by viewModel.isTimerRunning.collectAsState()
    val isFinished by viewModel.isTimerFinished.collectAsState()

    val primaryColor = MaterialTheme.colorScheme.primary

    val progress = if (totalSeconds > 0) {
        remainingSeconds.toFloat() / totalSeconds.toFloat()
    } else 0f

    val hours = remainingSeconds / 3600
    val minutes = (remainingSeconds % 3600) / 60
    val seconds = remainingSeconds % 60

    val hoursList = remember { (0..23).map { String.format(Locale.US, "%02d", it) } }
    val minutesList = remember { (0..59).map { String.format(Locale.US, "%02d", it) } }
    val secondsList = remember { (0..59).map { String.format(Locale.US, "%02d", it) } }

    var pickerHours by remember { mutableIntStateOf(0) }
    var pickerMinutes by remember { mutableIntStateOf(5) }
    var pickerSeconds by remember { mutableIntStateOf(0) }

    val presetTimes = listOf(
        "1 min" to 60,
        "5 min" to 300,
        "10 min" to 600,
        "15 min" to 900,
        "30 min" to 1800,
        "1 hr" to 3600
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .testTag("timer_screen")
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            if (isRunning || remainingSeconds > 0 && !isFinished) {
                // Running / Paused Timer Circular Dial
                CircularProgressTimer(
                    progress = progress,
                    size = 260.dp,
                    strokeWidth = 10.dp,
                    activeColor = primaryColor,
                    testTag = "timer_circular_dial"
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (hours > 0) {
                                String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds)
                            } else {
                                String.format(Locale.US, "%02d:%02d", minutes, seconds)
                            },
                            color = TextPrimary,
                            fontSize = if (hours > 0) 38.sp else 48.sp,
                            fontWeight = FontWeight.Light,
                            fontFamily = FontFamily.Monospace
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = if (isRunning) "COUNTING DOWN" else "PAUSED",
                            color = if (isRunning) primaryColor else AmberAccent,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp
                        )
                    }
                }
            } else {
                // Time Duration Wheel Selector
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .appleThickGlass(
                            shape = RoundedCornerShape(26.dp),
                            backgroundColor = GlassSurfaceDark,
                            borderWidth = 1.2.dp,
                            borderBrush = glassBorderBrush(0.55f, 0.18f, 0.06f),
                            highlightAlpha = 0.35f
                        )
                        .testTag("timer_picker_card")
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "SET DURATION",
                            color = TextTertiary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                WheelPicker(
                                    items = hoursList,
                                    selectedIndex = pickerHours,
                                    onItemSelected = { pickerHours = it },
                                    width = 68.dp,
                                    testTag = "timer_hours_picker"
                                )
                                Text("hours", color = TextSecondary, fontSize = 11.sp)
                            }

                            Text(":", color = primaryColor, fontSize = 28.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp))

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                WheelPicker(
                                    items = minutesList,
                                    selectedIndex = pickerMinutes,
                                    onItemSelected = { pickerMinutes = it },
                                    width = 68.dp,
                                    testTag = "timer_minutes_picker"
                                )
                                Text("min", color = TextSecondary, fontSize = 11.sp)
                            }

                            Text(":", color = primaryColor, fontSize = 28.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp))

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                WheelPicker(
                                    items = secondsList,
                                    selectedIndex = pickerSeconds,
                                    onItemSelected = { pickerSeconds = it },
                                    width = 68.dp,
                                    testTag = "timer_seconds_picker"
                                )
                                Text("sec", color = TextSecondary, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Quick Preset Chips
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(presetTimes) { (label, durationSec) ->
                    Box(
                        modifier = Modifier
                            .appleThickGlass(
                                shape = RoundedCornerShape(16.dp),
                                backgroundColor = Color(0x1EFFFFFF),
                                borderWidth = 1.dp,
                                borderBrush = glassBorderBrush(0.40f, 0.12f, 0.04f),
                                highlightAlpha = 0.20f
                            )
                            .clickable {
                                val h = durationSec / 3600
                                val m = (durationSec % 3600) / 60
                                val s = durationSec % 60
                                pickerHours = h
                                pickerMinutes = m
                                pickerSeconds = s
                                viewModel.setTimerDuration(h, m, s)
                                viewModel.startTimer()
                            }
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Text(text = label, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Timer Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isRunning || remainingSeconds > 0) {
                    // Reset Button
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .appleThickGlass(
                                shape = CircleShape,
                                backgroundColor = Color(0x22FFFFFF),
                                borderWidth = 1.2.dp,
                                borderBrush = glassBorderBrush(0.45f, 0.15f, 0.05f),
                                highlightAlpha = 0.25f
                            )
                            .clickable { viewModel.resetTimer() }
                            .testTag("timer_reset_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reset Timer",
                            tint = TextPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    // Play / Pause Button
                    Box(
                        modifier = Modifier
                            .size(82.dp)
                            .appleThickGlass(
                                shape = CircleShape,
                                backgroundColor = if (isRunning) Color(0xD934343E) else Color(0xFFFFFFFF),
                                borderWidth = 1.5.dp,
                                borderBrush = glassBorderBrush(0.80f, 0.30f, 0.10f),
                                highlightAlpha = 0.50f
                            )
                            .clickable {
                                if (isRunning) {
                                    viewModel.pauseTimer()
                                } else {
                                    viewModel.startTimer()
                                }
                            }
                            .testTag("timer_start_pause_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isRunning) "Pause" else "Start",
                            tint = if (isRunning) Color.White else Color.Black,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    // +1:00 Button
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .appleThickGlass(
                                shape = CircleShape,
                                backgroundColor = Color(0x22FFFFFF),
                                borderWidth = 1.2.dp,
                                borderBrush = glassBorderBrush(0.45f, 0.15f, 0.05f),
                                highlightAlpha = 0.25f
                            )
                            .clickable { viewModel.addTimerSeconds(60) }
                            .testTag("timer_add_minute_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("+1m", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    // Start Button when idle
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(58.dp)
                            .appleThickGlass(
                                shape = RoundedCornerShape(22.dp),
                                backgroundColor = Color(0xFFFFFFFF),
                                borderWidth = 1.5.dp,
                                borderBrush = glassBorderBrush(0.85f, 0.35f, 0.10f),
                                highlightAlpha = 0.50f
                            )
                            .clickable {
                                viewModel.setTimerDuration(pickerHours, pickerMinutes, pickerSeconds)
                                viewModel.startTimer()
                            }
                            .testTag("timer_start_initial_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Start", tint = Color.Black)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Start Timer", color = Color.Black, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Timer Finished Alert Overlay
        if (isFinished) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(DarkBg.copy(alpha = 0.95f))
                    .testTag("timer_finished_overlay"),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .background(Color.White.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = "Timer Finished",
                            tint = Color.White,
                            modifier = Modifier.size(48.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "Time's Up!",
                        color = TextPrimary,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    Button(
                        onClick = { viewModel.dismissTimerFinished() },
                        colors = ButtonDefaults.buttonColors(containerColor = primaryColor, contentColor = Color.Black),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.height(50.dp).testTag("timer_dismiss_finish_button")
                    ) {
                        Text("Dismiss", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
