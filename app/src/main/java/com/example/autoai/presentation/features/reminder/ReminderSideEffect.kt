package com.example.autoai.presentation.features.reminder

import com.example.autoai.presentation.util.UiText

sealed interface RemindersSideEffect {
    data class ShowError(val message: UiText) : RemindersSideEffect
    data class ShowSuccess(val message: UiText) : RemindersSideEffect
}