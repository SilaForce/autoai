package com.example.domain.usecase.chat

import com.example.domain.base.BaseUseCase
import com.example.domain.model.app.AppResult
import com.example.domain.model.app.DataError
import com.example.domain.model.chat.ChatMessage
import com.example.domain.repository.IAiChatRepository
import kotlinx.coroutines.CoroutineDispatcher

data class SendMessageParams(
    val prompt: String,
    val history: List<ChatMessage>,
    val systemInstruction: String,
    val images: List<ByteArray> = emptyList()
)

class SendMessageUseCase(
    private val repository: IAiChatRepository,
    dispatcher: CoroutineDispatcher
) : BaseUseCase<SendMessageParams, String>(dispatcher) {

    override suspend fun execute(params: SendMessageParams): AppResult<String> {
        // Validacija: ne dozvoljavamo slanje praznih poruka!
        if (params.prompt.isBlank()) {
            return AppResult.Failure(DataError.Local.ValidationError)
        }

        return repository.sendMessage(
            prompt = params.prompt.trim(),
            history = params.history,
            systemInstruction = params.systemInstruction,
            images = params.images
        )
    }
}