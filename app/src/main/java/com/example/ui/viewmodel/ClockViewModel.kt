package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AlarmEntity
import com.example.data.local.AppDatabase
import com.example.data.local.SecretConfigEntity
import com.example.data.local.WorldCityEntity
import com.example.data.repository.ClockRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

data class LapItem(
    val lapNumber: Int,
    val lapTimeMs: Long,
    val totalTimeMs: Long
)

class ClockViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: ClockRepository

    // Database Flows
    val alarms: StateFlow<List<AlarmEntity>>
    val worldCities: StateFlow<List<WorldCityEntity>>
    val secretConfig: StateFlow<SecretConfigEntity?>

    // Current Time Flow (updates every 50ms for smooth analog sweep)
    private val _currentTime = MutableStateFlow(System.currentTimeMillis())
    val currentTime = _currentTime.asStateFlow()

    // Active Navigation Tab: 0 = Clock, 1 = Alarm, 2 = Stopwatch, 3 = Timer
    private val _selectedTab = MutableStateFlow(0)
    val selectedTab = _selectedTab.asStateFlow()

    // Hidden Secret Settings Dialog State
    private val _showSecretSettings = MutableStateFlow(false)
    val showSecretSettings = _showSecretSettings.asStateFlow()

    private val _showPinPrompt = MutableStateFlow(false)
    val showPinPrompt = _showPinPrompt.asStateFlow()

    // Alarm creation / edit dialog state
    private val _editingAlarm = MutableStateFlow<AlarmEntity?>(null)
    val editingAlarm = _editingAlarm.asStateFlow()

    private val _showAlarmDialog = MutableStateFlow(false)
    val showAlarmDialog = _showAlarmDialog.asStateFlow()

    // Simulated Alarm Ringing popup state
    private val _ringingAlarm = MutableStateFlow<AlarmEntity?>(null)
    val ringingAlarm = _ringingAlarm.asStateFlow()

    // World Clock Add Dialog
    private val _showAddCityDialog = MutableStateFlow(false)
    val showAddCityDialog = _showAddCityDialog.asStateFlow()

    // Stopwatch State
    private val _stopwatchTimeMs = MutableStateFlow(0L)
    val stopwatchTimeMs = _stopwatchTimeMs.asStateFlow()

    private val _isStopwatchRunning = MutableStateFlow(false)
    val isStopwatchRunning = _isStopwatchRunning.asStateFlow()

    private val _laps = MutableStateFlow<List<LapItem>>(emptyList())
    val laps = _laps.asStateFlow()

    private var stopwatchJob: Job? = null
    private var stopwatchStartTime = 0L
    private var stopwatchAccumulated = 0L
    private var stopwatchStopCount = 0
    private val _currentStopwatchStopCount = MutableStateFlow(0)
    val currentStopwatchStopCount = _currentStopwatchStopCount.asStateFlow()

    // Timer State
    private val _timerTotalSeconds = MutableStateFlow(300) // Default 5 mins
    val timerTotalSeconds = _timerTotalSeconds.asStateFlow()

    private val _timerRemainingSeconds = MutableStateFlow(300)
    val timerRemainingSeconds = _timerRemainingSeconds.asStateFlow()

    private val _isTimerRunning = MutableStateFlow(false)
    val isTimerRunning = _isTimerRunning.asStateFlow()

    private val _isTimerFinished = MutableStateFlow(false)
    val isTimerFinished = _isTimerFinished.asStateFlow()

    private var timerJob: Job? = null

    // Discreet feedback message toast/banner
    private val _discreetNotice = MutableStateFlow<String?>(null)
    val discreetNotice = _discreetNotice.asStateFlow()

    // Alarm force dynamic arm state (for PROXIMITY_WAVE and VOLUME_BUTTON triggers)
    private val _isAlarmForceArmed = MutableStateFlow(false)
    val isAlarmForceArmed = _isAlarmForceArmed.asStateFlow()

    init {
        val database = AppDatabase.getDatabase(application, viewModelScope)
        repository = ClockRepository(database.clockDao())

        alarms = repository.allAlarms
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        worldCities = repository.allWorldCities
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        secretConfig = repository.secretConfig
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SecretConfigEntity())

        // Start clock ticker
        viewModelScope.launch(Dispatchers.Default) {
            while (isActive) {
                _currentTime.value = System.currentTimeMillis()
                delay(50)
            }
        }
    }

    fun selectTab(tab: Int) {
        _selectedTab.value = tab
    }

    // ==========================================
    // ALARMS OPERATIONS
    // ==========================================
    fun openAddAlarm() {
        val cal = Calendar.getInstance()
        val currentHour = cal.get(Calendar.HOUR)
        val currentIsPm = cal.get(Calendar.AM_PM) == Calendar.PM
        val h = if (currentHour == 0) 12 else currentHour
        val m = (cal.get(Calendar.MINUTE) + 5) % 60

        _editingAlarm.value = AlarmEntity(
            hour = h,
            minute = m,
            isPm = currentIsPm,
            label = "Alarm",
            daysOfWeek = "1,2,3,4,5",
            isEnabled = true
        )
        _showAlarmDialog.value = true
    }

    fun openEditAlarm(alarm: AlarmEntity) {
        _editingAlarm.value = alarm
        _showAlarmDialog.value = true
    }

    fun dismissAlarmDialog() {
        _showAlarmDialog.value = false
        _editingAlarm.value = null
    }

    fun saveAlarm(alarm: AlarmEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            if (alarm.id == 0L) {
                repository.insertAlarm(alarm)
            } else {
                repository.updateAlarm(alarm)
            }
        }
        _showAlarmDialog.value = false
        _editingAlarm.value = null
    }

    fun deleteAlarm(alarm: AlarmEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteAlarm(alarm)
        }
    }

    fun toggleAlarm(alarm: AlarmEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.toggleAlarm(alarm.id, !alarm.isEnabled)
        }
    }

    fun testRingAlarm(alarm: AlarmEntity) {
        _ringingAlarm.value = alarm
        performHaptic(pattern = longArrayOf(0, 300, 200, 300))
    }

    fun dismissRingingAlarm() {
        _ringingAlarm.value = null
    }

    // ==========================================
    // WORLD CLOCK OPERATIONS
    // ==========================================
    fun openAddCity() {
        _showAddCityDialog.value = true
    }

    fun dismissAddCityDialog() {
        _showAddCityDialog.value = false
    }

    fun addWorldCity(cityName: String, country: String, timeZoneId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertWorldCity(
                WorldCityEntity(
                    cityName = cityName,
                    country = country,
                    timeZoneId = timeZoneId
                )
            )
        }
        _showAddCityDialog.value = false
    }

    fun deleteWorldCity(city: WorldCityEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteWorldCity(city)
        }
    }

    // ==========================================
    // STOPWATCH OPERATIONS
    // ==========================================
    fun startStopwatch() {
        if (_isStopwatchRunning.value) return
        _isStopwatchRunning.value = true
        stopwatchStartTime = System.currentTimeMillis() - stopwatchAccumulated

        stopwatchJob = viewModelScope.launch(Dispatchers.Default) {
            while (isActive) {
                _stopwatchTimeMs.value = System.currentTimeMillis() - stopwatchStartTime
                delay(10)
            }
        }
    }

    fun pauseStopwatch() {
        if (!_isStopwatchRunning.value) return
        stopwatchJob?.cancel()
        _isStopwatchRunning.value = false
        stopwatchStopCount++
        _currentStopwatchStopCount.value = stopwatchStopCount
        val currentMs = _stopwatchTimeMs.value
        val config = secretConfig.value

        val isForceEnabled = config?.isStopwatchForceEnabled == true
        val targetTriggerStop = config?.stopwatchForceTriggerStopCount ?: 1
        // targetTriggerStop == 0 means Every Stop, otherwise trigger exactly on the Nth stop
        val shouldForce = isForceEnabled && (targetTriggerStop == 0 || stopwatchStopCount == targetTriggerStop)

        if (shouldForce) {
            val forcedCentis = config?.forcedStopwatchCentiseconds?.coerceIn(0, 99) ?: 37
            val fullSeconds = currentMs / 1000
            // Seamlessly lock the 1/100s to the forced target
            val forcedMs = (fullSeconds * 1000) + (forcedCentis * 10)
            _stopwatchTimeMs.value = forcedMs
            stopwatchAccumulated = forcedMs
            performHaptic(pattern = longArrayOf(0, 40))
        } else {
            stopwatchAccumulated = currentMs
        }
    }

    fun toggleStopwatchForceSecretly() {
        val current = secretConfig.value ?: SecretConfigEntity()
        val newState = !current.isStopwatchForceEnabled
        val updated = current.copy(isStopwatchForceEnabled = newState)
        viewModelScope.launch(Dispatchers.IO) {
            repository.saveSecretConfig(updated)
        }
        if (newState) {
            performHaptic(pattern = longArrayOf(0, 50, 60, 50))
        } else {
            performHaptic(pattern = longArrayOf(0, 80))
        }
    }

    fun resetStopwatch() {
        stopwatchJob?.cancel()
        _isStopwatchRunning.value = false
        stopwatchAccumulated = 0L
        _stopwatchTimeMs.value = 0L
        _laps.value = emptyList()
        stopwatchStopCount = 0
        _currentStopwatchStopCount.value = 0
    }

    fun recordLap() {
        val totalMs = _stopwatchTimeMs.value
        val currentLaps = _laps.value
        val prevTotal = currentLaps.firstOrNull()?.totalTimeMs ?: 0L
        val lapTime = totalMs - prevTotal

        val newLap = LapItem(
            lapNumber = currentLaps.size + 1,
            lapTimeMs = lapTime,
            totalTimeMs = totalMs
        )
        _laps.value = listOf(newLap) + currentLaps
        performHaptic(pattern = longArrayOf(0, 40))
    }

    // ==========================================
    // TIMER OPERATIONS
    // ==========================================
    fun setTimerDuration(hours: Int, minutes: Int, seconds: Int) {
        val total = (hours * 3600) + (minutes * 60) + seconds
        if (total > 0) {
            _timerTotalSeconds.value = total
            _timerRemainingSeconds.value = total
            _isTimerFinished.value = false
        }
    }

    fun startTimer() {
        if (_timerRemainingSeconds.value <= 0) return
        _isTimerRunning.value = true
        _isTimerFinished.value = false

        timerJob?.cancel()
        timerJob = viewModelScope.launch(Dispatchers.Default) {
            while (isActive && _timerRemainingSeconds.value > 0) {
                delay(1000)
                _timerRemainingSeconds.update { (it - 1).coerceAtLeast(0) }
            }
            if (_timerRemainingSeconds.value == 0) {
                _isTimerRunning.value = false
                _isTimerFinished.value = true
                performHaptic(pattern = longArrayOf(0, 500, 300, 500, 300, 500))
            }
        }
    }

    fun pauseTimer() {
        timerJob?.cancel()
        _isTimerRunning.value = false
    }

    fun resetTimer() {
        timerJob?.cancel()
        _isTimerRunning.value = false
        _isTimerFinished.value = false
        _timerRemainingSeconds.value = _timerTotalSeconds.value
    }

    fun addTimerSeconds(seconds: Int) {
        _timerRemainingSeconds.update { it + seconds }
        _timerTotalSeconds.update { it + seconds }
    }

    fun dismissTimerFinished() {
        _isTimerFinished.value = false
        _timerRemainingSeconds.value = _timerTotalSeconds.value
    }

    // ==========================================
    // SECRET HIDDEN SETTINGS & MAGIC FORCE ENGINE
    // ==========================================
    fun onSecretTriggerAttempt() {
        val config = secretConfig.value ?: SecretConfigEntity()
        performHaptic(pattern = longArrayOf(0, 50, 50, 80))

        if (config.isPinRequired) {
            _showPinPrompt.value = true
        } else {
            _showSecretSettings.value = true
        }
    }

    fun verifySecretPin(pin: String): Boolean {
        val config = secretConfig.value ?: SecretConfigEntity()
        return if (pin == config.secretPin) {
            _showPinPrompt.value = false
            _showSecretSettings.value = true
            true
        } else {
            false
        }
    }

    fun dismissPinPrompt() {
        _showPinPrompt.value = false
    }

    fun dismissSecretSettings() {
        _showSecretSettings.value = false
    }

    fun updateSecretConfig(updated: SecretConfigEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.saveSecretConfig(updated)
        }
    }

    fun isAlarmForceEffectivelyActive(): Boolean {
        val config = secretConfig.value ?: return false
        if (!config.isForceEnabled) return false
        return when (config.alarmForceTriggerType) {
            "PROXIMITY_WAVE", "VOLUME_BUTTON" -> _isAlarmForceArmed.value
            else -> true // "ALWAYS"
        }
    }

    /**
     * Triggered when a hand wave over the proximity sensor is detected.
     */
    fun onProximityWaveDetected() {
        val config = secretConfig.value ?: SecretConfigEntity()
        if (config.alarmForceTriggerType == "PROXIMITY_WAVE") {
            val newState = !_isAlarmForceArmed.value
            _isAlarmForceArmed.value = newState
            if (!config.isForceEnabled) {
                viewModelScope.launch(Dispatchers.IO) {
                    repository.saveSecretConfig(config.copy(isForceEnabled = true))
                }
            }
            if (newState) {
                // Subtle crisp double haptic pulse indicating force armed
                performHaptic(pattern = longArrayOf(0, 45, 60, 45))
            } else {
                // Single short haptic pulse indicating force disarmed
                performHaptic(pattern = longArrayOf(0, 80))
            }
        }
    }

    /**
     * Triggered when a volume button is clicked without changing volume.
     */
    fun onVolumeButtonTriggered() {
        val config = secretConfig.value ?: SecretConfigEntity()
        if (config.alarmForceTriggerType == "VOLUME_BUTTON") {
            val newState = !_isAlarmForceArmed.value
            _isAlarmForceArmed.value = newState
            if (!config.isForceEnabled) {
                viewModelScope.launch(Dispatchers.IO) {
                    repository.saveSecretConfig(config.copy(isForceEnabled = true))
                }
            }
            if (newState) {
                // Subtle crisp double haptic pulse indicating force armed
                performHaptic(pattern = longArrayOf(0, 45, 60, 45))
            } else {
                // Single short haptic pulse indicating force disarmed
                performHaptic(pattern = longArrayOf(0, 80))
            }
        }
    }

    fun resetAlarmForceArmed() {
        _isAlarmForceArmed.value = false
    }

    /**
     * Stealth Toggle: allows magician to secretly enable/disable force without opening UI
     */
    fun toggleForceSecretly() {
        val current = secretConfig.value ?: SecretConfigEntity()
        val newState = !current.isForceEnabled
        val updated = current.copy(isForceEnabled = newState)
        viewModelScope.launch(Dispatchers.IO) {
            repository.saveSecretConfig(updated)
        }
        // Discreet double buzz for ON, single buzz for OFF
        if (newState) {
            performHaptic(pattern = longArrayOf(0, 60, 80, 60))
        } else {
            performHaptic(pattern = longArrayOf(0, 100))
        }
    }

    private fun performHaptic(pattern: LongArray) {
        try {
            val vibrator = getApplication<Application>().getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            vibrator?.let {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    it.vibrate(VibrationEffect.createWaveform(pattern, -1))
                } else {
                    @Suppress("DEPRECATION")
                    it.vibrate(pattern, -1)
                }
            }
        } catch (_: Exception) {}
    }
}
