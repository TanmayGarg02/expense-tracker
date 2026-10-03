package com.example.walletmanager.ui.screen.plans

import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.walletmanager.data.model.InvestmentType
import com.example.walletmanager.data.model.RecurringPayment
import com.example.walletmanager.data.model.WalletUiState
import com.example.walletmanager.ui.component.ScreenHeader
import com.example.walletmanager.ui.component.WalletColors
import com.example.walletmanager.ui.component.WalletPage
import com.example.walletmanager.ui.component.WalletSheetBackButton
import com.example.walletmanager.ui.component.formatMoney
import com.example.walletmanager.ui.component.rememberDebouncedClick
import com.example.walletmanager.ui.theme.WalletGreen
import com.example.walletmanager.ui.theme.WalletSurface

@Composable
fun PlansScreen(
    state: WalletUiState,
    addPlan: (String, Long, String, Long, Boolean, String, InvestmentType) -> Unit,
    editPlan: (RecurringPayment) -> Unit,
    deletePlan: (String) -> Unit,
    togglePlan: (String) -> Unit,
    onBack: () -> Unit
) {
    var showSheet by rememberSaveable { mutableStateOf(false) }
    var selectedPlan by remember { mutableStateOf<RecurringPayment?>(null) }

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
                    TextButton(onClick = { selectedPlan = item }) {
                        Text("Edit")
                    }
                    TextButton(onClick = { togglePlan(item.id) }) {
                        Text(if (item.active) "Pause" else "Start")
                    }
                }
            }
        }

        Button(
            onClick = { showSheet = true },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(WalletGreen)
        ) {
            Text("+ Set up a recurring payment", color = WalletColors.ink, fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.height(70.dp))
    }

    if (showSheet) {
        PlanSheet(
            state = state,
            initial = null,
            dismiss = { showSheet = false },
            save = addPlan
        )
    }

    val currentSelectedPlan = selectedPlan
    if (currentSelectedPlan != null) {
        PlanSheet(
            state = state,
            initial = currentSelectedPlan,
            dismiss = { selectedPlan = null },
            save = { name, amount, frequency, cashback, investment, accountId, investmentType ->
                editPlan(
                    currentSelectedPlan.copy(
                        name = name,
                        amountMinor = amount,
                        frequency = frequency,
                        cashbackMinor = cashback,
                        accountId = accountId,
                        investmentType = investmentType,
                        category = if (investment) "Investments" else "Bills"
                    )
                )
            },
            delete = {
                deletePlan(currentSelectedPlan.id)
                selectedPlan = null
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlanSheet(
    state: WalletUiState,
    initial: RecurringPayment?,
    dismiss: () -> Unit,
    save: (String, Long, String, Long, Boolean, String, InvestmentType) -> Unit,
    delete: (() -> Unit)? = null
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
            Text(
                if (initial == null) "Set up recurring payment" else "Edit recurring payment",
                fontWeight = FontWeight.Bold
            )
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Payment name") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("Amount") },
                prefix = { Text("₹ ") },
                modifier = Modifier.fillMaxWidth()
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
                prefix = { Text("₹ ") },
                modifier = Modifier.fillMaxWidth()
            )
            Text("Debited from account", color = WalletColors.muted)
            Row(
                Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                state.accounts.forEach { account ->
                    FilterChip(
                        selected = accountId == account.id,
                        onClick = { accountId = account.id },
                        label = { Text(account.name) }
                    )
                }
            }
            Button(
                onClick = savePlan,
                enabled = name.isNotBlank() && amount.toDoubleOrNull()?.let { it > 0 } == true && accountId.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(WalletGreen)
            ) {
                Text("Save Plan", color = WalletColors.ink, fontWeight = FontWeight.Bold)
            }
            if (delete != null) {
                TextButton(onClick = delete, modifier = Modifier.fillMaxWidth()) {
                    Text("Delete payment plan", color = WalletColors.red)
                }
            }
            TextButton(onClick = dismiss, modifier = Modifier.fillMaxWidth()) {
                Text("Cancel")
            }
        }
    }
}
