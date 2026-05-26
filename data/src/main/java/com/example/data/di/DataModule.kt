package com.example.data.di

import com.example.data.datasource.chat.FirestoreChatHistoryDataSource
import com.example.data.datasource.chat.FirestoreChatThreadDataSource
import com.example.data.datasource.cost.FirestoreCostDataSource
import com.example.data.datasource.reminder.FirestoreReminderDataSource
import com.example.data.datasource.remote.util.HttpClientFactory
import com.example.data.datasource.vehicle.FirestoreVehicleDataSource
import com.example.data.repository.auth.FirebaseAuthRepository
import com.example.data.datasource.chat.GeminiChatDataSource
import com.example.data.datasource.preferences.DataStorePreferencesDataSource
import com.example.data.datasource.vehicle.NhtsaVehicleMakesDataSource
import com.example.domain.datasource.AiChatHistoryDataSource
import com.example.domain.datasource.AiChatDataSource
import com.example.domain.datasource.AiChatThreadDataSource
import com.example.domain.repository.AuthRepository
import com.example.domain.datasource.CostDataSource
import com.example.domain.datasource.PreferencesDataSource
import com.example.domain.datasource.RemindersDataSource
import com.example.domain.datasource.VehicleDataSource
import com.example.domain.datasource.VehicleMakesDataSource
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

    singleOf(::FirebaseAuthRepository) bind AuthRepository::class
    singleOf(::FirestoreVehicleDataSource) bind VehicleDataSource::class
    singleOf(::FirestoreCostDataSource) bind CostDataSource::class
    singleOf(::FirestoreReminderDataSource) bind RemindersDataSource::class
    singleOf(::GeminiChatDataSource) bind AiChatDataSource::class
    singleOf(::FirestoreChatHistoryDataSource) bind AiChatHistoryDataSource::class
    singleOf(::FirestoreChatThreadDataSource) bind AiChatThreadDataSource::class
    singleOf(::DataStorePreferencesDataSource) bind PreferencesDataSource::class
    singleOf(::NhtsaVehicleMakesDataSource) bind VehicleMakesDataSource::class
}