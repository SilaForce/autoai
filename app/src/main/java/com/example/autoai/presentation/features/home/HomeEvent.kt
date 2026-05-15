package com.example.autoai.presentation.features.home

import com.example.autoai.presentation.components.BottomNavItem
import com.example.autoai.presentation.util.UiText

sealed interface HomeEvent {
    data object OnVehicleClicked : HomeEvent
    data object OnFuelClicked : HomeEvent
    data object OnServiceClicked : HomeEvent
    data object OnAiClicked : HomeEvent
    data object OnProfileClicked : HomeEvent
    data class OnNavItemSelected(val item: BottomNavItem) : HomeEvent
    data object OnScreenResumed : HomeEvent
    data object OnDueReminderClicked : HomeEvent
}

// No side effects yet — will be populated as features are added
sealed interface HomeSideEffect {
    data class ShowError(val message: UiText) : HomeSideEffect
}
