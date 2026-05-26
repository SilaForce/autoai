package com.example.data.di

import com.example.data.datasource.chat.FirestoreChatHistoryDataSource
import com.example.data.datasource.chat.FirestoreChatThreadDataSource
import com.example.data.datasource.cost.FirestoreCostDataSource
import com.example.data.datasource.reminder.FirestoreReminderDataSource
import com.example.data.datasource.remote.util.HttpClientFactory
import com.example.data.datasource.vehicle.FirestoreVehicleDataSource
import com.example.data.repository.auth.AuthRepository
import com.example.data.repository.chat.GeminiChatRepository
import com.example.data.repository.preferences.PreferencesRepository
import com.example.data.repository.vehicle.NhtsaVehicleMakesRepository
import com.example.domain.datasource.AiChatHistoryDataSource
import com.example.domain.repository.IAiChatRepository
import com.example.domain.datasource.AiChatThreadDataSource
import com.example.domain.repository.IAuthRepository
import com.example.domain.datasource.CostDataSource
import com.example.domain.repository.IPreferencesRepository
import com.example.domain.datasource.RemindersDataSource
import com.example.domain.datasource.VehicleDataSource
import com.example.domain.repository.IVehicleMakesRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val dataModule = module {

    single { FirebaseAuth.getInstance() }

    single { FirebaseFirestore.getInstance() }

    single { FirebaseStorage.getInstance() }

    single { HttpClientFactory.create() }

    singleOf(::AuthRepository) bind IAuthRepository::class
    singleOf(::FirestoreVehicleDataSource) bind VehicleDataSource::class
    singleOf(::FirestoreCostDataSource) bind CostDataSource::class
    singleOf(::FirestoreReminderDataSource) bind RemindersDataSource::class
    singleOf(::GeminiChatRepository) bind IAiChatRepository::class
    singleOf(::FirestoreChatHistoryDataSource) bind AiChatHistoryDataSource::class
    singleOf(::FirestoreChatThreadDataSource) bind AiChatThreadDataSource::class
    singleOf(::PreferencesRepository) bind IPreferencesRepository::class
    singleOf(::NhtsaVehicleMakesRepository) bind IVehicleMakesRepository::class
}