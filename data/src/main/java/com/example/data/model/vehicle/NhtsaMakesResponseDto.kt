package com.example.data.model.vehicle

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

    @Serializable
    data class NhtsaMakesResponseDto(
        @SerialName("Results") val results: List<NhtsaMakeDto> = emptyList()
    )

    @Serializable
    data class NhtsaMakeDto(
        @SerialName("MakeName") val name: String
    )
