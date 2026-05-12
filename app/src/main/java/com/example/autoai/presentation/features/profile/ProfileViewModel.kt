package com.example.autoai.presentation.features.profile

import androidx.lifecycle.viewModelScope
import com.example.autoai.base.BaseViewModel
import com.example.autoai.navigation.IAppNavigator
import com.example.autoai.navigation.Route
import com.example.autoai.presentation.util.asUiText
import com.example.domain.model.app.AppResult
import com.example.domain.model.app.onFailure
import com.example.domain.model.app.onSuccess
import com.example.domain.model.user.User
import com.example.domain.usecase.cost.GetAllCostsByUserIdParams
import com.example.domain.usecase.cost.GetAllCostsByUserIdUseCase
import com.example.domain.usecase.user.GetCurrentUserUseCase
import com.example.domain.usecase.vehicle.GetVehiclesParams
import com.example.domain.usecase.vehicle.GetVehiclesUseCase
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ProfileViewModel(
    private val navigator: IAppNavigator,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val getVehiclesUseCase: GetVehiclesUseCase,
    private val getAllCostsByUserIdUseCase: GetAllCostsByUserIdUseCase,
) : BaseViewModel<ProfileState, ProfileEvent, ProfileSideEffect>(ProfileState()) {

    init {
        loadProfile()
    }

    override fun onEvent(event: ProfileEvent) {
        when (event) {
            ProfileEvent.OnEditProfileClick -> navigator.navigateTo(Route.EditProfile)
            ProfileEvent.OnRetry -> loadProfile()
        }
    }

    // ─── Loading ─────────────────────────────────────────────────────────────

    private fun loadProfile() {
        setState { it.copy(isLoading = true) }

        viewModelScope.launch {
            getCurrentUserUseCase(Unit)
                .onFailure { error ->
                    setState { it.copy(isLoading = false) }
                    emitSideEffect(ProfileSideEffect.ShowError(error.asUiText()))
                }
                .onSuccess { user ->
                    loadStats(user)
                }
        }
    }

    private suspend fun loadStats(user: User) {
        coroutineScope {
            val vehiclesDeferred = async {
                getVehiclesUseCase(GetVehiclesParams(user.id))
            }
            val costsDeferred = async {
                getAllCostsByUserIdUseCase(GetAllCostsByUserIdParams(user.id))
            }

            val vehicleCount = when (val result = vehiclesDeferred.await()) {
                is AppResult.Success -> result.data.size
                is AppResult.Failure -> 0
            }

            val totalCostCount = when (val result = costsDeferred.await()) {
                is AppResult.Success -> result.data.size
                is AppResult.Failure -> 0
            }

            setState {
                it.copy(
                    isLoading = false,
                    userName = user.name,
                    userEmail = user.email,
                    userInitial = user.name.firstOrNull()?.uppercaseChar()?.toString() ?: "",
                    memberSince = user.createdAt.formatMemberSince(),
                    plan = if (user.isPremium) "Premium" else "Free",
                    vehicleCount = vehicleCount,
                    totalCostCount = totalCostCount,
                    username = user.username,
                    phoneNumber = user.phoneNumber ?: "",
                    profilePictureUrl = user.profilePictureUrl
                )
            }
        }
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private fun Long.formatMemberSince(): String {
        val formatter = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        return formatter.format(Date(this))
    }
}
