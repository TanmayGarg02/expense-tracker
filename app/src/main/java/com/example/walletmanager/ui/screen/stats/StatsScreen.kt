package com.example.walletmanager.ui.screen.stats

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import java.time.LocalDate
import java.time.YearMonth
import java.util.Locale
import java.time.Instant
import java.time.ZoneId
import com.example.walletmanager.data.model.Transaction
import com.example.walletmanager.data.model.Category
import com.example.walletmanager.data.model.TransactionKind
import com.example.walletmanager.data.model.Currency
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
import com.example.walletmanager.ui.theme.WalletGreenAccent
import com.example.walletmanager.ui.theme.WalletRedAccent
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
    var selectedMonthValue by rememberSaveable { mutableStateOf(YearMonth.now().toString()) }
    var showFilter by rememberSaveable { mutableStateOf(false) }
    var transactionToEdit by remember { mutableStateOf<Transaction?>(null) }
    val selectedMonth = remember(selectedMonthValue) { YearMonth.parse(selectedMonthValue) }
    val selectedMonthLabel = selectedMonth.month.name.lowercase(Locale.getDefault())
        .replaceFirstChar { it.uppercase() } + " ${selectedMonth.year}"
    val transactions = remember(state.transactions, filter, selectedMonth) {
        state.transactions.filter {
            (filter == "All" || it.kind.name.equals(filter, ignoreCase = true) || it.category.contains(filter, ignoreCase = true)) &&
                transactionMonth(it) == selectedMonth
        }
    }

    WalletPage(PaddingValues()) {
        ScreenHeader("Statistics", "Monthly spending and cash flow")
        StatsControlButton(
            label = if (filter == "All") "Filter" else filter,
            selected = filter != "All",
            onClick = { showFilter = true },
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { selectedMonthValue = selectedMonth.minusMonths(1).toString() }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = "Previous Month",
                    tint = WalletColors.cream
                )
            }
            Text(
                text = selectedMonthLabel,
                color = WalletColors.cream,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp
            )
            IconButton(onClick = { selectedMonthValue = selectedMonth.plusMonths(1).toString() }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Next Month",
                    tint = WalletColors.cream
                )
            }
        }

        if (transactions.isEmpty()) {
            StatsMonthEmptyState(selectedMonthLabel)
        } else {
            PieChart(state, transactions)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatsControlButton(
                    label = "Weekly",
                    selected = period == "Weekly",
                    onClick = { period = "Weekly" },
                )
                StatsControlButton(
                    label = "Monthly",
                    selected = period == "Monthly",
                    onClick = { period = "Monthly" },
                )
            }
            ExpensePeriodGraph(transactions, period, state, selectedMonth)
            val monthSaved = transactions.filter { it.kind == TransactionKind.INCOME }.sumOf { it.amountMinor } -
                transactions.filter { it.kind == TransactionKind.EXPENSE }.sumOf { it.amountMinor }
            SummaryCard("Saved in $selectedMonthLabel", monthSaved, state)
            SectionTitle("Transactions")
            transactions.forEach {
                TransactionRow(
                    item = it,
                    accounts = state.accounts,
                    currency = state.baseCurrency,
                    onClick = { transactionToEdit = it }
                )
            }
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
private fun StatsMonthEmptyState(monthLabel: String) {
    val composition by rememberLottieComposition(
        LottieCompositionSpec.Asset("stats_empty.lottie")
    )
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LottieAnimation(
            composition = composition,
            progress = { progress },
            modifier = Modifier.size(200.dp)
        )
        Text(
            text = "No transactions for $monthLabel.",
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
            color = WalletColors.muted,
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )
    }
}

private fun transactionDate(transaction: Transaction): LocalDate? = runCatching {
        LocalDate.parse(transaction.date.substringBefore(" "))
    }.getOrNull() ?: transaction.createdAt.takeIf { it > 0 }?.let { timestamp ->
        Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()).toLocalDate()
    }

private fun transactionMonth(transaction: Transaction): YearMonth? = transactionDate(transaction)?.let(YearMonth::from)

@Composable
private fun StatsControlButton(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) WalletColors.cream else WalletSurface,
            contentColor = if (selected) WalletColors.ink else WalletColors.cream
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
    ) {
        Text(label)
    }
}

