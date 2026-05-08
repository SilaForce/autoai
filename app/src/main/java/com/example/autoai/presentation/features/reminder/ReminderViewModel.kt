package com.example.autoai.presentation.features.reminder

import androidx.lifecycle.viewModelScope
import com.example.autoai.R
import com.example.autoai.base.BaseViewModel
import com.example.autoai.navigation.IAppNavigator
import com.example.autoai.navigation.Route
import com.example.autoai.presentation.components.BottomNavItem
import com.example.autoai.presentation.util.UiText
import com.example.autoai.presentation.util.asUiText
import com.example.domain.model.app.onFailure
import com.example.domain.model.app.onSuccess
import com.example.domain.usecase.reminder.AddReminderParams
import com.example.domain.usecase.reminder.AddReminderUseCase
import com.example.domain.usecase.reminder.GetRemindersParams
import com.example.domain.usecase.reminder.GetRemindersUseCase
import com.example.domain.usecase.user.GetCurrentUserUseCase
import com.example.domain.usecase.vehicle.GetVehiclesParams
import com.example.domain.usecase.vehicle.GetVehiclesUseCase
import kotlinx.coroutines.launch

class ReminderViewModel(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val getVehiclesUseCase: GetVehiclesUseCase,
    private val getRemindersUseCase: GetRemindersUseCase,
    private val addReminderUseCase: AddReminderUseCase,
    private val navigator: IAppNavigator,
) : BaseViewModel<ReminderState, ReminderEvent, RemindersSideEffect>(ReminderState()) {

    // Ove ID-jeve čuvamo u ViewModelu, jer UI nema potrebu da ih iscrtava
    private var currentUserId: String? = null
    private var activeVehicleId: String? = null

    init {
        loadData()
    }

    override fun onEvent(event: ReminderEvent) {
        when (event) {
            ReminderEvent.OnAddReminderClicked -> setState { it.copy(isSheetOpen = true) }

            ReminderEvent.OnAddSheetDismissed -> resetSheet()

            is ReminderEvent.OnTitleChanged -> setState { it.copy(titleInput = event.value) }

            is ReminderEvent.OnDateSelected -> setState { it.copy(dueDateMillis = event.dateMillis) }

            is ReminderEvent.OnNoteChanged -> setState { it.copy(noteInput = event.value) }

            ReminderEvent.OnSaveReminderClicked -> saveReminder()

            is ReminderEvent.OnNavItemSelected -> handleBottomNavigation(event.item)
        }
    }

    // ─── Učitavanje podataka ───────────────────────────────────────────────────

    private fun loadData() {
        setState { it.copy(isLoading = true) }

        viewModelScope.launch {
            // 1. Dobavi trenutnog korisnika
            getCurrentUserUseCase(Unit)
                .onFailure { error ->
                    setState { it.copy(isLoading = false) }
                    emitSideEffect(RemindersSideEffect.ShowError(error.asUiText()))
                    return@launch
                }
                .onSuccess { user ->
                    currentUserId = user.id

                    // 2. Dobavi njegova vozila
                    getVehiclesUseCase(GetVehiclesParams(user.id))
                        .onFailure { error ->
                            setState { it.copy(isLoading = false) }
                            emitSideEffect(RemindersSideEffect.ShowError(error.asUiText()))
                            return@launch
                        }
                        .onSuccess { vehicles ->
                            // 3. Pronađi aktivno vozilo
                            val activeVehicle = vehicles.firstOrNull { it.isActive }

                            if (activeVehicle == null) {
                                // Korisnik nema aktivno vozilo
                                setState { it.copy(isLoading = false, hasNoActiveVehicle = true) }
                                return@launch
                            }

                            activeVehicleId = activeVehicle.id
                            setState { it.copy(hasNoActiveVehicle = false) }

                            // 4. Dobavi podsjetnike za to vozilo
                            refreshReminders(activeVehicle.id)
                        }
                }
        }
    }

    private fun refreshReminders(vehicleId: String) {
        viewModelScope.launch {
            setState { it.copy(isLoading = true) }

            getRemindersUseCase(GetRemindersParams(vehicleId))
                .onSuccess { reminders ->
                    // Transformišemo čiste modele u UI modele
                    val uiModels = reminders.map { it.toReminderItemUi() }
                    setState { it.copy(isLoading = false, reminders = uiModels) }
                }
                .onFailure { error ->
                    setState { it.copy(isLoading = false) }
                    emitSideEffect(RemindersSideEffect.ShowError(error.asUiText()))
                }
        }
    }

    // ─── Snimanje ─────────────────────────────────────────────────────────────

    private fun saveReminder() {
        val userId = currentUserId ?: run {
            emitSideEffect(RemindersSideEffect.ShowError(UiText.StringResource(R.string.error_unknown)))
            return
        }

        val vehicleId = activeVehicleId ?: run {
            emitSideEffect(RemindersSideEffect.ShowError(UiText.StringResource(R.string.error_unknown)))
            return
        }

        val title = state.value.titleInput
        if (title.isBlank()) {
            // Ovdje ispaljujemo grešku (možeš dodati specifičan string u strings.xml kasnije)
            emitSideEffect(RemindersSideEffect.ShowError(UiText.DynamicString("Enter reminder title.")))
            return
        }

        val dueDate = state.value.dueDateMillis
        if (dueDate == null) {
            emitSideEffect(RemindersSideEffect.ShowError(UiText.DynamicString("Select due date..")))
            return
        }

        setState { it.copy(isSaving = true) }

        viewModelScope.launch {
            addReminderUseCase(
                AddReminderParams(
                    userId = userId,
                    vehicleId = vehicleId,
                    title = title,
                    dueDateMillis = dueDate,
                    note = state.value.noteInput
                )
            ).onSuccess {
                resetSheet(isSaving = false)
                emitSideEffect(RemindersSideEffect.ShowSuccess(UiText.DynamicString("Reminder added successfully.")))
                refreshReminders(vehicleId)
            }.onFailure { error ->
                setState { it.copy(isSaving = false) }
                emitSideEffect(RemindersSideEffect.ShowError(error.asUiText()))
            }
        }
    }

    // ─── Pomoćne funkcije ─────────────────────────────────────────────────────

    private fun resetSheet(isSaving: Boolean = false) {
        setState {
            it.copy(
                isSaving = isSaving,
                isSheetOpen = false,
                titleInput = "",
                dueDateMillis = null,
                noteInput = ""
            )
        }
    }

    private fun handleBottomNavigation(item: BottomNavItem) {
        when (item) {
            BottomNavItem.HOME -> {
                setState { it.copy(selectedNavItem = BottomNavItem.HOME) }
                navigator.navigateTo(Route.Home)
            }
            BottomNavItem.GARAGE -> {
                setState { it.copy(selectedNavItem = BottomNavItem.GARAGE) }
                navigator.navigateTo(Route.Garage)
            }
            BottomNavItem.COSTS -> {
                setState { it.copy(selectedNavItem = BottomNavItem.COSTS) }
                navigator.navigateTo(Route.Costs)
            }
            BottomNavItem.REMINDERS -> setState { it.copy(selectedNavItem = BottomNavItem.REMINDERS) }
            BottomNavItem.AI_CHAT -> {
                setState { it.copy(selectedNavItem = BottomNavItem.AI_CHAT) }
                navigator.navigateTo(Route.AiChat)
             }
        }
    }
}