package com.example.autoai.presentation.features.settings

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.autoai.presentation.util.ObserveAsEvents
import org.koin.androidx.compose.koinViewModel

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    ObserveAsEvents(viewModel.sideEffects) { effect ->
        when (effect) {
            is SettingsSideEffect.ShowError -> {
                snackbarHostState.showSnackbar(
                    message = effect.message.asString(context)
                )
            }
            is SettingsSideEffect.ShowMessage -> {
                snackbarHostState.showSnackbar(
                    message = effect.message.asString(context)
                )
            }
        }
    }

    SettingsContent(
        state = state,
        snackbarHostState = snackbarHostState,
        onEvent = viewModel::onEvent
    )
}