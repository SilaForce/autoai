package com.example.autoai.presentation.features.chat

import com.example.domain.model.chat.ChatThread
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class ChatThreadUi(
    val id: String,
    val title: String,
    val formattedSubtitle: String,
)

fun ChatThread.toUiModel(now: Long = System.currentTimeMillis()): ChatThreadUi {
    return ChatThreadUi(
        id = id,
        title = title.ifBlank { "New chat" },
        formattedSubtitle = formatRelativeDay(updatedAt, now),
    )
}

private fun formatRelativeDay(timestamp: Long, now: Long): String {
    val cal = Calendar.getInstance().apply { timeInMillis = timestamp }
    val today = Calendar.getInstance().apply { timeInMillis = now }
    val yesterday = (today.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, -1) }

    return when {
        sameDay(cal, today) -> "Today"
        sameDay(cal, yesterday) -> "Yesterday"
        else -> SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(timestamp))
    }
}

private fun sameDay(a: Calendar, b: Calendar): Boolean =
    a.get(Calendar.YEAR) == b.get(Calendar.YEAR) &&
        a.get(Calendar.DAY_OF_YEAR) == b.get(Calendar.DAY_OF_YEAR)
