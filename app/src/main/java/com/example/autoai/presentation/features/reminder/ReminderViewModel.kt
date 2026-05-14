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
import com.example.domain.model.reminder.Reminder
import com.example.domain.usecase.reminder.AddReminderParams
import com.example.domain.usecase.reminder.AddReminderUseCase
import com.example.domain.usecase.reminder.DeleteReminderParams
import com.example.domain.usecase.reminder.DeleteReminderUseCase
import com.example.domain.usecase.reminder.GetRemindersParams
import com.example.domain.usecase.reminder.GetRemindersUseCase
import com.example.domain.usecase.reminder.UpdateReminderUseCase
import com.example.domain.usecase.user.GetCurrentUserUseCase
import com.example.domain.usecase.vehicle.GetVehiclesParams
import com.example.domain.usecase.vehicle.GetVehiclesUseCase
import kotlinx.coroutines.launch

class ReminderViewModel(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val getVehiclesUseCase: GetVehiclesUseCase,
    private val getRemindersUseCase: GetRemindersUseCase,
    private val addReminderUseCase: AddReminderUseCase,
    private val updateReminderUseCase: UpdateReminderUseCase,
    private val deleteReminderUseCase: DeleteReminderUseCase,
    private val navigator: IAppNavigator,
) : BaseViewModel<ReminderState, ReminderEvent, RemindersSideEffect>(ReminderState()) {

    private var currentUserId: String? = null
    private var activeVehicleId: String? = null
    private var domainReminders: List<Reminder> = emptyList()

    init {
        loadData()
    }

    override fun onEvent(event: ReminderEvent) {
        when (event) {
            ReminderEvent.OnAddReminderClicked -> setState {
                it.copy(isSheetOpen = true, editingReminderId = null, titleInput = "", dueDateMillis = null, noteInput = "")
            }
            ReminderEvent.OnAddSheetDismissed -> resetSheet()
            is ReminderEvent.OnTitleChanged -> setState { it.copy(titleInput = event.value) }
            is ReminderEvent.OnDateSelected -> setState { it.copy(dueDateMillis = event.dateMillis) }
            is ReminderEvent.OnNoteChanged -> setState { it.copy(noteInput = event.value) }
            ReminderEvent.OnSaveReminderClicked -> saveReminder()
            is ReminderEvent.OnNavItemSelected -> handleBottomNavigation(event.item)
            is ReminderEvent.OnToggleCompleted -> toggleCompleted(event.reminder)
            is ReminderEvent.OnEditClicked -> openEditSheet(event.reminder)
            is ReminderEvent.OnDeleteClicked -> deleteReminder(event.reminderId)
        }
    }

    // ─── Load data ────────────────────────────────────────────────────────────

    private fun loadData() {
        setState { it.copy(isLoading = true) }

        viewModelScope.launch {
            getCurrentUserUseCase(Unit)
                .onFailure { error ->
                    setState { it.copy(isLoading = false) }
                    emitSideEffect(RemindersSideEffect.ShowError(error.asUiText()))
                    return@launch
                }
                .onSuccess { user ->
                    currentUserId = user.id

                    getVehiclesUseCase(GetVehiclesParams(user.id))
                        .onFailure { error ->
                            setState { it.copy(isLoading = false) }
                            emitSideEffect(RemindersSideEffect.ShowError(error.asUiText()))
                            return@launch
                        }
                        .onSuccess { vehicles ->
                            val activeVehicle = vehicles.firstOrNull { it.isActive }

                            if (activeVehicle == null) {
                                setState { it.copy(isLoading = false, hasNoActiveVehicle = true) }
                                return@launch
                            }

                            activeVehicleId = activeVehicle.id
                            setState { it.copy(hasNoActiveVehicle = false) }
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
                    domainReminders = reminders
                    val uiModels = reminders.map { it.toReminderItemUi() }
                    setState { it.copy(isLoading = false, reminders = uiModels) }
                }
                .onFailure { error ->
                    setState { it.copy(isLoading = false) }
                    emitSideEffect(RemindersSideEffect.ShowError(error.asUiText()))
                }
        }
    }

    // ─── Save (add or edit) ───────────────────────────────────────────────────

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
            emitSideEffect(RemindersSideEffect.ShowError(UiText.DynamicString("Enter reminder title.")))
            return
        }
        val dueDate = state.value.dueDateMillis
        if (dueDate == null) {
            emitSideEffect(RemindersSideEffect.ShowError(UiText.DynamicString("Select due date.")))
            return
        }

        setState { it.copy(isSaving = true) }

        val editingId = state.value.editingReminderId

        viewModelScope.launch {
            if (editingId != null) {
                val existing = domainReminders.firstOrNull { it.id == editingId } ?: return@launch
                val updated = existing.copy(
                    title = title,
                    dueDateMillis = dueDate,
                    note = state.value.noteInput.ifBlank { null }
                )
                updateReminderUseCase(updated)
                    .onSuccess {
                        resetSheet(isSaving = false)
                        emitSideEffect(RemindersSideEffect.ShowSuccess(UiText.DynamicString("Reminder updated.")))
                        refreshReminders(vehicleId)
                    }
                    .onFailure { error ->
                        setState { it.copy(isSaving = false) }
                        emitSideEffect(RemindersSideEffect.ShowError(error.asUiText()))
                    }
            } else {
                addReminderUseCase(
                    AddReminderParams(
                        userId = userId,
                        vehicleId = vehicleId,
                        title = title,
                        dueDateMillis = dueDate,
                        note = state.value.noteInput.ifBlank { null }
                    )
                ).onSuccess {
                    resetSheet(isSaving = false)
                    emitSideEffect(RemindersSideEffect.ShowSuccess(UiText.DynamicString("Reminder added.")))
                    refreshReminders(vehicleId)
                }.onFailure { error ->
                    setState { it.copy(isSaving = false) }
                    emitSideEffect(RemindersSideEffect.ShowError(error.asUiText()))
                }
            }
        }
    }

    // ─── Toggle completed ─────────────────────────────────────────────────────

    private fun toggleCompleted(reminderUi: ReminderItemUi) {
        val vehicleId = activeVehicleId ?: return
        val existing = domainReminders.firstOrNull { it.id == reminderUi.id } ?: return
        val updated = existing.copy(isCompleted = !existing.isCompleted)

        viewModelScope.launch {
            updateReminderUseCase(updated)
                .onSuccess { refreshReminders(vehicleId) }
                .onFailure { error ->
                    emitSideEffect(RemindersSideEffect.ShowError(error.asUiText()))
                }
        }
    }

    // ─── Edit ─────────────────────────────────────────────────────────────────

    private fun openEditSheet(reminderUi: ReminderItemUi) {
        setState {
            it.copy(
                isSheetOpen = true,
                editingReminderId = reminderUi.id,
                titleInput = reminderUi.title,
                dueDateMillis = reminderUi.dueDateMillis,
                noteInput = reminderUi.note ?: ""
            )
        }
    }

    // ─── Delete ───────────────────────────────────────────────────────────────

    private fun deleteReminder(reminderId: String) {
        val vehicleId = activeVehicleId ?: return

        viewModelScope.launch {
            deleteReminderUseCase(DeleteReminderParams(reminderId))
                .onSuccess {
                    emitSideEffect(RemindersSideEffect.ShowSuccess(UiText.DynamicString("Reminder deleted.")))
                    refreshReminders(vehicleId)
                }
                .onFailure { error ->
                    emitSideEffect(RemindersSideEffect.ShowError(error.asUiText()))
                }
        }
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private fun resetSheet(isSaving: Boolean = false) {
        setState {
            it.copy(
                isSaving = isSaving,
                isSheetOpen = false,
                editingReminderId = null,
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
