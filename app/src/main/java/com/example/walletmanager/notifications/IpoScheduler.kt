package com.example.walletmanager.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.walletmanager.data.model.PendingIpo

object IpoScheduler {
    fun schedule(context: Context, ipo: PendingIpo) {
        val intent = Intent(context, IpoReceiver::class.java).apply {
            action = IpoReceiver.notifyAction
            putExtra(IpoReceiver.ipoIdKey, ipo.id)
            putExtra(IpoReceiver.ipoNameKey, ipo.name)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            ipo.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        context.getSystemService(AlarmManager::class.java).setAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            ipo.allotmentDateMillis + 24 * 60 * 60 * 1000L,
            pendingIntent
        )
    }
}
