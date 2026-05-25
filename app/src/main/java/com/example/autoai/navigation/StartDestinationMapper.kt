package com.example.autoai.navigation

import com.example.domain.model.app.StartDestination

fun StartDestination.toRoute(): Route {
    return when (this) {
        StartDestination.Auth -> Route.AuthGraph
        StartDestination.Login -> Route.Login
        StartDestination.Home -> Route.Home
        StartDestination.Setup -> Route.AuthGraph
    }
}
