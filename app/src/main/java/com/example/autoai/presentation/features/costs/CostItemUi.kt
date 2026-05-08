package com.example.autoai.presentation.features.costs

import androidx.compose.ui.graphics.vector.ImageVector

data class CostItemUi(
    val id: String,
    val title: String,
    val subtitle: String,
    val amount: String,
    val categoryIcon: ImageVector,
)

