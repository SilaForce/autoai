package com.example.data.model.reminder

data class ReminderDto (
    val id: String = "",
    val userId: String = "",
    val vehicleId: String = "",
    val title: String = "",
    val dueDateMillis: Long = 0L,
    val note: String? = null,
)