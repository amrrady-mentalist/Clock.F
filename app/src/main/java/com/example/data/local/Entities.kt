package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alarms")
data class AlarmEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val hour: Int, // 1-12 or 0-23
    val minute: Int, // 0-59
    val isPm: Boolean = false,
    val is24Hour: Boolean = false,
    val label: String = "Alarm",
    val daysOfWeek: String = "1,2,3,4,5", // CSV of day numbers (1=Mon..7=Sun) or empty for once
    val isEnabled: Boolean = true,
    val ringtone: String = "Cosmic Bell",
    val vibrate: Boolean = true,
    val snoozeMinutes: Int = 10
)

@Entity(tableName = "world_cities")
data class WorldCityEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val cityName: String,
    val country: String,
    val timeZoneId: String,
    val isFavorite: Boolean = true
)

@Entity(tableName = "secret_config")
data class SecretConfigEntity(
    @PrimaryKey
    val id: Int = 1,
    val isForceEnabled: Boolean = false,
    val forcedHour: Int = 7,
    val forcedMinute: Int = 30,
    val forcedIsPm: Boolean = false, // false = AM, true = PM
    val alarmForceTriggerType: String = "ALWAYS", // ALWAYS, PROXIMITY_WAVE, VOLUME_BUTTON
    val isStopwatchForceEnabled: Boolean = false,
    val forcedStopwatchCentiseconds: Int = 37, // 0-99 hundredths of a second (.00 to .99)
    val stopwatchForceTriggerStopCount: Int = 1, // 0 = Every Stop, 1 = 1st Stop, 2 = 2nd Stop, 3 = 3rd Stop, etc.
    val stopwatchForceTriggerType: String = "ALWAYS", // ALWAYS, PROXIMITY_WAVE, VOLUME_BUTTON, WAVE_OR_VOLUME
    val forceMode: String = "MAGNETIC", // MAGNETIC, INSTANT, GRAVITY, DISCREET_TAP
    val secretPin: String = "1234",
    val isPinRequired: Boolean = false,
    val hapticFeedback: Boolean = true,
    val secretTriggerGesture: String = "LONG_PRESS_HEADER", // LONG_PRESS_HEADER, TRIPLE_TAP, DOUBLE_TAP_COLON
    val forceAlarmsCount: Int = 0, // Statistics on successful forces
    val accentColorTheme: String = "WHITE"
)
