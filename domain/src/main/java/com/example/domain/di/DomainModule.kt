package com.example.domain.di

import com.example.domain.dispatcher.DispatcherProvider
import com.example.domain.usecase.chat.CreateChatThreadUseCase
import com.example.domain.usecase.chat.DeleteChatThreadUseCase
import com.example.domain.usecase.chat.LoadChatHistoryUseCase
import com.example.domain.usecase.chat.LoadChatThreadsUseCase
import com.example.domain.usecase.chat.SaveChatMessageUseCase
import com.example.domain.usecase.chat.SendMessageUseCase
import com.example.domain.usecase.chat.UpdateChatThreadUseCase
import com.example.domain.usecase.login.LoginUseCase
import com.example.domain.usecase.register.RegisterUseCase
import com.example.domain.usecase.session.CheckSessionUseCase
import com.example.domain.usecase.user.DeleteUserUseCase
import com.example.domain.usecase.user.GetCurrentUserUseCase
import com.example.domain.usecase.user.UpdateUserUseCase
import com.example.domain.usecase.cost.AddCostUseCase
import com.example.domain.usecase.cost.DeleteCostUseCase
import com.example.domain.usecase.cost.GetAllCostsByUserIdUseCase
import com.example.domain.usecase.cost.GetCostsHistoryUseCase
import com.example.domain.usecase.cost.GetCostStatisticsForPeriodUseCase
import com.example.domain.usecase.cost.GetCostStatisticsUseCase
import com.example.domain.usecase.cost.UpdateCostUseCase
import com.example.domain.usecase.reminder.AddReminderUseCase
import com.example.domain.usecase.reminder.DeleteReminderUseCase
import com.example.domain.usecase.reminder.GetRemindersUseCase
import com.example.domain.usecase.reminder.UpdateReminderUseCase
import com.example.domain.usecase.user.LogoutUseCase
import com.example.domain.usecase.vehicle.AddVehicleUseCase
import com.example.domain.usecase.vehicle.DeleteVehicleUseCase
import com.example.domain.usecase.vehicle.GetCarMakesUseCase
import com.example.domain.usecase.vehicle.GetModelsForMakeUseCase
import com.example.domain.usecase.vehicle.GetVehicleByIdUseCase
import com.example.domain.usecase.vehicle.GetVehiclesUseCase
import com.example.domain.usecase.vehicle.ObserveActiveVehicleUseCase
import com.example.domain.usecase.vehicle.SetActiveVehicleUseCase
import com.example.domain.usecase.vehicle.UpdateVehicleUseCase
import kotlinx.coroutines.CoroutineDispatcher
import org.koin.core.module.dsl.factoryOf
import org.koin.core.qualifier.named
import org.koin.dsl.module

val domainModule = module {

    // Default CoroutineDispatcher for BaseUseCase — IO is the right default since most
    // use cases hit Firestore. For CPU-bound work (image compression, JSON parsing,
    // heavy mapping), inject DispatcherProvider directly and pick .default or .main.
    // The named qualifiers below are available for use cases that want to be explicit.
    single<CoroutineDispatcher> { get<DispatcherProvider>().io }
    single<CoroutineDispatcher>(named("io")) { get<DispatcherProvider>().io }
    single<CoroutineDispatcher>(named("default")) { get<DispatcherProvider>().default }
    single<CoroutineDispatcher>(named("main")) { get<DispatcherProvider>().main }

    factoryOf(::RegisterUseCase)
    factoryOf(::LoginUseCase)
    factoryOf(::GetCurrentUserUseCase)
    factoryOf(::UpdateUserUseCase)
    factoryOf(::DeleteUserUseCase)
    factoryOf(::CheckSessionUseCase)
    factoryOf(::AddVehicleUseCase)
    factoryOf(::GetVehiclesUseCase)
    factoryOf(::GetVehicleByIdUseCase)
    factoryOf(::UpdateVehicleUseCase)
    factoryOf(::DeleteVehicleUseCase)
    factoryOf(::SetActiveVehicleUseCase)
    factoryOf(::ObserveActiveVehicleUseCase)
    factoryOf(::AddCostUseCase)
    factoryOf(::UpdateCostUseCase)
    factoryOf(::DeleteCostUseCase)
    factoryOf(::GetCostsHistoryUseCase)
    factoryOf(::GetCostStatisticsUseCase)
    factoryOf(::GetCostStatisticsForPeriodUseCase)
    factoryOf(::GetAllCostsByUserIdUseCase)
    factoryOf(::GetRemindersUseCase)
    factoryOf(::AddReminderUseCase)
    factoryOf(::UpdateReminderUseCase)
    factoryOf(::DeleteReminderUseCase)
    factoryOf(::SendMessageUseCase)
    factoryOf(::LoadChatHistoryUseCase)
    factoryOf(::SaveChatMessageUseCase)
    factoryOf(::LoadChatThreadsUseCase)
    factoryOf(::CreateChatThreadUseCase)
    factoryOf(::UpdateChatThreadUseCase)
    factoryOf(::DeleteChatThreadUseCase)
    factoryOf(::LogoutUseCase)
    factoryOf(::GetCarMakesUseCase)
    factoryOf(::GetModelsForMakeUseCase)
}