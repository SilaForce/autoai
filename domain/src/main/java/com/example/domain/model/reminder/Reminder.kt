package com.example.domain.model.reminder

data class Reminder (
    val id: String,
    val userId: String,
    val vehicleId: String,
    val title: String,
    val dueDateMillis: Long,
    val note: String?,
    val isCompleted: Boolean = false,
)