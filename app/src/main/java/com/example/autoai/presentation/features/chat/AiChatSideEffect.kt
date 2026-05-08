package com.example.autoai.presentation.features.chat

import com.example.autoai.presentation.util.UiText

sealed interface AiChatSideEffect {
    data class ShowError(val message: UiText) : AiChatSideEffect
}