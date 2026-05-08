package com.example.autoai.presentation.features.reminder

import com.example.domain.model.reminder.Reminder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private fun dateFormatter() = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())

fun Reminder.toReminderItemUi(): ReminderItemUi {

    val daysLeftInMillis = dueDateMillis - System.currentTimeMillis()
    val fourteenDaysInMillis = 14 * 24 * 60 * 60 * 1000L
    val isUrgent = daysLeftInMillis <= fourteenDaysInMillis

    return ReminderItemUi(
        id = id,
        title = title,
        formattedDate = "Do: ${dateFormatter().format(Date(dueDateMillis))}",
        isUrgent = isUrgent
    )
}