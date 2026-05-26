package com.example.domain.usecase.chat

import com.example.domain.base.BaseUseCase
import com.example.domain.model.app.AppResult
import com.example.domain.model.app.DataError
import com.example.domain.model.app.andThen
import com.example.domain.datasource.AiChatHistoryDataSource
import com.example.domain.datasource.AiChatThreadDataSource
import kotlinx.coroutines.CoroutineDispatcher

data class DeleteChatThreadParams(
    val threadId: String,
    val userId: String,
)

class DeleteChatThreadUseCase(
    private val threadDataSource: AiChatThreadDataSource,
    private val historyDataSource: AiChatHistoryDataSource,
    dispatcher: CoroutineDispatcher,
) : BaseUseCase<DeleteChatThreadParams, Unit>(dispatcher) {

    override suspend fun execute(params: DeleteChatThreadParams): AppResult<Unit> {
        if (params.threadId.isBlank() || params.userId.isBlank()) {
            return AppResult.Failure(DataError.Local.Validation.Generic)
        }
        return historyDataSource.deleteMessagesForThread(params.userId, params.threadId)
            .andThen { threadDataSource.deleteThread(params.threadId) }
    }
}
