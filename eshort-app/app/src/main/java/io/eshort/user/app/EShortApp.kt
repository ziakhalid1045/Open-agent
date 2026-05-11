package io.eshort.user.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class EShortApp : Application() {

    override fun onCreate() {
        super.onCreate()
        setupNotificationChannels()
    }

    private fun setupNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NotificationManager::class.java)

            val generalChannel = NotificationChannel(
                CHANNEL_GENERAL,
                "General",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "General notifications"
                enableVibration(true)
            }

            val chatChannel = NotificationChannel(
                CHANNEL_MESSAGES,
                "Messages",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Chat message notifications"
                enableVibration(true)
                setShowBadge(true)
            }

            val socialChannel = NotificationChannel(
                CHANNEL_SOCIAL,
                "Social",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Likes, comments, and follows"
            }

            manager.createNotificationChannels(
                listOf(generalChannel, chatChannel, socialChannel)
            )
        }
    }

    companion object {
        const val CHANNEL_GENERAL = "eshort_general"
        const val CHANNEL_MESSAGES = "eshort_messages"
        const val CHANNEL_SOCIAL = "eshort_social"
    }
}
