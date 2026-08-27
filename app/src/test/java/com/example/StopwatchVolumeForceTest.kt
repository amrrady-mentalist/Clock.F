package com.example

import android.app.Application
import android.view.KeyEvent
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.SecretConfigEntity
import com.example.ui.viewmodel.ClockViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class StopwatchVolumeForceTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var application: Application

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        application = ApplicationProvider.getApplicationContext()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private suspend fun setupConfig(viewModel: ClockViewModel, config: SecretConfigEntity) {
        viewModel.updateSecretConfig(config)
        // Wait until Room flow emits the updated config
        withTimeout(2000) {
            viewModel.secretConfig.first {
                it?.isStopwatchForceEnabled == config.isStopwatchForceEnabled &&
                    it?.forcedStopwatchCentiseconds == config.forcedStopwatchCentiseconds &&
                    it?.stopwatchForceTriggerType == config.stopwatchForceTriggerType &&
                    it?.stopwatchForceTriggerStopCount == config.stopwatchForceTriggerStopCount
            }
        }
    }

    @Test
    fun `test volume button trigger arms stopwatch force and overrides displayed centiseconds`() = runTest(testDispatcher) {
        val viewModel = ClockViewModel(application)
        advanceUntilIdle()

        val forcedCentiseconds = 42
        val testConfig = SecretConfigEntity(
            isStopwatchForceEnabled = true,
            forcedStopwatchCentiseconds = forcedCentiseconds,
            stopwatchForceTriggerStopCount = 1,
            stopwatchForceTriggerType = "VOLUME_BUTTON"
        )
        setupConfig(viewModel, testConfig)

        // 1. Initially, before volume key trigger, force is not armed
        assertFalse("Stopwatch force should not be armed initially", viewModel.isStopwatchForceArmed.value)
        assertFalse("Effective force should be inactive", viewModel.isStopwatchForceEffectivelyActive())

        // 2. Trigger Volume Button event (e.g. user clicked volume rocker)
        viewModel.onVolumeButtonTriggered()
        advanceUntilIdle()

        assertTrue("Stopwatch force should now be armed after volume event", viewModel.isStopwatchForceArmed.value)
        assertTrue("Effective force should be active", viewModel.isStopwatchForceEffectivelyActive())

        // 3. Start stopwatch, simulate elapsed time, and pause/stop
        viewModel.startStopwatch()
        // Simulate running time to 4.89 seconds (4890ms)
        Thread.sleep(50)
        viewModel.pauseStopwatch()
        advanceUntilIdle()

        // 4. Verify that the hundredths of a second were overridden to 42
        val stoppedTimeMs = viewModel.stopwatchTimeMs.value
        val actualCentiseconds = ((stoppedTimeMs % 1000L) / 10L).toInt()
        assertEquals("Displayed centiseconds must match forced centiseconds value (42)", forcedCentiseconds, actualCentiseconds)
    }

    @Test
    fun `test volume button trigger with 2nd stop count allows 1st stop natural and forces 2nd stop`() = runTest(testDispatcher) {
        val viewModel = ClockViewModel(application)
        advanceUntilIdle()

        val forcedCentiseconds = 88
        val testConfig = SecretConfigEntity(
            isStopwatchForceEnabled = true,
            forcedStopwatchCentiseconds = forcedCentiseconds,
            stopwatchForceTriggerStopCount = 2, // Force on 2nd stop
            stopwatchForceTriggerType = "VOLUME_BUTTON"
        )
        setupConfig(viewModel, testConfig)

        // Arm via volume button
        viewModel.onVolumeButtonTriggered()
        advanceUntilIdle()
        assertTrue("Stopwatch force should be armed", viewModel.isStopwatchForceArmed.value)

        // --- 1st Stop (Natural / Unforced test run) ---
        viewModel.startStopwatch()
        Thread.sleep(30)
        viewModel.pauseStopwatch()
        advanceUntilIdle()

        assertEquals("Stop count should be 1 after first pause", 1, viewModel.currentStopwatchStopCount.value)

        // --- 2nd Stop (Forced target) ---
        viewModel.startStopwatch()
        Thread.sleep(30)
        viewModel.pauseStopwatch()
        advanceUntilIdle()

        assertEquals("Stop count should be 2 after second pause", 2, viewModel.currentStopwatchStopCount.value)
        val secondStopMs = viewModel.stopwatchTimeMs.value
        val secondCentis = ((secondStopMs % 1000L) / 10L).toInt()
        assertEquals("Second stop centiseconds must be overridden to forced 88", forcedCentiseconds, secondCentis)
    }

    @Test
    fun `test MainActivity volume key event intercept triggers stopwatch arming and overrides time`() = runTest(testDispatcher) {
        val controller = Robolectric.buildActivity(MainActivity::class.java).setup()
        val activity = controller.get()
        val viewModel = activity.viewModel
        advanceUntilIdle()

        val forcedCentiseconds = 77
        val testConfig = SecretConfigEntity(
            isStopwatchForceEnabled = true,
            forcedStopwatchCentiseconds = forcedCentiseconds,
            stopwatchForceTriggerStopCount = 1,
            stopwatchForceTriggerType = "VOLUME_BUTTON"
        )
        setupConfig(viewModel, testConfig)

        assertFalse("Force should not be armed initially", viewModel.isStopwatchForceArmed.value)

        // Dispatch Volume Down key event to the Activity
        val volumeDownEvent = KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_VOLUME_DOWN)
        val handled = activity.dispatchKeyEvent(volumeDownEvent)
        advanceUntilIdle()

        assertTrue("Activity should intercept volume event when configured", handled)
        assertTrue("Stopwatch force should be armed via activity key event", viewModel.isStopwatchForceArmed.value)

        // Start and pause stopwatch, verify forced centiseconds
        viewModel.startStopwatch()
        Thread.sleep(40)
        viewModel.pauseStopwatch()
        advanceUntilIdle()

        val actualCentiseconds = ((viewModel.stopwatchTimeMs.value % 1000L) / 10L).toInt()
        assertEquals("Displayed centiseconds must match forced value 77", forcedCentiseconds, actualCentiseconds)

        controller.destroy()
    }
}
