package com.example.autoai.presentation.features.chat

import com.example.autoai.presentation.components.BottomNavItem

sealed interface AiChatEvent {
    data class OnInputChanged(val value: String) : AiChatEvent
    data class OnImageSelected(val imageBytes: ByteArray) : AiChatEvent
    data class OnRemoveImage(val index: Int) : AiChatEvent
    data object OnSendMessageClicked : AiChatEvent
    data class OnNavItemSelected(val item: BottomNavItem) : AiChatEvent
    data class OnSelectThread(val threadId: String) : AiChatEvent
    data object OnStartNewChat : AiChatEvent
    data class OnLongPressThread(val threadId: String) : AiChatEvent
    data object OnDismissThreadMenu : AiChatEvent
    data class OnDeleteThreadClicked(val threadId: String) : AiChatEvent
    data object OnConfirmDeleteThread : AiChatEvent
    data object OnDismissDeleteDialog : AiChatEvent
    data object OnScreenResumed : AiChatEvent
}