@Composable
private fun ExpensePeriodGraph(
    transactions: List<Transaction>,
    period: String,
    state: WalletUiState,
    selectedMonth: YearMonth
) {
    val entries = remember(transactions, period, selectedMonth) {
        if (period == "Monthly") {
            (1..5).map { week ->
                transactions
                    .filter { it.kind == TransactionKind.EXPENSE && it.weekOfMonth == week }
                    .let { expenses -> expenses.sumOf { it.amountMinor } to expenses.size }
            }
        } else {
            val today = LocalDate.now()
            val weekEnd = if (selectedMonth == YearMonth.from(today)) today else selectedMonth.atEndOfMonth()
            (0..6).map { offset ->
                val date = weekEnd.minusDays((6 - offset).toLong())
                transactions
                    .filter { transaction ->
                        val parsed = transactionDate(transaction)
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
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("$period expenses", color = WalletColors.muted, fontSize = 14.sp)
            Text("AMOUNT · TX", color = WalletColors.muted, fontSize = 10.sp)
        }
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
                        text = compactAmount(amount, state),
                        color = WalletColors.cream,
                        fontSize = 11.sp,
                        maxLines = 1,
                        softWrap = false,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )
                    val transactionCount = entries[index].second
                    val barHeightDp = if (transactionCount > 0) {
                        (100f * amount.toFloat() / maxAmount).coerceAtLeast(24f)
                    } else 0f
                    Box(
                        modifier = Modifier.fillMaxWidth().height(100.dp),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        Canvas(Modifier.fillMaxSize()) {
                            val barHeight = if (transactionCount > 0) {
                                (size.height * amount.toFloat() / maxAmount).coerceAtLeast(24.dp.toPx())
                            } else 0f
                            drawRoundRect(
                                brush = Brush.verticalGradient(
                                    listOf(
                                        WalletColors.redAccent.copy(alpha = 0.98f),
                                        WalletColors.redAccent.copy(alpha = 0.58f)
                                    )
                                ),
                                topLeft = androidx.compose.ui.geometry.Offset(0f, size.height - barHeight),
                                size = androidx.compose.ui.geometry.Size(size.width, barHeight),
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f)
                            )
                        }
                        if (transactionCount > 0) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(barHeightDp.dp)
                                    .align(Alignment.BottomCenter),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = transactionCount.toString(),
                                    color = WalletColors.cream,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    textAlign = TextAlign.Center,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                    Text(labels[index], color = WalletColors.muted, fontSize = 11.sp)
                }
            }
        }
        Text(
            "Total ${period.lowercase()} expenses: ${formatMoney(amounts.sum(), state.balancesVisible, state.baseCurrency)}",
            color = WalletColors.red,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

private fun compactAmount(amountMinor: Long, state: WalletUiState): String {
    if (!state.balancesVisible) return "•••"
    val amount = amountMinor / 100.0 * state.baseCurrency.inrRate
    val absolute = kotlin.math.abs(amount)
    val (scaled, suffix) = if (state.baseCurrency == Currency.INR) {
        when {
            absolute >= 10_000_000 -> amount / 10_000_000 to "Cr"
            absolute >= 100_000 -> amount / 100_000 to "L"
            absolute >= 1_000 -> amount / 1_000 to "K"
            else -> amount to ""
        }
    } else {
        when {
            absolute >= 1_000_000_000 -> amount / 1_000_000_000 to "B"
            absolute >= 1_000_000 -> amount / 1_000_000 to "M"
            absolute >= 1_000 -> amount / 1_000 to "K"
            else -> amount to ""
        }
    }
    val digits = if (suffix.isEmpty() || kotlin.math.abs(scaled) >= 10) "%.0f" else "%.1f"
    val number = String.format(Locale.US, digits, scaled).removeSuffix(".0")
    return "${state.baseCurrency.symbol}$number$suffix"
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
    val chartColors = remember {
        listOf(
            WalletRedAccent,
            WalletColors.amber,
            WalletColors.blue,
            WalletColors.purple,
            WalletGreenAccent
        )
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
        Canvas(Modifier.size(140.dp)) {
            var start = -90f
            val total = totals.sumOf { it.second }.coerceAtLeast(1)

            totals.forEachIndexed { index, item ->
                val sweep = item.second.toFloat() / total * 360f
                drawArc(chartColors[index % chartColors.size], start, sweep, true)
                start += sweep
            }
        }

        Spacer(Modifier.width(14.dp))

        Column {
            totals.take(5).forEachIndexed { index, item ->
                Text(
                    text = "● ${item.first}  ${formatMoney(item.second, state.balancesVisible, state.baseCurrency)}",
                    color = chartColors[index % chartColors.size],
                    fontSize = 12.sp
                )
            }
        }
    }
}
