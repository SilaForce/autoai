package com.example.autoai.presentation.features.auth.register

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.autoai.presentation.util.ObserveAsEvents
import org.koin.androidx.compose.koinViewModel

@Composable
fun RegisterScreen(
    viewModel: RegisterViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    val context = LocalContext.current

    ObserveAsEvents(viewModel.sideEffects) { effect ->
        when (effect) {
            is RegisterSideEffect.ShowError -> {
                snackbarHostState.showSnackbar(effect.message.asString(context))
            }
            is RegisterSideEffect.ShowSuccess -> {
                snackbarHostState.showSnackbar(effect.message.asString(context))
            }
        }
    }

    RegisterContent(
        state = state,
        snackbarHostState = snackbarHostState,
        onEvent = viewModel::onEvent
    )
}