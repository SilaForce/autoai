package com.example.autoai.presentation.features.garage

import androidx.compose.runtime.Immutable
import com.example.autoai.presentation.util.UiText

@Immutable
data class GarageVehicleUi(
    val id: String,
    val title: UiText,
    val subtitle: UiText,
    val isActive: Boolean,
)
