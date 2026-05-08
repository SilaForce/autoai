package com.example.domain.repository

import com.example.domain.model.app.AppResult
import com.example.domain.model.reminder.Reminder

interface IRemindersRepository {
    suspend fun addReminder(reminder: Reminder): AppResult<Reminder>
    suspend fun getReminders(vehicleId: String): AppResult<List<Reminder>>
}