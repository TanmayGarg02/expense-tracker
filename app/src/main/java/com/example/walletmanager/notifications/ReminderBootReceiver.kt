package com.example.walletmanager.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class ReminderBootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            ReminderScheduler.rescheduleMonthlyReminders(context)
            ReminderScheduler.rescheduleYearlyReminders(context)
        }
    }
}
