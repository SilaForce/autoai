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
import com.example.domain.model.chat.ChatTool
import com.example.domain.model.chat.ChatToolParam
import com.example.domain.model.chat.ChatToolParamType
import com.example.domain.model.chat.MessageRole
import com.example.domain.model.cost.Cost
import com.example.domain.model.cost.CostCategory
import com.example.domain.model.cost.CostStatistics
import com.example.domain.repository.IPreferencesRepository
import com.example.domain.usecase.chat.SendMessageParams
import com.example.domain.usecase.chat.SendMessageUseCase
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
import kotlinx.coroutines.launch
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
    private val preferencesRepository: IPreferencesRepository,
    private val addReminderUseCase: AddReminderUseCase,
    private val navigator: IAppNavigator
) : BaseViewModel<AiChatState, AiChatEvent, AiChatSideEffect>(AiChatState()) {

    // Čuvamo čiste Domain modele u ViewModelu za historiju razgovora
    private val apiChatHistory = mutableListOf<ChatMessage>()

    private var systemInstruction = "You are an expert auto mechanic. Provide short and precise automotive advice."

    private var aiAutoReminderEnabled = false

    private var activeVehicleId: String? = null

    private var currentUserId: String? = null

    init {
        buildSystemInstruction()

        val welcomeMessage = ChatMessage(text = "Dobar dan! Ja sam tvoj lični AI mehaničar. Kako ti mogu pomoći danas?", role = MessageRole.AI)
        setState { it.copy(messages = listOf(welcomeMessage.toUiModel())) }

        viewModelScope.launch {
            preferencesRepository.isAiAutoRemindersEnabled.collect { enabled ->
                aiAutoReminderEnabled = enabled
            }
            }
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
                currentUserId = user.id
                getVehiclesUseCase(GetVehiclesParams(user.id)).onSuccess { vehicles ->
                    val activeVehicle = vehicles.firstOrNull { it.isActive } ?: return@onSuccess
                    activeVehicleId = activeVehicle.id
                    getReminderUseCase(GetRemindersParams(activeVehicle.id)).onSuccess { reminders ->
                        val closestReminder = reminders
                            .filter { !it.isCompleted }
                            .minByOrNull { it.dueDateMillis }
                        val activeReminders = reminders.filter { !it.isCompleted }

                        val today = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(Date())

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

                        var costs: List<Cost> = emptyList()
                        var stats: CostStatistics? = null
                        getCostsHistoryUseCase(GetCostsHistoryParams(activeVehicle.id)).onSuccess { costs = it }
                        getCostStatisticsUseCase(GetCostStatisticsParams(activeVehicle.id)).onSuccess { stats = it }
                        val costsBlock = buildCostsBlock(costs, stats)

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
                    history = previousApiHistory,
                    systemInstruction = systemInstruction,
                    images = images,
                    tools = buildTools(),
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

    private fun buildCostsBlock(costs: List<Cost>, stats: CostStatistics?): String {
        if (costs.isEmpty() || stats == null) {
            return "The user has not logged any costs for this vehicle yet. If they ask cost-related questions, say so honestly."
        }

        val dateFormat = DateFormat.getDateInstance()
        fun money(amount: Double): String = String.format(Locale.getDefault(), "%.2f", amount)

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

        val recent = costs.sortedByDescending { it.dateMillis }.take(10).joinToString("\n") { cost ->
            val date = dateFormat.format(Date(cost.dateMillis))
            val descPart = cost.description
                ?.takeIf { it.isNotBlank() }
                ?.let { " — \"${it.take(40)}\"" }
                .orEmpty()
            "- $date — ${cost.category.name} — ${money(cost.amount)}$descPart"
        }

        return """
            The user's spending summary for this vehicle (amounts in their local currency):
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
            ChatToolParam("date", ChatToolParamType.STRING, "Due date in YYYY-MM-DD format. Must be in the future."),
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
            ChatToolParam("since", ChatToolParamType.STRING, "Inclusive start date in YYYY-MM-DD format. Omit for no lower bound.", required = false),
            ChatToolParam("until", ChatToolParamType.STRING, "Inclusive end date in YYYY-MM-DD format. Omit for no upper bound.", required = false),
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
        getCostStatisticsForPeriodUseCase(
            GetCostStatisticsForPeriodParams(
                vehicleId = vehicleId,
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
        return outcome
    }

    private fun buildGetCostsByCategoryTool(vehicleId: String): ChatTool = ChatTool(
        name = "getCostsByCategory",
        description = "Returns individual cost entries for a given category, optionally within a date range. Use when the user asks to see specific expenses (e.g. \"show me all my fuel receipts from April\", \"what brake-related services have I logged?\"). Returns up to 20 most recent matching entries.",
        parameters = listOf(
            ChatToolParam("category", ChatToolParamType.STRING, "One of: FUEL, SERVICE, TIRES, EQUIPMENT, OTHER."),
            ChatToolParam("since", ChatToolParamType.STRING, "Inclusive start date in YYYY-MM-DD format. Omit for no lower bound.", required = false),
            ChatToolParam("until", ChatToolParamType.STRING, "Inclusive end date in YYYY-MM-DD format. Omit for no upper bound.", required = false),
        ),
        execute = { args -> executeGetCostsByCategory(vehicleId, args) },
    )

    private suspend fun executeGetCostsByCategory(
        vehicleId: String,
        args: Map<String, String?>
    ): String {
        val categoryArg = args["category"]?.takeIf { it.isNotBlank() }
            ?: return "Failure: 'category' is required."
        val category = runCatching { CostCategory.valueOf(categoryArg.uppercase(Locale.ROOT)) }.getOrNull()
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
                    val descPart = cost.description?.takeIf { it.isNotBlank() }?.let { " — \"${it.take(60)}\"" }.orEmpty()
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