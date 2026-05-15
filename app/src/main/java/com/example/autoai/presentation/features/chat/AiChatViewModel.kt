package com.example.autoai.presentation.features.chat

import androidx.lifecycle.viewModelScope
import com.example.autoai.base.BaseViewModel
import com.example.autoai.navigation.IAppNavigator
import com.example.autoai.navigation.Route
import com.example.autoai.presentation.components.BottomNavItem
import com.example.autoai.presentation.util.ImageUtils
import com.example.autoai.presentation.util.asUiText
import com.example.domain.model.app.onFailure
import com.example.domain.model.app.onSuccess
import com.example.domain.model.chat.ChatMessage
import com.example.domain.model.chat.MessageRole
import com.example.domain.usecase.chat.SendMessageParams
import com.example.domain.usecase.chat.SendMessageUseCase
import com.example.domain.usecase.reminder.GetRemindersParams
import com.example.domain.usecase.reminder.GetRemindersUseCase
import com.example.domain.usecase.user.GetCurrentUserUseCase
import com.example.domain.usecase.vehicle.GetVehiclesParams
import com.example.domain.usecase.vehicle.GetVehiclesUseCase
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.util.Date

class AiChatViewModel(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val getVehiclesUseCase: GetVehiclesUseCase,
    private val sendMessageUseCase: SendMessageUseCase,
    private val getReminderUseCase: GetRemindersUseCase,
    private val navigator: IAppNavigator
) : BaseViewModel<AiChatState, AiChatEvent, AiChatSideEffect>(AiChatState()) {

    // Čuvamo čiste Domain modele u ViewModelu za historiju razgovora
    private val apiChatHistory = mutableListOf<ChatMessage>()

    private var systemInstruction = "You are an expert auto mechanic. Provide short and precise automotive advice."

    init {
        buildSystemInstruction()

        val welcomeMessage = ChatMessage(text = "Dobar dan! Ja sam tvoj lični AI mehaničar. Kako ti mogu pomoći danas?", role = MessageRole.AI)
        setState { it.copy(messages = listOf(welcomeMessage.toUiModel())) }
    }

    override fun onEvent(event: AiChatEvent) {
        when (event) {
            is AiChatEvent.OnInputChanged -> setState { it.copy(inputText = event.value) }
            AiChatEvent.OnSendMessageClicked -> sendMessage()
            is AiChatEvent.OnNavItemSelected -> handleBottomNavigation(event.item)
            is AiChatEvent.OnImageSelected -> {
                val compressed = ImageUtils.compressForAi(event.imageBytes) ?: event.imageBytes
                setState { currentState ->
                    if (currentState.selectedImages.size < 3) {
                        currentState.copy(selectedImages = currentState.selectedImages + compressed)
                    } else {
                        currentState
                    }
                }
            }
            is AiChatEvent.OnRemoveImage -> {
                setState { it.copy(selectedImages = it.selectedImages.toMutableList().apply { removeAt(event.index) }) }
            }
        }
    }

    private fun buildSystemInstruction() {
        viewModelScope.launch {
            getCurrentUserUseCase(Unit).onSuccess { user ->
                getVehiclesUseCase(GetVehiclesParams(user.id)).onSuccess { vehicles ->
                    val activeVehicle = vehicles.firstOrNull { it.isActive } ?: return@onSuccess
                    getReminderUseCase(GetRemindersParams(activeVehicle.id)).onSuccess { reminders ->
                        val closestReminder = reminders
                            .filter { !it.isCompleted }
                            .minByOrNull { it.dueDateMillis }
                        val activeReminders = reminders.filter { !it.isCompleted }

                        val closestBlock = closestReminder?.let {
                            val formattedDate = DateFormat.getDateInstance().format(Date(it.dueDateMillis))
                            val overdue = it.dueDateMillis < System.currentTimeMillis()
                            val status = if (overdue) "overdue since" else "due on"
                            "The user's closest reminder is: \"${it.title}\" $status $formattedDate. If it comes up naturally, you may proactively mention this."
                        } ?: "The user has no upcoming reminders."

                        val allRemindersBlock = if (activeReminders.isNotEmpty()) {
                            val list = activeReminders.joinToString("\n") { reminder ->
                                val date = DateFormat.getDateInstance().format(Date(reminder.dueDateMillis))
                                "- \"${reminder.title}\" — $date"
                            }
                            "The user's full reminder list:\n$list\nIf the user asks about their reminders, list all of these."
                        } else {
                            ""
                        }

                        systemInstruction = """
                            You are an expert auto mechanic and vehicle diagnostics assistant.

                            $closestBlock

                            $allRemindersBlock

                            The user currently drives: ${activeVehicle.make} ${activeVehicle.model} (${activeVehicle.year}), Fuel type: ${activeVehicle.fuelType}.
                            Always tailor your answers to this specific vehicle (correct parts, known issues, compatible fluids, etc.).

                            When the user sends a photo:
                            - First describe what you see (the part, its condition, any visible damage or wear).
                            - Then give your diagnosis and recommendation.
                            - If the image is unclear, ask the user for a better angle.

                            Safety:
                            - If the issue involves brakes, tires, steering, suspension, or any safety-critical system, start your answer with "⚠️ SAFETY:" and strongly recommend professional inspection.
                            - Never suggest the user ignore a potential safety issue.

                            Cost estimates:
                            - When relevant, provide a rough price range for parts and labor (e.g. "Parts: 50-80 EUR, Labor: 30-60 EUR").
                            - Clarify that prices vary by region and workshop.

                            DIY guidance:
                            - For simple maintenance tasks (oil top-up, bulb replacement, filter change, etc.), offer numbered step-by-step instructions.
                            - Always list the tools needed before the steps.
                            - If a repair requires lifting the car, special tools, or carries risk, recommend a professional instead.

                            General rules:
                            - Keep answers concise and well-structured. Use bullet points or numbered lists.
                            - Respond in the same language the user writes in.
                            - If you are unsure about something, say so honestly rather than guessing.
                        """.trimIndent()
                    }
                }
            }
        }
    }

    private fun sendMessage() {
        val images = state.value.selectedImages
        val prompt = state.value.inputText.trim()
        if (prompt.isBlank() || state.value.isAiTyping) return

        setState { it.copy(inputText = "", isAiTyping = true, selectedImages = emptyList()) }

        val userMessage = ChatMessage(text = prompt, role = MessageRole.USER, images = images)

        // 3. We take a snapshot of the current API history BEFORE adding the new message
        val previousApiHistory = apiChatHistory.toList()

        // Add to both API history and UI
        addMessageToApiAndUi(userMessage)

        viewModelScope.launch {
            sendMessageUseCase(
                SendMessageParams(
                    prompt = prompt,
                    history = previousApiHistory, // Sending the clean history!
                    systemInstruction = systemInstruction,
                    images = images
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