package com.example.autoai.presentation.features.costs

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.autoai.localization.AppStrings
import com.example.autoai.presentation.components.BottomNavItem
import com.example.autoai.presentation.components.BottomNavigationBar
import com.example.autoai.presentation.components.DeleteConfirmationDialog
import com.example.autoai.presentation.features.costs.components.AddCostSheet
import com.example.autoai.presentation.features.costs.components.CostsHistoryTab
import com.example.autoai.presentation.features.costs.components.CostsStatisticsTab
import com.example.autoai.presentation.features.costs.components.CostsTabRow
import com.example.autoai.presentation.theme.AutoAITheme
import com.example.autoai.presentation.theme.VerdantGreen
import com.example.autoai.presentation.util.UiText
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
                    selectedItem = BottomNavItem.COSTS,
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
                        CostsTab.HISTORY -> CostsHistoryTab(state = state, onEvent = onEvent)
                        CostsTab.STATISTICS -> CostsStatisticsTab(state = state, onEvent = onEvent)
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
                AddCostSheet(state = state, onEvent = onEvent)
            }
        }
    }

    if (state.pendingDeleteCostId != null) {
        DeleteConfirmationDialog(
            title = AppStrings.Costs.deleteDialogTitle,
            message = AppStrings.Costs.deleteDialogMessage,
            confirmLabel = AppStrings.Costs.deleteConfirm,
            cancelLabel = AppStrings.Costs.deleteCancel,
            onConfirm = { onEvent(CostsEvent.OnConfirmDeleteCost) },
            onDismiss = { onEvent(CostsEvent.OnDismissDeleteDialog) },
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CostsContentPreview() {
    AutoAITheme {
        CostsContent(
            state = CostsState(
                selectedTab = CostsTab.HISTORY,
                history = listOf(
                    CostItemUi(
                        id = "1",
                        title = UiText.DynamicString("Fuel up - Shell"),
                        subtitle = "Apr 25",
                        amount = "120 KM",
                        categoryIcon = CostCategory.FUEL.toIcon(),
                    ),
                    CostItemUi(
                        id = "2",
                        title = UiText.DynamicString("Oil change service"),
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
                stats = CostStatsUi(
                    totalAmount = "1045 KM",
                    categoryBreakdowns = listOf(
                        CostStatsByCategoryUi(
                            category = CostCategory.OTHER,
                            categoryName = UiText.DynamicString("Other"),
                            amount = "480 KM",
                            percentage = 46,
                            count = 5,
                            averagePerEntry = "96 KM",
                            progress = 0.46f,
                        ),
                        CostStatsByCategoryUi(
                            category = CostCategory.SERVICE,
                            categoryName = UiText.DynamicString("Service"),
                            amount = "350 KM",
                            percentage = 33,
                            count = 4,
                            averagePerEntry = "87 KM",
                            progress = 0.33f,
                        ),
                        CostStatsByCategoryUi(
                            category = CostCategory.FUEL,
                            categoryName = UiText.DynamicString("Fuel"),
                            amount = "215 KM",
                            percentage = 21,
                            count = 7,
                            averagePerEntry = "31 KM",
                            progress = 0.21f,
                        ),
                    ),
                ),
            ),
            snackbarHostState = remember { SnackbarHostState() },
            onEvent = {},
        )
    }
}
