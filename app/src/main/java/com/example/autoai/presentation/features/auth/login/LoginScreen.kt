package com.example.autoai.presentation.features.auth.login

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.autoai.presentation.util.ObserveAsEvents
import org.koin.androidx.compose.koinViewModel

@Composable
fun LoginScreen(
    viewModel: LoginViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    ObserveAsEvents(viewModel.sideEffects) { effect ->
        when (effect) {
            is LoginSideEffect.ShowError -> {
                snackbarHostState.showSnackbar(effect.message.asString(context))
            }
            is LoginSideEffect.ShowSuccess -> {
                snackbarHostState.showSnackbar(effect.message.asString(context))
            }
        }
    }

    LoginContent(
        state = state,
        snackbarHostState = snackbarHostState,
        onEvent = viewModel::onEvent
    )
}
