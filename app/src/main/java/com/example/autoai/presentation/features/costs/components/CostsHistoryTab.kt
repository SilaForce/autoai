package com.example.autoai.presentation.features.costs.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.autoai.localization.AppStrings
import com.example.autoai.presentation.features.costs.CostsEvent
import com.example.autoai.presentation.features.costs.CostsState

@Composable
fun CostsHistoryTab(
    state: CostsState,
    onEvent: (CostsEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (state.history.isEmpty()) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = AppStrings.Costs.emptyTitle,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = AppStrings.Costs.emptyDescription,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                )
            }
        }
    } else {
        LazyColumn(
            modifier = modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            itemsIndexed(
                items = state.history,
                key = { _, item -> item.id },
            ) { _, item ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                ) {
                    Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                        CostHistoryCard(
                            title = item.title.asString(),
                            subtitle = item.subtitle,
                            amount = item.amount,
                            categoryIcon = item.categoryIcon,
                            onLongClick = { onEvent(CostsEvent.OnCostLongPressed(item.id)) },
                        )
                        DropdownMenu(
                            expanded = state.costMenuId == item.id,
                            onDismissRequest = { onEvent(CostsEvent.OnDismissCostMenu) },
                        ) {
                            DropdownMenuItem(
                                text = { Text(AppStrings.Costs.menuEdit) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Outlined.Edit,
                                        contentDescription = null,
                                    )
                                },
                                onClick = { onEvent(CostsEvent.OnEditCostClicked(item.id)) },
                            )
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = AppStrings.Costs.menuDelete,
                                        color = MaterialTheme.colorScheme.error,
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Outlined.Delete,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error,
                                    )
                                },
                                onClick = { onEvent(CostsEvent.OnDeleteCostClicked(item.id)) },
                            )
                        }
                    }
                }
            }
        }
    }
}
