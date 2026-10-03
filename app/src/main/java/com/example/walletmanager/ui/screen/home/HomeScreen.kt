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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.background
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.OutlinedButton
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
import com.example.walletmanager.ui.component.SectionTitle
import com.example.walletmanager.ui.component.TransactionRow
import com.example.walletmanager.ui.component.WalletColors
import com.example.walletmanager.ui.component.WalletPage
import com.example.walletmanager.ui.component.WalletSheetBackButton
import com.example.walletmanager.ui.component.debounceClickable
import com.example.walletmanager.ui.component.formatMoney
import com.example.walletmanager.ui.component.rememberDebouncedClick
import com.example.walletmanager.ui.component.walletDropdownItem
import com.example.walletmanager.ui.component.walletDropdownSurface
import com.example.walletmanager.ui.navigation.CalendarRoute
import com.example.walletmanager.ui.navigation.PlansRoute
import com.example.walletmanager.ui.navigation.MoreRoute
import com.example.walletmanager.ui.navigation.WalletRoute
import com.example.walletmanager.ui.theme.WalletGreen
import com.example.walletmanager.ui.theme.WalletSurface

@Composable
fun HomeScreen(
    state: WalletUiState,
    toggleBalances: () -> Unit,
    addTransaction: (String, Long, TransactionKind, String, String, String, Long) -> Unit,
    transfer: (String, Long, String, String, String) -> Unit,
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
                icon = "📅",
                label = "Calendar",
                onClick = { open(CalendarRoute) },
                modifier = Modifier.weight(1f)
            )
            HeaderAction(
                icon = "🗓️",
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
                glyph = "💸",
                label = "Expense",
                color = WalletColors.red,
                onClick = {
                    transferMode = false
                    transactionKind = TransactionKind.EXPENSE.name
                    transactionDialog = true
                },
                modifier = Modifier.weight(1f)
            )
            QuickAction(
                glyph = "💰",
                label = "Income",
                color = WalletGreen,
                onClick = {
                    transferMode = false
                    transactionKind = TransactionKind.INCOME.name
                    transactionDialog = true
                },
                modifier = Modifier.weight(1f)
            )
            QuickAction(
                glyph = "🔀",
                label = "Transfer",
                color = WalletColors.blue,
                onClick = {
                    transferMode = true
                    transactionKind = TransactionKind.EXPENSE.name
                    transactionDialog = true
                },
                modifier = Modifier.weight(1f)
            )
        }

        SectionTitle("Recent activity")
        Card(
            colors = CardDefaults.cardColors(WalletSurface),
            shape = RoundedCornerShape(22.dp)
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

        SectionTitle("Coming up")
        state.recurringPayments.firstOrNull()?.let {
            Text(
                "${it.name} · ${formatMoney(it.amountMinor, true, state.baseCurrency)} due ${it.due}",
                color = WalletColors.muted
            )
        }
        val completedPayments = visibleTransactions
            .filter { it.id.startsWith("auto-") }
            .take(3)
        if (completedPayments.isNotEmpty()) {
            SectionTitle("Completed payment plans")
            completedPayments.forEach { transaction ->
                TransactionRow(transaction, state.accounts, state.baseCurrency)
            }
        }

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
            .clip(RoundedCornerShape(16.dp))
            .background(WalletSurface)
            .debounceClickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(icon, fontSize = 18.sp)
        Text(
            text = label,
            modifier = Modifier.padding(start = 8.dp),
            color = WalletColors.muted
        )
    }
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

    SectionTitle("October at a glance")
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        weekRows.forEach { (summary, count) ->
            val (week, income, expense) = summary

            Card(
                colors = CardDefaults.cardColors(WalletSurface),
                shape = RoundedCornerShape(16.dp)
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
private fun TransactionDialog(
    state: WalletUiState,
    initialKind: TransactionKind,
    dismiss: () -> Unit,
    save: (String, Long, TransactionKind, String, String, String, Long) -> Unit,
    addCategory: (String, String?) -> Unit
) {
    var title by rememberSaveable { mutableStateOf("") }
    var amount by rememberSaveable { mutableStateOf("") }
    var kind by rememberSaveable { mutableStateOf(initialKind) }
    var accountId by rememberSaveable {
        mutableStateOf(state.accounts.firstOrNull()?.id.orEmpty())
    }
    var selectedCategoryId by rememberSaveable {
        mutableStateOf(state.categories.firstOrNull()?.id)
    }
    var note by rememberSaveable { mutableStateOf("") }
    var cashback by rememberSaveable { mutableStateOf("") }
    var accountMenu by remember { mutableStateOf(false) }
    var categoryMenu by remember { mutableStateOf(false) }
    var categoryDialog by rememberSaveable { mutableStateOf(false) }
    val selectedCategory = state.categories.firstOrNull { it.id == selectedCategoryId }
    val categoryName = selectedCategory?.let { category ->
        val parent = state.categories.firstOrNull { it.id == category.parentId }
        if (parent == null) category.name else "${parent.name} · ${category.name}"
    } ?: "Choose category"
    val selectedAccount = state.accounts.firstOrNull { it.id == accountId }
    val saveTransaction = rememberDebouncedClick {
        save(
            title,
            ((amount.toDoubleOrNull() ?: 0.0) * 100).toLong(),
            kind,
            categoryName,
            accountId,
            note,
            ((cashback.toDoubleOrNull() ?: 0.0) * 100).toLong()
        )
        dismiss()
    }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = dismiss,
        contentWindowInsets = { WindowInsets(0, 0, 0, 0) },
        dragHandle = { WalletSheetBackButton(dismiss) },
        sheetState = sheetState
    ) {
        Column(
            Modifier
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Add transaction", style = androidx.compose.material3.MaterialTheme.typography.titleLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = kind == TransactionKind.EXPENSE,
                    onClick = { kind = TransactionKind.EXPENSE },
                    label = { Text("💸 Expense") }
                )
                FilterChip(
                    selected = kind == TransactionKind.INCOME,
                    onClick = { kind = TransactionKind.INCOME },
                    label = { Text("💰 Income") }
                )
            }
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("Amount") },
                prefix = { Text("₹ ") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth()
            )
            Box {
                OutlinedButton(
                    onClick = { accountMenu = !accountMenu },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(selectedAccount?.name ?: "Choose account")
                }
                DropdownMenu(
                    expanded = accountMenu,
                    onDismissRequest = { accountMenu = false },
                    modifier = Modifier.walletDropdownSurface()
                ) {
                    state.accounts.forEach { account ->
                        DropdownMenuItem(
                            modifier = Modifier.walletDropdownItem(),
                            text = { Text(account.name) },
                            onClick = {
                                accountId = account.id
                                accountMenu = false
                            }
                        )
                    }
                }
            }
            Box {
                OutlinedButton(
                    onClick = { categoryMenu = !categoryMenu },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(categoryName)
                }
                DropdownMenu(
                    expanded = categoryMenu,
                    onDismissRequest = { categoryMenu = false },
                    modifier = Modifier.walletDropdownSurface()
                ) {
                    state.categories.forEach { category ->
                        val parent = state.categories.firstOrNull { it.id == category.parentId }
                        DropdownMenuItem(
                            modifier = Modifier.walletDropdownItem(),
                            text = {
                                Text(
                                    if (parent == null) category.name else "   ${parent.name} · ${category.name}"
                                )
                            },
                            onClick = {
                                selectedCategoryId = category.id
                                categoryMenu = false
                            }
                        )
                    }
                    DropdownMenuItem(
                        modifier = Modifier.walletDropdownItem(),
                        text = { Text("+ Add category or subcategory") },
                        onClick = {
                            categoryMenu = false
                            categoryDialog = true
                        }
                    )
                }
            }
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Note") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )
            OutlinedTextField(
                value = cashback,
                onValueChange = { cashback = it },
                label = { Text("Cashback (optional)") },
                prefix = { Text("₹ ") },
                modifier = Modifier.fillMaxWidth()
            )
            if (state.notes.isNotEmpty()) {
                Text("Previous notes", color = WalletColors.muted)
                Row(Modifier.horizontalScroll(rememberScrollState())) {
                    state.notes.take(5).forEach { previousNote ->
                        FilterChip(
                            selected = note == previousNote,
                            onClick = { note = previousNote },
                            label = { Text(previousNote) }
                        )
                    }
                }
            }
            Button(
                onClick = saveTransaction,
                enabled = amount.toDoubleOrNull()?.let { it > 0 } == true && accountId.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(WalletGreen)
            ) {
                Text("Save transaction", color = WalletColors.ink)
            }
            Spacer(Modifier.height(18.dp))
        }
    }

    if (categoryDialog) {
        CategorySheet(
            categories = state.categories,
            selectedCategory = selectedCategory,
            dismiss = { categoryDialog = false },
            save = { name, parentId ->
                addCategory(name, parentId)
                categoryDialog = false
            }
        )
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

    ModalBottomSheet(
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
                colors = ButtonDefaults.buttonColors(WalletGreen)
            ) {
                Text("Save Category", color = WalletColors.ink, fontWeight = FontWeight.Bold)
            }
            TextButton(onClick = dismiss, modifier = Modifier.fillMaxWidth()) {
                Text("Cancel")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SalarySheet(
    state: WalletUiState,
    dismiss: () -> Unit,
    save: (String, Long, TransactionKind, String, String, String, Long) -> Unit
) {
    var amount by remember { mutableStateOf("") }
    val account = remember(state.accounts) {
        state.accounts.firstOrNull()?.id.orEmpty()
    }
    val saveSalary = rememberDebouncedClick {
        save(
            "Monthly salary",
            ((amount.toDoubleOrNull() ?: 0.0) * 100).toLong(),
            TransactionKind.INCOME,
            "Salary",
            account,
            "",
            0
        )
        dismiss()
    }

    ModalBottomSheet(
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
            Text("Add salary", fontWeight = FontWeight.Bold)
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("Net salary amount") },
                prefix = { Text("₹ ") },
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                onClick = saveSalary,
                enabled = amount.toDoubleOrNull()?.let { it > 0 } == true,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(WalletGreen)
            ) {
                Text("Save Salary", color = WalletColors.ink, fontWeight = FontWeight.Bold)
            }
            TextButton(onClick = dismiss, modifier = Modifier.fillMaxWidth()) {
                Text("Cancel")
            }
        }
    }
}
