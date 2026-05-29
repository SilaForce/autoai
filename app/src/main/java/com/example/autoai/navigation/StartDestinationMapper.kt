package com.example.autoai.navigation

import com.example.domain.model.app.StartDestination

data class ResolvedStart(val host: Route, val authStart: Route = Route.Onboarding)

fun StartDestination.resolve(): ResolvedStart = when (this) {
    StartDestination.Auth -> ResolvedStart(Route.AuthGraph, Route.Onboarding)
    StartDestination.Setup -> ResolvedStart(Route.AuthGraph, Route.Onboarding)
    StartDestination.Login -> ResolvedStart(Route.AuthGraph, Route.Login)
    StartDestination.Home -> ResolvedStart(Route.Home)
}
