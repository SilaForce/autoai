package com.example.autoai.presentation.features.chat

import androidx.compose.runtime.Stable
import com.example.autoai.presentation.components.BottomNavItem
import com.example.autoai.presentation.util.ImagePayload

@Stable
data class AiChatState(
    val messages: List<ChatMessageUi> = emptyList(),
    val inputText: String = "",
    val isAiTyping: Boolean = false,
    val selectedNavItem: BottomNavItem = BottomNavItem.AI_CHAT,
    val selectedImages: List<ImagePayload> = emptyList(),
    val threads: List<ChatThreadUi> = emptyList(),
    val currentThreadId: String? = null,
    val currentThreadTitle: String? = null,
    val threadMenuAnchorId: String? = null,
    val pendingDeleteThreadId: String? = null,
)
