package com.example.autoai.presentation.features.auth.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.autoai.localization.AppStrings
import androidx.compose.material3.MaterialTheme

// Definišemo stanja koja toggle može imati
enum class AuthTab {
    LOGIN, REGISTER
}

@Composable
fun AuthToggle(
    currentTab: AuthTab,
    onTabChanged: (AuthTab) -> Unit,
    modifier: Modifier = Modifier
) {
    // Vanjski sivi kontejner u obliku pilule
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(50))
            .padding(4.dp) // Razmak između ivice kontejnera i unutrašnjih tabova
    ) {
        // Tab za Prijavu
        AuthTabItem(
            text = AppStrings.Auth.loginTab,
            isSelected = currentTab == AuthTab.LOGIN,
            onClick = { onTabChanged(AuthTab.LOGIN) },
            modifier = Modifier.weight(1f)
        )

        // Tab za Registraciju
        AuthTabItem(
            text = AppStrings.Auth.registerTab,
            isSelected = currentTab == AuthTab.REGISTER,
            onClick = { onTabChanged(AuthTab.REGISTER) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun AuthTabItem(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Animacije za boju i sjenku (daju "premium" osjećaj prebacivanja)
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent,
        label = "BgColorAnimation"
    )
    val shadowElevation by animateDpAsState(
        targetValue = if (isSelected) 2.dp else 0.dp,
        label = "ShadowAnimation"
    )

    Box(
        modifier = modifier
            .shadow(shadowElevation, RoundedCornerShape(50))
            .background(backgroundColor, RoundedCornerShape(50))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null, // Gasimo defaultni ripple efekat
                onClick = onClick
            )
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Gray,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
            fontSize = 15.sp
        )
    }
}

// ─── Preview ──────────────────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun AuthTogglePreview() {
    var selectedTab by remember { mutableStateOf(AuthTab.REGISTER) }

    Box(modifier = Modifier.padding(24.dp)) {
        AuthToggle(
            currentTab = selectedTab,
            onTabChanged = { selectedTab = it }
        )
    }
}