package com.example.domain.usecase.chat

import com.example.domain.base.BaseUseCase
import com.example.domain.model.app.AppResult
import com.example.domain.model.app.DataError
import com.example.domain.model.chat.ChatMessage
import com.example.domain.model.chat.ChatTool
import com.example.domain.datasource.AiChatDataSource
import kotlinx.coroutines.CoroutineDispatcher

data class SendMessageParams(
    val prompt: String,
    val history: List<ChatMessage>,
    val systemInstruction: String,
    val images: List<ByteArray> = emptyList(),
    val tools: List<ChatTool> = emptyList(),
)

class SendMessageUseCase(
    private val repository: AiChatDataSource,
    dispatcher: CoroutineDispatcher
) : BaseUseCase<SendMessageParams, String>(dispatcher) {

    override suspend fun execute(params: SendMessageParams): AppResult<String> {
        if (params.prompt.isBlank()) {
            return AppResult.Failure(DataError.Local.Validation.Generic)
        }

        return repository.sendMessage(
            prompt = params.prompt.trim(),
            history = params.history,
            systemInstruction = params.systemInstruction,
            images = params.images,
            tools = params.tools,
        )
    }
}