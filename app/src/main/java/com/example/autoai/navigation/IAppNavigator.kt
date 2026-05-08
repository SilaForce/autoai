package com.example.autoai.navigation

import kotlinx.coroutines.flow.SharedFlow

interface IAppNavigator {
    val navigationActions: SharedFlow<NavigationAction>
     fun navigateTo(destination: Any, popUpTo: Any? = null, inclusive: Boolean = false)
     fun navigateBack()
}

sealed class NavigationAction {
    data class NavigateTo(
        val destination: Any,
        val popUpTo: Any? = null,
        val inclusive: Boolean = false
    ) : NavigationAction()

    data object NavigateBack : NavigationAction()
}