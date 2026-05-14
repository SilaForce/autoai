package com.example.autoai.presentation.features.chat

data class ChatMessageUi(
    val id: String,
    val text: String,
    val isFromUser: Boolean,
    val formattedTime: String,
    val images: List<ByteArray> = emptyList()
)