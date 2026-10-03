package com.example.walletmanager.ui.screen.stats

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.FilterChip
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import java.time.LocalDate
import java.time.format.DateTimeFormatter
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
import com.example.walletmanager.ui.component.WalletSheetBackButton
import com.example.walletmanager.ui.component.FilterSheet
import com.example.walletmanager.ui.component.TransactionSheet
import com.example.walletmanager.ui.component.formatMoney
import com.example.walletmanager.ui.theme.WalletGreen
import com.example.walletmanager.ui.theme.WalletSurface

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
    var period by rememberSaveable { mutableStateOf("Monthly") }
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
        PieChart(state, transactions)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = period == "Weekly",
                onClick = { period = "Weekly" },
                label = { Text("Weekly") }
            )
            FilterChip(
                selected = period == "Monthly",
                onClick = { period = "Monthly" },
                label = { Text("Monthly") }
            )
        }
        ExpensePeriodGraph(transactions, period, state)
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
private fun ExpensePeriodGraph(
    transactions: List<Transaction>,
    period: String,
    state: WalletUiState
) {
    val entries = remember(transactions, period) {
        if (period == "Monthly") {
            (1..5).map { week ->
                transactions
                    .filter { it.kind == TransactionKind.EXPENSE && it.weekOfMonth == week }
                    .let { expenses -> expenses.sumOf { it.amountMinor } to expenses.size }
            }
        } else {
            val today = LocalDate.now()
            (0..6).map { offset ->
                val date = today.minusDays((6 - offset).toLong())
                transactions
                    .filter { transaction ->
                        val parsed = runCatching {
                            LocalDate.parse(
                                transaction.date.substringBefore(" "),
                                DateTimeFormatter.ofPattern("yyyy-MM-dd")
                            )
                        }.getOrNull()
                        parsed == date && transaction.kind == TransactionKind.EXPENSE
                    }
                    .let { expenses -> expenses.sumOf { it.amountMinor } to expenses.size }
            }
        }
    }
    val amounts = entries.map { it.first }
    val labels = if (period == "Monthly") {
        listOf("W1", "W2", "W3", "W4", "W5")
    } else {
        listOf("M", "T", "W", "T", "F", "S", "S")
    }
    val maxAmount = amounts.maxOrNull()?.coerceAtLeast(1) ?: 1

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("${period} expenses", color = WalletColors.muted, fontSize = 13.sp)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(WalletSurface, RoundedCornerShape(18.dp))
                .padding(horizontal = 12.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            amounts.forEachIndexed { index, amount ->
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = if (period == "Monthly") {
                            "${formatMoney(amount, state.balancesVisible, state.baseCurrency)}\n${entries[index].second} tx"
                        } else {
                            "${entries[index].second} tx"
                        },
                        color = WalletColors.cream,
                        fontSize = 10.sp,
                        lineHeight = 11.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(WalletColors.red.copy(alpha = 0.25f))
                            .padding(horizontal = 3.dp, vertical = 2.dp)
                    )
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                    ) {
                        val barHeight = size.height * amount.toFloat() / maxAmount
                        drawRoundRect(
                            color = WalletColors.red,
                            topLeft = androidx.compose.ui.geometry.Offset(
                                x = 0f,
                                y = size.height - barHeight
                            ),
                            size = androidx.compose.ui.geometry.Size(size.width, barHeight),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f)
                        )
                    }
                    Text(labels[index], color = WalletColors.muted, fontSize = 11.sp)
                }
            }
        }
        Text(
            "Total ${period.lowercase()} expenses: ${formatMoney(amounts.sum(), state.balancesVisible, state.baseCurrency)}",
            color = WalletColors.red,
            fontSize = 12.sp
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

@OptIn(ExperimentalMaterial3Api::class)
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

    ModalBottomSheet(
        onDismissRequest = dismiss,
        contentWindowInsets = { WindowInsets(0, 0, 0, 0) },
        dragHandle = { WalletSheetBackButton(dismiss) },
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = WalletSurface
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Text("Filter transactions")
                options.forEach { option ->
                    FilterChip(
                        selected = selected == option,
                        onClick = { select(option) },
                        label = { Text(option) }
                    )
                }
            TextButton(
                onClick = dismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Done")
            }
        }
    }
}
