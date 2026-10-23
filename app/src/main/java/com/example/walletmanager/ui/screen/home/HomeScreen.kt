package com.example.walletmanager.ui.screen.home

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.background
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.walletmanager.data.model.TransactionKind
import com.example.walletmanager.data.model.Category
import com.example.walletmanager.data.model.Transaction
import com.example.walletmanager.data.model.WalletUiState
import com.example.walletmanager.ui.component.BalanceCard
import com.example.walletmanager.ui.component.QuickAction
import com.example.walletmanager.ui.component.ScreenHeader
import com.example.walletmanager.ui.component.TransactionSheet
import com.example.walletmanager.ui.component.TransactionRow
import com.example.walletmanager.ui.component.WalletColors
import com.example.walletmanager.ui.component.walletGlassBorder
import com.example.walletmanager.ui.component.walletGlassSurface
import com.example.walletmanager.ui.component.WalletIcon
import com.example.walletmanager.ui.component.WalletPage
import com.example.walletmanager.ui.component.WalletModalBottomSheet
import com.example.walletmanager.ui.component.WalletSheetBackButton
import com.example.walletmanager.ui.component.debounceClickable
import com.example.walletmanager.ui.component.formatMoney
import com.example.walletmanager.ui.component.rememberDebouncedClick
import com.example.walletmanager.ui.navigation.CalendarRoute
import com.example.walletmanager.ui.navigation.PlansRoute
import com.example.walletmanager.ui.navigation.MoreRoute
import com.example.walletmanager.ui.navigation.WalletRoute
import com.example.walletmanager.ui.theme.WalletGreen
import com.example.walletmanager.ui.theme.WalletGreenAccent
import com.example.walletmanager.ui.theme.WalletBeige
import com.example.walletmanager.ui.theme.WalletSurface
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun HomeScreen(
    state: WalletUiState,
    toggleBalances: () -> Unit,
    addTransaction: (String, Long, TransactionKind, String, String, String, Long) -> Unit,
    transfer: (String, Long, String, String, String, Long) -> Unit,
    addCategory: (String, String?) -> Unit,
    editCategory: (Category) -> Unit,
    deleteCategory: (Category) -> Unit,
    editTransaction: (Transaction) -> Unit,
    deleteTransaction: (Transaction) -> Unit,
    open: (WalletRoute) -> Unit
) {
    var transactionDialog by rememberSaveable { mutableStateOf(false) }
    var transactionKind by rememberSaveable { mutableStateOf(TransactionKind.EXPENSE.name) }
    var transferMode by rememberSaveable { mutableStateOf(false) }
    var transactionToEdit by remember { mutableStateOf<Transaction?>(null) }
    val visibleTransactions = remember(
        state.transactions,
        state.filterAccountId,
        state.filterCategory,
        state.filterKind
    ) {
        state.transactions.filter { transaction ->
            (state.filterAccountId == null || transaction.accountId == state.filterAccountId) &&
                (state.filterCategory == null ||
                    transaction.category == state.filterCategory ||
                    transaction.category.startsWith("${state.filterCategory} · ")) &&
                (state.filterKind == null || transaction.kind == state.filterKind)
        }
    }
    val visibleState = remember(state, visibleTransactions) {
        state.copy(transactions = visibleTransactions)
    }

    WalletPage(PaddingValues()) {
        ScreenHeader(
            "Good morning, ${state.profileName}",
            "Here’s your money at a glance",
            profileName = state.profileName,
            profileImageUri = state.profileImageUri,
            onProfileClick = { open(MoreRoute(openProfile = true)) }
        )
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            HeaderAction(
                icon = "calendar",
                label = "Calendar",
                onClick = { open(CalendarRoute) },
                modifier = Modifier.weight(1f)
            )
            HeaderAction(
                icon = "plans",
                label = "Plans",
                onClick = { open(PlansRoute) },
                modifier = Modifier.weight(1f)
            )
        }
        BalanceCard(state, toggleBalances)

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            QuickAction(
                glyph = "expense",
                label = "Expense",
                color = WalletColors.redAccent,
                onClick = {
                    transferMode = false
                    transactionKind = TransactionKind.EXPENSE.name
                    transactionDialog = true
                },
                modifier = Modifier.weight(1f)
            )
            QuickAction(
                glyph = "income",
                label = "Income",
                color = WalletGreenAccent,
                onClick = {
                    transferMode = false
                    transactionKind = TransactionKind.INCOME.name
                    transactionDialog = true
                },
                modifier = Modifier.weight(1f)
            )
            QuickAction(
                glyph = "transfer",
                label = "Transfer",
                color = WalletColors.amber,
                onClick = {
                    transferMode = true
                    transactionKind = TransactionKind.EXPENSE.name
                    transactionDialog = true
                },
                modifier = Modifier.weight(1f)
            )
        }

        HomeSectionHeading("Recent activity")
        Card(
            colors = CardDefaults.cardColors(WalletSurface.copy(alpha = 0.86f)),
            shape = RoundedCornerShape(22.dp),
            modifier = Modifier.walletGlassBorder(RoundedCornerShape(22.dp))
        ) {
            Column(Modifier.padding(horizontal = 16.dp, vertical = 5.dp)) {
                if (visibleTransactions.isEmpty()) {
                    Text(
                        "No transactions yet. Add your first income or expense.",
                        color = WalletColors.muted,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                } else {
                    visibleTransactions.take(5).forEach {
                        TransactionRow(
                            item = it,
                            accounts = state.accounts,
                            currency = state.baseCurrency,
                            onClick = { transactionToEdit = it }
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))
        HomeSectionHeading("Coming up")
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            val upcomingPayments = state.recurringPayments.filter { it.active }.take(3)
            if (upcomingPayments.isEmpty()) {
                Text("Nothing scheduled", color = WalletColors.muted)
            } else {
                upcomingPayments.forEach { payment ->
                    Card(
                        colors = CardDefaults.cardColors(WalletSurface.copy(alpha = 0.86f)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.walletGlassBorder(RoundedCornerShape(16.dp))
                    ) {
                        Row(
                            Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                Text(payment.name, fontWeight = FontWeight.SemiBold)
                                Text(upcomingDueLabel(payment), color = WalletColors.muted, fontSize = 12.sp)
                            }
                            Text(
                                formatMoney(payment.amountMinor, true, state.baseCurrency),
                                color = WalletColors.cream,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
        val completedPayments = visibleTransactions
            .filter { it.id.startsWith("auto-") }
            .take(3)
        if (completedPayments.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    "Completed payment plans",
                    style = MaterialTheme.typography.titleSmall,
                    color = WalletColors.muted
                )
                completedPayments.forEach { transaction ->
                    TransactionRow(transaction, state.accounts, state.baseCurrency)
                }
            }
        }

        Spacer(Modifier.height(14.dp))
        MonthlyWeeks(visibleState)
        Spacer(Modifier.height(70.dp))
    }

    if (transactionDialog) {
        TransactionSheet(
            state = state,
            initialKind = TransactionKind.valueOf(transactionKind),
            initialTransfer = transferMode,
            dismiss = { transactionDialog = false },
            onSave = addTransaction,
            onTransfer = transfer,
            onAddCategory = addCategory,
            onEditCategory = editCategory,
            onDeleteCategory = deleteCategory
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
private fun HeaderAction(
    icon: String,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier
) {
    Row(
        modifier = modifier
            .walletGlassSurface(RoundedCornerShape(16.dp))
            .debounceClickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        WalletIcon(icon, color = WalletColors.muted, size = 24.dp)
        Text(
            text = label,
            modifier = Modifier.padding(start = 8.dp),
            color = WalletColors.muted
        )
    }
}

@Composable
private fun HomeSectionHeading(title: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            Modifier
                .width(3.dp)
                .height(20.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(WalletBeige)
        )
        Text(
            title,
            style = MaterialTheme.typography.headlineSmall,
            color = WalletColors.cream
        )
    }
}

private fun upcomingDueLabel(payment: com.example.walletmanager.data.model.RecurringPayment): String {
    val dueDay = Regex("^Day\\s+(\\d+)$").matchEntire(payment.due)?.groupValues?.get(1)?.toIntOrNull()
        ?: return "Due ${payment.due.replaceFirstChar { it.lowercase() }}"
    val currentMonth = YearMonth.now()
    fun occurrence(month: YearMonth) = month.atDay(dueDay.coerceIn(1, month.lengthOfMonth()))
    val today = LocalDate.now()
    val dueDate = occurrence(currentMonth).let { if (it.isBefore(today)) occurrence(currentMonth.plusMonths(1)) else it }
    val ordinal = when (dueDate.dayOfMonth % 100) {
        11, 12, 13 -> "th"
        else -> when (dueDate.dayOfMonth % 10) {
            1 -> "st"
            2 -> "nd"
            3 -> "rd"
            else -> "th"
        }
    }
    val month = dueDate.month.getDisplayName(TextStyle.FULL, Locale.getDefault())
    return "Due on ${dueDate.dayOfMonth}$ordinal $month"
}

@Composable
private fun MonthlyWeeks(state: WalletUiState) {
    val weekRows = remember(state.transactions) {
        (1..5).map { week ->
            val items = state.transactions.filter { it.weekOfMonth == week }
            val income = items
                .filter { it.kind == TransactionKind.INCOME }
                .sumOf { it.amountMinor }
            val expense = items
                .filter { it.kind == TransactionKind.EXPENSE }
                .sumOf { it.amountMinor }

            Triple(week, income, expense) to items.size
        }
    }

    HomeSectionHeading("${LocalDate.now().month.getDisplayName(TextStyle.FULL, Locale.getDefault())} at a glance")
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        weekRows.forEach { (summary, count) ->
            val (week, income, expense) = summary

            Card(
                colors = CardDefaults.cardColors(WalletSurface.copy(alpha = 0.86f)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.walletGlassBorder(RoundedCornerShape(16.dp))
            ) {
                Column(
                    Modifier.padding(13.dp),
                    verticalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    Row(Modifier.fillMaxWidth()) {
                        Text(
                            text = "Week $week",
                            modifier = Modifier.weight(1f),
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "$count entries",
                            color = WalletColors.muted,
                            fontSize = 12.sp
                        )
                    }

                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Income ${formatMoney(income, state.balancesVisible, state.baseCurrency)}",
                            color = WalletGreen,
                            fontSize = 12.sp
                        )
                        Text(
                            text = "Spent ${formatMoney(expense, state.balancesVisible, state.baseCurrency)}",
                            color = WalletColors.red,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategorySheet(
    categories: List<Category>,
    selectedCategory: Category?,
    dismiss: () -> Unit,
    save: (String, String?) -> Unit
) {
    var name by rememberSaveable { mutableStateOf("") }
    var parentId by rememberSaveable {
        mutableStateOf(selectedCategory?.parentId ?: selectedCategory?.id)
    }
    val saveCategory = rememberDebouncedClick {
        save(name, parentId)
    }

    WalletModalBottomSheet(
        onDismissRequest = dismiss,
        contentWindowInsets = { WindowInsets(0, 0, 0, 0) },
        dragHandle = { WalletSheetBackButton(dismiss) },
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = WalletSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("Add category", fontWeight = FontWeight.Bold)
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Category Name") },
                modifier = Modifier.fillMaxWidth()
            )
            Text("Save under parent category", color = WalletColors.muted)
            Row(
                Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = parentId == null,
                    onClick = { parentId = null },
                    label = { Text("Main category") }
                )
                categories.filter { it.parentId == null }.forEach { category ->
                    FilterChip(
                        selected = parentId == category.id,
                        onClick = { parentId = category.id },
                        label = { Text(category.name) }
                    )
                }
            }
            Button(
                onClick = saveCategory,
                enabled = name.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(WalletBeige)
            ) {
                Text("Save Category", color = WalletColors.ink, fontWeight = FontWeight.Bold)
            }
            TextButton(onClick = dismiss, modifier = Modifier.fillMaxWidth()) {
                Text("Cancel")
            }
        }
    }
}
