package com.example.walletmanager.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.walletmanager.data.local.WalletDatabase
import com.example.walletmanager.data.model.InvestmentType
import com.example.walletmanager.data.repository.WalletRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class IpoReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getStringExtra(ipoIdKey) ?: return
        when (intent.action) {
            equityAction -> resolve(context, id, InvestmentType.EQUITY)
            stockAction -> resolve(context, id, InvestmentType.STOCK)
            rejectedAction -> resolve(context, id, null)
            else -> showNotification(context, id, intent.getStringExtra(ipoNameKey).orEmpty())
        }
    }

    private fun resolve(context: Context, id: String, type: InvestmentType?) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            val repository = WalletRepository(WalletDatabase.getInstance(context))
            if (type != null) repository.confirmIpo(id, type) else repository.rejectIpo(id)
            context.getSystemService(NotificationManager::class.java).cancel(id.hashCode())
            pendingResult.finish()
        }
    }

    private fun showNotification(context: Context, id: String, name: String) {
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(channelId, "IPO allotments", NotificationManager.IMPORTANCE_DEFAULT)
        )
        manager.notify(
            id.hashCode(),
            NotificationCompat.Builder(context, channelId)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("Was $name allotted?")
                .setContentText("Choose whether to add it to your investments.")
                .addAction(0, "Equity", action(context, id, equityAction))
                .addAction(0, "Stock", action(context, id, stockAction))
                .addAction(0, "No", action(context, id, rejectedAction))
                .setAutoCancel(true)
                .build()
        )
    }

    private fun action(context: Context, id: String, action: String): PendingIntent {
        val intent = Intent(context, IpoReceiver::class.java).apply {
            this.action = action
            putExtra(ipoIdKey, id)
        }
        return PendingIntent.getBroadcast(
            context,
            "$id-$action".hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    companion object {
        const val notifyAction = "wallet.action.IPO_NOTIFY"
        const val equityAction = "wallet.action.IPO_EQUITY"
        const val stockAction = "wallet.action.IPO_STOCK"
        const val rejectedAction = "wallet.action.IPO_REJECTED"
        const val ipoIdKey = "ipo_id"
        const val ipoNameKey = "ipo_name"
        const val channelId = "ipo_allotments"
    }
}
