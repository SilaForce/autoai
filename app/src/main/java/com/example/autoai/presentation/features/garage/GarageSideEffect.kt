package com.example.autoai.presentation.features.garage

import com.example.autoai.navigation.Route
import com.example.autoai.presentation.util.UiText

sealed interface GarageSideEffect {
    data class ShowError(val message: UiText) : GarageSideEffect
    data class NavigateTo(val route: Route) : GarageSideEffect
    data object ShowUndoableDelete : GarageSideEffect
}
