package com.iglesiaflow.gestion.data.remote

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.iglesiaflow.gestion.core.util.NotificationHelper
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/** Recibe las notificaciones push (recordatorios, check-in, avisos). */
@AndroidEntryPoint
class IglesiaMessagingService : FirebaseMessagingService() {

    @Inject lateinit var notificationHelper: NotificationHelper

    override fun onMessageReceived(message: RemoteMessage) {
        val title = message.notification?.title ?: message.data["title"] ?: "IglesiaFlow"
        val body = message.notification?.body ?: message.data["body"].orEmpty()
        val channel = when (message.data["channel"]) {
            "events" -> NotificationHelper.CHANNEL_EVENTS
            "checkin" -> NotificationHelper.CHANNEL_CHECKIN
            "birthdays" -> NotificationHelper.CHANNEL_BIRTHDAYS
            "prayer" -> NotificationHelper.CHANNEL_PRAYER
            else -> NotificationHelper.CHANNEL_GENERAL
        }
        notificationHelper.notify(channel, title, body)
    }

    override fun onNewToken(token: String) {
        // El backend puede registrar este token para envíos dirigidos.
    }
}
