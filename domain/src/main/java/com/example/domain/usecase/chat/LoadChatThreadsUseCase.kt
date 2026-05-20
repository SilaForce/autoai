package com.example.domain.usecase.chat

import com.example.domain.base.BaseUseCase
import com.example.domain.model.app.AppResult
import com.example.domain.model.app.DataError
import com.example.domain.model.chat.ChatThread
import com.example.domain.repository.IAiChatThreadRepository
import kotlinx.coroutines.CoroutineDispatcher

data class LoadChatThreadsParams(
    val userId: String,
)

class LoadChatThreadsUseCase(
    private val repository: IAiChatThreadRepository,
    dispatcher: CoroutineDispatcher,
) : BaseUseCase<LoadChatThreadsParams, List<ChatThread>>(dispatcher) {

    override suspend fun execute(params: LoadChatThreadsParams): AppResult<List<ChatThread>> {
        if (params.userId.isBlank()) {
            return AppResult.Failure(DataError.Local.Validation.Generic)
        }
        return repository.loadThreads(userId = params.userId.trim())
    }
}
