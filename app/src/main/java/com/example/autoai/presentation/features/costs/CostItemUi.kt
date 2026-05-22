package com.example.autoai.presentation.features.costs

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.autoai.presentation.util.UiText

@Immutable
data class CostItemUi(
    val id: String,
    val title: UiText,
    val subtitle: String,
    val amount: String,
    val categoryIcon: ImageVector,
)
