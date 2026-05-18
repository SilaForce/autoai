package com.example.domain.usecase.reminder

import com.example.domain.base.BaseUseCase
import com.example.domain.model.app.AppResult
import com.example.domain.model.app.DataError
import com.example.domain.repository.IRemindersRepository
import kotlinx.coroutines.CoroutineDispatcher

data class DeleteReminderParams(val reminderId: String)

class DeleteReminderUseCase(
    private val repository: IRemindersRepository,
    dispatcher: CoroutineDispatcher
) : BaseUseCase<DeleteReminderParams, Unit>(dispatcher) {

    override suspend fun execute(params: DeleteReminderParams): AppResult<Unit> {
        if (params.reminderId.isBlank()) {
            return AppResult.Failure(DataError.Local.Validation.Generic)
        }
        return repository.deleteReminder(params.reminderId)
    }
}
