package com.example.walletmanager.ui.screen.accounts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.walletmanager.data.model.WalletUiState
import com.example.walletmanager.ui.component.AccountRow
import com.example.walletmanager.ui.component.ScreenHeader
import com.example.walletmanager.ui.component.SummaryCard
import com.example.walletmanager.ui.component.WalletColors
import com.example.walletmanager.ui.component.WalletPage
import com.example.walletmanager.ui.navigation.AccountDetailsRoute
import com.example.walletmanager.ui.navigation.WalletRoute
import com.example.walletmanager.ui.component.rememberDebouncedClick
import com.example.walletmanager.ui.theme.WalletGreen

@Composable
fun AccountsScreen(
    state: WalletUiState,
    open: (WalletRoute) -> Unit,
    addAccount: (String, String) -> Unit
) {
    var showAddDialog by rememberSaveable { mutableStateOf(false) }

    WalletPage(PaddingValues()) {
        ScreenHeader("Your accounts", "Everything in one view")
        SummaryCard("Total across accounts", state.balanceMinor, state)

        state.accounts.forEach { account ->
            AccountRow(
                account = account,
                state = state,
                onClick = { open(AccountDetailsRoute(account.id)) }
            )
        }

        OutlinedButton(
            onClick = { showAddDialog = true },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("+ Add another account")
        }

        Spacer(Modifier.height(70.dp))
    }

    if (showAddDialog) {
        AddAccountDialog(
            dismiss = { showAddDialog = false },
            save = { name, type ->
                addAccount(name, type)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun AddAccountDialog(
    dismiss: () -> Unit,
    save: (String, String) -> Unit
) {
    var name by rememberSaveable { mutableStateOf("") }
    var type by rememberSaveable { mutableStateOf("Bank account") }
    val saveAccount = rememberDebouncedClick {
        save(name, type)
    }

    AlertDialog(
        onDismissRequest = dismiss,
        title = { Text("Add account") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Account name") }
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
            }
        },
        confirmButton = {
            Button(
                onClick = saveAccount,
                enabled = name.isNotBlank(),
                colors = ButtonDefaults.buttonColors(WalletGreen)
            ) {
                Text("Add account", color = WalletColors.ink)
            }
        },
        dismissButton = {
            TextButton(onClick = dismiss) {
                Text("Cancel")
            }
        }
    )
}
