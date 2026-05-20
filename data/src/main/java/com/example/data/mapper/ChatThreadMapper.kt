package com.example.data.mapper

import com.example.data.model.chat.ChatThreadDto
import com.example.domain.model.app.AppResult
import com.example.domain.model.app.DataError
import com.example.domain.model.chat.ChatThread

fun ChatThreadDto.toChatThread(): AppResult<ChatThread> {
    if (id.isBlank() || userId.isBlank()) {
        return AppResult.Failure(DataError.Network.Serialization)
    }
    return AppResult.Success(
        ChatThread(
            id = id,
            userId = userId,
            title = title.ifBlank { "New chat" },
            createdAt = createdAt,
            updatedAt = updatedAt.takeIf { it > 0 } ?: createdAt,
        )
    )
}

fun ChatThread.toChatThreadDto(): ChatThreadDto {
    return ChatThreadDto(
        id = id,
        userId = userId,
        title = title,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}
