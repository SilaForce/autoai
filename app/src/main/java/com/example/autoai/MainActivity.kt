package com.example.autoai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.example.autoai.navigation.AppNavGraph
import com.example.autoai.navigation.toRoute
import com.example.autoai.presentation.features.splash.SplashViewModel
import com.example.autoai.presentation.theme.AutoAITheme
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : ComponentActivity() {

    private val splashViewModel: SplashViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen().setKeepOnScreenCondition {
            splashViewModel.state.value.isLoading
        }

        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            val splashState = splashViewModel.state.collectAsStateWithLifecycle()

            AutoAITheme {
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