package com.example.autoai.presentation.features.chat

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.example.autoai.base.BaseViewModel
import com.example.autoai.navigation.IAppNavigator
import com.example.autoai.navigation.Route
import com.example.autoai.presentation.components.BottomNavItem
import com.example.autoai.presentation.util.ImagePayload
import com.example.autoai.presentation.util.ImageUtils
import com.example.autoai.presentation.util.asUiText
import com.example.domain.model.app.onFailure
import com.example.domain.model.app.onSuccess
import com.example.domain.model.chat.ChatMessage
import com.example.domain.model.chat.ChatThread
import com.example.domain.model.chat.ChatTool
import com.example.domain.model.chat.MessageRole
import com.example.domain.repository.IPreferencesRepository
import com.example.domain.usecase.chat.CreateChatThreadParams
import com.example.domain.usecase.chat.CreateChatThreadUseCase
import com.example.domain.usecase.chat.DeleteChatThreadParams
import com.example.domain.usecase.chat.DeleteChatThreadUseCase
import com.example.domain.usecase.chat.LoadChatHistoryParams
import com.example.domain.usecase.chat.LoadChatHistoryUseCase
import com.example.domain.usecase.chat.LoadChatThreadsParams
import com.example.domain.usecase.chat.LoadChatThreadsUseCase
import com.example.domain.usecase.chat.SaveChatMessageParams
import com.example.domain.usecase.chat.SaveChatMessageUseCase
import com.example.domain.usecase.chat.SendMessageParams
import com.example.domain.usecase.chat.SendMessageUseCase
import com.example.domain.usecase.chat.UpdateChatThreadParams
import com.example.domain.usecase.chat.UpdateChatThreadUseCase
import com.example.domain.usecase.user.GetCurrentUserUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AiChatViewModel(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val sendMessageUseCase: SendMessageUseCase,
    private val loadChatHistoryUseCase: LoadChatHistoryUseCase,
    private val saveChatMessageUseCase: SaveChatMessageUseCase,
    private val loadChatThreadsUseCase: LoadChatThreadsUseCase,
    private val createChatThreadUseCase: CreateChatThreadUseCase,
    private val updateChatThreadUseCase: UpdateChatThreadUseCase,
    private val deleteChatThreadUseCase: DeleteChatThreadUseCase,
    private val preferencesRepository: IPreferencesRepository,
    private val systemInstructionBuilder: AiSystemInstructionBuilder,
    private val chatTools: AiChatTools,
    private val navigator: IAppNavigator,
) : BaseViewModel<AiChatState, AiChatEvent, AiChatSideEffect>(AiChatState()) {

    private companion object {
        const val AI_CONTEXT_MESSAGES = 30
        private const val TAG = "AiChatViewModel"
        private const val DEFAULT_SYSTEM_INSTRUCTION =
            "You are an expert auto mechanic. Provide short and precise automotive advice."
    }

    private var systemInstruction = DEFAULT_SYSTEM_INSTRUCTION
    private var aiAutoReminderEnabled = false
    private var activeVehicleId: String? = null
    private var currentUserId: String? = null
    private var currentUserCurrency: String = "BAM"
    private var currentThreadId: String? = null

    init {
        // Single source of user resolution — no more race between hydrateHistory() and
        // refreshSystemInstruction() both writing currentUserId.
        viewModelScope.launch {
            getCurrentUserUseCase(Unit).onSuccess { user ->
                currentUserId = user.id
                currentUserCurrency = user.currency
                refreshThreadList(user.id)
                refreshSystemInstruction()
            }
        }

        // Auto-reminders preference flips → rebuild the system instruction so the AI's
        // tool availability matches the pref (previously this was written to a private
        // field that never propagated until the next per-send refresh).
        viewModelScope.launch {
            preferencesRepository.isAiAutoRemindersEnabled.collect { enabled ->
                if (aiAutoReminderEnabled != enabled) {
                    aiAutoReminderEnabled = enabled
                    if (currentUserId != null) refreshSystemInstruction()
                }
            }
        }
    }

    override fun onEvent(event: AiChatEvent) {
        when (event) {
            is AiChatEvent.OnInputChanged -> setState { it.copy(inputText = event.value) }
            AiChatEvent.OnSendMessageClicked -> sendMessage()
            is AiChatEvent.OnNavItemSelected -> handleBottomNavigation(event.item)
            is AiChatEvent.OnImageSelected -> {
                // Bitmap decode + JPEG encode of a 5MB phone photo can take hundreds of
                // ms on Main. Push to Default; UI stays interactive.
                viewModelScope.launch {
                    val compressed = withContext(Dispatchers.Default) {
                        ImagePayload(ImageUtils.compressForAi(event.imageBytes) ?: event.imageBytes)
                    }
                    setState { currentState ->
                        if (currentState.selectedImages.size < 3) {
                            currentState.copy(selectedImages = currentState.selectedImages + compressed)
                        } else {
                            currentState
                        }
                    }
                }
            }

            is AiChatEvent.OnRemoveImage -> {
                setState {
                    it.copy(
                        selectedImages = it.selectedImages.toMutableList()
                            .apply { removeAt(event.index) })
                }
            }

            is AiChatEvent.OnSelectThread -> selectThread(event.threadId)
            AiChatEvent.OnStartNewChat -> startNewChat()
            is AiChatEvent.OnLongPressThread ->
                setState { it.copy(threadMenuAnchorId = event.threadId) }

            AiChatEvent.OnDismissThreadMenu ->
                setState { it.copy(threadMenuAnchorId = null) }

            is AiChatEvent.OnDeleteThreadClicked ->
                setState {
                    it.copy(
                        threadMenuAnchorId = null,
                        pendingDeleteThreadId = event.threadId,
                    )
                }

            AiChatEvent.OnDismissDeleteDialog ->
                setState { it.copy(pendingDeleteThreadId = null) }

            AiChatEvent.OnConfirmDeleteThread -> deletePendingThread()

            AiChatEvent.OnScreenResumed -> {
                // User may have changed vehicle / costs / reminders in another screen.
                // Refresh the system instruction so the AI's grounding is current.
                if (currentUserId != null) {
                    viewModelScope.launch { refreshSystemInstruction() }
                }
            }
        }
    }

    private suspend fun refreshThreadList(userId: String) {
        loadChatThreadsUseCase(LoadChatThreadsParams(userId)).onSuccess { threads ->
            setState { it.copy(threads = threads.map { thread -> thread.toUiModel() }) }
        }
    }

    private suspend fun loadThreadMessages(userId: String, thread: ChatThread) {
        currentThreadId = thread.id
        setState { it.copy(currentThreadId = thread.id, currentThreadTitle = thread.title) }
        loadChatHistoryUseCase(LoadChatHistoryParams(userId, thread.id)).onSuccess { messages ->
            setState { it.copy(messages = messages.map { msg -> msg.toUiModel() }) }
        }
    }

    private fun deletePendingThread() {
        val threadId = state.value.pendingDeleteThreadId ?: return
        val userId = currentUserId ?: return
        setState { it.copy(pendingDeleteThreadId = null) }

        viewModelScope.launch {
            deleteChatThreadUseCase(DeleteChatThreadParams(threadId, userId))
                .onSuccess {
                    if (currentThreadId == threadId) {
                        startNewChat()
                    }
                    refreshThreadList(userId)
                }
                .onFailure { error ->
                    emitSideEffect(AiChatSideEffect.ShowError(error.asUiText()))
                }
        }
    }

    private fun selectThread(threadId: String) {
        val userId = currentUserId ?: return
        viewModelScope.launch {
            loadChatThreadsUseCase(LoadChatThreadsParams(userId)).onSuccess { threads ->
                val thread = threads.firstOrNull { it.id == threadId } ?: return@onSuccess
                setState { it.copy(threads = threads.map { t -> t.toUiModel() }) }
                loadThreadMessages(userId, thread)
            }
        }
    }

    private fun startNewChat() {
        currentThreadId = null
        setState {
            it.copy(
                currentThreadId = null,
                currentThreadTitle = null,
                messages = emptyList(),
            )
        }
    }

    private suspend fun refreshSystemInstruction() {
        val userId = currentUserId ?: return
        val built = systemInstructionBuilder.build(
            userId = userId,
            currency = currentUserCurrency,
            autoReminderEnabled = aiAutoReminderEnabled,
        ) ?: return
        systemInstruction = built.text
        activeVehicleId = built.activeVehicleId
    }

    private fun sendMessage() {
        val selectedImages = state.value.selectedImages
        val imageBytes = selectedImages.map { it.bytes }
        val prompt = state.value.inputText.trim()
        if (prompt.isBlank() || state.value.isAiTyping) return

        setState { it.copy(inputText = "", isAiTyping = true, selectedImages = emptyList()) }

        viewModelScope.launch {
            val uid = currentUserId
            if (uid.isNullOrBlank()) {
                setState { it.copy(isAiTyping = false) }
                return@launch
            }

            val threadId = ensureCurrentThread(uid, prompt) ?: run {
                setState { it.copy(isAiTyping = false) }
                return@launch
            }

            // Snapshot for the AI call BEFORE adding the new message, capped to the
            // context window. Derived from state.value.messages — the single source of
            // truth — not a parallel apiChatHistory list.
            val previousApiHistory = state.value.messages
                .map { it.toDomainModel() }
                .takeLast(AI_CONTEXT_MESSAGES)

            val userMessage = ChatMessage(
                text = prompt,
                role = MessageRole.USER,
                images = imageBytes,
                threadId = threadId,
            )
            addMessage(userMessage)

            saveChatMessageUseCase(SaveChatMessageParams(userMessage, uid))
                .onFailure { error ->
                    Log.w(TAG, "failed to persist user message: ${error::class.simpleName}")
                }

            sendMessageUseCase(
                SendMessageParams(
                    prompt = prompt,
                    history = previousApiHistory,
                    systemInstruction = systemInstruction,
                    images = imageBytes,
                    tools = buildTools(),
                )
            ).onSuccess { aiReply ->
                val aiMessage = ChatMessage(
                    text = aiReply,
                    role = MessageRole.AI,
                    threadId = threadId,
                )
                addMessage(aiMessage)
                setState { it.copy(isAiTyping = false) }

                saveChatMessageUseCase(SaveChatMessageParams(aiMessage, uid))
                    .onFailure { error ->
                        Log.w(TAG, "failed to persist AI message: ${error::class.simpleName}")
                    }

                bumpThreadUpdatedAt(uid, threadId)
            }.onFailure { error ->
                setState { it.copy(isAiTyping = false) }
                emitSideEffect(AiChatSideEffect.ShowError(error.asUiText()))
            }
        }
    }

    /**
     * Returns the active threadId — either the existing [currentThreadId] or a newly created thread
     * whose title is taken from the prompt. Returns null on creation failure.
     */
    private suspend fun ensureCurrentThread(userId: String, prompt: String): String? {
        currentThreadId?.let { return it }

        val title = prompt.take(50).trim().ifBlank { "New chat" }
        val newThread = ChatThread(userId = userId, title = title)
        var createdId: String? = null
        createChatThreadUseCase(CreateChatThreadParams(newThread)).onSuccess { saved ->
            currentThreadId = saved.id
            createdId = saved.id
            setState { it.copy(currentThreadId = saved.id, currentThreadTitle = saved.title) }
        }.onFailure { error ->
            Log.w(TAG, "failed to create thread: ${error::class.simpleName}")
        }
        // Refresh the sidebar so the new thread is visible immediately.
        refreshThreadList(userId)
        return createdId
    }

    private suspend fun bumpThreadUpdatedAt(userId: String, threadId: String) {
        val existing = state.value.threads.firstOrNull { it.id == threadId }
        val title = existing?.title ?: state.value.currentThreadTitle ?: "New chat"
        updateChatThreadUseCase(
            UpdateChatThreadParams(
                ChatThread(
                    id = threadId,
                    userId = userId,
                    title = title,
                    updatedAt = System.currentTimeMillis(),
                )
            )
        ).onFailure { error ->
            Log.w(TAG, "failed to update thread metadata: ${error::class.simpleName}")
        }
        refreshThreadList(userId)
    }

    private fun buildTools(): List<ChatTool> {
        val userId = currentUserId ?: return emptyList()
        val vehicleId = activeVehicleId ?: return emptyList()
        return chatTools.build(userId, vehicleId, aiAutoReminderEnabled)
    }

    private fun addMessage(message: ChatMessage) {
        setState { it.copy(messages = it.messages + message.toUiModel()) }
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
