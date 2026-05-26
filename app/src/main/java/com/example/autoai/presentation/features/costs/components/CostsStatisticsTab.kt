package com.example.autoai.presentation.features.costs.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.autoai.R
import com.example.autoai.localization.AppStrings
import com.example.autoai.presentation.features.costs.CostStatsByCategoryUi
import com.example.autoai.presentation.features.costs.CostsEvent
import com.example.autoai.presentation.features.costs.CostsState
import com.example.autoai.presentation.features.costs.StatsPeriod
import com.example.autoai.presentation.theme.VerdantGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CostsStatisticsTab(
    state: CostsState,
    onEvent: (CostsEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val stats = state.stats

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        PeriodChipRow(
            selected = state.selectedPeriod,
            onSelected = { onEvent(CostsEvent.OnPeriodSelected(it)) },
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = AppStrings.Costs.statsTotalLabel,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stats?.totalAmount ?: "0 KM",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }

        if (stats != null && stats.categoryBreakdowns.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = AppStrings.Costs.statsByCategoryLabel,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    stats.categoryBreakdowns.forEachIndexed { index, breakdown ->
                        if (index > 0) Spacer(modifier = Modifier.height(16.dp))
                        CategoryStatRow(breakdown = breakdown)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Composable
private fun CategoryStatRow(breakdown: CostStatsByCategoryUi) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = breakdown.categoryName.asString(),
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = "${breakdown.percentage}%",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
            modifier = Modifier.padding(end = 12.dp),
        )
        Text(
            text = breakdown.amount,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
    Spacer(modifier = Modifier.height(4.dp))
    Text(
        text = "${pluralStringResource(R.plurals.costs_stats_entries, breakdown.count, breakdown.count)} · ${stringResource(R.string.costs_stats_avg_format, breakdown.averagePerEntry)}",
        fontSize = 12.sp,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
    )
    Spacer(modifier = Modifier.height(6.dp))
    CostProgressBar(progress = breakdown.progress)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PeriodChipRow(
    selected: StatsPeriod,
    onSelected: (StatsPeriod) -> Unit,
) {
    val periods = listOf(
        StatsPeriod.MONTH to AppStrings.Costs.periodMonth,
        StatsPeriod.LAST_3_MONTHS to AppStrings.Costs.period3Months,
        StatsPeriod.YEAR to AppStrings.Costs.periodYear,
        StatsPeriod.ALL_TIME to AppStrings.Costs.periodAll,
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        periods.forEach { (period, label) ->
            FilterChip(
                selected = selected == period,
                onClick = { onSelected(period) },
                label = {
                    Text(
                        text = label,
                        fontSize = 12.sp,
                        maxLines = 1,
                        softWrap = false,
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = VerdantGreen.copy(alpha = 0.15f),
                    selectedLabelColor = VerdantGreen,
                ),
            )
        }
    }
}
