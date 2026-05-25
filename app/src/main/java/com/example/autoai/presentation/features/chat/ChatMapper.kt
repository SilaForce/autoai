package com.example.autoai.presentation.features.chat

import com.example.autoai.presentation.util.ImagePayload
import com.example.domain.model.chat.ChatMessage
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private fun timeFormatter() = SimpleDateFormat("HH:mm", Locale.getDefault())

fun ChatMessage.toUiModel(): ChatMessageUi {
    return ChatMessageUi(
        id = id,
        text = text,
        role = role,
        timestamp = timestamp,
        formattedTime = timeFormatter().format(Date(timestamp)),
        threadId = threadId,
        images = images.map { ImagePayload(it) },
    )
}

fun ChatMessageUi.toDomainModel(): ChatMessage {
    return ChatMessage(
        id = id,
        text = text,
        role = role,
        timestamp = timestamp,
        images = images.map { it.bytes },
        threadId = threadId,
    )
}
