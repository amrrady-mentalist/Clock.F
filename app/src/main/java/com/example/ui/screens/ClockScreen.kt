package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Splitscreen
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.example.data.local.WorldCityEntity
import com.example.ui.components.AnalogClockView
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceHover
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.RedAccent
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.ui.theme.VioletAccent
import com.example.ui.viewmodel.ClockViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.abs

import com.example.ui.theme.glassBorderBrush
import com.example.ui.theme.liquidGlass
import com.example.ui.theme.GlassSurfaceDark

@Composable
fun ClockScreen(
    viewModel: ClockViewModel,
    modifier: Modifier = Modifier
) {
    val currentTimeMs by viewModel.currentTime.collectAsState()
    val worldCities by viewModel.worldCities.collectAsState()
    val showAddCityDialog by viewModel.showAddCityDialog.collectAsState()

    var showAnalogClock by remember { mutableStateOf(true) }

    val calendar = remember(currentTimeMs) {
        Calendar.getInstance().apply { timeInMillis = currentTimeMs }
    }

    val hour = calendar.get(Calendar.HOUR)
    val hour24 = calendar.get(Calendar.HOUR_OF_DAY)
    val minute = calendar.get(Calendar.MINUTE)
    val second = calendar.get(Calendar.SECOND)
    val millis = calendar.get(Calendar.MILLISECOND)
    val smoothSecond = second + (millis / 1000f)

    val dateFormat = remember { SimpleDateFormat("EEEE, MMMM d", Locale.getDefault()) }
    val timeFormat12 = remember { SimpleDateFormat("hh:mm", Locale.getDefault()) }
    val amPmFormat = remember { SimpleDateFormat("a", Locale.getDefault()) }
    val secFormat = remember { SimpleDateFormat("ss", Locale.getDefault()) }

    val primaryColor = MaterialTheme.colorScheme.primary

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("clock_screen")
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(12.dp))

                // Hero Clock Display (Switchable between Analog and Digital)
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    shape = RoundedCornerShape(28.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .liquidGlass(
                            shape = RoundedCornerShape(28.dp),
                            backgroundColor = GlassSurfaceDark,
                            borderWidth = 1.dp,
                            borderBrush = glassBorderBrush(0.4f, 0.12f, 0.04f)
                        )
                        .testTag("hero_clock_card")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp, horizontal = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Date & Timezone Header
                        Text(
                            text = dateFormat.format(Date(currentTimeMs)).uppercase(),
                            color = primaryColor,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        if (showAnalogClock) {
                            AnalogClockView(
                                hour = hour,
                                minute = minute,
                                second = smoothSecond,
                                size = 220.dp,
                                accentColor = primaryColor,
                                modifier = Modifier
                                    .pointerInput(Unit) {
                                        detectTapGestures(
                                            onDoubleTap = { viewModel.onSecretTriggerAttempt() },
                                            onTap = { showAnalogClock = false }
                                        )
                                    }
                            )
                        } else {
                            // Digital Clock Big Display
                            Row(
                                verticalAlignment = Alignment.Bottom,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier
                                    .padding(vertical = 20.dp)
                                    .clickable { showAnalogClock = true }
                            ) {
                                Text(
                                    text = timeFormat12.format(Date(currentTimeMs)),
                                    color = TextPrimary,
                                    fontSize = 64.sp,
                                    fontWeight = FontWeight.Light,
                                    fontFamily = FontFamily.Monospace
                                )
                                Column(
                                    modifier = Modifier.padding(bottom = 12.dp, start = 8.dp)
                                ) {
                                    Text(
                                        text = secFormat.format(Date(currentTimeMs)),
                                        color = TextPrimary,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Text(
                                        text = amPmFormat.format(Date(currentTimeMs)),
                                        color = TextSecondary,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Switch Analog / Digital Button
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(
                                onClick = { showAnalogClock = !showAnalogClock },
                                modifier = Modifier.testTag("toggle_clock_style_button")
                            ) {
                                Icon(
                                    imageVector = if (showAnalogClock) Icons.Outlined.Splitscreen else Icons.Outlined.Schedule,
                                    contentDescription = "Switch Style",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (showAnalogClock) "Show Digital" else "Show Analog",
                                    color = TextSecondary,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }

            // World Clock Section Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "WORLD CLOCK",
                        color = TextTertiary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    )

                    TextButton(
                        onClick = { viewModel.openAddCity() },
                        modifier = Modifier.testTag("add_city_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add City",
                            tint = primaryColor,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Add City",
                            color = primaryColor,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // World Cities List
            items(worldCities, key = { it.id }) { city ->
                WorldCityCard(
                    city = city,
                    currentTimeMs = currentTimeMs,
                    primaryColor = primaryColor,
                    onDelete = { viewModel.deleteWorldCity(city) }
                )
            }

            item {
                Spacer(modifier = Modifier.height(140.dp))
            }
        }

        // Add City Dialog
        if (showAddCityDialog) {
            AddCityDialog(
                primaryColor = primaryColor,
                onDismiss = { viewModel.dismissAddCityDialog() },
                onAddCity = { cityName, country, timeZoneId ->
                    viewModel.addWorldCity(cityName, country, timeZoneId)
                }
            )
        }
    }
}

@Composable
fun WorldCityCard(
    city: WorldCityEntity,
    currentTimeMs: Long,
    primaryColor: Color,
    onDelete: () -> Unit
) {
    val tz = remember(city.timeZoneId) { TimeZone.getTimeZone(city.timeZoneId) }
    val localTz = remember { TimeZone.getDefault() }

    val cityCalendar = remember(currentTimeMs, tz) {
        Calendar.getInstance(tz).apply { timeInMillis = currentTimeMs }
    }

    val localCalendar = remember(currentTimeMs, localTz) {
        Calendar.getInstance(localTz).apply { timeInMillis = currentTimeMs }
    }

    // Time difference calculation
    val offsetDiffHours = (tz.getOffset(currentTimeMs) - localTz.getOffset(currentTimeMs)) / (1000 * 60 * 60)
    val isAhead = offsetDiffHours >= 0
    val diffText = if (offsetDiffHours == 0) "Same time" else "${if (isAhead) "+" else ""}$offsetDiffHours hrs"

    val cityHour = cityCalendar.get(Calendar.HOUR)
    val cityHourDisplay = if (cityHour == 0) 12 else cityHour
    val cityMinute = cityCalendar.get(Calendar.MINUTE)
    val cityAmPm = if (cityCalendar.get(Calendar.AM_PM) == Calendar.PM) "PM" else "AM"
    val cityHour24 = cityCalendar.get(Calendar.HOUR_OF_DAY)
    val isDayTime = cityHour24 in 6..18

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .fillMaxWidth()
            .liquidGlass(
                shape = RoundedCornerShape(20.dp),
                backgroundColor = GlassSurfaceDark,
                borderWidth = 1.dp,
                borderBrush = glassBorderBrush(0.3f, 0.1f, 0.03f)
            )
            .testTag("world_city_card_${city.cityName}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = city.cityName,
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = if (isDayTime) Icons.Outlined.LightMode else Icons.Outlined.DarkMode,
                        contentDescription = if (isDayTime) "Day" else "Night",
                        tint = if (isDayTime) Color.White else Color(0xFFA1A1AA),
                        modifier = Modifier.size(16.dp)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "${city.country} • $diffText",
                    color = TextSecondary,
                    fontSize = 13.sp
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = String.format(Locale.US, "%d:%02d", cityHourDisplay, cityMinute),
                    color = TextPrimary,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = cityAmPm,
                    color = primaryColor,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp).testTag("delete_city_${city.cityName}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete City",
                        tint = TextTertiary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun AddCityDialog(
    primaryColor: Color,
    onDismiss: () -> Unit,
    onAddCity: (String, String, String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    val presetCities = remember {
        listOf(
            Triple("Cairo", "Egypt", "Africa/Cairo"),
            Triple("Dubai", "UAE", "Asia/Dubai"),
            Triple("London", "United Kingdom", "Europe/London"),
            Triple("New York", "United States", "America/New_York"),
            Triple("Tokyo", "Japan", "Asia/Tokyo"),
            Triple("Paris", "France", "Europe/Paris"),
            Triple("Sydney", "Australia", "Australia/Sydney"),
            Triple("San Francisco", "United States", "America/Los_Angeles"),
            Triple("Singapore", "Singapore", "Asia/Singapore"),
            Triple("Berlin", "Germany", "Europe/Berlin"),
            Triple("Toronto", "Canada", "America/Toronto"),
            Triple("Hong Kong", "China", "Asia/Hong_Kong"),
            Triple("Seoul", "South Korea", "Asia/Seoul"),
            Triple("Bangkok", "Thailand", "Asia/Bangkok"),
            Triple("Rome", "Italy", "Europe/Rome"),
            Triple("Madrid", "Spain", "Europe/Madrid"),
            Triple("Rio de Janeiro", "Brazil", "America/Sao_Paulo"),
            Triple("Cape Town", "South Africa", "Africa/Johannesburg"),
            Triple("Mumbai", "India", "Asia/Kolkata"),
            Triple("Auckland", "New Zealand", "Pacific/Auckland"),
            Triple("Honolulu", "Hawaii, US", "Pacific/Honolulu")
        )
    }

    val filteredCities = remember(searchQuery) {
        if (searchQuery.isBlank()) {
            presetCities
        } else {
            presetCities.filter {
                it.first.contains(searchQuery, ignoreCase = true) ||
                it.second.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = {
            Text(
                text = "Add World Clock",
                color = TextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth().height(360.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search city or country...", color = TextTertiary) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Search,
                            contentDescription = "Search",
                            tint = primaryColor
                        )
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = primaryColor,
                        unfocusedBorderColor = DarkSurfaceVariant,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("city_search_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredCities) { (cityName, country, tzId) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(DarkSurfaceVariant)
                                .clickable {
                                    onAddCity(cityName, country, tzId)
                                }
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = cityName,
                                    color = TextPrimary,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = country,
                                    color = TextSecondary,
                                    fontSize = 12.sp
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add",
                                tint = primaryColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}
