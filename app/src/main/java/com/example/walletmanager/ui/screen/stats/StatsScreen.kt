package com.example.walletmanager.ui.screen.stats

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.walletmanager.data.model.Transaction
import com.example.walletmanager.data.model.Category
import com.example.walletmanager.data.model.TransactionKind
import com.example.walletmanager.data.model.WalletUiState
import com.example.walletmanager.ui.component.ScreenHeader
import com.example.walletmanager.ui.component.SectionTitle
import com.example.walletmanager.ui.component.SummaryCard
import com.example.walletmanager.ui.component.TransactionRow
import com.example.walletmanager.ui.component.WalletColors
import com.example.walletmanager.ui.component.WalletPage
import com.example.walletmanager.ui.component.FilterSheet
import com.example.walletmanager.ui.component.TransactionSheet
import com.example.walletmanager.ui.component.formatMoney
import com.example.walletmanager.ui.theme.WalletGreen

@Composable
fun StatsScreen(
    state: WalletUiState,
    editTransaction: (Transaction) -> Unit = {},
    deleteTransaction: (Transaction) -> Unit = {},
    addCategory: (String, String?) -> Unit = { _, _ -> },
    editCategory: (Category) -> Unit = {},
    deleteCategory: (Category) -> Unit = {}
) {
    var filter by rememberSaveable { mutableStateOf("All") }
    var showFilter by rememberSaveable { mutableStateOf(false) }
    var transactionToEdit by remember { mutableStateOf<Transaction?>(null) }
    val transactions = remember(state.transactions, filter) {
        state.transactions.filter {
            filter == "All" || it.kind.name.equals(filter, ignoreCase = true) || it.category.contains(filter, ignoreCase = true)
        }
    }

    WalletPage(PaddingValues()) {
        ScreenHeader("Statistics", "Monthly spending and cash flow")
        FilterChip(
            selected = filter != "All",
            onClick = { showFilter = true },
            label = { Text(if (filter == "All") "Filter" else filter) }
        )
        PieChart(state, state.transactions)
        SummaryCard("Saved this month", state.incomeMinor - state.expenseMinor, state)
        SectionTitle("Transactions")
        transactions.forEach {
            TransactionRow(
                item = it,
                accounts = state.accounts,
                currency = state.baseCurrency,
                onClick = { transactionToEdit = it }
            )
        }
        Spacer(Modifier.height(70.dp))
    }

    if (showFilter) {
        FilterSheet(
            state = state,
            selectedFilterName = filter,
            dismiss = { showFilter = false },
            onApplyFilter = { newFilter ->
                filter = newFilter
            }
        )
    }

    transactionToEdit?.let { transaction ->
        TransactionSheet(
            state = state,
            initialTransaction = transaction,
            dismiss = { transactionToEdit = null },
            onSave = { title, amount, kind, category, accountId, note, cashback ->
                editTransaction(
                    transaction.copy(
                        title = title,
                        amountMinor = amount,
                        kind = kind,
                        category = category,
                        accountId = accountId,
                        note = note,
                        cashbackMinor = cashback
                    )
                )
                transactionToEdit = null
            },
            onDelete = {
                deleteTransaction(it)
                transactionToEdit = null
            },
            onAddCategory = addCategory,
            onEditCategory = editCategory,
            onDeleteCategory = deleteCategory
        )
    }
}

@Composable
private fun PieChart(
    state: WalletUiState,
    transactions: List<Transaction>
) {
    val totals = remember(transactions) {
        transactions
            .filter { it.kind == TransactionKind.EXPENSE }
            .groupBy { it.category }
            .map { it.key to it.value.sumOf { item -> item.amountMinor } }
    }
    val colors = remember {
        listOf(
            WalletColors.red,
            WalletColors.amber,
            WalletColors.blue,
            WalletColors.purple,
            WalletGreen
        )
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
        Canvas(Modifier.size(140.dp)) {
            var start = -90f
            val total = totals.sumOf { it.second }.coerceAtLeast(1)

            totals.forEachIndexed { index, item ->
                val sweep = item.second.toFloat() / total * 360f
                drawArc(colors[index % colors.size], start, sweep, true)
                start += sweep
            }
        }

        Spacer(Modifier.width(14.dp))

        Column {
            totals.take(5).forEachIndexed { index, item ->
                Text(
                    text = "● ${item.first}  ${formatMoney(item.second, state.balancesVisible, state.baseCurrency)}",
                    color = colors[index % colors.size],
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
private fun FilterDialog(
    categories: List<String>,
    selected: String,
    dismiss: () -> Unit,
    select: (String) -> Unit
) {
    val options = remember(categories) {
        (listOf("All", "INCOME", "EXPENSE") + categories).distinct()
    }

    AlertDialog(
        onDismissRequest = dismiss,
        title = { Text("Filter transactions") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                options.forEach { option ->
                    FilterChip(
                        selected = selected == option,
                        onClick = { select(option) },
                        label = { Text(option) }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = dismiss) {
                Text("Done")
            }
        }
    )
}
