package com.example.autoai.presentation.features.chat

import com.example.domain.model.app.onSuccess
import com.example.domain.model.cost.Cost
import com.example.domain.model.cost.CostCategory
import com.example.domain.model.cost.CostStatistics
import com.example.domain.usecase.cost.GetCostStatisticsParams
import com.example.domain.usecase.cost.GetCostStatisticsUseCase
import com.example.domain.usecase.cost.GetCostsHistoryParams
import com.example.domain.usecase.cost.GetCostsHistoryUseCase
import com.example.domain.usecase.reminder.GetRemindersParams
import com.example.domain.usecase.reminder.GetRemindersUseCase
import com.example.domain.usecase.vehicle.GetVehiclesParams
import com.example.domain.usecase.vehicle.GetVehiclesUseCase
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class AiSystemInstruction(
    val text: String,
    val activeVehicleId: String,
)

class AiSystemInstructionBuilder(
    private val getVehiclesUseCase: GetVehiclesUseCase,
    private val getRemindersUseCase: GetRemindersUseCase,
    private val getCostsHistoryUseCase: GetCostsHistoryUseCase,
    private val getCostStatisticsUseCase: GetCostStatisticsUseCase,
) {

    suspend fun build(
        userId: String,
        currency: String,
        autoReminderEnabled: Boolean,
    ): AiSystemInstruction? {
        var result: AiSystemInstruction? = null
        getVehiclesUseCase(GetVehiclesParams(userId)).onSuccess { vehicles ->
            val activeVehicle = vehicles.firstOrNull { it.isActive } ?: return@onSuccess
            getRemindersUseCase(GetRemindersParams(activeVehicle.id)).onSuccess { reminders ->
                val activeReminders = reminders.filter { !it.isCompleted }
                val closestReminder = activeReminders.minByOrNull { it.dueDateMillis }

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
                val costsBlock = buildCostsBlock(costs, stats, currency)

                val autoReminderBlock = if (autoReminderEnabled) {
                    """
                        You have access to a tool named "addReminder" that creates a maintenance reminder for the user's active vehicle. When the user asks to be reminded about a future service or check, call the tool with a short title and a YYYY-MM-DD date in the future. After the tool reports success, confirm in your reply that you added the reminder. If the tool reports failure, explain the problem to the user honestly without retrying with the same arguments.
                        """.trimIndent()
                } else {
                    """
                     Auto-Reminders (DISABLED):
                     You CANNOT create reminders right now. If the user asks you to add a reminder, do NOT pretend to add it. Instead, politely tell them to enable "AI Auto-Reminders" in the Settings screen first.
                     """.trimIndent()
                }

                val text = """
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

                result = AiSystemInstruction(text = text, activeVehicleId = activeVehicle.id)
            }
        }
        return result
    }

    private fun buildCostsBlock(
        costs: List<Cost>,
        stats: CostStatistics?,
        currencyCode: String,
    ): String {
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

    private fun money(amount: Double): String =
        String.format(Locale.getDefault(), "%.2f", amount)
}
