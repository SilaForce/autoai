package com.example.autoai.presentation.features.reminder.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.autoai.presentation.features.reminder.ReminderItemUi
import androidx.compose.material3.MaterialTheme
import com.example.autoai.presentation.theme.SoftAmber
import com.example.autoai.presentation.theme.VerdantGreen

@Composable
fun ReminderCard(
    item: ReminderItemUi,
    modifier: Modifier = Modifier
) {
    // Određivanje boja na osnovu hitnosti
    val dotColor = if (item.isUrgent) SoftAmber else VerdantGreen
    val dateColor = if (item.isUrgent) SoftAmber else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Obojena tačkica indikatora
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(dotColor)
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Naziv podsjetnika
            Text(
                text = item.title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Datum isteka
            Text(
                text = item.formattedDate.toString(),
                fontSize = 14.sp,
                fontWeight = if (item.isUrgent) FontWeight.SemiBold else FontWeight.Normal,
                color = dateColor,
            )
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    }
}