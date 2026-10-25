package com.example.walletmanager.ui.screen.accounts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.walletmanager.data.model.Account
import com.example.walletmanager.data.model.Currency
import com.example.walletmanager.data.model.Category
import com.example.walletmanager.data.model.Transaction
import com.example.walletmanager.data.model.TransactionKind
import com.example.walletmanager.data.model.WalletUiState
import com.example.walletmanager.ui.component.ScreenHeader
import com.example.walletmanager.ui.component.SectionTitle
import com.example.walletmanager.ui.component.SummaryCard
import com.example.walletmanager.ui.component.TransactionRow
import com.example.walletmanager.ui.component.TransactionSheet
import com.example.walletmanager.ui.component.WalletColors
import com.example.walletmanager.ui.component.WalletPage
import com.example.walletmanager.ui.component.WalletActionButton
import com.example.walletmanager.ui.component.WalletConfirmDialog
import com.example.walletmanager.ui.component.WalletModalBottomSheet
import com.example.walletmanager.ui.component.WalletSheetBackButton
import com.example.walletmanager.ui.component.debounceClickable
import com.example.walletmanager.ui.component.formatMoney
import com.example.walletmanager.ui.component.AmountGroupingVisualTransformation
import com.example.walletmanager.ui.theme.WalletGreen
import com.example.walletmanager.ui.theme.WalletBeige
import com.example.walletmanager.ui.component.rememberDebouncedClick
import com.example.walletmanager.ui.theme.WalletSurface
import java.math.BigDecimal
import java.math.RoundingMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountDetailScreen(
    state: WalletUiState,
    accountId: String,
    goBack: () -> Unit,
    editAccount: (Account) -> Unit,
    deleteAccount: (Account) -> Unit,
    editTransaction: (Transaction) -> Unit,
    deleteTransaction: (Transaction) -> Unit,
    addCategory: (String, String?) -> Unit,
    editCategory: (Category) -> Unit,
    deleteCategory: (Category) -> Unit
) {
    var transactionToEdit by remember { mutableStateOf<Transaction?>(null) }
    var showAccountEditor by rememberSaveable { mutableStateOf(false) }
    var showDeleteConfirmation by rememberSaveable { mutableStateOf(false) }
    val account = remember(state.accounts, accountId) {
        state.accounts.firstOrNull { it.id == accountId }
    }
    val transactions = remember(state.transactions, accountId) {
        state.transactions.filter { it.accountId == accountId }
    }
    val linkedFdPrincipal = remember(state.fixedDeposits, accountId) {
        state.fixedDeposits.filter { it.accountId == accountId }.sumOf { it.principalMinor }
    }
    val liquidBalance = (account?.balanceMinor?.minus(linkedFdPrincipal) ?: 0L).coerceAtLeast(0)
    val platformHoldings = remember(state.holdings, accountId) {
        state.holdings.filter { it.platformAccountId == accountId }
    }

    WalletPage(PaddingValues()) {
        if (account == null) {
            ScreenHeader(
                "Account not found",
                "This account is no longer available",
                onBack = goBack
            )
            Text("Go back to your accounts to choose another one.")
            Text(
                text = "Back",
                color = WalletColors.cream,
                modifier = Modifier.debounceClickable(onClick = goBack)
            )
        } else {
            ScreenHeader(account.name, account.type, onBack = goBack)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                WalletActionButton("Edit account", { showAccountEditor = true })
                WalletActionButton(
                    "Delete account",
                    { showDeleteConfirmation = true },
                    destructive = true
                )
            }
            SummaryCard(
                if (account.type == "Brokerage") "Free funds" else "Current liquid balance",
                liquidBalance,
                state
            )
            if (linkedFdPrincipal > 0) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Fixed deposits", color = WalletColors.muted)
                    Text(
                        formatMoney(linkedFdPrincipal, state.balancesVisible, state.baseCurrency),
                        color = WalletColors.cream,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            if (account.type == "Brokerage") {
                SectionTitle("Investments on this platform")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Total invested", color = WalletColors.muted)
                    Text(
                        formatMoney(
                            platformHoldings.sumOf { it.costMinor },
                            state.balancesVisible,
                            state.baseCurrency
                        ),
                        fontWeight = FontWeight.SemiBold
                    )
                }
                if (platformHoldings.isEmpty()) {
                    Text("No investments are linked to this brokerage yet.", color = WalletColors.muted)
                } else {
                    platformHoldings.forEach { holding ->
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                            verticalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Text(holding.name, fontWeight = FontWeight.SemiBold)
                            Text(
                                "${holding.type.displayCategory().name.replace('_', ' ')} · Invested ${formatMoney(holding.costMinor, state.balancesVisible, state.baseCurrency)}",
                                color = WalletColors.muted
                            )
                        }
                    }
                }
            }
            SectionTitle("Account activity")

            if (transactions.isEmpty()) {
                Text("No transactions have been added to this account yet.")
            } else {
                transactions.forEach { transaction ->
                    TransactionRow(
                        item = transaction,
                        accounts = state.accounts,
                        currency = state.baseCurrency,
                        onClick = { transactionToEdit = transaction }
                    )
                }
            }

            val incomeTotal = transactions
                .filter { it.kind == TransactionKind.INCOME }
                .sumOf { it.amountMinor }
            val expenseTotal = transactions
                .filter { it.kind == TransactionKind.EXPENSE }
                .sumOf { it.amountMinor }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Income ${formatMoney(incomeTotal, state.balancesVisible, state.baseCurrency)}",
                    color = WalletGreen
                )
                Text(
                    text = "Spent ${formatMoney(expenseTotal, state.balancesVisible, state.baseCurrency)}",
                    color = WalletColors.red
                )
            }
        }
    }

    val selectedAccount = account
    if (selectedAccount != null) {
        if (showAccountEditor) {
            AccountEditorSheet(
                initial = selectedAccount,
                linkedFdPrincipalMinor = linkedFdPrincipal,
                currency = state.baseCurrency,
                dismiss = { showAccountEditor = false },
                save = {
                    editAccount(it)
                    showAccountEditor = false
                }
            )
        }

        if (showDeleteConfirmation) {
            WalletConfirmDialog(
                title = "Delete account?",
                message = "This also deletes every transaction stored in this account.",
                onDismiss = { showDeleteConfirmation = false },
                onConfirm = {
                    deleteAccount(selectedAccount)
                    showDeleteConfirmation = false
                    goBack()
                },
                confirmLabel = "Delete account"
            )
        }
    }

    val currentTransactionToEdit = transactionToEdit
    if (currentTransactionToEdit != null) {
        TransactionSheet(
            state = state,
            initialTransaction = currentTransactionToEdit,
            dismiss = { transactionToEdit = null },
            onSave = { title, amount, kind, category, accountId, note, cashback ->
                editTransaction(
                    currentTransactionToEdit.copy(
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

    Spacer(Modifier.height(70.dp))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AccountEditorSheet(
    initial: Account,
    linkedFdPrincipalMinor: Long,
    currency: Currency,
    dismiss: () -> Unit,
    save: (Account) -> Unit
) {
    var name by rememberSaveable { mutableStateOf(initial.name) }
    var type by rememberSaveable { mutableStateOf(initial.type) }
    var balance by rememberSaveable {
        mutableStateOf(((initial.balanceMinor - linkedFdPrincipalMinor).coerceAtLeast(0) / 100.0).toString())
    }
    var validationError by rememberSaveable { mutableStateOf<String?>(null) }
    val decimalPattern = remember { Regex("\\d*(\\.\\d{0,2})?") }
    val saveAccount = rememberDebouncedClick {
        val balanceValue = balance.toBigDecimalOrNull()
        when {
            name.isBlank() -> validationError = "Enter a name for the account."
            balanceValue == null || balanceValue < BigDecimal.ZERO ->
                validationError = "Enter a valid account balance."
            else -> runCatching {
                val balanceMinor = balanceValue.movePointRight(2)
                    .setScale(0, RoundingMode.HALF_UP).longValueExact()
                save(initial.copy(
                    name = name.trim(),
                    type = type,
                    balanceMinor = balanceMinor + linkedFdPrincipalMinor
                ))
            }.onFailure {
                validationError = "Enter a valid account balance."
            }.onSuccess {
                validationError = null
            }
        }
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
            Text("Edit account", fontWeight = FontWeight.Bold)
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Account name") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = balance,
                onValueChange = { if (it.matches(decimalPattern)) balance = it },
                label = { Text("Bank balance") },
                prefix = { Text("₹ ") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                visualTransformation = AmountGroupingVisualTransformation(currency),
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                "Fixed deposits linked to this account are counted separately in its total.",
                color = WalletColors.muted
            )
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Bank account", "Cash", "Credit card", "Brokerage").forEach { option ->
                    FilterChip(
                        selected = type == option,
                        onClick = { type = option },
                        label = { Text(option) }
                    )
                }
            }
            if (type == "Brokerage") {
                Text(
                    "For cash held at an investment broker, such as Zerodha funds.",
                    color = WalletColors.muted
                )
            }
            validationError?.let { Text(it, color = WalletColors.red, fontSize = 12.sp) }
            Button(
                onClick = saveAccount,
                enabled = name.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(WalletBeige)
            ) {
                Text("Save Account", color = WalletColors.ink, fontWeight = FontWeight.Bold)
            }
            TextButton(onClick = dismiss, modifier = Modifier.fillMaxWidth()) {
                Text("Cancel")
            }
        }
    }
}
