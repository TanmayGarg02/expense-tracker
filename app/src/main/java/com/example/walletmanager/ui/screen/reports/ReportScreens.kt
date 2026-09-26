package com.example.walletmanager.ui.screen.reports

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.walletmanager.data.model.WalletUiState
import com.example.walletmanager.ui.component.ScreenHeader
import com.example.walletmanager.ui.component.MonthlyCalendar
import com.example.walletmanager.ui.component.SectionTitle
import com.example.walletmanager.ui.component.TransactionRow
import com.example.walletmanager.ui.component.Sparkline
import com.example.walletmanager.ui.component.SummaryCard
import com.example.walletmanager.ui.component.WalletColors
import com.example.walletmanager.ui.component.WalletPage
import com.example.walletmanager.ui.component.formatMoney
import com.example.walletmanager.ui.theme.WalletGreen
import com.example.walletmanager.ui.theme.WalletSurface
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId

@Composable
fun WeeklyScreen(state: WalletUiState, onBack: () -> Unit) {
    var expandedWeeks by remember { mutableStateOf(emptySet<Int>()) }
    val weeklyExpense = state.transactions
        .filter { it.kind.name == "EXPENSE" }
        .sumOf { it.amountMinor } / 4

    WalletPage(PaddingValues()) {
        ScreenHeader("Weekly review", "Tap a week to see its transactions", onBack = onBack)
        SummaryCard("Expenses", weeklyExpense, state)
        (1..5).forEach { week ->
            val transactions = state.transactions.filter { it.weekOfMonth == week }
            val expense = transactions
                .filter { it.kind.name == "EXPENSE" }
                .sumOf { it.amountMinor }
            val expanded = week in expandedWeeks

            Card(
                colors = CardDefaults.cardColors(WalletSurface),
                onClick = {
                    expandedWeeks = if (expanded) {
                        expandedWeeks - week
                    } else {
                        expandedWeeks + week
                    }
                }
            ) {
                Column(
                    Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Week $week · ${transactions.size} transactions")
                    Text(
                        "Spent ${formatMoney(expense, state.balancesVisible, state.baseCurrency)}",
                        color = WalletColors.red
                    )
                    if (expanded) {
                        transactions.forEach { transaction ->
                            TransactionRow(transaction, state.accounts, state.baseCurrency)
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(70.dp))
    }
}

@Composable
fun MonthlyScreen(state: WalletUiState, onBack: () -> Unit) {
    ReportScreen("Monthly review", "October 2026", state.expenseMinor, state, onBack)
}

@Composable
fun CalendarScreen(state: WalletUiState, onBack: () -> Unit) {
    val month = YearMonth.now()
    val transactionsByDay = remember(state.transactions) {
        state.transactions
            .filter { transaction ->
                transaction.createdAt > 0 &&
                    Instant.ofEpochMilli(transaction.createdAt)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate()
                        .let { YearMonth.from(it) == month }
            }
            .groupBy { transaction ->
                Instant.ofEpochMilli(transaction.createdAt)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
            }
            .toSortedMap(compareByDescending { it })
    }

    WalletPage(PaddingValues()) {
        ScreenHeader(
            "Monthly calendar",
            "Transactions for ${month.month.name.lowercase().replaceFirstChar { it.uppercase() }} ${month.year}",
            onBack = onBack
        )
        MonthlyCalendar()

        if (transactionsByDay.isEmpty()) {
            Text("No transactions recorded this month.", color = WalletColors.muted)
        } else {
            transactionsByDay.forEach { (date, transactions) ->
                SectionTitle(
                    "${date.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }}, " +
                        "${date.dayOfMonth} " +
                        date.month.name.lowercase().replaceFirstChar { it.uppercase() }
                )
                transactions.forEach { transaction ->
                    TransactionRow(transaction, state.accounts, state.baseCurrency)
                }
            }
        }
        Spacer(Modifier.height(70.dp))
    }
}

@Composable
private fun ReportScreen(
    title: String,
    subtitle: String,
    amount: Long,
    state: WalletUiState,
    onBack: () -> Unit
) {
    WalletPage(PaddingValues()) {
        ScreenHeader(title, subtitle, onBack = onBack)
        SummaryCard("Expenses", amount, state)
        Text(
            "Income  ${formatMoney(state.incomeMinor, state.balancesVisible, state.baseCurrency)}",
            color = WalletGreen
        )
        Text(
            "Expenses  ${formatMoney(amount, state.balancesVisible, state.baseCurrency)}",
            color = WalletColors.red
        )
        Text(
            "Available  ${formatMoney(state.incomeMinor - amount, state.balancesVisible, state.baseCurrency)}",
            color = WalletColors.blue
        )
        Sparkline()
        Spacer(Modifier.height(70.dp))
    }
}
