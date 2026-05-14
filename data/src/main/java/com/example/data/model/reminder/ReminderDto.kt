package com.example.data.model.reminder

import com.google.firebase.firestore.PropertyName

data class ReminderDto (
    val id: String = "",
    val userId: String = "",
    val vehicleId: String = "",
    val title: String = "",
    val dueDateMillis: Long = 0L,
    val note: String? = null,
    @get:PropertyName("isCompleted") @set:PropertyName("isCompleted")
    var isCompleted: Boolean = false,
)