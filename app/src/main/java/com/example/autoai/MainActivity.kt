package com.example.autoai

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.example.autoai.navigation.AppNavGraph
import com.example.autoai.navigation.toRoute
import com.example.autoai.presentation.features.splash.SplashViewModel
import com.example.autoai.presentation.theme.AutoAITheme
import com.example.domain.repository.IPreferencesRepository
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : ComponentActivity() {

    private val splashViewModel: SplashViewModel by viewModel()
    private val preferencesRepository: IPreferencesRepository by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen().setKeepOnScreenCondition {
            splashViewModel.state.value.isLoading
        }

        super.onCreate(savedInstanceState)

        // enableEdgeToEdge is called inside the SideEffect below (with dark/light styles
        // matching the current theme); no unconditional first call needed.

        setContent {
            val splashState = splashViewModel.state.collectAsStateWithLifecycle()
            val isDarkMode by preferencesRepository.isDarkModeEnabled
                .collectAsStateWithLifecycle(initialValue = false)

            // POST_NOTIFICATIONS (Android 13+) — declared in manifest but Android won't
            // grant it automatically. Without this prompt, ReminderNotificationWorker
            // silently no-ops in sendNotification() and users never see reminders fire.
            val context = LocalContext.current
            val notificationPermissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission(),
                onResult = { /* User's choice — denial just means no notifications */ },
            )
            LaunchedEffect(Unit) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    val granted = ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.POST_NOTIFICATIONS,
                    ) == PackageManager.PERMISSION_GRANTED
                    if (!granted) {
                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }
            }

            // SideEffect runs on every successful composition; cheap because the system
            // bar config rarely changes. Previously this was a DisposableEffect with an
            // empty onDispose — the disposable lifecycle was unnecessary.
            androidx.compose.runtime.SideEffect {
                val statusStyle = if (isDarkMode) {
                    SystemBarStyle.dark(Color.TRANSPARENT)
                } else {
                    SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
                }
                val navStyle = if (isDarkMode) {
                    SystemBarStyle.dark(Color.TRANSPARENT)
                } else {
                    SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
                }
                enableEdgeToEdge(statusBarStyle = statusStyle, navigationBarStyle = navStyle)
            }

            AutoAITheme(darkTheme = isDarkMode) {
                val startDestination = splashState.value.startDestination
                if (startDestination != null) {
                    val navController = rememberNavController()
                    AppNavGraph(
                        navController = navController,
                        startDestination = startDestination.toRoute(),
                    )
                } else if (!splashState.value.isLoading) {
                    // Defensive: splash finished but no destination resolved. The
                    // SplashViewModel's catch-all routes to Auth on exception + on
                    // timeout, so this branch should be unreachable — but if a future
                    // change forgets the fallback, the user sees a centered spinner
                    // rather than an empty screen.
                    androidx.compose.foundation.layout.Box(
                        modifier = androidx.compose.ui.Modifier.fillMaxSize(),
                        contentAlignment = androidx.compose.ui.Alignment.Center,
                    ) {
                        androidx.compose.material3.CircularProgressIndicator()
                    }
                }
            }
        }
    }
}
