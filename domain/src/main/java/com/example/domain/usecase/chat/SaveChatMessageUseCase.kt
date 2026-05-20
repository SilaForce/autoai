package com.example.domain.usecase.chat

import com.example.domain.base.BaseUseCase
import com.example.domain.model.app.AppResult
import com.example.domain.model.app.DataError
import com.example.domain.model.chat.ChatMessage
import com.example.domain.repository.IAiChatHistoryRepository
import kotlinx.coroutines.CoroutineDispatcher

data class SaveChatMessageParams(
    val message: ChatMessage,
    val userId: String,
)

class SaveChatMessageUseCase(
    private val repository: IAiChatHistoryRepository,
    dispatcher: CoroutineDispatcher,
) : BaseUseCase<SaveChatMessageParams, Unit>(dispatcher) {

    override suspend fun execute(params: SaveChatMessageParams): AppResult<Unit> {
        if (params.userId.isBlank() || params.message.text.isBlank()) {
            return AppResult.Failure(DataError.Local.Validation.Generic)
        }
        return repository.saveMessage(message = params.message, userId = params.userId.trim())
    }
}
