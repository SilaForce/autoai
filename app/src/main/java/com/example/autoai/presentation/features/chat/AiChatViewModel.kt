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
import com.example.domain.model.app.AppResult
import com.example.domain.model.app.onFailure
import com.example.domain.model.app.onSuccess
import com.example.domain.model.chat.ChatMessage
import com.example.domain.model.chat.ChatThread
import com.example.domain.model.chat.ChatTool
import com.example.domain.model.chat.ChatToolParam
import com.example.domain.model.chat.ChatToolParamType
import com.example.domain.model.chat.MessageRole
import com.example.domain.model.cost.Cost
import com.example.domain.model.cost.CostCategory
import com.example.domain.model.cost.CostStatistics
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
import com.example.domain.usecase.cost.GetCostStatisticsForPeriodParams
import com.example.domain.usecase.cost.GetCostStatisticsForPeriodUseCase
import com.example.domain.usecase.cost.GetCostStatisticsParams
import com.example.domain.usecase.cost.GetCostStatisticsUseCase
import com.example.domain.usecase.cost.GetCostsHistoryParams
import com.example.domain.usecase.cost.GetCostsHistoryUseCase
import com.example.domain.usecase.reminder.AddReminderParams
import com.example.domain.usecase.reminder.AddReminderUseCase
import com.example.domain.usecase.reminder.GetRemindersParams
import com.example.domain.usecase.reminder.GetRemindersUseCase
import com.example.domain.usecase.user.GetCurrentUserUseCase
import com.example.domain.usecase.vehicle.GetVehiclesParams
import com.example.domain.usecase.vehicle.GetVehiclesUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AiChatViewModel(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val getVehiclesUseCase: GetVehiclesUseCase,
    private val sendMessageUseCase: SendMessageUseCase,
    private val getReminderUseCase: GetRemindersUseCase,
    private val getCostsHistoryUseCase: GetCostsHistoryUseCase,
    private val getCostStatisticsUseCase: GetCostStatisticsUseCase,
    private val getCostStatisticsForPeriodUseCase: GetCostStatisticsForPeriodUseCase,
    private val loadChatHistoryUseCase: LoadChatHistoryUseCase,
    private val saveChatMessageUseCase: SaveChatMessageUseCase,
    private val loadChatThreadsUseCase: LoadChatThreadsUseCase,
    private val createChatThreadUseCase: CreateChatThreadUseCase,
    private val updateChatThreadUseCase: UpdateChatThreadUseCase,
    private val deleteChatThreadUseCase: DeleteChatThreadUseCase,
    private val preferencesRepository: IPreferencesRepository,
    private val addReminderUseCase: AddReminderUseCase,
    private val navigator: IAppNavigator
) : BaseViewModel<AiChatState, AiChatEvent, AiChatSideEffect>(AiChatState()) {

    private companion object {
        const val AI_CONTEXT_MESSAGES = 30
        private const val TAG = "AiChatViewModel"
    }

    private var systemInstruction =
        "You are an expert auto mechanic. Provide short and precise automotive advice."

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

    private suspend fun refreshThreadList(userId: String): AppResult<List<ChatThread>> {
        val result = loadChatThreadsUseCase(LoadChatThreadsParams(userId))
        result.onSuccess { threads ->
            setState { it.copy(threads = threads.map { thread -> thread.toUiModel() }) }
        }
        return result
    }

    private suspend fun loadThreadMessages(userId: String, thread: ChatThread) {
        currentThreadId = thread.id
        setState { it.copy(currentThreadId = thread.id, currentThreadTitle = thread.title) }
        loadChatHistoryUseCase(LoadChatHistoryParams(userId, thread.id)).onSuccess { messages ->
            setState { it.copy(messages = messages.map { msg -> msg.toUiModel() }) }
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
        // `currentUserId` is resolved once in init and never re-fetched here. Earlier this
        // method called getCurrentUserUseCase and wrote currentUserId, racing with the
        // init's own user fetch. Now there's a single owner.
        val userId = currentUserId ?: return
        getVehiclesUseCase(GetVehiclesParams(userId)).onSuccess { vehicles ->
            val activeVehicle = vehicles.firstOrNull { it.isActive } ?: return@onSuccess
            activeVehicleId = activeVehicle.id
            getReminderUseCase(GetRemindersParams(activeVehicle.id)).onSuccess { reminders ->
                    val closestReminder = reminders
                        .filter { !it.isCompleted }
                        .minByOrNull { it.dueDateMillis }
                    val activeReminders = reminders.filter { !it.isCompleted }

                    val today = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(Date())

                    val closestBlock = closestReminder?.let {
                        val formattedDate =
                            DateFormat.getDateInstance().format(Date(it.dueDateMillis))
                        val overdue = it.dueDateMillis < System.currentTimeMillis()
                        val status = if (overdue) "overdue since" else "due on"
                        "The user's closest reminder is: \"${it.title}\" $status $formattedDate. If it comes up naturally, you may proactively mention this."
                    } ?: "The user has no upcoming reminders."

                    val allRemindersBlock = if (activeReminders.isNotEmpty()) {
                        val list = activeReminders.joinToString("\n") { reminder ->
                            val date =
                                DateFormat.getDateInstance().format(Date(reminder.dueDateMillis))
                            "- \"${reminder.title}\" — $date"
                        }
                        "The user's full reminder list:\n$list\nIf the user asks about their reminders, list all of these."
                    } else {
                        ""
                    }

                    var costs: List<Cost> = emptyList()
                    var stats: CostStatistics? = null
                    getCostsHistoryUseCase(GetCostsHistoryParams(activeVehicle.id)).onSuccess {
                        costs = it
                    }
                    getCostStatisticsUseCase(GetCostStatisticsParams(activeVehicle.id)).onSuccess {
                        stats = it
                    }
                    val costsBlock = buildCostsBlock(costs, stats, currentUserCurrency)

                    val autoReminderBlock = if (aiAutoReminderEnabled) {
                        """
                            You have access to a tool named "addReminder" that creates a maintenance reminder for the user's active vehicle. When the user asks to be reminded about a future service or check, call the tool with a short title and a YYYY-MM-DD date in the future. After the tool reports success, confirm in your reply that you added the reminder. If the tool reports failure, explain the problem to the user honestly without retrying with the same arguments.
                            """.trimIndent()
                    } else {
                        """
                         Auto-Reminders (DISABLED):
                         You CANNOT create reminders right now. If the user asks you to add a reminder, do NOT pretend to add it. Instead, politely tell them to enable "AI Auto-Reminders" in the Settings screen first.
                         """.trimIndent()
                    }

                    systemInstruction = """
                            You are an expert auto mechanic and vehicle diagnostics assistant.

                            Today's date is $today.
                                                                                                                                                                                                         
                            Put it before the vehicle info, reminder blocks, everything — so the AI has the date context for all its reasoning.
                             
                            $closestBlock

                            $allRemindersBlock

                            $costsBlock

                            $autoReminderBlock

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

    private fun buildCostsBlock(costs: List<Cost>, stats: CostStatistics?, currencyCode: String): String {
        if (costs.isEmpty() || stats == null) {
            return "The user has not logged any costs for this vehicle yet. If they ask cost-related questions, say so honestly."
        }

        val dateFormat = DateFormat.getDateInstance()

        val total = money(stats.totalAmount)
        val perCategoryStats = CostCategory.entries.joinToString("\n") { category ->
            "- ${category.name}: ${money(stats.amountByCategory[category] ?: 0.0)}"
        }

        val lastEntryByCategory = costs.groupBy { it.category }
            .mapValues { (_, list) -> list.maxByOrNull { it.dateMillis } }
        val lastEntryBlock = CostCategory.entries.joinToString("\n") { category ->
            val entry = lastEntryByCategory[category]
            if (entry == null) {
                "- ${category.name}: no entries yet"
            } else {
                "- ${category.name}: ${money(entry.amount)} on ${dateFormat.format(Date(entry.dateMillis))}"
            }
        }

        val recent =
            costs.sortedByDescending { it.dateMillis }.take(10).joinToString("\n") { cost ->
                val date = dateFormat.format(Date(cost.dateMillis))
                val descPart = cost.description
                    ?.takeIf { it.isNotBlank() }
                    ?.let { " — \"${it.take(40)}\"" }
                    .orEmpty()
                "- $date — ${cost.category.name} — ${money(cost.amount)}$descPart"
            }

        return """
            The user's spending summary for this vehicle (all amounts are in $currencyCode):
            - Total spent: $total
            $perCategoryStats

            Last entry per category:
            $lastEntryBlock

            Recent activity (most recent first, up to 10):
            $recent

            Notes on using this cost data:
            - When the user asks WHEN something was last done, use the "Last entry per category" section.
            - When the user asks HOW MUCH was spent on a category in total (lifetime), use the per-category totals — do not re-sum the recent activity list.
            - For date-range questions (e.g. "this month", "last 30 days", "between March and May") or filtered lookups, call the "getCostStatisticsForPeriod" or "getCostsByCategory" tools. You know today's date, so resolve relative ranges yourself before calling. Do NOT guess date-range totals from the recent activity list.
            - When giving mechanic advice, cross-reference the user's symptoms with recent activity — flag if a related service was recently logged, or appears overdue.
        """.trimIndent()
    }

    private fun buildTools(): List<ChatTool> {
        val userId = currentUserId ?: return emptyList()
        val vehicleId = activeVehicleId ?: return emptyList()
        val tools = mutableListOf<ChatTool>()
        if (aiAutoReminderEnabled) {
            tools += buildAddReminderTool(userId, vehicleId)
        }
        tools += buildGetCostStatisticsForPeriodTool(vehicleId)
        tools += buildGetCostsByCategoryTool(vehicleId)
        return tools
    }

    private fun buildAddReminderTool(userId: String, vehicleId: String): ChatTool = ChatTool(
        name = "addReminder",
        description = "Creates a maintenance reminder for the user's active vehicle. Use when the user asks to be reminded about a future service or check. Returns success/failure as a string.",
        parameters = listOf(
            ChatToolParam("title", ChatToolParamType.STRING, "Short title (e.g. 'Oil change')."),
            ChatToolParam(
                "date",
                ChatToolParamType.STRING,
                "Due date in YYYY-MM-DD format. Must be in the future."
            ),
        ),
        execute = { args -> executeAddReminder(userId, vehicleId, args) },
    )

    private suspend fun executeAddReminder(
        userId: String,
        vehicleId: String,
        args: Map<String, String?>
    ): String {
        val title = args["title"]?.takeIf { it.isNotBlank() }
            ?: return "Failure: 'title' is required."
        val dateString = args["date"]?.takeIf { it.isNotBlank() }
            ?: return "Failure: 'date' is required."
        val dateMillis = try {
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dateString)?.time
        } catch (_: Exception) {
            null
        } ?: return "Failure: 'date' is not a valid YYYY-MM-DD."
        if (dateMillis < System.currentTimeMillis()) {
            return "Failure: 'date' must be in the future."
        }

        var outcome = "Success: reminder \"$title\" added for $dateString."
        addReminderUseCase(
            AddReminderParams(
                userId = userId,
                vehicleId = vehicleId,
                title = title,
                dueDateMillis = dateMillis,
            )
        ).onFailure { error ->
            outcome = "Failure: could not save reminder (${error::class.simpleName})."
        }
        return outcome
    }

    private fun buildGetCostStatisticsForPeriodTool(vehicleId: String): ChatTool = ChatTool(
        name = "getCostStatisticsForPeriod",
        description = "Returns the user's spending totals for this vehicle within a date range. Use when the user asks about spending in a specific period (e.g. \"this month\", \"last 30 days\", \"between March and May\"). If both dates are omitted, returns lifetime totals.",
        parameters = listOf(
            ChatToolParam(
                "since",
                ChatToolParamType.STRING,
                "Inclusive start date in YYYY-MM-DD format. Omit for no lower bound.",
                required = false
            ),
            ChatToolParam(
                "until",
                ChatToolParamType.STRING,
                "Inclusive end date in YYYY-MM-DD format. Omit for no upper bound.",
                required = false
            ),
        ),
        execute = { args -> executeGetCostStatisticsForPeriod(vehicleId, args) },
    )

    private suspend fun executeGetCostStatisticsForPeriod(
        vehicleId: String,
        args: Map<String, String?>
    ): String {
        val sinceMillis = parseDayStartMillis(args["since"])
        val untilMillis = parseDayEndMillis(args["until"])
        if (args["since"]?.isNotBlank() == true && sinceMillis == null) {
            return "Failure: 'since' is not a valid YYYY-MM-DD."
        }
        if (args["until"]?.isNotBlank() == true && untilMillis == null) {
            return "Failure: 'until' is not a valid YYYY-MM-DD."
        }

        var outcome = "Failure: could not load statistics."
        getCostsHistoryUseCase(GetCostsHistoryParams(vehicleId))
            .onSuccess { costs ->
                getCostStatisticsForPeriodUseCase(
                    GetCostStatisticsForPeriodParams(
                        costs = costs,
                        sinceMillis = sinceMillis,
                        untilMillis = untilMillis,
                    )
                ).onSuccess { stats ->
                    val rangeLabel = formatRangeLabel(args["since"], args["until"])
                    outcome = if (stats.totalAmount == 0.0 && stats.amountByCategory.isEmpty()) {
                        "No costs logged $rangeLabel."
                    } else {
                        val perCategory = CostCategory.entries.joinToString(", ") { category ->
                            "${category.name} ${money(stats.amountByCategory[category] ?: 0.0)}"
                        }
                        "Spending $rangeLabel: total ${money(stats.totalAmount)}. By category: $perCategory."
                    }
                }.onFailure { error ->
                    outcome = "Failure: could not load statistics (${error::class.simpleName})."
                }
            }
            .onFailure { error ->
                outcome = "Failure: could not load statistics (${error::class.simpleName})."
            }
        return outcome
    }

    private fun buildGetCostsByCategoryTool(vehicleId: String): ChatTool = ChatTool(
        name = "getCostsByCategory",
        description = "Returns individual cost entries for a given category, optionally within a date range. Use when the user asks to see specific expenses (e.g. \"show me all my fuel receipts from April\", \"what brake-related services have I logged?\"). Returns up to 20 most recent matching entries.",
        parameters = listOf(
            ChatToolParam(
                name = "category",
                type = ChatToolParamType.ENUM,
                description = "Cost category to filter by.",
                values = CostCategory.entries.map { it.name },
            ),
            ChatToolParam(
                "since",
                ChatToolParamType.STRING,
                "Inclusive start date in YYYY-MM-DD format. Omit for no lower bound.",
                required = false
            ),
            ChatToolParam(
                "until",
                ChatToolParamType.STRING,
                "Inclusive end date in YYYY-MM-DD format. Omit for no upper bound.",
                required = false
            ),
        ),
        execute = { args -> executeGetCostsByCategory(vehicleId, args) },
    )

    private suspend fun executeGetCostsByCategory(
        vehicleId: String,
        args: Map<String, String?>
    ): String {
        val categoryArg = args["category"]?.takeIf { it.isNotBlank() }
            ?: return "Failure: 'category' is required."
        val category =
            runCatching { CostCategory.valueOf(categoryArg.uppercase(Locale.ROOT)) }.getOrNull()
                ?: return "Failure: 'category' must be one of ${CostCategory.entries.joinToString { it.name }}."
        val sinceMillis = parseDayStartMillis(args["since"])
        val untilMillis = parseDayEndMillis(args["until"])
        if (args["since"]?.isNotBlank() == true && sinceMillis == null) {
            return "Failure: 'since' is not a valid YYYY-MM-DD."
        }
        if (args["until"]?.isNotBlank() == true && untilMillis == null) {
            return "Failure: 'until' is not a valid YYYY-MM-DD."
        }

        var outcome = "Failure: could not load costs."
        getCostsHistoryUseCase(GetCostsHistoryParams(vehicleId)).onSuccess { allCosts ->
            val matching = allCosts
                .filter { it.category == category }
                .filter { sinceMillis == null || it.dateMillis >= sinceMillis }
                .filter { untilMillis == null || it.dateMillis <= untilMillis }
                .sortedByDescending { it.dateMillis }
            val rangeLabel = formatRangeLabel(args["since"], args["until"])
            outcome = if (matching.isEmpty()) {
                "No ${category.name} entries found $rangeLabel."
            } else {
                val shown = matching.take(20)
                val isoFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                val lines = shown.joinToString("\n") { cost ->
                    val descPart = cost.description?.takeIf { it.isNotBlank() }
                        ?.let { " — \"${it.take(60)}\"" }.orEmpty()
                    "- ${isoFormat.format(Date(cost.dateMillis))}: ${money(cost.amount)}$descPart"
                }
                val header = if (matching.size > shown.size) {
                    "Found ${matching.size} ${category.name} entries $rangeLabel (showing 20 most recent):"
                } else {
                    "Found ${matching.size} ${category.name} entries $rangeLabel:"
                }
                "$header\n$lines"
            }
        }.onFailure { error ->
            outcome = "Failure: could not load costs (${error::class.simpleName})."
        }
        return outcome
    }

    private fun parseDayStartMillis(value: String?): Long? {
        val s = value?.takeIf { it.isNotBlank() } ?: return null
        return try {
            SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(s)?.time
        } catch (_: Exception) {
            null
        }
    }

    private fun parseDayEndMillis(value: String?): Long? {
        val start = parseDayStartMillis(value) ?: return null
        return start + 24L * 60L * 60L * 1000L - 1L
    }

    private fun formatRangeLabel(since: String?, until: String?): String = when {
        !since.isNullOrBlank() && !until.isNullOrBlank() -> "between $since and $until"
        !since.isNullOrBlank() -> "since $since"
        !until.isNullOrBlank() -> "up to $until"
        else -> "(lifetime)"
    }

    private fun money(amount: Double): String = String.format(Locale.getDefault(), "%.2f", amount)


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