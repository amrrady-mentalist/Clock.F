package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.WatchLater
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.HourglassBottom
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.SecretAura
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.ui.viewmodel.ClockViewModel

data class NavTab(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

class MainActivity : ComponentActivity() {
    private val viewModel: ClockViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val secretConfig by viewModel.secretConfig.collectAsState()
            val accentTheme = remember(secretConfig?.accentColorTheme) {
                secretConfig?.accentColorTheme?.let { name ->
                    try {
                        AccentTheme.valueOf(name)
                    } catch (_: Exception) {
                        AccentTheme.CYAN
                    }
                } ?: AccentTheme.CYAN
            }

            ClockTheme(accent = accentTheme) {
                MainClockApp(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainClockApp(viewModel: ClockViewModel) {
    val selectedTab by viewModel.selectedTab.collectAsState()
    val showSecretSettings by viewModel.showSecretSettings.collectAsState()
    val showPinPrompt by viewModel.showPinPrompt.collectAsState()
    val secretConfig by viewModel.secretConfig.collectAsState()

    val primaryColor = MaterialTheme.colorScheme.primary

    val tabs = listOf(
        NavTab("Clock", Icons.Filled.Schedule, Icons.Outlined.Schedule),
        NavTab("Alarm", Icons.Filled.Alarm, Icons.Outlined.Alarm),
        NavTab("Stopwatch", Icons.Filled.Timer, Icons.Outlined.Timer),
        NavTab("Timer", Icons.Filled.HourglassBottom, Icons.Outlined.HourglassBottom)
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = DarkBg,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            // Primary Secret Trigger: Long pressing the top header bar opens Secret Force Settings!
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onLongPress = { viewModel.onSecretTriggerAttempt() },
                                    onDoubleTap = { viewModel.onSecretTriggerAttempt() }
                                )
                            }
                            .testTag("app_top_header")
                    ) {
                        Text(
                            text = tabs[selectedTab].title,
                            color = TextPrimary,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )

                        // If force is active, subtle invisible stealth affordance or indicator
                        if (secretConfig?.isForceEnabled == true) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .size(4.dp)
                                    .background(primaryColor.copy(alpha = 0.5f), CircleShape)
                            )
                        }
                    }
                },
                actions = {
                    // Discreet icon or stealth trigger
                    IconButton(
                        onClick = { viewModel.onSecretTriggerAttempt() },
                        modifier = Modifier
                            .size(44.dp)
                            .testTag("secret_settings_entry_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More Options",
                            tint = TextSecondary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkBg
                ),
                modifier = Modifier.statusBarsPadding()
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = DarkSurface,
                tonalElevation = 0.dp,
                modifier = Modifier
                    .navigationBarsPadding()
                    .testTag("bottom_nav_bar")
            ) {
                tabs.forEachIndexed { index, tab ->
                    val isSelected = selectedTab == index
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { viewModel.selectTab(index) },
                        icon = {
                            Icon(
                                imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                contentDescription = tab.title,
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        label = {
                            Text(
                                text = tab.title,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = primaryColor,
                            selectedTextColor = primaryColor,
                            unselectedIconColor = TextTertiary,
                            unselectedTextColor = TextTertiary,
                            indicatorColor = DarkSurfaceVariant
                        ),
                        modifier = Modifier.testTag("nav_tab_${tab.title.lowercase()}")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
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
