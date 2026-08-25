package com.example.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.CircularProgressTimer
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.GreenAccent
import com.example.ui.theme.RedAccent
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.ui.viewmodel.ClockViewModel
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun StopwatchScreen(
    viewModel: ClockViewModel,
    modifier: Modifier = Modifier
) {
    val totalTimeMs by viewModel.stopwatchTimeMs.collectAsState()
    val isRunning by viewModel.isStopwatchRunning.collectAsState()
    val laps by viewModel.laps.collectAsState()

    val primaryColor = MaterialTheme.colorScheme.primary

    // Formatted time components
    val minutes = (totalTimeMs / 60000) % 60
    val seconds = (totalTimeMs / 1000) % 60
    val millis = (totalTimeMs % 1000) / 10 // 2-digit centiseconds

    val fastestLap = remember(laps) {
        if (laps.size >= 2) laps.minByOrNull { it.lapTimeMs } else null
    }
    val slowestLap = remember(laps) {
        if (laps.size >= 2) laps.maxByOrNull { it.lapTimeMs } else null
    }

    // Circular dial progress based on seconds (0 to 60s)
    val secondProgress = (seconds + millis / 100f) / 60f

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .testTag("stopwatch_screen"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // Center Animated Dial & Time Display
        CircularProgressTimer(
            progress = secondProgress,
            size = 250.dp,
            strokeWidth = 8.dp,
            activeColor = primaryColor,
            testTag = "stopwatch_circular_dial"
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(
                    verticalAlignment = Alignment.Bottom,
                    modifier = Modifier
                        .combinedClickable(
                            onClick = {},
                            onLongClick = { viewModel.toggleStopwatchForceSecretly() },
                            onDoubleClick = { viewModel.onSecretTriggerAttempt() }
                        )
                ) {
                    Text(
                        text = String.format(Locale.US, "%02d:%02d", minutes, seconds),
                        color = TextPrimary,
                        fontSize = 44.sp,
                        fontWeight = FontWeight.Light,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = String.format(Locale.US, ".%02d", millis),
                        color = AmberAccent,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(bottom = 6.dp, start = 2.dp)
                    )
                }

                if (laps.isNotEmpty()) {
                    val currentLapTime = totalTimeMs - (laps.firstOrNull()?.totalTimeMs ?: 0L)
                    val lapSec = (currentLapTime / 1000) % 60
                    val lapMs = (currentLapTime % 1000) / 10
                    Text(
                        text = String.format(Locale.US, "Lap %d  %02d.%02d", laps.size + 1, lapSec, lapMs),
                        color = TextTertiary,
                        fontSize = 14.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Action Buttons Row (Lap / Reset & Start / Pause)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Lap / Reset Button
            Button(
                onClick = {
                    if (isRunning) {
                        viewModel.recordLap()
                    } else {
                        viewModel.resetStopwatch()
                    }
                },
                enabled = totalTimeMs > 0,
                colors = ButtonDefaults.buttonColors(
                    containerColor = DarkSurfaceVariant,
                    disabledContainerColor = DarkSurfaceVariant.copy(alpha = 0.4f)
                ),
                shape = CircleShape,
                modifier = Modifier.size(72.dp).testTag("lap_reset_button")
            ) {
                Icon(
                    imageVector = if (isRunning) Icons.Default.Flag else Icons.Default.Refresh,
                    contentDescription = if (isRunning) "Lap" else "Reset",
                    tint = if (totalTimeMs > 0) TextPrimary else TextTertiary,
                    modifier = Modifier.size(26.dp)
                )
            }

            // Start / Pause Button
            Button(
                onClick = {
                    if (isRunning) {
                        viewModel.pauseStopwatch()
                    } else {
                        viewModel.startStopwatch()
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isRunning) RedAccent else primaryColor,
                    contentColor = if (isRunning) Color.White else Color.Black
                ),
                shape = CircleShape,
                modifier = Modifier.size(80.dp).testTag("start_pause_stopwatch_button")
            ) {
                Icon(
                    imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isRunning) "Pause" else "Start",
                    modifier = Modifier.size(36.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Laps List
        if (laps.isNotEmpty()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .testTag("laps_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("LAP", color = TextTertiary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text("LAP TIME", color = TextTertiary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text("TOTAL", color = TextTertiary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(laps, key = { it.lapNumber }) { lap ->
                            val isFastest = lap == fastestLap
                            val isSlowest = lap == slowestLap

                            val lapMin = (lap.lapTimeMs / 60000) % 60
                            val lapSec = (lap.lapTimeMs / 1000) % 60
                            val lapCent = (lap.lapTimeMs % 1000) / 10

                            val totMin = (lap.totalTimeMs / 60000) % 60
                            val totSec = (lap.totalTimeMs / 1000) % 60
                            val totCent = (lap.totalTimeMs % 1000) / 10

                            val rowColor = when {
                                isFastest -> GreenAccent
                                isSlowest -> RedAccent
                                else -> TextPrimary
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = String.format(Locale.US, "%02d", lap.lapNumber),
                                        color = TextSecondary,
                                        fontSize = 14.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    if (isFastest) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("BEST", color = GreenAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                Text(
                                    text = String.format(Locale.US, "+%02d:%02d.%02d", lapMin, lapSec, lapCent),
                                    color = rowColor,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    fontFamily = FontFamily.Monospace
                                )

                                Text(
                                    text = String.format(Locale.US, "%02d:%02d.%02d", totMin, totSec, totCent),
                                    color = TextSecondary,
                                    fontSize = 14.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            }
        } else {
            Spacer(modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
