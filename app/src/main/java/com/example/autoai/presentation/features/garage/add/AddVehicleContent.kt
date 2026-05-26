package com.example.autoai.presentation.features.garage.add

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.autoai.localization.AppStrings
import com.example.autoai.presentation.components.AutoAiTextField
import com.example.autoai.presentation.components.MainButton
import com.example.autoai.presentation.features.garage.add.components.FuelTypeSelector
import com.example.autoai.presentation.features.garage.add.components.PhotoSourceSheet
import com.example.autoai.presentation.features.garage.add.components.VehicleAutocompleteField
import com.example.autoai.presentation.features.garage.add.components.VehiclePhotoSection
import com.example.autoai.presentation.features.garage.add.components.YearPickerDialog
import com.example.autoai.presentation.theme.AutoAITheme
import com.example.domain.model.vehicle.FuelType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.time.Year

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddVehicleContent(
    state: AddVehicleState,
    snackbarHostState: SnackbarHostState,
    onEvent: (AddVehicleEvent) -> Unit,
) {
    val focusRequester = remember { FocusRequester() }
    val context = LocalContext.current
    val ioScope = rememberCoroutineScope()

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                // Read MediaStore bytes off Main — compression happens in the VM on Default.
                ioScope.launch {
                    val bytes = withContext(Dispatchers.IO) {
                        context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                    }
                    if (bytes != null) onEvent(AddVehicleEvent.OnPhotoSelected(bytes))
                }
            }
        }
    )

    // Stable URI per screen instance — TakePicture writes here, then we read the bytes back.
    val cameraImageUri = remember {
        val cameraDir = File(context.cacheDir, "camera_images").apply { mkdirs() }
        val imageFile = File(cameraDir, "vehicle_photo.jpg")
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", imageFile)
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            if (success) {
                ioScope.launch {
                    val bytes = withContext(Dispatchers.IO) {
                        context.contentResolver.openInputStream(cameraImageUri)?.use { it.readBytes() }
                    }
                    if (bytes != null) onEvent(AddVehicleEvent.OnPhotoSelected(bytes))
                }
            }
        }
    )

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            if (granted) cameraLauncher.launch(cameraImageUri)
        }
    )

    fun launchCamera() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED
        ) {
            cameraLauncher.launch(cameraImageUri)
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    BackHandler {
        onEvent(AddVehicleEvent.OnBackClicked)
    }

    LaunchedEffect(state.isEditMode, state.isLoading) {
        if (!state.isEditMode && !state.isLoading) focusRequester.requestFocus()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .systemBarsPadding()
            .imePadding(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Spacer(modifier = Modifier.height(28.dp))

            Row(
                verticalAlignment = CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                IconButton(onClick = { onEvent(AddVehicleEvent.OnBackClicked) }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = AppStrings.Common.back,
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                }

                Text(
                    text = if (state.isEditMode) AppStrings.AddVehicle.editTitle else AppStrings.AddVehicle.title,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (state.isEditMode) AppStrings.AddVehicle.editSubtitle else AppStrings.AddVehicle.subtitle,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
            )

            Spacer(modifier = Modifier.height(24.dp))

            VehiclePhotoSection(
                existingPhotoBase64 = state.existingPhotoBase64,
                selectedPhotoBytes = state.selectedPhotoBytes,
                onClick = { onEvent(AddVehicleEvent.OnPhotoFieldClicked) },
                onRemoveClick = { onEvent(AddVehicleEvent.OnRemovePhoto) },
            )

            Spacer(modifier = Modifier.height(20.dp))

            VehicleAutocompleteField(
                value = state.make,
                onValueChange = { onEvent(AddVehicleEvent.OnMakeChanged(it)) },
                label = AppStrings.AddVehicle.makeLabel,
                placeholder = AppStrings.AddVehicle.makePlaceholder,
                options = state.filteredMakes,
                onOptionSelected = { onEvent(AddVehicleEvent.OnMakeSelected(it)) },
                isExpanded = state.isMakesDropdownExpanded,
                onExpandedChange = { onEvent(AddVehicleEvent.OnMakeDropdownExpandedChange(it)) },
                isLoading = state.isMakesLoading,
                modifier = Modifier.focusRequester(focusRequester),
            )

            Spacer(modifier = Modifier.height(16.dp))

            VehicleAutocompleteField(
                value = state.model,
                onValueChange = { onEvent(AddVehicleEvent.OnModelChanged(it)) },
                label = AppStrings.AddVehicle.modelLabel,
                placeholder = AppStrings.AddVehicle.modelPlaceholder,
                options = state.filteredModels,
                onOptionSelected = { onEvent(AddVehicleEvent.OnModelSelected(it)) },
                isExpanded = state.isModelsDropdownExpanded && state.isMakeSelected,
                onExpandedChange = { onEvent(AddVehicleEvent.OnModelDropdownExpandedChange(it)) },
                isLoading = state.isModelsLoading,
                enabled = state.isMakeSelected,
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
                color = MaterialTheme.colorScheme.onSurface,
            )

            Spacer(modifier = Modifier.height(8.dp))

            val fuelTypeOptions = remember(state.selectedFuelType) {
                FuelType.entries.map { it.toFuelTypeOptionUi(isSelected = it == state.selectedFuelType) }
            }
            FuelTypeSelector(
                options = fuelTypeOptions,
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
                text = if (state.isEditMode) AppStrings.AddVehicle.editSaveButton else AppStrings.AddVehicle.saveButton,
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
                title = { Text(text = AppStrings.AddVehicle.discardDialogTitle) },
                text = { Text(text = AppStrings.AddVehicle.discardDialogMessage) },
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
                initialYear = state.year.toIntOrNull() ?: Year.now().value,
            )
        }

        if (state.showPhotoSourceSheet) {
            PhotoSourceSheet(
                onDismiss = { onEvent(AddVehicleEvent.OnPhotoSourceSheetDismissed) },
                onGallerySelected = {
                    onEvent(AddVehicleEvent.OnPhotoSourceSheetDismissed)
                    photoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                onCameraSelected = {
                    onEvent(AddVehicleEvent.OnPhotoSourceSheetDismissed)
                    launchCamera()
                },
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
                selectedFuelType = FuelType.DIZEL,
            ),
            snackbarHostState = remember { SnackbarHostState() },
            onEvent = {},
        )
    }
}
