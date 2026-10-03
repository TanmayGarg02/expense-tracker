package com.example.walletmanager.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent

data class ReminderDraft(val title: String, val message: String, val triggerAtMillis: Long)

object ReminderScheduler {
    fun schedule(context: Context, reminder: ReminderDraft) {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra(ReminderReceiver.titleKey, reminder.title)
            putExtra(ReminderReceiver.messageKey, reminder.message)
        }
        val pendingIntent = PendingIntent.getBroadcast(context, reminder.triggerAtMillis.hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        context.getSystemService(AlarmManager::class.java).setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, reminder.triggerAtMillis, pendingIntent)
    }
}
