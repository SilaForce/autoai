package com.example.domain.model.app

sealed interface StartDestination {
    data object Auth : StartDestination
    data object Setup : StartDestination
    data object Home : StartDestination
}

