package com.example.autoai.presentation.features.chat

import com.example.autoai.presentation.components.BottomNavItem

sealed interface AiChatEvent {
    data class OnInputChanged(val value: String) : AiChatEvent
    data object OnSendMessageClicked : AiChatEvent
    data class OnNavItemSelected(val item: BottomNavItem) : AiChatEvent
}