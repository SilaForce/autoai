package com.example.data.mapper

import com.example.data.model.chat.ChatMessageDto
import com.example.domain.model.app.AppResult
import com.example.domain.model.app.DataError
import com.example.domain.model.chat.ChatMessage
import com.example.domain.model.chat.MessageRole

fun ChatMessageDto.toChatMessage(): AppResult<ChatMessage> {
    val parsedRole = runCatching {
        MessageRole.valueOf(role)
    }.getOrElse {
        return AppResult.Failure(DataError.Network.Serialization)
    }

    if (text.isBlank()) {
        return AppResult.Failure(DataError.Network.Serialization)
    }

    return AppResult.Success(
        ChatMessage(
            id = id,
            text = text,
            role = parsedRole,
            timestamp = timestamp,
            images = emptyList(),
            threadId = threadId,
        )
    )
}

fun ChatMessage.toChatMessageDto(userId: String): ChatMessageDto {
    return ChatMessageDto(
        id = id,
        userId = userId,
        threadId = threadId,
        text = text,
        role = role.name,
        timestamp = timestamp,
    )
}
