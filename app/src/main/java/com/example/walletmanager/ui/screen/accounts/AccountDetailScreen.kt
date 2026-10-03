package com.example.walletmanager.ui.screen.accounts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.ModalBottomSheet
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.walletmanager.data.model.Account
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
import com.example.walletmanager.ui.component.WalletSheetBackButton
import com.example.walletmanager.ui.component.debounceClickable
import com.example.walletmanager.ui.component.formatMoney
import com.example.walletmanager.ui.component.rememberDebouncedClick
import com.example.walletmanager.ui.theme.WalletGreen
import com.example.walletmanager.ui.theme.WalletSurface

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
                color = WalletColors.blue,
                modifier = Modifier.debounceClickable(onClick = goBack)
            )
        } else {
            ScreenHeader(account.name, account.type, onBack = goBack)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Edit account",
                    color = WalletColors.blue,
                    modifier = Modifier.debounceClickable {
                        showAccountEditor = true
                    }
                )
                Text(
                    text = "Delete account",
                    color = WalletColors.red,
                    modifier = Modifier.debounceClickable {
                        showDeleteConfirmation = true
                    }
                )
            }
            SummaryCard("Current balance", account.balanceMinor, state)
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

            Text(
                text = "Income ${formatMoney(
                    transactions
                        .filter { it.kind == TransactionKind.INCOME }
                        .sumOf { it.amountMinor },
                    state.balancesVisible,
                    state.baseCurrency
                )}",
                color = WalletColors.muted
            )
        }
    }

    val selectedAccount = account
    if (selectedAccount != null) {
        if (showAccountEditor) {
            AccountEditorSheet(
                initial = selectedAccount,
                dismiss = { showAccountEditor = false },
                save = {
                    editAccount(it)
                    showAccountEditor = false
                }
            )
        }

        if (showDeleteConfirmation) {
            ModalBottomSheet(
                onDismissRequest = { showDeleteConfirmation = false },
                contentWindowInsets = { WindowInsets(0, 0, 0, 0) },
                dragHandle = {
                    WalletSheetBackButton { showDeleteConfirmation = false }
                },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false),
                containerColor = WalletSurface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Delete account?", fontWeight = FontWeight.Bold)
                    Text("This also deletes every transaction stored in this account.", color = WalletColors.muted)
                    Button(
                        onClick = {
                            deleteAccount(selectedAccount)
                            showDeleteConfirmation = false
                            goBack()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(WalletColors.red)
                    ) {
                        Text("Delete Account", fontWeight = FontWeight.Bold)
                    }
                    TextButton(onClick = { showDeleteConfirmation = false }, modifier = Modifier.fillMaxWidth()) {
                        Text("Cancel")
                    }
                }
            }
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
    dismiss: () -> Unit,
    save: (Account) -> Unit
) {
    var name by rememberSaveable { mutableStateOf(initial.name) }
    var type by rememberSaveable { mutableStateOf(initial.type) }
    val saveAccount = rememberDebouncedClick {
        save(initial.copy(name = name, type = type))
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
            Text("Edit account", fontWeight = FontWeight.Bold)
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Account name") },
                modifier = Modifier.fillMaxWidth()
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Bank account", "Cash", "Credit card").forEach { option ->
                    FilterChip(
                        selected = type == option,
                        onClick = { type = option },
                        label = { Text(option) }
                    )
                }
            }
            Button(
                onClick = saveAccount,
                enabled = name.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(WalletGreen)
            ) {
                Text("Save Account", color = WalletColors.ink, fontWeight = FontWeight.Bold)
            }
            TextButton(onClick = dismiss, modifier = Modifier.fillMaxWidth()) {
                Text("Cancel")
            }
        }
    }
}
