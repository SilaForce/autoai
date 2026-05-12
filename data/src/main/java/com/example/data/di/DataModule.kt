package com.example.data.di

import com.example.data.repository.auth.AuthRepository
import com.example.data.repository.chat.GeminiChatRepository
import com.example.data.repository.cost.FirestoreCostRepository
import com.example.data.repository.reminder.FirestoreReminderRepository
import com.example.data.repository.vehicle.FirestoreVehicleRepository
import com.example.domain.repository.IAiChatRepository
import com.example.domain.repository.IAuthRepository
import com.example.domain.repository.ICostRepository
import com.example.domain.repository.IRemindersRepository
import com.example.domain.repository.IStorageRepository
import com.example.domain.repository.IVehicleRepository
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

    singleOf(::AuthRepository) bind IAuthRepository::class
    singleOf(::FirestoreVehicleRepository) bind IVehicleRepository::class
    singleOf(::FirestoreCostRepository) bind ICostRepository::class
    singleOf(::FirestoreReminderRepository) bind IRemindersRepository::class
    singleOf(::GeminiChatRepository) bind IAiChatRepository::class
    // singleOf(::StorageRepository) bind IStorageRepository::class
}