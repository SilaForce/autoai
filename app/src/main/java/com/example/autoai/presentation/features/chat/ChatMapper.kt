package com.example.autoai.presentation.features.chat

import com.example.domain.model.chat.ChatMessage
import com.example.domain.model.chat.MessageRole
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private fun timeFormatter() = SimpleDateFormat("HH:mm", Locale.getDefault())

fun ChatMessage.toUiModel(): ChatMessageUi {
    return ChatMessageUi(
        id = id,
        text = text,
        isFromUser = role == MessageRole.USER,
        formattedTime = timeFormatter().format(Date(timestamp)),
        imageBytes = imageBytes
    )
}