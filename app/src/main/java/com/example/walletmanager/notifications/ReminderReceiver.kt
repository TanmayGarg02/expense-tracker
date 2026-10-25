package com.example.walletmanager.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.walletmanager.R

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val manager = context.getSystemService(NotificationManager::class.java)
        val reminderId = intent.getStringExtra(idKey) ?: intent.hashCode().toString()
        if (intent.action == doneAction) {
            manager.cancel(reminderId.hashCode())
            return
        }
        manager.createNotificationChannel(NotificationChannel(channelId, "Important reminders", NotificationManager.IMPORTANCE_DEFAULT))
        val title = intent.getStringExtra(titleKey) ?: "Wallet reminder"
        val message = intent.getStringExtra(messageKey) ?: "You have an important money reminder"
        val doneIntent = Intent(context, ReminderReceiver::class.java).apply {
            action = doneAction
            putExtra(idKey, reminderId)
        }
        val donePendingIntent = PendingIntent.getBroadcast(
            context,
            reminderId.hashCode() xor doneRequestCodeSalt,
            doneIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .addAction(
                NotificationCompat.Action.Builder(
                    android.R.drawable.checkbox_on_background,
                    "Done",
                    donePendingIntent
                ).build()
            )
            .build()
        manager.notify(reminderId.hashCode(), notification)

        val monthlyDay = intent.getIntExtra(monthlyDayKey, 0)
        val yearlyMonth = intent.getIntExtra(yearlyMonthKey, 0)
        val yearlyDay = intent.getIntExtra(yearlyDayKey, 0)
        if (monthlyDay in 1..31) {
            ReminderScheduler.scheduleNextMonthly(
                context = context,
                title = title,
                message = message,
                id = reminderId,
                day = monthlyDay,
                hour = intent.getIntExtra(hourKey, 9),
                minute = intent.getIntExtra(minuteKey, 0)
            )
        } else if (yearlyMonth in 1..12 && yearlyDay in 1..31) {
            ReminderScheduler.scheduleNextYearly(
                context = context,
                title = title,
                message = message,
                id = reminderId,
                month = yearlyMonth,
                day = yearlyDay,
                hour = intent.getIntExtra(hourKey, 9),
                minute = intent.getIntExtra(minuteKey, 0)
            )
        }
    }

    companion object {
        const val channelId = "important_reminders"
        const val titleKey = "title"
        const val messageKey = "message"
        const val idKey = "reminder_id"
        const val monthlyDayKey = "monthly_day"
        const val yearlyMonthKey = "yearly_month"
        const val yearlyDayKey = "yearly_day"
        const val hourKey = "hour"
        const val minuteKey = "minute"
        const val doneAction = "com.example.walletmanager.REMINDER_DONE"
        private const val doneRequestCodeSalt = 0xD06E
    }
}
