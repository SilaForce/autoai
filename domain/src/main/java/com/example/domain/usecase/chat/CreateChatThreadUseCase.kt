package com.example.domain.usecase.chat

import com.example.domain.base.BaseUseCase
import com.example.domain.model.app.AppResult
import com.example.domain.model.app.DataError
import com.example.domain.model.chat.ChatThread
import com.example.domain.repository.IAiChatThreadRepository
import kotlinx.coroutines.CoroutineDispatcher

data class CreateChatThreadParams(
    val thread: ChatThread,
)

class CreateChatThreadUseCase(
    private val repository: IAiChatThreadRepository,
    dispatcher: CoroutineDispatcher,
) : BaseUseCase<CreateChatThreadParams, ChatThread>(dispatcher) {

    override suspend fun execute(params: CreateChatThreadParams): AppResult<ChatThread> {
        if (params.thread.userId.isBlank() || params.thread.title.isBlank()) {
            return AppResult.Failure(DataError.Local.Validation.Generic)
        }
        return repository.createThread(params.thread)
    }
}
