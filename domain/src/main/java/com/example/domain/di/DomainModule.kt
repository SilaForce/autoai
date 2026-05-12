package com.example.domain.di

import com.example.domain.dispatcher.DispatcherProvider
import com.example.domain.usecase.chat.SendMessageUseCase
import com.example.domain.usecase.login.LoginUseCase
import com.example.domain.usecase.register.RegisterUseCase
import com.example.domain.usecase.session.CheckSessionUseCase
import com.example.domain.usecase.user.DeleteUserUseCase
import com.example.domain.usecase.user.GetCurrentUserUseCase
import com.example.domain.usecase.user.UpdateUserUseCase
import com.example.domain.usecase.cost.AddCostUseCase
import com.example.domain.usecase.cost.GetAllCostsByUserIdUseCase
import com.example.domain.usecase.cost.GetCostsHistoryUseCase
import com.example.domain.usecase.cost.GetCostStatisticsUseCase
import com.example.domain.usecase.reminder.AddReminderUseCase
import com.example.domain.usecase.reminder.GetRemindersUseCase
import com.example.domain.usecase.vehicle.AddVehicleUseCase
import com.example.domain.usecase.vehicle.GetVehiclesUseCase
import com.example.domain.usecase.vehicle.SetActiveVehicleUseCase
import kotlinx.coroutines.CoroutineDispatcher
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val domainModule = module {

    single<CoroutineDispatcher> { get<DispatcherProvider>().io }

    factoryOf(::RegisterUseCase)
    factoryOf(::LoginUseCase)
    factoryOf(::GetCurrentUserUseCase)
    factoryOf(::UpdateUserUseCase)
    factoryOf(::DeleteUserUseCase)
    factoryOf(::CheckSessionUseCase)
    factoryOf(::AddVehicleUseCase)
    factoryOf(::GetVehiclesUseCase)
    factoryOf(::SetActiveVehicleUseCase)
    factoryOf(::AddCostUseCase)
    factoryOf(::GetCostsHistoryUseCase)
    factoryOf(::GetCostStatisticsUseCase)
    factoryOf(::GetAllCostsByUserIdUseCase)
    factoryOf(::GetRemindersUseCase)
    factoryOf(::AddReminderUseCase)
    factoryOf(::SendMessageUseCase)
}