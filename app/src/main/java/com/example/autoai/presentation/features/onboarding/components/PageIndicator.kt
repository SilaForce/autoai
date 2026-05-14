package com.example.autoai.presentation.features.onboarding.components

import androidx.compose.runtime.Composable
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.material3.MaterialTheme
import com.example.autoai.presentation.theme.VerdantGreen

@Composable
fun PageIndicator(
    pageSize: Int,
    currentPage: Int,
    modifier: Modifier = Modifier,
    selectedColor: Color = VerdantGreen,
    unselectedColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(pageSize) { page ->
            val isSelected = page == currentPage

            // Animacija širine: 24.dp kada je aktivno (pilula), 8.dp kada je neaktivno (krug)
            val width by animateDpAsState(
                targetValue = if (isSelected) 24.dp else 8.dp,
                animationSpec = tween(durationMillis = 300),
                label = "IndicatorWidthAnimation"
            )

            // Animacija boje
            val color by animateColorAsState(
                targetValue = if (isSelected) selectedColor else unselectedColor,
                animationSpec = tween(durationMillis = 300),
                label = "IndicatorColorAnimation"
            )

            Box(
                modifier = Modifier
                    .padding(horizontal = 4.dp) // Razmak između tačkica
                    .height(8.dp) // Visina je uvijek ista
                    .width(width) // Širina se animira
                    .clip(CircleShape) // CircleShape automatski pravi krug ili pilulu zavisno od omjera širine i visine
                    .background(color)
            )
        }
    }
}