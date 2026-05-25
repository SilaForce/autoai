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
            // Images are intentionally NOT persisted. The project is on Firebase Spark
            // (no Cloud Storage), and inlining base64 in Firestore docs hits the 1 MB
            // doc limit fast — a single 1MB chat photo (after compression) plus thread
            // history is already in the danger zone. Behavior: a user attaches a photo,
            // gets an AI reply, then on reopening the thread the image is gone (only the
            // text remains). This is documented UX, not a bug. Revisit if/when Storage
            // is enabled — see ChatMessageDto for where to add an imageUrl field.
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
