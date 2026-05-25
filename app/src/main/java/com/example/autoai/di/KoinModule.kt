package com.example.autoai.di

import com.example.domain.dispatcher.DefaultDispatcherProvider
import com.example.autoai.navigation.AppNavigator
import com.example.autoai.navigation.IAppNavigator
import com.example.autoai.presentation.features.auth.login.LoginViewModel
import com.example.autoai.presentation.features.auth.register.RegisterViewModel
import com.example.autoai.presentation.features.chat.AiChatViewModel
import com.example.autoai.presentation.features.costs.CostsViewModel
import com.example.autoai.presentation.features.garage.add.AddVehicleViewModel
import com.example.autoai.presentation.features.garage.GarageViewModel
import com.example.autoai.presentation.features.home.HomeViewModel
import com.example.autoai.presentation.features.onboarding.OnboardingViewModel
import com.example.autoai.presentation.features.profile.ProfileViewModel
import com.example.autoai.presentation.features.profile.edit.EditProfileViewModel
import com.example.autoai.presentation.features.reminder.ReminderViewModel
import com.example.autoai.presentation.features.settings.SettingsViewModel
import com.example.autoai.presentation.features.splash.SplashViewModel
import com.example.domain.dispatcher.DispatcherProvider
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

val koinModule = module {

    singleOf(::DefaultDispatcherProvider) bind DispatcherProvider::class
    singleOf(::AppNavigator) bind IAppNavigator::class

    viewModelOf(::OnboardingViewModel)
    viewModelOf(::RegisterViewModel)
    viewModelOf(::LoginViewModel)
    viewModelOf(::HomeViewModel)
    viewModelOf(::GarageViewModel)
    viewModelOf(::AddVehicleViewModel)
    viewModelOf(::CostsViewModel)
    viewModelOf(::SplashViewModel)
    viewModelOf(::ReminderViewModel)
    viewModelOf(::AiChatViewModel)
    viewModelOf(::ProfileViewModel)
    viewModelOf(::EditProfileViewModel)
    viewModelOf(::SettingsViewModel)
}