package com.example.walletmanager.ui.screen.plans

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.walletmanager.data.model.InvestmentType
import com.example.walletmanager.data.model.OTHER_INVESTMENT_PLATFORM_ID
import com.example.walletmanager.data.model.RecurringPayment
import com.example.walletmanager.data.model.WalletUiState
import com.example.walletmanager.ui.component.ScreenHeader
import com.example.walletmanager.ui.component.WalletColors
import com.example.walletmanager.ui.component.walletGlassBorder
import com.example.walletmanager.ui.component.WalletActionButton
import com.example.walletmanager.ui.component.WalletConfirmDialog
import com.example.walletmanager.ui.component.WalletPage
import com.example.walletmanager.ui.component.WalletModalBottomSheet
import com.example.walletmanager.ui.component.WalletSheetBackButton
import com.example.walletmanager.ui.component.formatMoney
import com.example.walletmanager.ui.component.rememberDebouncedClick
import com.example.walletmanager.ui.theme.WalletGreen
import com.example.walletmanager.ui.theme.WalletSurface

@Composable
fun PlansScreen(
    state: WalletUiState,
    addPlan: (String, Long, String, Long, Boolean, String, InvestmentType, String?, String?) -> Unit,
    editPlan: (RecurringPayment) -> Unit,
    deletePlan: (String) -> Unit,
    togglePlan: (String) -> Unit,
    onBack: () -> Unit
) {
    var showSheet by rememberSaveable { mutableStateOf(false) }
    var selectedPlan by remember { mutableStateOf<RecurringPayment?>(null) }
    var confirmPlanDelete by rememberSaveable { mutableStateOf(false) }

    WalletPage(PaddingValues()) {
        ScreenHeader(
            "Payment plans",
            "Automatic salary, SIPs, insurance and subscriptions",
            onBack = onBack
        )

        state.recurringPayments.forEach { item ->
            Card(
                colors = CardDefaults.cardColors(WalletSurface.copy(alpha = 0.88f)),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.walletGlassBorder(RoundedCornerShape(18.dp))
            ) {
                Column(
                    Modifier.fillMaxWidth().padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Column(Modifier.fillMaxWidth()) {
                        Text(item.name, fontWeight = FontWeight.Bold)
                        Text(
                            if (item.category == "Investments") {
                                val existingFund = state.holdings.firstOrNull { it.id == item.investmentHoldingId }?.name
                                "${investmentTypeName(item.investmentType)} SIP · ${existingFund ?: "New fund"} · ${item.frequency} · ${item.due} · ${if (item.active) "Active" else "Paused"}"
                            } else {
                                "${item.frequency} · ${item.due} · ${if (item.active) "Active" else "Paused"}"
                            },
                            color = if (item.active) WalletGreen else WalletColors.muted
                        )
                        Text(
                            formatMoney(item.amountMinor, state.balancesVisible, state.baseCurrency),
                            fontWeight = FontWeight.Bold
                        )
                        if (item.cashbackMinor > 0 && item.category != "Investments") {
                            Text(
                                "Cashback ${formatMoney(item.cashbackMinor, state.balancesVisible, state.baseCurrency)}",
                                color = WalletGreen,
                                fontSize = 12.sp
                            )
                        }
                    }
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
                    ) {
                        WalletActionButton("Edit", onClick = { selectedPlan = item }, compact = true)
                        WalletActionButton(
                            if (item.active) "Pause" else "Start",
                            onClick = { togglePlan(item.id) },
                            compact = true
                        )
                    }
                }
            }
        }

        Button(
            onClick = { showSheet = true },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(WalletColors.cream)
        ) {
            Text("+ Set up a payment or SIP", color = WalletColors.ink, fontWeight = FontWeight.Bold)
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
            save = { name, amount, frequency, cashback, investment, accountId, investmentType, investmentHoldingId, investmentPlatformAccountId ->
                editPlan(
                    currentSelectedPlan.copy(
                        name = name,
                        amountMinor = amount,
                        frequency = frequency,
                        cashbackMinor = cashback,
                        accountId = accountId,
                        investmentType = investmentType,
                        investmentHoldingId = investmentHoldingId,
                        investmentPlatformAccountId = investmentPlatformAccountId,
                        category = if (investment) "Investments" else "Bills"
                    )
                )
            },
            delete = {
                confirmPlanDelete = true
            }
        )
    }

    if (confirmPlanDelete && selectedPlan != null) {
        WalletConfirmDialog(
            title = "Delete payment plan?",
            message = "${selectedPlan?.name} will be removed.",
            onDismiss = { confirmPlanDelete = false },
            onConfirm = {
                selectedPlan?.let { deletePlan(it.id) }
                confirmPlanDelete = false
                selectedPlan = null
            },
            confirmLabel = "Delete plan"
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlanSheet(
    state: WalletUiState,
    initial: RecurringPayment?,
    dismiss: () -> Unit,
    save: (String, Long, String, Long, Boolean, String, InvestmentType, String?, String?) -> Unit,
    delete: (() -> Unit)? = null
) {
    var name by remember { mutableStateOf(initial?.name.orEmpty()) }
    var amount by remember { mutableStateOf(initial?.amountMinor?.div(100.0)?.toString().orEmpty()) }
    var frequency by remember { mutableStateOf(initial?.frequency ?: "Monthly") }
    var cashback by remember { mutableStateOf(initial?.cashbackMinor?.div(100.0)?.toString().orEmpty()) }
    var investment by remember { mutableStateOf(initial?.category == "Investments") }
    var investmentType by remember {
        mutableStateOf(initial?.investmentType?.displayCategory() ?: InvestmentType.MUTUAL_FUND)
    }
    var investmentHoldingId by rememberSaveable { mutableStateOf(initial?.investmentHoldingId.orEmpty()) }
    var investmentPlatformAccountId by rememberSaveable {
        mutableStateOf(
            initial?.investmentPlatformAccountId
                ?: initial?.investmentHoldingId?.let { holdingId -> state.holdings.firstOrNull { it.id == holdingId }?.platformAccountId }
                ?: initial?.let { payment -> state.holdings.firstOrNull { it.recurringPaymentId == payment.id }?.platformAccountId }
                ?: state.accounts.firstOrNull { it.type == "Brokerage" }?.id
                ?: OTHER_INVESTMENT_PLATFORM_ID
        )
    }
    var accountId by remember { mutableStateOf(initial?.accountId ?: state.accounts.firstOrNull()?.id.orEmpty()) }
    val savePlan = rememberDebouncedClick {
        save(
            name,
            ((amount.toDoubleOrNull() ?: 0.0) * 100).toLong(),
            if (investment) "Monthly" else frequency,
            if (investment) 0L else ((cashback.toDoubleOrNull() ?: 0.0) * 100).toLong(),
            investment,
            accountId,
            investmentType,
            investmentHoldingId.takeIf { investment && it.isNotBlank() },
            investmentPlatformAccountId.takeIf { investment }
        )
        dismiss()
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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                when {
                    initial == null && investment -> "Set up mutual fund SIP"
                    initial != null && investment -> "Edit investment SIP"
                    initial == null -> "Set up recurring payment"
                    else -> "Edit recurring payment"
                },
                fontWeight = FontWeight.Bold
            )
            Text("Choose what you’re setting up", color = WalletColors.muted)
            Row(
                Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = !investment,
                    onClick = { investment = false },
                    label = { Text("Regular payment") }
                )
                FilterChip(
                    selected = investment,
                    onClick = { investment = true },
                    label = { Text("Mutual fund SIP") }
                )
            }
            if (investment) {
                Text("A SIP adds a recurring investment to your Investments.", color = WalletColors.muted)
                Text("Invest in", color = WalletColors.muted)
                Row(
                    Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(InvestmentType.MUTUAL_FUND, InvestmentType.INDEX_FUND).forEach { type ->
                        FilterChip(
                            selected = investmentType == type,
                            onClick = {
                                investmentType = type
                                investmentHoldingId = ""
                            },
                            label = { Text(investmentTypeName(type)) }
                        )
                    }
                }
                if (investmentHoldingId.isBlank()) {
                    Text("Platform", color = WalletColors.muted)
                    Row(
                        Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        state.accounts.filter { it.type == "Brokerage" }.forEach { platform ->
                            FilterChip(
                                selected = investmentPlatformAccountId == platform.id,
                                onClick = {
                                    investmentPlatformAccountId = platform.id
                                    investmentHoldingId = ""
                                },
                                label = { Text(platform.name) }
                            )
                        }
                        FilterChip(
                            selected = investmentPlatformAccountId == OTHER_INVESTMENT_PLATFORM_ID,
                            onClick = {
                                investmentPlatformAccountId = OTHER_INVESTMENT_PLATFORM_ID
                                investmentHoldingId = ""
                            },
                            label = { Text("Other") }
                        )
                    }
                }
                Text("Apply SIP to", color = WalletColors.muted)
                Row(
                    Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = investmentHoldingId.isBlank(),
                        onClick = { investmentHoldingId = "" },
                        label = { Text("New fund") }
                    )
                    state.holdings
                        .filter {
                            it.recurringPaymentId == null &&
                                it.type.displayCategory() == investmentType
                        }
                        .forEach { holding ->
                            FilterChip(
                                selected = investmentHoldingId == holding.id,
                                onClick = {
                                    investmentHoldingId = holding.id
                                    investmentType = holding.type.displayCategory()
                                    investmentPlatformAccountId = holding.platformAccountId ?: OTHER_INVESTMENT_PLATFORM_ID
                                },
                                label = { Text(holding.name) }
                            )
                        }
                }
            }
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(if (investment) "Fund or SIP name" else "Payment name") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text(if (investment) "SIP amount" else "Payment amount") },
                prefix = { Text("₹ ") },
                modifier = Modifier.fillMaxWidth()
            )
            if (investment) {
                Text("Monthly schedule", color = WalletColors.muted)
            } else {
                Row(
                    Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
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
            }
            if (!investment) {
                OutlinedTextField(
                    value = cashback,
                    onValueChange = { cashback = it },
                    label = { Text("Cashback (optional)") },
                    prefix = { Text("₹ ") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Text(
                "Debited from account",
                modifier = Modifier.fillMaxWidth(),
                color = WalletColors.muted,
                textAlign = TextAlign.Center
            )
            Row(
                Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                state.accounts.forEach { account ->
                    FilterChip(
                        selected = accountId == account.id,
                        onClick = { accountId = account.id },
                        label = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Text(account.name)
                                if (accountId == account.id) {
                                    Text("✓", color = WalletColors.ink, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    )
                }
            }
            Button(
                onClick = savePlan,
                enabled = name.isNotBlank() && amount.toDoubleOrNull()?.let { it > 0 } == true && accountId.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(WalletColors.cream)
            ) {
                Text(if (investment) "Set up SIP" else "Save Plan", color = WalletColors.ink, fontWeight = FontWeight.Bold)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (delete != null) {
                    OutlinedButton(
                        onClick = delete,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, WalletColors.redAccent.copy(alpha = 0.65f)),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = WalletColors.red)
                    ) {
                        Text("Delete plan", color = WalletColors.red, style = androidx.compose.material3.MaterialTheme.typography.labelSmall)
                    }
                }
                OutlinedButton(
                    onClick = dismiss,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = WalletColors.cream)
                ) {
                    Text("Cancel", color = WalletColors.cream)
                }
            }
        }
    }
}

private fun investmentTypeName(type: InvestmentType): String = when (type.displayCategory()) {
    InvestmentType.STOCK -> "Stocks"
    InvestmentType.MUTUAL_FUND -> "Mutual funds"
    InvestmentType.INDEX_FUND -> "Index funds"
    InvestmentType.OTHER -> "Other plans"
    else -> type.name.replace('_', ' ')
}
