package com.example.autoai.presentation.features.chat

import androidx.compose.runtime.Immutable
import com.example.autoai.presentation.util.ImagePayload
import com.example.domain.model.chat.MessageRole

/**
 * Presentation model for a chat message. Carries enough data (`role`, `timestamp`,
 * `threadId`) to round-trip back to a domain `ChatMessage` at send time — that's how we
 * keep `AiChatState.messages` as the single source of truth instead of mirroring a
 * separate `apiChatHistory` list in the ViewModel.
 */
@Immutable
data class ChatMessageUi(
    val id: String,
    val text: String,
    val role: MessageRole,
    val timestamp: Long,
    val formattedTime: String,
    val threadId: String,
    val images: List<ImagePayload> = emptyList(),
) {
    val isFromUser: Boolean get() = role == MessageRole.USER
}
