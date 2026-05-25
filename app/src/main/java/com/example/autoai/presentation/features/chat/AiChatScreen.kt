package com.example.autoai.presentation.features.chat

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.autoai.presentation.util.ObserveAsEvents
import org.koin.androidx.compose.koinViewModel

@Composable
fun AiChatScreen(
    viewModel: AiChatViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LifecycleResumeEffect(Unit) {
        viewModel.onEvent(AiChatEvent.OnScreenResumed)
        onPauseOrDispose { }
    }

    ObserveAsEvents(viewModel.sideEffects) { effect ->
        when (effect) {
            is AiChatSideEffect.ShowError -> snackbarHostState.showSnackbar(effect.message.asString(context))
        }
    }

    AiChatContent(
        state = state,
        snackbarHostState = snackbarHostState,
        onEvent = viewModel::onEvent
    )
}