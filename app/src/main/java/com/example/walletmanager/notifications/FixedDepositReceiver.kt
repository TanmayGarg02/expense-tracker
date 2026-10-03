package com.example.walletmanager.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.walletmanager.R
import java.text.NumberFormat
import java.util.Locale

class FixedDepositReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val name = intent.getStringExtra(nameKey) ?: "Fixed deposit"
        val bankName = intent.getStringExtra(bankKey).orEmpty()
        val amount = intent.getLongExtra(amountKey, 0L)
        val formattedAmount = NumberFormat.getCurrencyInstance(Locale("en", "IN")).format(amount / 100.0)

        val notificationBody = if (bankName.isNotBlank()) {
            "$name at $bankName has matured. Maturity amount: $formattedAmount"
        } else {
            "$name has matured. Maturity amount: $formattedAmount"
        }

        val manager = context.getSystemService(NotificationManager::class.java)
        manager?.createNotificationChannel(
            NotificationChannel(channelId, "Fixed Deposit Maturity", NotificationManager.IMPORTANCE_HIGH)
        )
        manager?.notify(
            intent.hashCode(),
            NotificationCompat.Builder(context, channelId)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("Fixed Deposit Matured 🏦")
                .setContentText(notificationBody)
                .setStyle(NotificationCompat.BigTextStyle().bigText(notificationBody))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .build()
        )
    }

    companion object {
        const val channelId = "fixed_deposit_maturity"
        const val nameKey = "name"
        const val amountKey = "amount"
        const val bankKey = "bank"
    }
}
