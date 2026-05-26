package com.example.autoai.presentation.features.costs.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.autoai.localization.AppStrings
import com.example.autoai.presentation.features.costs.CostsTab

@Composable
fun CostsTabRow(
    selectedTab: CostsTab,
    onTabSelected: (CostsTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Row(modifier = Modifier.padding(4.dp)) {
            CostsTab.entries.forEach { tab ->
                val isSelected = tab == selectedTab
                val label = when (tab) {
                    CostsTab.HISTORY -> AppStrings.Costs.tabHistory
                    CostsTab.STATISTICS -> AppStrings.Costs.tabStatistics
                }
                Surface(
                    onClick = { onTabSelected(tab) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(50),
                    color = if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent,
                ) {
                    Text(
                        text = label,
                        modifier = Modifier.padding(vertical = 10.dp),
                        fontSize = 14.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}
