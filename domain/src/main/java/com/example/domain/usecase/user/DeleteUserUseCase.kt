package com.example.domain.usecase.user

import com.example.domain.base.BaseUseCase
import com.example.domain.model.app.AppResult
import com.example.domain.model.app.andThen
import com.example.domain.datasource.AiChatHistoryDataSource
import com.example.domain.datasource.AiChatThreadDataSource
import com.example.domain.repository.AuthRepository
import com.example.domain.datasource.CostDataSource
import com.example.domain.datasource.RemindersDataSource
import com.example.domain.datasource.VehicleDataSource
import kotlinx.coroutines.CoroutineDispatcher

/**
 * Cascade-deletes a user's data across every collection they own, then their Firebase
 * Auth record. Mirrors the [com.example.domain.usecase.vehicle.DeleteVehicleUseCase]
 * pattern: children first, parent last — if the auth delete fails after the data cascade,
 * the user can retry; if a cascade step fails first, the auth record is still intact and
 * the operation is recoverable.
 *
 * Why client-side and not a Cloud Function: the project is on the Firebase Spark plan,
 * which doesn't support Cloud Functions. The trade-off is that a network drop mid-cascade
 * can leave half-deleted state — acceptable for a portfolio app with no real users.
 */
class DeleteUserUseCase(
    private val authRepository: AuthRepository,
    private val vehicleDataSource: VehicleDataSource,
    private val costDataSource: CostDataSource,
    private val reminderDataSource: RemindersDataSource,
    private val chatHistoryDataSource: AiChatHistoryDataSource,
    private val chatThreadDataSource: AiChatThreadDataSource,
    dispatcher: CoroutineDispatcher,
) : BaseUseCase<Unit, Unit>(dispatcher) {

    override suspend fun execute(params: Unit): AppResult<Unit> {
        // Need the userId BEFORE deleting the auth record — once it's gone, we lose
        // the ability to query our own collections.
        return when (val userResult = authRepository.getCurrentUser()) {
            is AppResult.Failure -> userResult
            is AppResult.Success -> {
                val userId = userResult.data.id
                vehicleDataSource.deleteAllForUser(userId)
                    .andThen { costDataSource.deleteAllForUser(userId) }
                    .andThen { reminderDataSource.deleteAllForUser(userId) }
                    .andThen { chatHistoryDataSource.deleteAllForUser(userId) }
                    .andThen { chatThreadDataSource.deleteAllForUser(userId) }
                    .andThen { authRepository.deleteUser() }
            }
        }
    }
}
