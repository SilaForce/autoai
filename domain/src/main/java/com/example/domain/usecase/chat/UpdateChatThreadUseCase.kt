package com.example.domain.usecase.chat

import com.example.domain.base.BaseUseCase
import com.example.domain.model.app.AppResult
import com.example.domain.model.app.DataError
import com.example.domain.model.chat.ChatThread
import com.example.domain.repository.IAiChatThreadRepository
import kotlinx.coroutines.CoroutineDispatcher

data class UpdateChatThreadParams(
    val thread: ChatThread,
)

class UpdateChatThreadUseCase(
    private val repository: IAiChatThreadRepository,
    dispatcher: CoroutineDispatcher,
) : BaseUseCase<UpdateChatThreadParams, Unit>(dispatcher) {

    override suspend fun execute(params: UpdateChatThreadParams): AppResult<Unit> {
        if (params.thread.id.isBlank() || params.thread.userId.isBlank()) {
            return AppResult.Failure(DataError.Local.Validation.Generic)
        }
        return repository.updateThread(params.thread)
    }
}
