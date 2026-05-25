package com.example.domain.repository

import com.example.domain.model.app.AppResult
import com.example.domain.model.reminder.Reminder

interface IRemindersRepository {
    suspend fun addReminder(reminder: Reminder): AppResult<Reminder>
    suspend fun getReminders(vehicleId: String): AppResult<List<Reminder>>
    suspend fun updateReminder(reminder: Reminder): AppResult<Unit>
    suspend fun deleteReminder(reminderId: String): AppResult<Unit>
    suspend fun deleteRemindersForVehicle(vehicleId: String): AppResult<Unit>
    suspend fun deleteAllForUser(userId: String): AppResult<Unit>
    suspend fun getActiveRemindersForVehicle(vehicleId: String): AppResult<List<Reminder>>
}