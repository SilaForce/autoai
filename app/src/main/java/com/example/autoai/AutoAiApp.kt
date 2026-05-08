package com.example.autoai

import android.app.Application
import com.example.autoai.di.koinModule
import com.example.data.di.dataModule
import com.example.domain.di.domainModule
import com.google.firebase.FirebaseApp
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin
class AutoAiApp : Application() {

    override fun onCreate() {
        super.onCreate()

        FirebaseApp.initializeApp(this)

        initKoin()
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
}