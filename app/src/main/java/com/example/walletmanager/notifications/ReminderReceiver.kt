package com.example.walletmanager.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.walletmanager.R

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(NotificationChannel(channelId, "Important reminders", NotificationManager.IMPORTANCE_DEFAULT))
        manager.notify(intent.hashCode(), NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(intent.getStringExtra(titleKey) ?: "Wallet reminder")
            .setContentText(intent.getStringExtra(messageKey) ?: "You have an important money reminder")
            .setAutoCancel(true)
            .build())
    }

    companion object {
        const val channelId = "important_reminders"
        const val titleKey = "title"
        const val messageKey = "message"
    }
}
