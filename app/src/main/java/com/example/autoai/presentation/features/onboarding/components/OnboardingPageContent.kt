package com.example.autoai.presentation.features.onboarding.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.MaterialTheme
import com.example.autoai.presentation.theme.VerdantGreen

// 1. Data klasa koja drži podatke za jedan slajd
data class OnboardingPage(
    val icon: ImageVector,
    val title: String,
    val description: String
)

// 2. Composable komponenta koja iscrtava taj slajd
@Composable
fun OnboardingPageContent(
    page: OnboardingPage,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Veliki krug sa ikonicom unutra
        Box(
            modifier = Modifier
                .size(160.dp) // Veličina cijelog kruga
                .clip(CircleShape)
                .background(VerdantGreen.copy(alpha = 0.1f)), // Blaga zelena pozadina (10% opacity)
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = page.icon,
                contentDescription = null,
                tint = VerdantGreen, // Jaka zelena za samu ikonicu
                modifier = Modifier.size(72.dp) // Veličina same ikonice
            )
        }

        Spacer(modifier = Modifier.height(48.dp))

        // Naslov
        Text(
            text = page.title,
            fontSize = 28.sp, // Velik i jasan naslov
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Opis
        Text(
            text = page.description,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp), // Da tekst ne ide skroz do ivice ekrana
            lineHeight = 24.sp
        )
    }
}