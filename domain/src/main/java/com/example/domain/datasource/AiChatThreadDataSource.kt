package com.example.domain.datasource

import com.example.domain.model.app.AppResult
import com.example.domain.model.chat.ChatThread

interface AiChatThreadDataSource {
    suspend fun loadThreads(userId: String): AppResult<List<ChatThread>>
    suspend fun createThread(thread: ChatThread): AppResult<ChatThread>
    suspend fun updateThread(thread: ChatThread): AppResult<Unit>
    suspend fun deleteThread(threadId: String): AppResult<Unit>
    suspend fun deleteAllForUser(userId: String): AppResult<Unit>
}
