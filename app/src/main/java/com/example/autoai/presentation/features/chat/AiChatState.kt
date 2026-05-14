package com.example.autoai.presentation.features.chat

import com.example.autoai.presentation.components.BottomNavItem

data class AiChatState(
    val messages: List<ChatMessageUi> = emptyList(),
    val inputText: String = "",
    val isAiTyping: Boolean = false, // Za prikazivanje loading animacije dok AI smišlja odgovor
    val selectedNavItem: BottomNavItem = BottomNavItem.AI_CHAT,
    val selectedImage : ByteArray? = null // Za čuvanje izabrane slike (ako je podržano
)