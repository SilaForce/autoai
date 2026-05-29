package com.example.autoai.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.example.autoai.presentation.features.auth.login.LoginScreen
import com.example.autoai.presentation.features.auth.register.RegisterScreen
import com.example.autoai.presentation.features.onboarding.OnboardingScreen

fun NavGraphBuilder.authGraph(startDestination: Route = Route.Onboarding) {
    navigation<Route.AuthGraph>(startDestination = startDestination) {
        composable<Route.Onboarding> {
            OnboardingScreen()
        }
        composable<Route.Register> {
            RegisterScreen()
        }
        composable<Route.Login> {
            LoginScreen()
        }
    }
}