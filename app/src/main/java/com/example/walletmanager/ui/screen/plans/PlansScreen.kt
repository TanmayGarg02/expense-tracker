package com.example.walletmanager.ui.screen.plans

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.walletmanager.data.model.RecurringPayment
import com.example.walletmanager.data.model.InvestmentType
import com.example.walletmanager.data.model.WalletUiState
import com.example.walletmanager.ui.component.ScreenHeader
import com.example.walletmanager.ui.component.WalletColors
import com.example.walletmanager.ui.component.WalletPage
import com.example.walletmanager.ui.component.formatMoney
import com.example.walletmanager.ui.component.rememberDebouncedClick
import com.example.walletmanager.ui.theme.WalletGreen
import com.example.walletmanager.ui.theme.WalletSurface

@Composable
fun PlansScreen(
    state: WalletUiState,
    addPlan: (String, Long, String, Long, Boolean, String, InvestmentType) -> Unit,
    editPlan: (RecurringPayment) -> Unit,
    togglePlan: (String) -> Unit,
    onBack: () -> Unit
) {
    var dialog by rememberSaveable { mutableStateOf(false) }
    var selected by remember { mutableStateOf<RecurringPayment?>(null) }

    WalletPage(PaddingValues()) {
        ScreenHeader(
            "Payment plans",
            "Automatic salary, SIPs, insurance and subscriptions",
            onBack = onBack
        )

        state.recurringPayments.forEach { item ->
            Card(
                colors = CardDefaults.cardColors(WalletSurface),
                shape = RoundedCornerShape(18.dp)
            ) {
                Row(
                    Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(item.name, fontWeight = FontWeight.Bold)
                        Text(
                            "${item.frequency} · ${item.due} · ${if (item.active) "Active" else "Paused"}",
                            color = if (item.active) WalletGreen else WalletColors.muted
                        )
                        Text(
                            formatMoney(item.amountMinor, state.balancesVisible, state.baseCurrency),
                            fontWeight = FontWeight.Bold
                        )
                        if (item.cashbackMinor > 0) {
                            Text(
                                "Cashback ${formatMoney(item.cashbackMinor, state.balancesVisible, state.baseCurrency)}",
                                color = WalletGreen,
                                fontSize = 12.sp
                            )
                        }
                    }
                    TextButton(onClick = { selected = item }) {
                        Text("Edit")
                    }
                    TextButton(onClick = { togglePlan(item.id) }) {
                        Text(if (item.active) "Pause" else "Start")
                    }
                }
            }
        }

        Button(
            onClick = { dialog = true },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(WalletGreen)
        ) {
            Text("+ Set up a recurring payment", color = WalletColors.ink)
        }

        Spacer(Modifier.height(70.dp))
    }

    if (dialog) {
        PlanDialog(
            state = state,
            initial = null,
            dismiss = { dialog = false },
            save = addPlan
        )
    }

    selected?.let { payment ->
        PlanDialog(
            state = state,
            initial = payment,
            dismiss = { selected = null },
            save = { name, amount, frequency, cashback, investment, accountId, investmentType ->
                editPlan(
                    payment.copy(
                        name = name,
                        amountMinor = amount,
                        frequency = frequency,
                        cashbackMinor = cashback,
                        accountId = accountId,
                        investmentType = investmentType,
                        category = if (investment) "Investments" else "Bills"
                    )
                )
            }
        )
    }
}

@Composable
private fun PlanDialog(
    state: WalletUiState,
    initial: RecurringPayment?,
    dismiss: () -> Unit,
    save: (String, Long, String, Long, Boolean, String, InvestmentType) -> Unit
) {
    var name by remember { mutableStateOf(initial?.name.orEmpty()) }
    var amount by remember { mutableStateOf(initial?.amountMinor?.div(100.0)?.toString().orEmpty()) }
    var frequency by remember { mutableStateOf(initial?.frequency ?: "Monthly") }
    var cashback by remember { mutableStateOf(initial?.cashbackMinor?.div(100.0)?.toString().orEmpty()) }
    var investment by remember { mutableStateOf(initial?.category == "Investments") }
    var investmentType by remember { mutableStateOf(initial?.investmentType ?: InvestmentType.MUTUAL_FUND) }
    var accountId by remember { mutableStateOf(initial?.accountId ?: state.accounts.firstOrNull()?.id.orEmpty()) }
    val savePlan = rememberDebouncedClick {
        save(
            name,
            ((amount.toDoubleOrNull() ?: 0.0) * 100).toLong(),
            frequency,
            ((cashback.toDoubleOrNull() ?: 0.0) * 100).toLong(),
            investment,
            accountId,
            investmentType
        )
        dismiss()
    }

    AlertDialog(
        onDismissRequest = dismiss,
        title = { Text(if (initial == null) "Set up recurring payment" else "Edit recurring payment") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") }
                )
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount") },
                    prefix = { Text("₹ ") }
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = frequency == "Weekly",
                        onClick = { frequency = "Weekly" },
                        label = { Text("Weekly") }
                    )
                    FilterChip(
                        selected = frequency == "Monthly",
                        onClick = { frequency = "Monthly" },
                        label = { Text("Monthly") }
                    )
                    FilterChip(
                        selected = frequency == "Yearly",
                        onClick = { frequency = "Yearly" },
                        label = { Text("Yearly") }
                    )
                }
                if (investment) {
                    Text("SIP invests in", color = WalletColors.muted)
                    Row(
                        Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        InvestmentType.entries
                            .filter { it != InvestmentType.SIP }
                            .forEach { type ->
                                FilterChip(
                                    selected = investmentType == type,
                                    onClick = { investmentType = type },
                                    label = { Text(type.name.replace('_', ' ')) }
                                )
                            }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = !investment,
                        onClick = { investment = false },
                        label = { Text("Regular payment") }
                    )
                    FilterChip(
                        selected = investment,
                        onClick = { investment = true },
                        label = { Text("Investment SIP") }
                    )
                }
                OutlinedTextField(
                    value = cashback,
                    onValueChange = { cashback = it },
                    label = { Text("Cashback (optional)") },
                    prefix = { Text("₹ ") }
                )
                Text("Account", color = WalletColors.muted)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    state.accounts.forEach { account ->
                        FilterChip(
                            selected = accountId == account.id,
                            onClick = { accountId = account.id },
                            label = { Text(account.name) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = savePlan,
                enabled = name.isNotBlank() && amount.toDoubleOrNull()?.let { it > 0 } == true && accountId.isNotBlank(),
                colors = ButtonDefaults.buttonColors(WalletGreen)
            ) {
                Text("Save", color = WalletColors.ink)
            }
        }
    )
}
