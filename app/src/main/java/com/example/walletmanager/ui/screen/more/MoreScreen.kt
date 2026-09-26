package com.example.walletmanager.ui.screen.more

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.walletmanager.ui.component.MoreRow
import com.example.walletmanager.ui.component.ScreenHeader
import com.example.walletmanager.ui.component.WalletPage

@Composable
fun MoreScreen(open: (String) -> Unit) {
    WalletPage(PaddingValues()) {
        ScreenHeader("More", "Review, plan and manage your money")
        MoreRow("📅", "Weekly review", "Monday to Sunday") {
            open("weekly")
        }
        MoreRow("🗓️", "Monthly review", "Compare this month") {
            open("monthly")
        }
        MoreRow("🔁", "Payment plans", "Salary, SIPs and subscriptions") {
            open("plans")
        }
        MoreRow("⚙️", "Settings and data", "Currency, CSV and notes") {
            open("settings")
        }
        Spacer(Modifier.height(70.dp))
    }
}
