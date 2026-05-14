package com.example.autoai.presentation.features.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.LocalGasStation
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.QueryStats
import androidx.compose.material3.IconButton
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.autoai.localization.AppStrings
import com.example.autoai.presentation.components.ActionCard
import com.example.autoai.presentation.components.BottomNavItem
import com.example.autoai.presentation.components.BottomNavigationBar
import com.example.autoai.presentation.components.MonthlyCostCard
import com.example.autoai.presentation.components.VehicleCard
import com.example.autoai.presentation.theme.AutoAITheme
import androidx.compose.material3.MaterialTheme

@Composable
fun HomeContent(
    state: HomeState,
    snackbarHostState: SnackbarHostState,
    onEvent: (HomeEvent) -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            bottomBar = {
                BottomNavigationBar(
                    selectedItem = state.selectedNavItem,
                    onItemSelected = { onEvent(HomeEvent.OnNavItemSelected(it)) },
                )
            },
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 20.dp)
                            .verticalScroll(rememberScrollState()),
                    ) {
                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = stringResource(AppStrings.Home.greetingRes, state.userName),
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            IconButton(onClick = { onEvent(HomeEvent.OnProfileClicked) }) {
                                Icon(
                                    imageVector = Icons.Outlined.Person,
                                    contentDescription = "Profil",
                                    tint = MaterialTheme.colorScheme.onSurface,
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // ── Active vehicle card ────────────────────────────
                        // Tapping always navigates to the Garage (to change/add a vehicle)
                        VehicleCard(
                            vehicleName = if (state.hasActiveVehicle) {
                                state.activeVehicleName
                            } else {
                                AppStrings.Home.noActiveVehicleTitle
                            },
                            vehiclePlate = if (state.hasActiveVehicle) {
                                state.activeVehiclePlate.ifBlank {
                                    AppStrings.Home.noActiveVehicleSubtitle
                                }
                            } else {
                                AppStrings.Home.noActiveVehicleSubtitle
                            },
                            onClick = { onEvent(HomeEvent.OnVehicleClicked) },
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // ── Total expenses card ────────────────────────────
                        MonthlyCostCard(
                            label = AppStrings.Home.monthlyCostLabel,
                            amount = state.totalExpenses,
                            currency = "KM",
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // ── Quick action buttons ───────────────────────────
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            ActionCard(
                                label = AppStrings.Costs.tabStatistics,
                                icon = Icons.Outlined.QueryStats,
                                modifier = Modifier.weight(1f),
                                onClick = { onEvent(HomeEvent.OnFuelClicked) },
                            )
                            ActionCard(
                                label = AppStrings.Home.actionService,
                                icon = Icons.Outlined.Build,
                                modifier = Modifier.weight(1f),
                                onClick = { onEvent(HomeEvent.OnServiceClicked) },
                            )
                            ActionCard(
                                label = AppStrings.Home.actionAi,
                                icon = Icons.Outlined.ChatBubbleOutline,
                                modifier = Modifier.weight(1f),
                                onClick = { onEvent(HomeEvent.OnAiClicked) },
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }

        // SnackbarHost at TopCenter — per project architecture rules
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.TopCenter),
        )
    }
}

// ─── Previews ─────────────────────────────────────────────────────────────────

@Preview(showBackground = true)
@Composable
private fun HomeContentWithVehiclePreview() {
    AutoAITheme {
        HomeContent(
            state = HomeState(
                userName = "Amar",
                hasActiveVehicle = true,
                activeVehicleName = "Volkswagen Golf 7",
                activeVehiclePlate = "A12-M-345",
                totalExpenses = "1045",
                selectedNavItem = BottomNavItem.HOME,
            ),
            snackbarHostState = remember { SnackbarHostState() },
            onEvent = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeContentNoVehiclePreview() {
    AutoAITheme {
        HomeContent(
            state = HomeState(
                userName = "Amar",
                hasActiveVehicle = false,
                selectedNavItem = BottomNavItem.HOME,
            ),
            snackbarHostState = remember { SnackbarHostState() },
            onEvent = {},
        )
    }
}
