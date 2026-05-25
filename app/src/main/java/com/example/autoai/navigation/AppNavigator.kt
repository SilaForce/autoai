package com.example.autoai.navigation

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Navigation actions are funnelled through a buffered SharedFlow so emitting from a
 * ViewModel never blocks. No debounce here — fast legitimate navigations to different
 * screens (e.g. error snackbar followed by `navigateTo(Home)`) used to silently drop the
 * second action under a single shared debounce. Click-debouncing belongs at the call site
 * via [com.example.autoai.base.BaseViewModel.withDebounce].
 *
 * Buffer is sized to 8 so a brief burst of back-to-back emissions (e.g. snackbar event +
 * navigation in the same frame) doesn't drop on overflow. Consumer keeps up under normal
 * lifecycle conditions.
 */
class AppNavigator : IAppNavigator {
    private val _navigationActions = MutableSharedFlow<NavigationAction>(extraBufferCapacity = 8)
    override val navigationActions = _navigationActions.asSharedFlow()

    override fun navigateTo(destination: Any, popUpTo: Any?, inclusive: Boolean) {
        _navigationActions.tryEmit(NavigationAction.NavigateTo(destination, popUpTo, inclusive))
    }

    override fun navigateBack() {
        _navigationActions.tryEmit(NavigationAction.NavigateBack)
    }
}
