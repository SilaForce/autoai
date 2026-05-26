package com.example.domain.usecase.reminder

import com.example.domain.base.BaseUseCase
import com.example.domain.model.app.AppResult
import com.example.domain.model.app.DataError
import com.example.domain.model.reminder.Reminder
import com.example.domain.datasource.RemindersDataSource
import kotlinx.coroutines.CoroutineDispatcher

class UpdateReminderUseCase(
    private val repository: RemindersDataSource,
    dispatcher: CoroutineDispatcher
) : BaseUseCase<Reminder, Unit>(dispatcher) {

    override suspend fun execute(params: Reminder): AppResult<Unit> {
        if (params.id.isBlank()) {
            return AppResult.Failure(DataError.Local.Validation.Generic)
        }
        if (params.title.isBlank()) {
            return AppResult.Failure(DataError.Local.Validation.FieldEmpty)
        }
        if (params.dueDateMillis <= 0L) {
            return AppResult.Failure(DataError.Local.Validation.InvalidDate)
        }
        return repository.updateReminder(params)
    }
}
