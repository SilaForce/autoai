package com.example.autoai.navigation

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
class AppNavigator : IAppNavigator {
    // 1. Dodajemo bafer da navigacija ne blokira ViewModel
    private val _navigationActions = MutableSharedFlow<NavigationAction>(extraBufferCapacity = 1)
    override val navigationActions = _navigationActions.asSharedFlow()

    private var lastNavigateTime = 0L
    private val debounceTime = 500L // 500ms zaštita od duplog klika

    // 2. Skidamo 'suspend' jer koristimo tryEmit
    override fun navigateTo(destination: Any, popUpTo: Any?, inclusive: Boolean) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastNavigateTime >= debounceTime) {
            lastNavigateTime = currentTime
            _navigationActions.tryEmit(NavigationAction.NavigateTo(destination, popUpTo, inclusive))
        }
    }

    // Skidamo 'suspend'
    override fun navigateBack() {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastNavigateTime >= debounceTime) {
            lastNavigateTime = currentTime
            _navigationActions.tryEmit(NavigationAction.NavigateBack)
        }
    }
}