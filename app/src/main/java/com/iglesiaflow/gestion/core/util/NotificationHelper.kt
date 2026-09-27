package com.iglesiaflow.gestion.core.util

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.iglesiaflow.gestion.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationHelper @Inject constructor(@ApplicationContext private val context: Context) {

    fun createChannels() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        val channels = listOf(
            NotificationChannel(CHANNEL_GENERAL, "General", NotificationManager.IMPORTANCE_DEFAULT),
            NotificationChannel(CHANNEL_EVENTS, "Recordatorios de eventos", NotificationManager.IMPORTANCE_DEFAULT),
            NotificationChannel(CHANNEL_CHECKIN, "Check-in infantil", NotificationManager.IMPORTANCE_HIGH),
            NotificationChannel(CHANNEL_BIRTHDAYS, "Cumpleaños", NotificationManager.IMPORTANCE_LOW),
            NotificationChannel(CHANNEL_PRAYER, "Peticiones de oración", NotificationManager.IMPORTANCE_LOW)
        )
        channels.forEach { manager.createNotificationChannel(it) }
    }

    fun notify(channelId: String, title: String, message: String, id: Int = System.currentTimeMillis().toInt()) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
        ) return
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setAutoCancel(true)
            .build()
        runCatching { NotificationManagerCompat.from(context).notify(id, notification) }
    }

    companion object {
        const val CHANNEL_GENERAL = "iglesiaflow_general"
        const val CHANNEL_EVENTS = "iglesiaflow_events"
        const val CHANNEL_CHECKIN = "iglesiaflow_checkin"
        const val CHANNEL_BIRTHDAYS = "iglesiaflow_birthdays"
        const val CHANNEL_PRAYER = "iglesiaflow_prayer"
    }
}
