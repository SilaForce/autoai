package com.example.autoai.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.autoai.R
import com.example.domain.model.app.AppResult
import com.example.domain.repository.AuthRepository
import com.example.domain.datasource.PreferencesDataSource
import com.example.domain.datasource.RemindersDataSource
import com.example.domain.datasource.VehicleDataSource
import kotlinx.coroutines.flow.first
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.concurrent.TimeUnit

class ReminderNotificationWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params), KoinComponent {

    private val authRepository: AuthRepository by inject()
    private val remindersRepository: RemindersDataSource by inject()
    private val vehicleRepository: VehicleDataSource by inject()
    private val preferencesRepository: PreferencesDataSource by inject()

    override suspend fun doWork(): Result {
        // Respect the user's Settings toggle. Short-circuit before any Firestore reads.
        if (!preferencesRepository.isNotificationsEnabled.first()) {
            return Result.success()
        }

        val user = when (val result = authRepository.getCurrentUser()) {
            is AppResult.Success -> result.data
            is AppResult.Failure -> {
                Log.w(TAG, "Failed to get user: ${result.error}")
                return Result.retry()
            }
        }

        val activeVehicle = when (val result = vehicleRepository.getVehicles(user.id)) {
            is AppResult.Success -> result.data.firstOrNull { it.isActive }
            is AppResult.Failure -> {
                Log.w(TAG, "Failed to get vehicles: ${result.error}")
                return Result.retry()
            }
        }
        if (activeVehicle == null) {
            return Result.success()
        }

        val reminders = when (val result = remindersRepository.getActiveRemindersForVehicle(activeVehicle.id)) {
            is AppResult.Success -> result.data
            is AppResult.Failure -> {
                Log.w(TAG, "Failed to get reminders: ${result.error}")
                return Result.retry()
            }
        }

        val now = System.currentTimeMillis()
        val threeDaysMillis = TimeUnit.DAYS.toMillis(3)

        val dueSoon = reminders.filter { reminder ->
            val daysUntilDue = reminder.dueDateMillis - now
            daysUntilDue in 0..threeDaysMillis
        }

        if (dueSoon.isNotEmpty()) {
            createNotificationChannel()
            dueSoon.forEach { reminder ->
                sendNotification(reminder.id.hashCode(), reminder.title)
            }
        }

        return Result.success()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Reminder Notifications",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Notifications for upcoming vehicle reminders"
        }
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    private fun sendNotification(notificationId: Int, title: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Reminder Due Soon")
            .setContentText("\"$title\" is due within 3 days.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(notificationId, notification)
    }

    companion object {
        const val CHANNEL_ID = "reminder_notifications"
        const val WORK_NAME = "daily_reminder_check"
        private const val TAG = "ReminderWorker"
    }
}
