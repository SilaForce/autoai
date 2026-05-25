package com.example.autoai.presentation.features.reminder

import com.example.autoai.presentation.components.BottomNavItem

data class ReminderState (
    val isLoading: Boolean = false,
    val hasNoActiveVehicle: Boolean = false,
    val reminders: List<ReminderItemUi> = emptyList(),
    val selectedNavItem: BottomNavItem = BottomNavItem.REMINDERS,

    // Bottom Sheet (Add / Edit)
    val isSheetOpen: Boolean = false,
    val editingReminderId: String? = null,
    val titleInput: String = "",
    val dueDateMillis: Long? = null,
    val noteInput: String = "",
    val isSaving: Boolean = false,

    // Set when the user taps delete on a reminder; null otherwise. Drives the confirmation dialog.
    val pendingDeleteReminderId: String? = null,
)