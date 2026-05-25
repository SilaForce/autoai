package com.example.autoai.presentation.features.garage

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.autoai.R
import com.example.autoai.navigation.IAppNavigator
import com.example.autoai.presentation.util.ObserveAsEvents
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@Composable
fun GarageScreen(
    viewModel: GarageViewModel = koinViewModel(),
    navigator: IAppNavigator = koinInject(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LifecycleResumeEffect(Unit) {
        viewModel.onEvent(GarageEvent.OnScreenResumed)
        onPauseOrDispose { }
    }

    ObserveAsEvents(viewModel.sideEffects) { effect ->
        when (effect) {
            is GarageSideEffect.ShowError -> {
                snackbarHostState.showSnackbar(effect.message.asString(context))
            }
            is GarageSideEffect.NavigateTo -> navigator.navigateTo(effect.route)
            GarageSideEffect.ShowUndoableDelete -> {
                val result = snackbarHostState.showSnackbar(
                    message = context.getString(R.string.garage_vehicle_deleted_snackbar),
                    actionLabel = context.getString(R.string.garage_undo),
                    withDismissAction = false,
                    duration = SnackbarDuration.Short,
                )
                if (result == SnackbarResult.ActionPerformed) {
                    viewModel.onEvent(GarageEvent.OnUndoDelete)
                }
            }
        }
    }

    GarageContent(
        state = state,
        snackbarHostState = snackbarHostState,
        onEvent = viewModel::onEvent,
    )
}
