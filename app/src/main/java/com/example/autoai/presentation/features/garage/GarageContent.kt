package com.example.autoai.presentation.features.garage

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.autoai.R
import com.example.autoai.localization.AppStrings
import com.example.autoai.presentation.components.BottomNavigationBar
import com.example.autoai.presentation.components.MainButton
import com.example.autoai.presentation.components.VehicleCard
import com.example.autoai.presentation.theme.AutoAITheme
import com.example.autoai.presentation.theme.CharcoalGray
import com.example.autoai.presentation.theme.OffWhiteBg
import com.example.autoai.presentation.util.UiText

@Composable
fun GarageContent(
    state: GarageState,
    snackbarHostState: SnackbarHostState,
    onEvent: (GarageEvent) -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = OffWhiteBg,
            bottomBar = {
                BottomNavigationBar(
                    selectedItem = state.selectedNavItem,
                    onItemSelected = { onEvent(GarageEvent.OnNavItemSelected(it)) },
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
                } else if (state.isUpdatingActiveVehicle) {
                    // Show current list with a semi-transparent overlay while updating
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        item { Spacer(modifier = Modifier.height(28.dp)) }
                        items(
                            items = state.vehicles,
                            key = { vehicle -> vehicle.id },
                        ) { vehicle ->
                            VehicleCard(
                                vehicleName = vehicle.title.asString(),
                                vehiclePlate = vehicle.subtitle.asString(),
                                isActive = vehicle.isActive,
                                onClick = {},
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator()
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        item {
                            Spacer(modifier = Modifier.height(28.dp))
                            Text(
                                text = AppStrings.Garage.title,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = CharcoalGray,
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = AppStrings.Garage.subtitle,
                                fontSize = 15.sp,
                                color = CharcoalGray.copy(alpha = 0.65f),
                            )
                        }

                        if (state.vehicles.isEmpty()) {
                            item {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                ) {
                                    Text(
                                        text = AppStrings.Garage.emptyTitle,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = CharcoalGray,
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = AppStrings.Garage.emptyDescription,
                                        fontSize = 14.sp,
                                        color = CharcoalGray.copy(alpha = 0.65f),
                                    )
                                    Spacer(modifier = Modifier.height(24.dp))
                                    MainButton(
                                        text = AppStrings.Garage.addVehicle,
                                        trailingIcon = Icons.Outlined.Add,
                                        onClick = { onEvent(GarageEvent.OnAddVehicleClicked) },
                                        modifier = Modifier.fillMaxWidth(),
                                    )
                                }
                            }
                        } else {
                            items(
                                items = state.vehicles,
                                key = { vehicle -> vehicle.id },
                            ) { vehicle ->
                                VehicleCard(
                                    vehicleName = vehicle.title.asString(),
                                    vehiclePlate = vehicle.subtitle.asString(),
                                    isActive = vehicle.isActive,
                                    onClick = { onEvent(GarageEvent.OnVehicleSelected(vehicle.id)) },
                                )
                            }

                            item {
                                MainButton(
                                    text = AppStrings.Garage.addVehicle,
                                    onClick = { onEvent(GarageEvent.OnAddVehicleClicked) },
                                    modifier = Modifier.fillMaxWidth(),
                                )
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(24.dp))
                        }
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.TopCenter),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun GarageContentPreview() {
    AutoAITheme {
        GarageContent(
            state = GarageState(
                vehicles = listOf(
                    GarageVehicleUi(
                        id = "1",
                        title = UiText.DynamicString("Volkswagen Golf"),
                        subtitle = UiText.DynamicString("A12-M-345"),
                        isActive = true,
                    ),
                    GarageVehicleUi(
                        id = "2",
                        title = UiText.DynamicString("Audi A4"),
                        subtitle = UiText.StringResource(R.string.garage_no_license_plate),
                        isActive = false,
                    ),
                )
            ),
            snackbarHostState = remember { SnackbarHostState() },
            onEvent = {},
        )
    }
}

