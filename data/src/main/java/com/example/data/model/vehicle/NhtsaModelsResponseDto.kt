package com.example.data.model.vehicle

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

    @Serializable
    data class NhtsaModelsResponseDto(
        @SerialName("Results") val results: List<NhtsaModelDto> = emptyList()
    )

    @Serializable
    data class NhtsaModelDto(
        @SerialName("Model_Name") val name: String
    )