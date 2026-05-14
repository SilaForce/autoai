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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.autoai.presentation.features.reminder.ReminderItemUi
import com.example.autoai.presentation.theme.DangerRed
import com.example.autoai.presentation.theme.SoftAmber
import com.example.autoai.presentation.theme.VerdantGreen

@Composable
fun ReminderCard(
    item: ReminderItemUi,
    onToggleCompleted: () -> Unit,
    onEditClicked: () -> Unit,
    onDeleteClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val dotColor = if (item.isCompleted) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
        else if (item.isUrgent) SoftAmber else VerdantGreen
    val dateColor = if (item.isCompleted) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
        else if (item.isUrgent) SoftAmber else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
    val titleColor = if (item.isCompleted) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
        else MaterialTheme.colorScheme.onSurface

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = item.isCompleted,
                onCheckedChange = { onToggleCompleted() },
                colors = CheckboxDefaults.colors(
                    checkedColor = VerdantGreen,
                    uncheckedColor = dotColor
                ),
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = titleColor,
                    textDecoration = if (item.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                )
                Text(
                    text = item.formattedDate,
                    fontSize = 12.sp,
                    fontWeight = if (item.isUrgent && !item.isCompleted) FontWeight.SemiBold else FontWeight.Normal,
                    color = dateColor,
                )
            }

            if (!item.isCompleted) {
                IconButton(onClick = onEditClicked, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = Icons.Outlined.Edit,
                        contentDescription = "Edit",
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            IconButton(onClick = onDeleteClicked, modifier = Modifier.size(36.dp)) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = "Delete",
                    tint = DangerRed.copy(alpha = 0.6f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    }
}
