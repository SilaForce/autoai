package com.example.autoai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
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

        enableEdgeToEdge()

        setContent {
            val splashState = splashViewModel.state.collectAsStateWithLifecycle()
            val isDarkMode by preferencesRepository.isDarkModeEnabled
                .collectAsStateWithLifecycle(initialValue = false)

            AutoAITheme(darkTheme = isDarkMode) {
                splashState.value.startDestination?.let { startDestination ->
                    val navController = rememberNavController()
                    AppNavGraph(
                        navController = navController,
                        startDestination = startDestination.toRoute(),
                    )
                }
            }
        }
    }
}
