package com.carelink.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.carelink.MainActivity

// ─────────────────────────────────────────────────────────────────────────────
// NotificationHelper — handles all in-app notifications for CareLink
// Covers: new request assigned, visit completed, case escalated, overdue alert
// ─────────────────────────────────────────────────────────────────────────────
object NotificationHelper {

    private const val CHANNEL_ID_GENERAL  = "carelink_general"
    private const val CHANNEL_ID_URGENT   = "carelink_urgent"
    private const val CHANNEL_NAME_GENERAL = "CareLink Notifications"
    private const val CHANNEL_NAME_URGENT  = "CareLink Urgent Alerts"

    // ─────────────────────────────────────────────────────────────────────────
    // createChannels — must be called on app startup (in MainActivity)
    // Android 8.0+ requires notification channels before posting
    // ─────────────────────────────────────────────────────────────────────────
    fun createChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE)
                    as NotificationManager

            // General channel for routine notifications
            val generalChannel = NotificationChannel(
                CHANNEL_ID_GENERAL,
                CHANNEL_NAME_GENERAL,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications for new assignments and status updates"
            }

            // Urgent channel for overdue and escalated cases
            val urgentChannel = NotificationChannel(
                CHANNEL_ID_URGENT,
                CHANNEL_NAME_URGENT,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Urgent alerts for overdue requests and escalated cases"
            }

            manager.createNotificationChannel(generalChannel)
            manager.createNotificationChannel(urgentChannel)
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Private helper — builds and posts a notification
    // ─────────────────────────────────────────────────────────────────────────
    private fun post(
        context: Context,
        id: Int,
        title: String,
        message: String,
        channelId: String = CHANNEL_ID_GENERAL
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(
                if (channelId == CHANNEL_ID_URGENT)
                    NotificationCompat.PRIORITY_HIGH
                else
                    NotificationCompat.PRIORITY_DEFAULT
            )
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(id, notification)
        } catch (e: SecurityException) {
            // Permission not granted yet — silently skip
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Public notification triggers — called from ViewModel at the right moment
    // ─────────────────────────────────────────────────────────────────────────

    // Called when coordinator assigns a request to a worker
    fun notifyVisitAssigned(context: Context, requestTitle: String, workerName: String) {
        post(
            context = context,
            id      = 1001,
            title   = "New visit assigned",
            message = "You have been assigned: $requestTitle. Please check your visit list."
        )
    }

    // Called when a worker marks a visit as completed
    fun notifyVisitCompleted(context: Context, requestTitle: String) {
        post(
            context = context,
            id      = 1002,
            title   = "Visit completed",
            message = "$requestTitle has been marked as completed and is ready for review."
        )
    }

    // Called when a reviewer escalates a case
    fun notifyEscalated(context: Context, requestTitle: String, reason: String) {
        post(
            context    = context,
            id         = 1003,
            title      = "Case escalated",
            message    = "$requestTitle has been escalated. Reason: $reason",
            channelId  = CHANNEL_ID_URGENT
        )
    }

    // Called when a reviewer verifies a case
    fun notifyVerified(context: Context, requestTitle: String) {
        post(
            context = context,
            id      = 1004,
            title   = "Case verified",
            message = "$requestTitle has been verified and closed successfully."
        )
    }

    // Called when a new request is submitted by a resident
    fun notifyNewRequest(context: Context, requestTitle: String, category: String) {
        post(
            context = context,
            id      = 1005,
            title   = "New welfare request",
            message = "A new $category request has been submitted: $requestTitle"
        )
    }

    // Called for overdue requests
    fun notifyOverdue(context: Context, requestTitle: String) {
        post(
            context   = context,
            id        = 1006,
            title     = "Overdue request",
            message   = "$requestTitle has passed its deadline and needs immediate attention.",
            channelId = CHANNEL_ID_URGENT
        )
    }
}