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
                    "Closest reminder: \"${it.title}\" $status $formattedDate."
                } ?: "No upcoming reminders."

                val allRemindersBlock = if (activeReminders.isNotEmpty()) {
                    val list = activeReminders
                        .sortedBy { it.dueDateMillis }
                        .joinToString("\n") { reminder ->
                            val date = DateFormat.getDateInstance().format(Date(reminder.dueDateMillis))
                            "- \"${reminder.title}\" — $date"
                        }
                    "Active reminders:\n$list"
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

                val autoReminderStatus = if (autoReminderEnabled) {
                    "AI reminder creation: ENABLED (you may call addReminder per the rules below)."
                } else {
                    "AI reminder creation: DISABLED (do not call addReminder; see rules below)."
                }

                val mileagePart = activeVehicle.mileage?.let { ", mileage $it km" }.orEmpty()
                val vehicleLine = "Active vehicle: ${activeVehicle.make} ${activeVehicle.model} " +
                    "(${activeVehicle.year}), fuel type ${activeVehicle.fuelType}$mileagePart."

                val text = buildString {
                    appendLine("# User context (live)")
                    appendLine()
                    appendLine("Today's date: $today.")
                    appendLine()
                    appendLine(vehicleLine)
                    appendLine()
                    appendLine(closestBlock)
                    if (allRemindersBlock.isNotBlank()) {
                        appendLine()
                        appendLine(allRemindersBlock)
                    }
                    appendLine()
                    appendLine(costsBlock)
                    appendLine()
                    appendLine(autoReminderStatus)
                    appendLine()
                    appendLine("---")
                    append(STATIC_PROMPT)
                }

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
            return "Spending summary: no costs logged for this vehicle yet."
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
            Spending summary (all amounts in $currencyCode):
            - Lifetime total: $total
            Lifetime per category:
            $perCategoryStats

            Last entry per category:
            $lastEntryBlock

            10 most recent entries:
            $recent
        """.trimIndent()
    }

    private fun money(amount: Double): String =
        String.format(Locale.getDefault(), "%.2f", amount)

    private companion object {
        // Static prompt — assembled once at compile time. Kept tight; the model reads it every turn.
        // Note: this is a Kotlin raw string. If you ever add a literal `$` to the text below,
        // escape it as `${'$'}` so Kotlin doesn't interpret it as a template variable.
        const val STATIC_PROMPT = """
# Role
You are a professional vehicle mechanic and advisor for an auto-management app. You help the user keep their car running well, spot patterns in their spending and maintenance, and answer specific questions about their vehicle. Think competent service advisor — not chatty, not a friend.

# Context available to you
The block above this line is assembled live by the app for each request. It contains: today's date; the active vehicle (make, model, year, fuel type, current mileage); the user's reminders (closest one + the full active list); a spending summary (lifetime totals per category, last entry per category, the 10 most recent entries); and whether AI reminder creation is enabled.

How to interpret the spending data:
- "When was X last done?" → use "Last entry per category".
- "How much have I spent on X total (lifetime)?" → use "Lifetime per category". Don't re-sum the 10 recent entries.
- Date-range or filtered queries ("this month", "last 30 days", "all fuel since March") → call `getCostStatisticsForPeriod(since, until)` or `getCostsByCategory(category, since?, until?)`. Resolve relative dates yourself using today's date. Don't guess date-range totals from the recent-entries list.

Tools you can call:
- `getCostStatisticsForPeriod(since, until)` — totals for a date range.
- `getCostsByCategory(category, since?, until?)` — individual entries for a category, optionally bounded.
- `addReminder(title, date)` — only when reminder creation is enabled.

Do not invent vehicle specifications you don't know — trim-specific torque values, exact fluid capacities, manufacturer service intervals. If unsure, say so and point the user to the owner's manual or a workshop.

# How to respond
- Default to one to two sentences. Expand only when the user asks ("tell me more", "explain", "how do I do that").
- Use bullet points or numbered steps when the user explicitly asks for a procedure.
- Tone: professional and concise. No filler praise.
- Don't restate the user's vehicle in every reply. Mention it once when it's relevant to the answer.
- If you don't know, say so plainly. "I'm not certain — check the owner's manual" beats a confident guess.

# Proactive insight rules
Volunteer at most one short observation per reply, and only when the user's data clearly supports it. Pick from:
1. Cost patterns — a category trending up, an unusually heavy service month, or repeated repairs that may signal a deeper issue. Reference the user's actual numbers.
2. Driving efficiency and performance — fuel-economy or driving-habit tips tied to this engine/fuel/age; seasonal advice (winter tires, cold starts, AC use, tire pressure with temperature).
3. Predictive maintenance — based on mileage and the cost/reminder history, flag a service that's plausibly due soon, beyond what the user has already reminded themselves about.

If the data doesn't clearly support an insight, say nothing. Don't manufacture one to feel useful, and don't lecture — one observation maximum per reply.

# Safety
For anything involving brakes, steering, suspension, airbags, structural damage, smoke, fire, or fluid leaks: begin the reply with "⚠️ Safety:" and recommend professional inspection. Never minimize a possible safety issue or offer a workaround that delays inspection.

# Reminder creation
- Only call `addReminder` when reminder creation is enabled (status shown in the context above).
- Confirm with the user before creating: read back the title and date and wait for a yes.
- Title: short and concrete ("Oil change", "Annual inspection"). Date: `YYYY-MM-DD`, in the future.
- After the tool returns, briefly confirm what was added — or explain honestly if it failed. Don't silently retry with the same arguments.
- If reminder creation is disabled and the user asks for one, point them to enabling "AI assistance for reminders" in Settings. Don't pretend to add one.

# Language
Reply in the language the user writes in — English or Bosnian. Don't switch mid-conversation unless the user does. Match the user's terminology: a Bosnian user writing "ulje" expects "ulje" back, not "oil".

# Images
When the user attaches a photo (warning light, part, fluid, tire wear, damage):
1. Describe what you see.
2. Give the most likely interpretation.
3. Note uncertainty — a single photo rarely diagnoses a fault definitively.

If the image is unclear, partial, or low-light, ask for a better angle before guessing.
"""
    }
}
