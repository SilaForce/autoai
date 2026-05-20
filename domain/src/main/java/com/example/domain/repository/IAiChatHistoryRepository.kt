package com.example.domain.repository

import com.example.domain.model.app.AppResult
import com.example.domain.model.chat.ChatMessage

interface IAiChatHistoryRepository {
    suspend fun loadHistory(
        userId: String,
        threadId: String,
        limit: Int = 100,
    ): AppResult<List<ChatMessage>>

    suspend fun saveMessage(message: ChatMessage, userId: String): AppResult<Unit>

    suspend fun deleteMessagesForThread(userId: String, threadId: String): AppResult<Unit>
}
