package com.example.walletmanager.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.walletmanager.data.model.FixedDeposit

object FixedDepositScheduler {
    fun schedule(context: Context, deposit: FixedDeposit, bankName: String = "") {
        val intent = Intent(context, FixedDepositReceiver::class.java).apply {
            putExtra(FixedDepositReceiver.nameKey, deposit.name)
            putExtra(FixedDepositReceiver.amountKey, deposit.maturityAmountMinor)
            putExtra(FixedDepositReceiver.bankKey, bankName)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            deposit.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val alarmManager = context.getSystemService(AlarmManager::class.java)
        val triggerTime = if (deposit.maturityDateMillis <= System.currentTimeMillis()) {
            System.currentTimeMillis() + 3000L
        } else {
            deposit.maturityDateMillis
        }
        alarmManager.setAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerTime,
            pendingIntent
        )
    }
}
