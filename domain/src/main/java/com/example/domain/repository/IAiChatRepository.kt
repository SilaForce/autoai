package com.example.domain.repository

import com.example.domain.model.app.AppResult
import com.example.domain.model.chat.ChatMessage

interface IAiChatRepository {
    suspend fun sendMessage(
        prompt: String,
        history: List<ChatMessage>,
        systemInstruction: String,
        imageBytes: ByteArray? = null,
        ): AppResult<String>
}