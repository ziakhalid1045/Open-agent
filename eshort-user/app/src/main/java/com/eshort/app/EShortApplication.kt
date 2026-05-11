package com.eshort.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class EShortApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channels = listOf(
                NotificationChannel(
                    "eshort_notifications",
                    "eShort Notifications",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply { description = "General notifications" },
                NotificationChannel(
                    "eshort_messages",
                    "Messages",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply { description = "Chat messages" }
            )
            val manager = getSystemService(NotificationManager::class.java)
            channels.forEach { manager.createNotificationChannel(it) }
        }
    }
}
