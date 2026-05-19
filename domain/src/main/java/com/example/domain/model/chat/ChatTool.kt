package com.example.domain.model.chat

enum class ChatToolParamType { STRING, LONG, INT, DOUBLE, BOOL, ENUM }

data class ChatToolParam(
    val name: String,
    val type: ChatToolParamType,
    val description: String,
    val required: Boolean = true,
    val values: List<String>? = null,
)

data class ChatTool(
    val name: String,
    val description: String,
    val parameters: List<ChatToolParam>,
    val execute: suspend (Map<String, String?>) -> String,
)
