package com.example

import android.app.Application
import android.view.KeyEvent
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.local.SecretConfigEntity
import com.example.ui.viewmodel.ClockViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class StopwatchVolumeForceTest {

    private lateinit var application: Application

    @Before
    fun setUp() {
        application = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun `test volume button trigger arms force for next stop and automatically disarms after force`() {
        val forcedCentiseconds = 42
        val testConfig = SecretConfigEntity(
            isStopwatchForceEnabled = true,
            forcedStopwatchCentiseconds = forcedCentiseconds,
            stopwatchForceTriggerStopCount = 1,
            stopwatchForceTriggerType = "VOLUME_BUTTON"
        )

        val controller = Robolectric.buildActivity(MainActivity::class.java).setup()
        val activity = controller.get()
        val viewModel = activity.viewModel
        viewModel.updateSecretConfig(testConfig)

        // 1. Initially un-armed
        assertFalse("Stopwatch force should not be armed initially", viewModel.isStopwatchForceArmed.value)

        // 2. Perform 2 random spectator test stops (unforced)
        viewModel.startStopwatch()
        viewModel.pauseStopwatch()
        assertEquals(1, viewModel.currentStopwatchStopCount.value)

        viewModel.startStopwatch()
        viewModel.pauseStopwatch()
        assertEquals(2, viewModel.currentStopwatchStopCount.value)

        // 3. Magician clicks volume button to arm force on demand
        viewModel.onVolumeButtonTriggered()
        assertTrue("Stopwatch force should now be armed", viewModel.isStopwatchForceArmed.value)
        assertTrue("Effective force should be active", viewModel.isStopwatchForceEffectivelyActive())

        // 4. Next stop should apply forced hundredths (.42)
        viewModel.startStopwatch()
        viewModel.pauseStopwatch()

        val forcedMs = viewModel.stopwatchTimeMs.value
        val actualCentis = ((forcedMs % 1000L) / 10L).toInt()
        assertEquals("Displayed centiseconds must match forced value (42)", forcedCentiseconds, actualCentis)

        // 5. Force should automatically disarm after successful stop
        assertFalse("Force should automatically disarm after being executed", viewModel.isStopwatchForceArmed.value)

        controller.destroy()
    }

    @Test
    fun `test wave and volume synced toggle allows random stops and disarming on demand`() {
        val forcedCentiseconds = 88
        val testConfig = SecretConfigEntity(
            isStopwatchForceEnabled = true,
            forcedStopwatchCentiseconds = forcedCentiseconds,
            stopwatchForceTriggerStopCount = 1,
            stopwatchForceTriggerType = "WAVE_OR_VOLUME"
        )

        val controller = Robolectric.buildActivity(MainActivity::class.java).setup()
        val activity = controller.get()
        val viewModel = activity.viewModel
        viewModel.updateSecretConfig(testConfig)

        // Wave over sensor to arm
        viewModel.onProximityWaveDetected()
        assertTrue("Stopwatch force should be armed after wave", viewModel.isStopwatchForceArmed.value)

        // Spectator wants another test -> Click volume button to disarm
        viewModel.onVolumeButtonTriggered()
        assertFalse("Stopwatch force should be disarmed after volume click", viewModel.isStopwatchForceArmed.value)

        // Spectator does unforced test stop
        viewModel.startStopwatch()
        viewModel.pauseStopwatch()

        // Magician waves again to re-arm
        viewModel.onProximityWaveDetected()
        assertTrue("Stopwatch force should be re-armed after wave", viewModel.isStopwatchForceArmed.value)

        // Stop is now forced!
        viewModel.startStopwatch()
        viewModel.pauseStopwatch()

        val stoppedMs = viewModel.stopwatchTimeMs.value
        val centis = ((stoppedMs % 1000L) / 10L).toInt()
        assertEquals("Stop centiseconds must be forced 88", forcedCentiseconds, centis)
        assertFalse("Stopwatch force should disarm after firing", viewModel.isStopwatchForceArmed.value)

        controller.destroy()
    }

    @Test
    fun `test MainActivity volume key event intercept triggers stopwatch arming`() {
        val forcedCentiseconds = 77
        val testConfig = SecretConfigEntity(
            isStopwatchForceEnabled = true,
            forcedStopwatchCentiseconds = forcedCentiseconds,
            stopwatchForceTriggerStopCount = 1,
            stopwatchForceTriggerType = "VOLUME_BUTTON"
        )

        val controller = Robolectric.buildActivity(MainActivity::class.java).setup()
        val activity = controller.get()
        val viewModel = activity.viewModel
        viewModel.updateSecretConfig(testConfig)

        assertFalse("Force should not be armed initially", viewModel.isStopwatchForceArmed.value)

        // Dispatch Volume Down key event to Activity
        val volumeDownEvent = KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_VOLUME_DOWN)
        val handled = activity.dispatchKeyEvent(volumeDownEvent)

        assertTrue("Activity should intercept volume event when configured", handled)
        assertTrue("Stopwatch force should be armed via activity key event", viewModel.isStopwatchForceArmed.value)

        // Start and pause stopwatch, verify forced centiseconds
        viewModel.startStopwatch()
        viewModel.pauseStopwatch()

        val actualCentiseconds = ((viewModel.stopwatchTimeMs.value % 1000L) / 10L).toInt()
        assertEquals("Displayed centiseconds must match forced value 77", forcedCentiseconds, actualCentiseconds)

        controller.destroy()
    }
}
