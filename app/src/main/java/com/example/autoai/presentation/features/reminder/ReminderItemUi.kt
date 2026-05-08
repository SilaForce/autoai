package com.example.autoai.presentation.features.reminder

data class ReminderItemUi (
    val id: String,
    val title: String,
    val formattedDate: String,
    val isUrgent: Boolean,
)