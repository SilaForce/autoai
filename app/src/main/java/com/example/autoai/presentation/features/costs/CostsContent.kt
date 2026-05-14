package com.example.autoai.presentation.features.costs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.autoai.localization.AppStrings
import com.example.autoai.presentation.components.AutoAiTextField
import com.example.autoai.presentation.components.BottomNavItem
import com.example.autoai.presentation.components.BottomNavigationBar
import com.example.autoai.presentation.components.MainButton
import com.example.autoai.presentation.features.costs.components.CategorySelectionRow
import com.example.autoai.presentation.features.costs.components.CostHistoryCard
import com.example.autoai.presentation.features.costs.components.CostProgressBar
import com.example.autoai.presentation.theme.AutoAITheme
import com.example.autoai.presentation.theme.VerdantGreen
import com.example.domain.model.cost.CostCategory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CostsContent(
    state: CostsState,
    snackbarHostState: SnackbarHostState,
    onEvent: (CostsEvent) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { onEvent(CostsEvent.OnAddCostClicked) },
                    shape = CircleShape,
                    containerColor = VerdantGreen,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Add,
                        contentDescription = AppStrings.Costs.addFabDescription,
                    )
                }
            },
            bottomBar = {
                BottomNavigationBar(
                    selectedItem = state.selectedNavItem,
                    onItemSelected = { onEvent(CostsEvent.OnNavItemSelected(it)) },
                )
            },
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 20.dp),
            ) {
                Spacer(modifier = Modifier.height(28.dp))

                Text(
                    text = AppStrings.Costs.title,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Spacer(modifier = Modifier.height(16.dp))

                CostsTabRow(
                    selectedTab = state.selectedTab,
                    onTabSelected = { onEvent(CostsEvent.OnTabSelected(it)) },
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (state.isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else if (state.hasNoActiveVehicle) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = AppStrings.Costs.noActiveVehicleTitle,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = AppStrings.Costs.noActiveVehicleSubtitle,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                            )
                        }
                    }
                } else {
                    when (state.selectedTab) {
                        CostsTab.HISTORY -> HistoryTab(state = state)
                        CostsTab.STATISTICS -> StatisticsTab(state = state)
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.TopCenter),
        )

        if (state.isSheetOpen) {
            ModalBottomSheet(
                onDismissRequest = { onEvent(CostsEvent.OnAddSheetDismissed) },
                sheetState = sheetState,
                containerColor = MaterialTheme.colorScheme.surface,
            ) {
                AddCostSheetContent(state = state, onEvent = onEvent)
            }
        }
    }
}

// ─── Tab Row ──────────────────────────────────────────────────────────────────

@Composable
private fun CostsTabRow(
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
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    )
                }
            }
        }
    }
}

// ─── History Tab ──────────────────────────────────────────────────────────────

@Composable
private fun HistoryTab(
    state: CostsState,
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
        Card(
            modifier = modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        ) {
            LazyColumn(
                modifier = Modifier.padding(horizontal = 16.dp),
            ) {
                itemsIndexed(
                    items = state.history,
                    key = { _, item -> item.id },
                ) { index, item ->
                    CostHistoryCard(
                        title = item.title,
                        subtitle = item.subtitle,
                        amount = item.amount,
                        categoryIcon = item.categoryIcon,
                    )
                    if (index < state.history.lastIndex) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    }
                }
            }
        }
    }
}

@Composable
private fun StatisticsTab(
    state: CostsState,
    modifier: Modifier = Modifier,
) {
    val stats = state.stats

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Total card
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

        // Per-category card
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
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(
                                text = breakdown.categoryName,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            )
                            Text(
                                text = breakdown.amount,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        CostProgressBar(progress = breakdown.progress)
                    }
                }
            }
        }
    }
}

// ─── Add Cost Bottom Sheet ────────────────────────────────────────────────────

@Composable
private fun AddCostSheetContent(
    state: CostsState,
    onEvent: (CostsEvent) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .imePadding(),
    ) {
        Text(
            text = AppStrings.Costs.addSheetTitle,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.align(Alignment.CenterHorizontally),
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = AppStrings.Costs.categoryLabel,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Spacer(modifier = Modifier.height(10.dp))

        CategorySelectionRow(
            selectedCategory = state.selectedCategory,
            onCategorySelected = { onEvent(CostsEvent.OnCategorySelected(it)) },
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            AutoAiTextField(
                modifier = Modifier.weight(1f),
                value = state.amountInput,
                onValueChange = { onEvent(CostsEvent.OnAmountChanged(it)) },
                label = AppStrings.Costs.amountLabel,
                placeholder = AppStrings.Costs.amountPlaceholder,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            )
            AutoAiTextField(
                modifier = Modifier.weight(1f),
                value = state.locationInput,
                onValueChange = { onEvent(CostsEvent.OnLocationChanged(it)) },
                label = AppStrings.Costs.locationLabel,
                placeholder = AppStrings.Costs.locationPlaceholder,
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        AutoAiTextField(
            value = state.descriptionInput,
            onValueChange = { onEvent(CostsEvent.OnDescriptionChanged(it)) },
            label = AppStrings.Costs.descriptionLabel,
            placeholder = AppStrings.Costs.descriptionPlaceholder,
        )

        Spacer(modifier = Modifier.height(24.dp))

        MainButton(
            text = AppStrings.Costs.saveButton,
            onClick = { onEvent(CostsEvent.OnSaveCostClicked) },
            enabled = !state.isSaving,
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

// ─── Preview ──────────────────────────────────────────────────────────────────

@Preview(showBackground = true)
@Composable
private fun CostsContentPreview() {
    AutoAITheme {
        CostsContent(
            state = CostsState(
                selectedTab = CostsTab.HISTORY,
                selectedNavItem = BottomNavItem.COSTS,
                history = listOf(
                    CostItemUi(
                        id = "1",
                        title = "Punjenje rezervoara - Shell",
                        subtitle = "Apr 25",
                        amount = "120 KM",
                        categoryIcon = CostCategory.FUEL.toIcon(),
                    ),
                    CostItemUi(
                        id = "2",
                        title = "Mali servis - zamjena ulja i filtera",
                        subtitle = "Apr 20",
                        amount = "350 KM",
                        categoryIcon = CostCategory.SERVICE.toIcon(),
                    ),
                ),
            ),
            snackbarHostState = remember { SnackbarHostState() },
            onEvent = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CostsStatisticsPreview() {
    AutoAITheme {
        CostsContent(
            state = CostsState(
                selectedTab = CostsTab.STATISTICS,
                selectedNavItem = BottomNavItem.COSTS,
                stats = CostStatsUi(
                    totalAmount = "1045 KM",
                    categoryBreakdowns = listOf(
                        CostStatsByCategoryUi("Gorivo", "215 KM", 0.45f),
                        CostStatsByCategoryUi("Servis", "350 KM", 0.73f),
                        CostStatsByCategoryUi("Ostalo", "480 KM", 1.0f),
                    ),
                ),
            ),
            snackbarHostState = remember { SnackbarHostState() },
            onEvent = {},
        )
    }
}




