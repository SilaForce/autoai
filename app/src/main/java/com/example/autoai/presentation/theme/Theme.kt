package com.example.autoai.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val MinimalistColorScheme = lightColorScheme(
    primary = VerdantGreen,
    onPrimary = PureWhite,
    background = OffWhiteBg,
    onBackground = CharcoalGray,
    surface = PureWhite,
    onSurface = CharcoalGray,
    error = SoftAmber,
    outline = SubtleBorder
)

@Composable
fun AutoAITheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = MinimalistColorScheme,
        // Ovdje kasnije možemo dodati Custom Typography
        content = content
    )
}