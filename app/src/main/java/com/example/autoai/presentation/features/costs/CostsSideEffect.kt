package com.example.autoai.presentation.features.costs

import com.example.autoai.presentation.util.UiText

sealed interface CostsSideEffect {
    data class ShowError(val message: UiText) : CostsSideEffect
    data class ShowSuccess(val message: UiText) : CostsSideEffect
}



