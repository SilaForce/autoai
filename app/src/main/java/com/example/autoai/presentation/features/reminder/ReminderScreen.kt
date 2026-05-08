package com.example.autoai.presentation.features.reminder

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.autoai.presentation.util.ObserveAsEvents
import org.koin.androidx.compose.koinViewModel

@Composable
fun ReminderScreen(
    viewModel: ReminderViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    ObserveAsEvents(viewModel.sideEffects) { effect ->
        when (effect) {
            is RemindersSideEffect.ShowError -> snackbarHostState.showSnackbar(effect.message.asString(context))
            is RemindersSideEffect.ShowSuccess -> snackbarHostState.showSnackbar(effect.message.asString(context))
        }
    }

    ReminderContent(
        state = state,
        snackbarHostState = snackbarHostState,
        onEvent = viewModel::onEvent
    )
}