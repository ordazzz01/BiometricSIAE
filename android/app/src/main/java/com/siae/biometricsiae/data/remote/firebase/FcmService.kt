package com.siae.biometricsiae.data.remote.firebase

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.siae.biometricsiae.util.SecureLogger
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FcmService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        SecureLogger.d("FCM", "New token received")
        // Token will be sent to server during next sync
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        
        message.data.let { data ->
            val type = data["type"]
            when (type) {
                "config_update" -> {
                    SecureLogger.d("FCM", "Config update received")
                    // Trigger config refresh
                }
                "sync_trigger" -> {
                    SecureLogger.d("FCM", "Sync trigger received")
                    // Trigger immediate sync
                }
                "notification" -> {
                    val title = data["title"] ?: "Notificación"
                    val body = data["body"] ?: ""
                    SecureLogger.d("FCM", "Notification: $title - $body")
                    // Show notification
                }
                else -> {
                    SecureLogger.d("FCM", "Unknown message type: $type")
                }
            }
        }
    }
}
