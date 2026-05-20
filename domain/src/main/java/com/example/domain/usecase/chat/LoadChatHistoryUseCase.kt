package com.example.domain.usecase.chat

import com.example.domain.base.BaseUseCase
import com.example.domain.model.app.AppResult
import com.example.domain.model.app.DataError
import com.example.domain.model.chat.ChatMessage
import com.example.domain.repository.IAiChatHistoryRepository
import kotlinx.coroutines.CoroutineDispatcher

data class LoadChatHistoryParams(
    val userId: String,
    val threadId: String,
    val limit: Int = 100,
)

class LoadChatHistoryUseCase(
    private val repository: IAiChatHistoryRepository,
    dispatcher: CoroutineDispatcher,
) : BaseUseCase<LoadChatHistoryParams, List<ChatMessage>>(dispatcher) {

    override suspend fun execute(params: LoadChatHistoryParams): AppResult<List<ChatMessage>> {
        if (params.userId.isBlank() || params.threadId.isBlank()) {
            return AppResult.Failure(DataError.Local.Validation.Generic)
        }
        return repository.loadHistory(
            userId = params.userId.trim(),
            threadId = params.threadId.trim(),
            limit = params.limit,
        )
    }
}
