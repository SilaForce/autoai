package com.example.autoai.presentation.features.garage.add

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.activity.compose.BackHandler
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.autoai.localization.AppStrings
import com.example.autoai.presentation.components.AutoAiTextField
import com.example.autoai.presentation.components.MainButton
import com.example.autoai.presentation.features.garage.add.components.FuelTypeSelector
import com.example.autoai.presentation.features.garage.add.components.YearPickerDialog
import com.example.autoai.presentation.theme.AutoAITheme
import com.example.autoai.presentation.theme.CharcoalGray

@Composable
fun AddVehicleContent(
    state: AddVehicleState,
    snackbarHostState: SnackbarHostState,
    onEvent: (AddVehicleEvent) -> Unit,
) {
    val focusRequester = remember { FocusRequester() }

    BackHandler {
        onEvent(AddVehicleEvent.OnBackClicked)
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .imePadding(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = CenterVertically,
            ) {
                IconButton(onClick = { onEvent(AddVehicleEvent.OnBackClicked) }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = AppStrings.Common.back,
                    )
                }

                Text(
                    text = AppStrings.AddVehicle.title,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = CharcoalGray,
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = AppStrings.AddVehicle.subtitle,
                fontSize = 15.sp,
                color = CharcoalGray.copy(alpha = 0.65f),
            )

            Spacer(modifier = Modifier.height(24.dp))

            AutoAiTextField(
                modifier = Modifier.focusRequester(focusRequester),
                value = state.make,
                onValueChange = { onEvent(AddVehicleEvent.OnMakeChanged(it)) },
                label = AppStrings.AddVehicle.makeLabel,
                placeholder = AppStrings.AddVehicle.makePlaceholder,
            )

            Spacer(modifier = Modifier.height(16.dp))

            AutoAiTextField(
                value = state.model,
                onValueChange = { onEvent(AddVehicleEvent.OnModelChanged(it)) },
                label = AppStrings.AddVehicle.modelLabel,
                placeholder = AppStrings.AddVehicle.modelPlaceholder,
            )

            Spacer(modifier = Modifier.height(16.dp))

            AutoAiTextField(
                value = state.year,
                onValueChange = {},
                label = AppStrings.AddVehicle.yearLabel,
                placeholder = AppStrings.AddVehicle.yearPlaceholder,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                readOnly = true,
                onClick = { onEvent(AddVehicleEvent.OnYearFieldClicked) },
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = AppStrings.AddVehicle.fuelTypeLabel,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = CharcoalGray,
            )

            Spacer(modifier = Modifier.height(8.dp))

            FuelTypeSelector(
                options = state.fuelTypeOptions,
                onFuelTypeSelected = { onEvent(AddVehicleEvent.OnFuelTypeSelected(it)) },
            )

            Spacer(modifier = Modifier.height(16.dp))

            AutoAiTextField(
                value = state.mileage,
                onValueChange = { onEvent(AddVehicleEvent.OnMileageChanged(it)) },
                label = AppStrings.AddVehicle.mileageLabel,
                placeholder = AppStrings.AddVehicle.mileagePlaceholder,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            )

            Spacer(modifier = Modifier.height(16.dp))

            AutoAiTextField(
                value = state.licensePlate,
                onValueChange = { onEvent(AddVehicleEvent.OnLicensePlateChanged(it)) },
                label = AppStrings.AddVehicle.licensePlateLabel,
                placeholder = AppStrings.AddVehicle.licensePlatePlaceholder,
            )

            Spacer(modifier = Modifier.height(24.dp))

            MainButton(
                text = AppStrings.AddVehicle.saveButton,
                onClick = { onEvent(AddVehicleEvent.OnSaveClicked) },
                enabled = !state.isLoading,
            )

            Spacer(modifier = Modifier.height(24.dp))
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.TopCenter),
        )

        if (state.showDiscardDialog) {
            AlertDialog(
                onDismissRequest = { onEvent(AddVehicleEvent.OnDiscardDialogDismissed) },
                title = {
                    Text(text = AppStrings.AddVehicle.discardDialogTitle)
                },
                text = {
                    Text(text = AppStrings.AddVehicle.discardDialogMessage)
                },
                confirmButton = {
                    TextButton(onClick = { onEvent(AddVehicleEvent.OnDiscardChangesConfirmed) }) {
                        Text(text = AppStrings.AddVehicle.discardDialogConfirm)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { onEvent(AddVehicleEvent.OnDiscardDialogDismissed) }) {
                        Text(text = AppStrings.AddVehicle.discardDialogDismiss)
                    }
                },
            )
        }

        if (state.showYearPicker) {
            YearPickerDialog(
                onDismissRequest = { onEvent(AddVehicleEvent.OnYearPickerDismissed) },
                onYearSelected = { year -> onEvent(AddVehicleEvent.OnYearSelected(year)) },
                title = AppStrings.AddVehicle.yearPickerTitle,
                cancelText = AppStrings.AddVehicle.yearPickerCancel,
                confirmText = AppStrings.AddVehicle.yearPickerConfirm,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AddVehicleContentPreview() {
    AutoAITheme {
        AddVehicleContent(
            state = AddVehicleState(
                make = "Volkswagen",
                model = "Golf",
                year = "2020",
                mileage = "120000",
                licensePlate = "A12-M-345",
                selectedFuelType = com.example.domain.model.vehicle.FuelType.DIZEL,
                fuelTypeOptions = com.example.domain.model.vehicle.FuelType.entries.map { fuelType ->
                    fuelType.toFuelTypeOptionUi(isSelected = fuelType == com.example.domain.model.vehicle.FuelType.DIZEL)
                },
            ),
            snackbarHostState = remember { SnackbarHostState() },
            onEvent = {},
        )
    }
}


