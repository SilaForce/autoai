package com.example.autoai.presentation.features.chat

import com.example.domain.model.app.onFailure
import com.example.domain.model.app.onSuccess
import com.example.domain.model.chat.ChatTool
import com.example.domain.model.chat.ChatToolParam
import com.example.domain.model.chat.ChatToolParamType
import com.example.domain.model.cost.CostCategory
import com.example.domain.usecase.cost.GetCostStatisticsForPeriodParams
import com.example.domain.usecase.cost.GetCostStatisticsForPeriodUseCase
import com.example.domain.usecase.cost.GetCostsHistoryParams
import com.example.domain.usecase.cost.GetCostsHistoryUseCase
import com.example.domain.usecase.reminder.AddReminderParams
import com.example.domain.usecase.reminder.AddReminderUseCase
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AiChatTools(
    private val addReminderUseCase: AddReminderUseCase,
    private val getCostsHistoryUseCase: GetCostsHistoryUseCase,
    private val getCostStatisticsForPeriodUseCase: GetCostStatisticsForPeriodUseCase,
) {

    fun build(
        userId: String,
        vehicleId: String,
        autoReminderEnabled: Boolean,
    ): List<ChatTool> {
        val tools = mutableListOf<ChatTool>()
        if (autoReminderEnabled) {
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

    private fun money(amount: Double): String =
        String.format(Locale.getDefault(), "%.2f", amount)
}
