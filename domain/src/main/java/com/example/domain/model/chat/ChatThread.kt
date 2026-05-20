package com.example.domain.model.chat

import java.util.UUID

data class ChatThread(
    val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val title: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = createdAt,
)
