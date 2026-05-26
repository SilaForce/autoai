package com.example.domain.usecase.reminder

import com.example.domain.base.BaseUseCase
import com.example.domain.model.app.AppResult
import com.example.domain.model.app.DataError
import com.example.domain.model.reminder.Reminder
import com.example.domain.datasource.RemindersDataSource
import kotlinx.coroutines.CoroutineDispatcher

data class GetRemindersParams(val vehicleId: String)

class GetRemindersUseCase(
    private val repository: RemindersDataSource,
    dispatcher: CoroutineDispatcher
) : BaseUseCase<GetRemindersParams, List<Reminder>>(dispatcher) {

    override suspend fun execute(params: GetRemindersParams): AppResult<List<Reminder>> {
        if (params.vehicleId.isBlank()) {
            return AppResult.Failure(DataError.Local.Validation.Generic)
        }
        return repository.getReminders(params.vehicleId.trim())
    }
}