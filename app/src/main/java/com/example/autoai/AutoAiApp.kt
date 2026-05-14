package com.example.autoai

import android.app.Application
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.autoai.di.koinModule
import com.example.autoai.notification.ReminderNotificationWorker
import com.example.data.di.dataModule
import com.example.domain.di.domainModule
import com.google.firebase.FirebaseApp
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin
import java.util.concurrent.TimeUnit

class AutoAiApp : Application() {

    override fun onCreate() {
        super.onCreate()

        FirebaseApp.initializeApp(this)

        initKoin()
        scheduleDailyReminderCheck()
    }

    private fun initKoin() {
        if (GlobalContext.getOrNull() == null) {
            startKoin {

                androidLogger()
                androidContext(this@AutoAiApp)

                modules(
                    koinModule,
                    domainModule,
                    dataModule
                )
            }
        }
    }

    private fun scheduleDailyReminderCheck() {
        val workRequest = PeriodicWorkRequestBuilder<ReminderNotificationWorker>(
            1, TimeUnit.DAYS
        ).build()

        val testRequest = OneTimeWorkRequestBuilder<ReminderNotificationWorker>()
            .setInitialDelay(10, TimeUnit.SECONDS)
            .build()
        WorkManager.getInstance(this).enqueue(testRequest)

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            ReminderNotificationWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }


}