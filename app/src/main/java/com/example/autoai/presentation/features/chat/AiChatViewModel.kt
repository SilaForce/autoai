package com.example.autoai.presentation.features.chat

import androidx.lifecycle.viewModelScope
import com.example.autoai.base.BaseViewModel
import com.example.autoai.navigation.IAppNavigator
import com.example.autoai.navigation.Route
import com.example.autoai.presentation.components.BottomNavItem
import com.example.autoai.presentation.util.asUiText
import com.example.domain.model.app.onFailure
import com.example.domain.model.app.onSuccess
import com.example.domain.model.chat.ChatMessage
import com.example.domain.model.chat.MessageRole
import com.example.domain.usecase.chat.SendMessageParams
import com.example.domain.usecase.chat.SendMessageUseCase
import com.example.domain.usecase.user.GetCurrentUserUseCase
import com.example.domain.usecase.vehicle.GetVehiclesParams
import com.example.domain.usecase.vehicle.GetVehiclesUseCase
import kotlinx.coroutines.launch

class AiChatViewModel(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val getVehiclesUseCase: GetVehiclesUseCase,
    private val sendMessageUseCase: SendMessageUseCase,
    private val navigator: IAppNavigator
) : BaseViewModel<AiChatState, AiChatEvent, AiChatSideEffect>(AiChatState()) {

    // Čuvamo čiste Domain modele u ViewModelu za historiju razgovora
    private val apiChatHistory = mutableListOf<ChatMessage>()

    private var systemInstruction = "You are an expert auto mechanic. Provide short and precise automotive advice."

    init {
        buildSystemInstruction()

        // 2. We add the welcome message DIRECTLY to the UI state, NOT to the API history.
        val welcomeMessage = ChatMessage(text = "Dobar dan! Ja sam tvoj lični AI mehaničar. Kako ti mogu pomoći danas?", role = MessageRole.AI)
        setState { it.copy(messages = listOf(welcomeMessage.toUiModel())) }
    }

    override fun onEvent(event: AiChatEvent) {
        when (event) {
            is AiChatEvent.OnInputChanged -> setState { it.copy(inputText = event.value) }
            AiChatEvent.OnSendMessageClicked -> sendMessage()
            is AiChatEvent.OnNavItemSelected -> handleBottomNavigation(event.item)
        }
    }

    private fun buildSystemInstruction() {
        viewModelScope.launch {
            getCurrentUserUseCase(Unit).onSuccess { user ->
                getVehiclesUseCase(GetVehiclesParams(user.id)).onSuccess { vehicles ->
                    val activeVehicle = vehicles.firstOrNull { it.isActive }
                    if (activeVehicle != null) {
                        // Ovdje pravimo tajnu instrukciju!
                        systemInstruction = """
                            You are an expert auto mechanic. 
                            The user you are talking to currently drives a: 
                            ${activeVehicle.make} ${activeVehicle.model} (${activeVehicle.year}), Fuel type: ${activeVehicle.fuelType}. 
                            Always keep this specific vehicle in mind when giving advice. 
                            Provide short, clear, and professional answers. Respond in the same language the user writes in.
                        """.trimIndent()
                    }
                }
            }
        }
    }

    private fun sendMessage() {
        val prompt = state.value.inputText.trim()
        if (prompt.isBlank() || state.value.isAiTyping) return

        setState { it.copy(inputText = "", isAiTyping = true) }

        val userMessage = ChatMessage(text = prompt, role = MessageRole.USER)

        // 3. We take a snapshot of the current API history BEFORE adding the new message
        val previousApiHistory = apiChatHistory.toList()

        // Add to both API history and UI
        addMessageToApiAndUi(userMessage)

        viewModelScope.launch {
            sendMessageUseCase(
                SendMessageParams(
                    prompt = prompt,
                    history = previousApiHistory, // Sending the clean history!
                    systemInstruction = systemInstruction
                )
            ).onSuccess { aiReply ->
                addMessageToApiAndUi(ChatMessage(text = aiReply, role = MessageRole.AI))
                setState { it.copy(isAiTyping = false) }
            }.onFailure { error ->
                setState { it.copy(isAiTyping = false) }
                emitSideEffect(AiChatSideEffect.ShowError(error.asUiText()))
            }
        }
    }

    private fun addMessageToApiAndUi(message: ChatMessage) {
        apiChatHistory.add(message)

        // We take the existing UI messages and append the new one
        val welcomeMessage = ChatMessageUi(
            id = "welcome_msg",
            text = "Pozdrav! Ja sam tvoj lični AI mehaničar. Kako ti mogu pomoći danas?",
            isFromUser = false,
            formattedTime = ""
        )
        val currentUiMessages = listOf(welcomeMessage) + apiChatHistory.map { it.toUiModel() }

        setState { it.copy(messages = currentUiMessages) }
    }

    private fun handleBottomNavigation(item: BottomNavItem) {
        when (item) {
            BottomNavItem.HOME -> {
                setState { it.copy(selectedNavItem = BottomNavItem.HOME) }
                navigator.navigateTo(Route.Home)
            }
            BottomNavItem.GARAGE -> {
                setState { it.copy(selectedNavItem = BottomNavItem.GARAGE) }
                navigator.navigateTo(Route.Garage)
            }
            BottomNavItem.COSTS -> {
                setState { it.copy(selectedNavItem = BottomNavItem.COSTS) }
                navigator.navigateTo(Route.Costs)
            }
            BottomNavItem.REMINDERS -> {
                setState { it.copy(selectedNavItem = BottomNavItem.REMINDERS) }
                navigator.navigateTo(Route.Reminder)
            }
            BottomNavItem.AI_CHAT -> Unit
        }
    }
}