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
        // Dajemo mu ID koji je Firestore upravo generisao
        val reminderWithId = reminder.copy(id = documentReference.id)

        return safeFirebaseCall {
            documentReference
                .set(reminderWithId.toReminderDto())
                .await() // Kotlin coroutine funkcija za čekanje Firebase-a

            reminderWithId
        }
    }

    override suspend fun getReminders(vehicleId: String): AppResult<List<Reminder>> {
        // NAPOMENA: Ovaj upit filtrira po jednom polju i sortira po drugom.
        // Firestore će zahtijevati kompozitni indeks (Composite Index).
        return safeFirebaseCall {
            firestore.collection(REMINDERS_COLLECTION)
                .whereEqualTo(FIELD_VEHICLE_ID, vehicleId)
                // Ovdje sortiramo ASCENDING (rastuće) jer želimo da nam na vrhu budu
                // podsjetnici koji najskorije ističu!
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

    private companion object {
        const val REMINDERS_COLLECTION = "reminders"
        const val FIELD_VEHICLE_ID = "vehicleId"
        const val FIELD_DUE_DATE = "dueDateMillis"
    }
}