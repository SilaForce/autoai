package com.example.autoai.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = VerdantGreen,
    onPrimary = PureWhite,
    background = OffWhiteBg,
    onBackground = CharcoalGray,
    surface = PureWhite,
    onSurface = CharcoalGray,
    error = SoftAmber,
    outline = SubtleBorder,
    outlineVariant = SubtleBorder,
    surfaceVariant = OffWhiteBg,
)

private val DarkColorScheme = darkColorScheme(
    primary = VerdantGreen,
    onPrimary = PureWhite,
    background = DarkBackground,
    onBackground = DarkText,
    surface = DarkSurface,
    onSurface = DarkText,
    error = SoftAmber,
    outline = DarkBorder,
    outlineVariant = DarkBorder,
    surfaceVariant = DarkIconBg,
)

@Composable
fun AutoAITheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
