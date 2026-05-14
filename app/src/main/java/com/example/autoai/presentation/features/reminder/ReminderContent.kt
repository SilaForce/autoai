package com.example.autoai.presentation.features.reminder

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.autoai.localization.AppStrings // <-- OBAVEZAN IMPORT
import com.example.autoai.presentation.components.AutoAiTextField
import com.example.autoai.presentation.components.BottomNavItem
import com.example.autoai.presentation.components.BottomNavigationBar
import com.example.autoai.presentation.components.MainButton
import com.example.autoai.presentation.features.reminder.components.ReminderCard
import com.example.autoai.presentation.theme.AutoAITheme
import com.example.autoai.presentation.theme.VerdantGreen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderContent(
    state: ReminderState,
    snackbarHostState: SnackbarHostState,
    onEvent: (ReminderEvent) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            floatingActionButton = {
                // Skrivamo FAB ako nema vozila, da ga ne bi zbunilo
                if (!state.hasNoActiveVehicle && !state.isLoading) {
                    FloatingActionButton(
                        onClick = { onEvent(ReminderEvent.OnAddReminderClicked) },
                        shape = CircleShape,
                        containerColor = VerdantGreen,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Add,
                            contentDescription = AppStrings.Reminders.addFabDescription
                        )
                    }
                }
            },
            bottomBar = {
                BottomNavigationBar(
                    selectedItem = state.selectedNavItem,
                    onItemSelected = { onEvent(ReminderEvent.OnNavItemSelected(it)) }
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 20.dp)
            ) {
                Spacer(modifier = Modifier.height(28.dp))

                Text(
                    text = AppStrings.Reminders.title,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(24.dp))

                if (state.isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = VerdantGreen)
                    }
                } else if (state.hasNoActiveVehicle) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = AppStrings.Home.noActiveVehicleTitle,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = AppStrings.Home.noActiveVehicleSubtitle,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
                            )
                        }
                    }
                } else if (state.reminders.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = AppStrings.Reminders.emptyTitle,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                } else {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        LazyColumn(modifier = Modifier.padding(horizontal = 20.dp)) {
                            items(state.reminders) { item ->
                                ReminderCard(item = item)
                            }
                        }
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.TopCenter)
        )

        if (state.isSheetOpen) {
            ModalBottomSheet(
                onDismissRequest = { onEvent(ReminderEvent.OnAddSheetDismissed) },
                sheetState = sheetState,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                AddReminderSheet(state, onEvent)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddReminderSheet(
    state: ReminderState,
    onEvent: (ReminderEvent) -> Unit
) {
    // Lokalni UI state za prikaz DatePicker dialoga
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = state.dueDateMillis ?: System.currentTimeMillis()
    )

    // Formatiranje datuma za polje unosa
    val dateString = state.dueDateMillis?.let {
        SimpleDateFormat("dd. MM. yyyy.", Locale.getDefault()).format(Date(it))
    } ?: ""

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .imePadding()
    ) {
        Text(
            text = AppStrings.Reminders.addSheetTitle,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.height(24.dp))

        AutoAiTextField(
            value = state.titleInput,
            onValueChange = { onEvent(ReminderEvent.OnTitleChanged(it)) },
            label = AppStrings.Reminders.titleLabel,
            placeholder = AppStrings.Reminders.titlePlaceholder
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Ovdje koristimo onClick da otvorimo DatePicker, a tastatura se ne otvara jer je readOnly
        AutoAiTextField(
            value = dateString,
            onValueChange = {},
            label = AppStrings.Reminders.dateLabel,
            placeholder = AppStrings.Reminders.datePlaceholder,
            readOnly = true,
            onClick = { showDatePicker = true }
        )
        Spacer(modifier = Modifier.height(16.dp))

        AutoAiTextField(
            value = state.noteInput,
            onValueChange = { onEvent(ReminderEvent.OnNoteChanged(it)) },
            label = AppStrings.Reminders.noteLabel,
            placeholder = AppStrings.Reminders.notePlaceholder
        )
        Spacer(modifier = Modifier.height(32.dp))

        MainButton(
            text = AppStrings.Reminders.saveButton,
            onClick = { onEvent(ReminderEvent.OnSaveReminderClicked) },
            enabled = !state.isSaving
        )
        Spacer(modifier = Modifier.height(24.dp))
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { onEvent(ReminderEvent.OnDateSelected(it)) }
                    showDatePicker = false
                }) {
                    Text(text = AppStrings.Reminders.ok, color = VerdantGreen)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(text = AppStrings.Reminders.cancel, color = MaterialTheme.colorScheme.onSurface)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
