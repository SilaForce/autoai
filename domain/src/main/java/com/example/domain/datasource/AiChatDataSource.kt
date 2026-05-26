package com.example.domain.datasource

import com.example.domain.model.app.AppResult
import com.example.domain.model.chat.ChatMessage
import com.example.domain.model.chat.ChatTool

interface AiChatDataSource {
    suspend fun sendMessage(
        prompt: String,
        history: List<ChatMessage>,
        systemInstruction: String,
        images: List<ByteArray> = emptyList(),
        tools: List<ChatTool> = emptyList(),
    ): AppResult<String>
}
