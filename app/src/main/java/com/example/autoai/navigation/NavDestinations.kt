package com.example.autoai.navigation

import kotlinx.serialization.Serializable

sealed interface Route {

    @Serializable data object AuthGraph : Route

    @Serializable data object Onboarding : Route

    @Serializable data object Register : Route

    @Serializable data object Login : Route

    @Serializable data object Home : Route

    @Serializable data object Garage : Route

    @Serializable data object AddVehicle : Route

    @Serializable data object Costs : Route
    @Serializable data object Reminder : Route

        @Serializable data object AiChat : Route

        @Serializable data object Settings : Route

        @Serializable data object Profile : Route

        @Serializable data object Help : Route
}
