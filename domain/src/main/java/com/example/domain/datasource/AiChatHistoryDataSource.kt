package com.example.domain.datasource

import com.example.domain.model.app.AppResult
import com.example.domain.model.chat.ChatMessage

interface AiChatHistoryDataSource {
    suspend fun loadHistory(
        userId: String,
        threadId: String,
        limit: Int = 100,
    ): AppResult<List<ChatMessage>>

    suspend fun saveMessage(message: ChatMessage, userId: String): AppResult<Unit>

    suspend fun deleteMessagesForThread(userId: String, threadId: String): AppResult<Unit>

    suspend fun deleteAllForUser(userId: String): AppResult<Unit>
}
