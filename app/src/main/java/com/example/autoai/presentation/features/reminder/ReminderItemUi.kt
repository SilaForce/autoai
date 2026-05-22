package com.example.autoai.presentation.features.reminder

import androidx.compose.runtime.Immutable

@Immutable
data class ReminderItemUi (
    val id: String,
    val title: String,
    val note: String?,
    val dueDateMillis: Long,
    val formattedDate: String,
    val isUrgent: Boolean,
    val isCompleted: Boolean,
)