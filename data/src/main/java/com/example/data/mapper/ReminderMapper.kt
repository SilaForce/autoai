package com.example.data.mapper

import com.example.data.model.reminder.ReminderDto
import com.example.domain.model.app.AppResult
import com.example.domain.model.app.DataError
import com.example.domain.model.reminder.Reminder

fun ReminderDto.toReminder(): AppResult<Reminder> {
   if (vehicleId.isBlank() || title.isBlank()) {
       return AppResult.Failure(DataError.Network.Serialization)
   }
    return AppResult.Success(
        Reminder(
            id = id,
            userId = userId,
            vehicleId = vehicleId,
            title = title,
            dueDateMillis = dueDateMillis,
            note = note,
            isCompleted = isCompleted
        )
    )
}

fun Reminder.toReminderDto(): ReminderDto {
    return ReminderDto(
        id = id,
        userId = userId,
        vehicleId = vehicleId,
        title = title,
        dueDateMillis = dueDateMillis,
        note = note,
        isCompleted = isCompleted
    )
}