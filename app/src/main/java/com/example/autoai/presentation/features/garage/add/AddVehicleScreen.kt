package com.example.autoai.presentation.features.garage.add

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.autoai.navigation.IAppNavigator
import com.example.autoai.presentation.util.ObserveAsEvents
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@Composable
fun AddVehicleScreen(
    viewModel: AddVehicleViewModel = koinViewModel(),
    navigator: IAppNavigator = koinInject(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    ObserveAsEvents(viewModel.sideEffects) { effect ->
        when (effect) {
            is AddVehicleSideEffect.ShowError -> {
                snackbarHostState.showSnackbar(effect.message.asString(context))
            }

            AddVehicleSideEffect.NavigateBack -> navigator.navigateBack()
        }
    }

    AddVehicleContent(
        state = state,
        snackbarHostState = snackbarHostState,
        onEvent = viewModel::onEvent,
    )
}
