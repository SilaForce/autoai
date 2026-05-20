package com.example.data.model.chat

data class ChatThreadDto(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
)
