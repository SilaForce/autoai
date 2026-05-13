package com.example.autoai.navigation


import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.autoai.presentation.features.chat.AiChatScreen
import com.example.autoai.presentation.features.costs.CostsScreen
import com.example.autoai.presentation.features.garage.add.AddVehicleScreen
import com.example.autoai.presentation.features.garage.GarageScreen
import com.example.autoai.presentation.features.home.HomeScreen
import com.example.autoai.presentation.features.profile.ProfileScreen
import com.example.autoai.presentation.features.profile.edit.EditProfileScreen
import com.example.autoai.presentation.features.reminder.ReminderScreen
import com.example.autoai.presentation.features.settings.SettingsScreen
import org.koin.compose.koinInject

@Composable
fun AppNavGraph(
    navController: NavHostController,
    startDestination: Route,
    navigator: IAppNavigator = koinInject(),
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        authGraph()

        composable<Route.Home> {
            HomeScreen()
        }

        composable<Route.Garage> {
            GarageScreen()
        }

        composable<Route.AddVehicle> {
            AddVehicleScreen()
        }

        composable<Route.Costs> {
            CostsScreen()
        }
        composable<Route.Reminder> {
            ReminderScreen()
        }
        composable<Route.AiChat> {
            AiChatScreen()
        }
        composable<Route.Profile> {
            ProfileScreen()
        }
        composable<Route.EditProfile> {
            EditProfileScreen()
        }
        composable<Route.Settings> {
            SettingsScreen()
        }
    }

    LaunchedEffect(Unit) {
        navigator.navigationActions.collect { action ->
            when (action) {
                is NavigationAction.NavigateTo -> {
                    navController.navigate(action.destination) {
                        launchSingleTop = true
                        action.popUpTo?.let { popUpTo(it) { inclusive = action.inclusive } }
                    }
                }
                is NavigationAction.NavigateBack -> navController.popBackStack()
            }
        }
    }
}