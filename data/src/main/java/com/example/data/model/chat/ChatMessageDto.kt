package com.example.data.model.chat

data class ChatMessageDto(
    val id: String = "",
    val userId: String = "",
    val threadId: String = "",
    val text: String = "",
    val role: String = "",
    val timestamp: Long = 0L,
)
