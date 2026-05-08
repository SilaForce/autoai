package com.example.autoai.presentation.features.reminder

import com.example.autoai.presentation.components.BottomNavItem

sealed interface ReminderEvent {
    data object OnAddReminderClicked : ReminderEvent
    data object OnAddSheetDismissed : ReminderEvent

    data class OnTitleChanged(val value: String) : ReminderEvent
    data class OnDateSelected(val dateMillis: Long) : ReminderEvent
    data class OnNoteChanged(val value: String) : ReminderEvent

    data object OnSaveReminderClicked : ReminderEvent
    data class OnNavItemSelected(val item: BottomNavItem) : ReminderEvent
}