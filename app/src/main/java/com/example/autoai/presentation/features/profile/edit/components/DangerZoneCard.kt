package com.example.autoai.presentation.features.profile.edit.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.autoai.localization.AppStrings
import com.example.autoai.presentation.theme.DangerRed

@Composable
fun DangerZoneCard(
    isDeleting: Boolean,
    onDeleteClick: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, DangerRed.copy(alpha = 0.3f)),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Text(
                text = AppStrings.EditProfile.dangerZone,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = DangerRed.copy(alpha = 0.8f),
                modifier = Modifier.padding(bottom = 12.dp),
            )

            TextButton(
                onClick = onDeleteClick,
                enabled = !isDeleting,
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (isDeleting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = DangerRed,
                        strokeWidth = 2.dp,
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.DeleteForever,
                        contentDescription = null,
                        tint = DangerRed,
                        modifier = Modifier
                            .size(20.dp)
                            .padding(end = 4.dp),
                    )
                    Text(
                        text = AppStrings.EditProfile.deleteAccount,
                        color = DangerRed,
                        fontWeight = FontWeight.Medium,
                        fontSize = 15.sp,
                    )
                }
            }
        }
    }
}
