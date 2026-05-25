package com.example.data.repository.reminder

import com.example.data.datasource.remote.util.safeFirebaseCall
import com.example.data.mapper.toReminder
import com.example.data.mapper.toReminderDto
import com.example.data.model.reminder.ReminderDto
import com.example.domain.model.app.AppResult
import com.example.domain.model.app.DataError
import com.example.domain.model.app.andThen
import com.example.domain.model.reminder.Reminder
import com.example.domain.repository.IRemindersRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class FirestoreReminderRepository(
    private val firestore: FirebaseFirestore
): IRemindersRepository {

    override suspend fun addReminder(reminder: Reminder): AppResult<Reminder> {
        val documentReference = firestore.collection(REMINDERS_COLLECTION).document()
        val reminderWithId = reminder.copy(id = documentReference.id)

        return safeFirebaseCall {
            documentReference
                .set(reminderWithId.toReminderDto())
                .await()

            reminderWithId
        }
    }

    override suspend fun getReminders(vehicleId: String): AppResult<List<Reminder>> {
        // whereEqualTo + orderBy on a different field needs a composite index
        // (configured in firestore.indexes.json). Ascending by due date so the
        // soonest-due reminders surface at the top.
        return safeFirebaseCall {
            firestore.collection(REMINDERS_COLLECTION)
                .whereEqualTo(FIELD_VEHICLE_ID, vehicleId)
                .orderBy(FIELD_DUE_DATE, Query.Direction.ASCENDING)
                .get()
                .await()
        }.andThen { querySnapshot ->
            val reminders = mutableListOf<Reminder>()

            for (document in querySnapshot.documents) {
                val dto = document.toObject(ReminderDto::class.java)
                    ?.let { currentDto ->
                        if (currentDto.id.isBlank()) currentDto.copy(id = document.id)
                        else currentDto
                    } ?: return@andThen AppResult.Failure(DataError.Network.Serialization)

                when (val result = dto.toReminder()) {
                    is AppResult.Success -> reminders.add(result.data)
                    is AppResult.Failure -> return@andThen AppResult.Failure(result.error)
                }
            }

            AppResult.Success(reminders)
        }
    }

    override suspend fun updateReminder(reminder: Reminder): AppResult<Unit> {
        return safeFirebaseCall {
            firestore.collection(REMINDERS_COLLECTION)
                .document(reminder.id)
                .set(reminder.toReminderDto())
                .await()
        }
    }

    override suspend fun deleteReminder(reminderId: String): AppResult<Unit> {
        return safeFirebaseCall {
            firestore.collection(REMINDERS_COLLECTION)
                .document(reminderId)
                .delete()
                .await()
        }
    }

    override suspend fun deleteRemindersForVehicle(vehicleId: String): AppResult<Unit> {
        return deleteWhere(FIELD_VEHICLE_ID, vehicleId)
    }

    override suspend fun deleteAllForUser(userId: String): AppResult<Unit> {
        return deleteWhere(FIELD_USER_ID, userId)
    }

    private suspend fun deleteWhere(field: String, value: String): AppResult<Unit> {
        return safeFirebaseCall {
            firestore.collection(REMINDERS_COLLECTION)
                .whereEqualTo(field, value)
                .get()
                .await()
        }.andThen { querySnapshot ->
            safeFirebaseCall {
                // Firestore WriteBatch caps at 500 ops; chunk for safety.
                querySnapshot.documents.chunked(BATCH_LIMIT).forEach { chunk ->
                    val batch = firestore.batch()
                    chunk.forEach { document -> batch.delete(document.reference) }
                    batch.commit().await()
                }
                Unit
            }
        }
    }

    override suspend fun getActiveRemindersForVehicle(vehicleId: String): AppResult<List<Reminder>> {
        // Query by vehicleId only — filter isCompleted in memory
        // because old documents may not have the isCompleted field at all,
        // and Firestore's whereEqualTo won't match missing fields.
        return safeFirebaseCall {
            firestore.collection(REMINDERS_COLLECTION)
                .whereEqualTo(FIELD_VEHICLE_ID, vehicleId)
                .get()
                .await()
        }.andThen { querySnapshot ->
            val reminders = mutableListOf<Reminder>()

            for (document in querySnapshot.documents) {
                val dto = document.toObject(ReminderDto::class.java)
                    ?.let { currentDto ->
                        if (currentDto.id.isBlank()) currentDto.copy(id = document.id)
                        else currentDto
                    } ?: continue

                when (val result = dto.toReminder()) {
                    is AppResult.Success -> {
                        if (!result.data.isCompleted) {
                            reminders.add(result.data)
                        }
                    }
                    is AppResult.Failure -> continue
                }
            }

            AppResult.Success(reminders)
        }
    }

    private companion object {
        const val REMINDERS_COLLECTION = "reminders"
        const val FIELD_VEHICLE_ID = "vehicleId"
        const val FIELD_USER_ID = "userId"
        const val FIELD_DUE_DATE = "dueDateMillis"
        const val BATCH_LIMIT = 500
    }
}