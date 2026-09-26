package com.example.walletmanager.ui.screen.filters

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.walletmanager.data.model.TransactionKind
import com.example.walletmanager.data.model.WalletUiState
import com.example.walletmanager.ui.component.ScreenHeader
import com.example.walletmanager.ui.component.SectionTitle
import com.example.walletmanager.ui.component.WalletColors
import com.example.walletmanager.ui.component.WalletPage
import com.example.walletmanager.ui.theme.WalletGreen

@Composable
fun FilterScreen(
    state: WalletUiState,
    apply: (String?, String?, TransactionKind?) -> Unit,
    clear: () -> Unit,
    onBack: () -> Unit
) {
    var accountId by remember { mutableStateOf(state.filterAccountId) }
    var category by remember { mutableStateOf(state.filterCategory) }
    var kind by remember { mutableStateOf(state.filterKind) }
    val categories = remember(state.categories) {
        state.categories.map { categoryItem ->
            val parent = state.categories.firstOrNull { it.id == categoryItem.parentId }
            val label = if (parent == null) {
                categoryItem.name
            } else {
                "${parent.name} · ${categoryItem.name}"
            }
            label to label
        }
    }

    WalletPage(PaddingValues()) {
        ScreenHeader("Filter activity", "Choose what Home should show", onBack = onBack)
        SectionTitle("Type")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = kind == null,
                onClick = { kind = null },
                label = { Text("All") }
            )
            FilterChip(
                selected = kind == TransactionKind.INCOME,
                onClick = { kind = TransactionKind.INCOME },
                label = { Text("Income") }
            )
            FilterChip(
                selected = kind == TransactionKind.EXPENSE,
                onClick = { kind = TransactionKind.EXPENSE },
                label = { Text("Expense") }
            )
        }

        SectionTitle("Accounts")
        Row(
            Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = accountId == null,
                onClick = { accountId = null },
                label = { Text("All accounts") }
            )
            state.accounts.forEach { account ->
                FilterChip(
                    selected = accountId == account.id,
                    onClick = { accountId = account.id },
                    label = { Text(account.name) }
                )
            }
        }

        SectionTitle("Categories")
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = category == null,
                onClick = { category = null },
                label = { Text("All categories") }
            )
            categories.forEach { (categoryName, label) ->
                FilterChip(
                    selected = category == categoryName,
                    onClick = { category = categoryName },
                    label = {
                        Text(
                            text = label,
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                )
            }
        }

        Text(
            "Filters are applied to Home activity and its weekly view.",
            color = WalletColors.muted
        )
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = clear,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors()
            ) {
                Text("Clear")
            }
            Button(
                onClick = { apply(accountId, category, kind) },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(WalletGreen)
            ) {
                Text("Apply", color = WalletColors.ink)
            }
        }
        Spacer(Modifier.height(70.dp))
    }
}
