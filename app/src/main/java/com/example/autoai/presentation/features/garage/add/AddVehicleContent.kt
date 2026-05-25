package com.example.autoai.presentation.features.garage.add

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.example.autoai.localization.AppStrings
import com.example.autoai.presentation.components.AutoAiTextField
import com.example.autoai.presentation.components.MainButton
import com.example.autoai.presentation.features.garage.add.components.FuelTypeSelector
import com.example.autoai.presentation.features.garage.add.components.YearPickerDialog
import com.example.autoai.presentation.theme.AutoAITheme
import com.example.autoai.presentation.theme.PureWhite
import com.example.autoai.presentation.theme.VerdantGreen
import com.example.autoai.presentation.util.ImageUtils
import com.example.domain.model.vehicle.FuelType
import androidx.compose.material3.MaterialTheme
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

    // ── Gallery picker ─────────────────────────────────────────────
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

    // ── Camera capture ─────────────────────────────────────────────
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

            // ── Photo (optional) ──────────────────────────────────
            VehiclePhotoSection(
                existingPhotoBase64 = state.existingPhotoBase64,
                selectedPhotoBytes = state.selectedPhotoBytes,
                onClick = { onEvent(AddVehicleEvent.OnPhotoFieldClicked) },
                onRemoveClick = { onEvent(AddVehicleEvent.OnRemovePhoto) },
            )

            Spacer(modifier = Modifier.height(20.dp))

            // ── Make autocomplete dropdown ────────────────────────
            ExposedDropdownMenuBox(
                expanded = state.isMakesDropdownExpanded,
                onExpandedChange = { onEvent(AddVehicleEvent.OnMakeDropdownExpandedChange(it)) },
            ) {
                OutlinedTextField(
                    value = state.make,
                    onValueChange = { onEvent(AddVehicleEvent.OnMakeChanged(it)) },
                    label = { Text(AppStrings.AddVehicle.makeLabel) },
                    placeholder = { Text(AppStrings.AddVehicle.makePlaceholder) },
                    singleLine = true,
                    trailingIcon = {
                        if (state.isMakesLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                            )
                        } else {
                            ExposedDropdownMenuDefaults.TrailingIcon(
                                expanded = state.isMakesDropdownExpanded,
                            )
                        }
                    },
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                    modifier = Modifier
                        .menuAnchor(MenuAnchorType.PrimaryEditable, enabled = true)
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                )

                ExposedDropdownMenu(
                    expanded = state.isMakesDropdownExpanded && state.filteredMakes.isNotEmpty(),
                    onDismissRequest = { onEvent(AddVehicleEvent.OnMakeDropdownExpandedChange(false)) },
                ) {
                    state.filteredMakes.forEach { make ->
                        DropdownMenuItem(
                            text = { Text(make) },
                            onClick = { onEvent(AddVehicleEvent.OnMakeSelected(make)) },
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── Model autocomplete dropdown ───────────────────────
            ExposedDropdownMenuBox(
                expanded = state.isModelsDropdownExpanded && state.isMakeSelected,
                onExpandedChange = {
                    if (state.isMakeSelected) {
                        onEvent(AddVehicleEvent.OnModelDropdownExpandedChange(it))
                    }
                },
            ) {
                OutlinedTextField(
                    value = state.model,
                    onValueChange = { onEvent(AddVehicleEvent.OnModelChanged(it)) },
                    label = { Text(AppStrings.AddVehicle.modelLabel) },
                    placeholder = { Text(AppStrings.AddVehicle.modelPlaceholder) },
                    singleLine = true,
                    enabled = state.isMakeSelected,
                    trailingIcon = {
                        if (state.isModelsLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                            )
                        } else {
                            ExposedDropdownMenuDefaults.TrailingIcon(
                                expanded = state.isModelsDropdownExpanded,
                            )
                        }
                    },
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                    modifier = Modifier
                        .menuAnchor(MenuAnchorType.PrimaryEditable, enabled = state.isMakeSelected)
                        .fillMaxWidth(),
                )

                ExposedDropdownMenu(
                    expanded = state.isModelsDropdownExpanded && state.filteredModels.isNotEmpty(),
                    onDismissRequest = { onEvent(AddVehicleEvent.OnModelDropdownExpandedChange(false)) },
                ) {
                    state.filteredModels.forEach { model ->
                        DropdownMenuItem(
                            text = { Text(model) },
                            onClick = { onEvent(AddVehicleEvent.OnModelSelected(model)) },
                        )
                    }
                }
            }

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
                initialYear = state.year.toIntOrNull() ?: Year.now().value,
            )
        }

        if (state.showPhotoSourceSheet) {
            val sheetState = rememberModalBottomSheetState()
            ModalBottomSheet(
                sheetState = sheetState,
                onDismissRequest = { onEvent(AddVehicleEvent.OnPhotoSourceSheetDismissed) },
            ) {
                Column(modifier = Modifier.padding(bottom = 16.dp)) {
                    PhotoSourceRow(
                        icon = Icons.Outlined.PhotoLibrary,
                        label = AppStrings.AddVehicle.photoSourceGallery,
                        onClick = {
                            onEvent(AddVehicleEvent.OnPhotoSourceSheetDismissed)
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                    )
                    PhotoSourceRow(
                        icon = Icons.Outlined.CameraAlt,
                        label = AppStrings.AddVehicle.photoSourceCamera,
                        onClick = {
                            onEvent(AddVehicleEvent.OnPhotoSourceSheetDismissed)
                            launchCamera()
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun VehiclePhotoSection(
    existingPhotoBase64: String?,
    selectedPhotoBytes: ByteArray?,
    onClick: () -> Unit,
    onRemoveClick: () -> Unit,
) {
    // Decode the saved photo once per change (form recomposes on every keystroke, so
    // re-running Base64.decode every time would be wasteful for a ~30KB string).
    val decodedExisting = remember(existingPhotoBase64) {
        existingPhotoBase64?.takeIf { it.isNotBlank() }
            ?.let { ImageUtils.decodeBase64ToByteArray(it) }
    }
    // A freshly picked photo wins over the saved one (matches AvatarSection precedence).
    val imageModel: Any? = selectedPhotoBytes ?: decodedExisting
    val hasPhoto = imageModel != null

    Text(
        text = AppStrings.AddVehicle.photoLabel,
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium,
        color = MaterialTheme.colorScheme.onSurface,
    )
    Spacer(modifier = Modifier.height(8.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(Color(0xFFE8F5E9))
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center,
        ) {
            if (imageModel != null) {
                AsyncImage(
                    model = imageModel,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
            } else {
                Icon(
                    imageVector = Icons.Outlined.DirectionsCar,
                    contentDescription = null,
                    tint = VerdantGreen,
                    modifier = Modifier.size(40.dp),
                )
            }

            // Camera-overlay badge — same visual affordance the avatar uses, so users see
            // the slot is tappable without needing an explicit "Add photo" button.
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(28.dp)
                    .align(Alignment.BottomEnd)
                    .background(VerdantGreen, CircleShape),
            ) {
                Icon(
                    imageVector = Icons.Filled.CameraAlt,
                    contentDescription = null,
                    tint = PureWhite,
                    modifier = Modifier.size(16.dp),
                )
            }
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = if (hasPhoto) AppStrings.AddVehicle.photoChange else AppStrings.AddVehicle.photoAdd,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = VerdantGreen,
                modifier = Modifier.clickable(onClick = onClick),
            )
            if (hasPhoto) {
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(
                    onClick = onRemoveClick,
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 0.dp),
                ) {
                    Text(
                        text = AppStrings.AddVehicle.photoRemove,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }
    }
}

@Composable
private fun PhotoSourceRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(modifier = Modifier.size(16.dp))
        Text(
            text = label,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurface,
        )
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


