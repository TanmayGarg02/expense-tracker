package com.example.walletmanager.ui.screen.investments

import androidx.compose.foundation.clickable
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.walletmanager.data.model.Holding
import com.example.walletmanager.data.model.InvestmentType
import com.example.walletmanager.data.model.PendingIpo
import com.example.walletmanager.data.model.WalletUiState
import com.example.walletmanager.ui.component.ScreenHeader
import com.example.walletmanager.ui.component.SectionTitle
import com.example.walletmanager.ui.component.SummaryCard
import com.example.walletmanager.ui.component.WalletColors
import com.example.walletmanager.ui.component.WalletPage
import com.example.walletmanager.ui.component.formatMoney
import com.example.walletmanager.ui.component.rememberDebouncedClick
import com.example.walletmanager.ui.theme.WalletGreen
import com.example.walletmanager.ui.theme.WalletSurface
import java.time.LocalDate
import java.time.ZoneId

@Composable
fun InvestmentsScreen(
    state: WalletUiState,
    addHolding: (String, InvestmentType, String, Long, Long) -> Unit,
    editHolding: (Holding) -> Unit,
    addIpo: (PendingIpo) -> Unit,
    rejectIpo: (String) -> Unit
) {
    var selected by remember { mutableStateOf<Holding?>(null) }
    var add by rememberSaveable { mutableStateOf(false) }
    var addIpoDialog by rememberSaveable { mutableStateOf(false) }
    val holdingsByType = remember(state.holdings) {
        InvestmentType.entries.associateWith { type ->
            state.holdings.filter { it.type == type }
        }
    }
    val totalInvested = remember(state.holdings) {
        state.holdings.sumOf { it.costMinor }
    }

    WalletPage(PaddingValues()) {
        ScreenHeader("Investments", "Stocks, equities, funds and SIPs")
        SummaryCard("Total invested", totalInvested, state)
        Text(
            "Track every stock, mutual fund, equity and SIP separately.",
            color = WalletColors.muted
        )

        if (state.pendingIpos.isNotEmpty()) {
            SectionTitle("⏳ Pending IPOs")
            state.pendingIpos.forEach { ipo ->
                Card(
                    colors = CardDefaults.cardColors(WalletSurface),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Row(Modifier.padding(16.dp)) {
                        Column(Modifier.weight(1f)) {
                            Text(ipo.name, fontWeight = FontWeight.Bold)
                            Text("Allotment ${ipo.allotmentDate}", color = WalletColors.muted)
                            Text(formatMoney(ipo.amountMinor, state.balancesVisible, state.baseCurrency))
                        }
                        TextButton(onClick = { rejectIpo(ipo.id) }) {
                            Text("Delete")
                        }
                    }
                }
            }
        }

        InvestmentType.entries.forEach { type ->
            val holdings = holdingsByType[type].orEmpty()
            if (holdings.isNotEmpty()) {
                SectionTitle("${investmentEmoji(type)} ${type.name.replace('_', ' ')}")
                holdings.forEach { holding ->
                    Card(
                        colors = CardDefaults.cardColors(WalletSurface),
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier.clickable { selected = holding }
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text(holding.name, fontWeight = FontWeight.Bold)
                            Text(
                                if (holding.recurringPaymentId != null) {
                                    "Ongoing SIP · ${holding.units}"
                                } else {
                                    holding.units
                                },
                                color = WalletColors.muted
                            )
                            Text(
                                "Invested ${formatMoney(holding.costMinor, state.balancesVisible, state.baseCurrency)}",
                                color = WalletColors.muted
                            )
                        }
                    }
                }
            }
        }

        Button(
            onClick = { add = true },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(WalletGreen)
        ) {
            Text("+ Add investment", color = WalletColors.ink)
        }
        Button(
            onClick = { addIpoDialog = true },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors()
        ) {
            Text("🏷️ Add IPO")
        }

        Spacer(Modifier.height(70.dp))
    }

    if (add) {
        InvestmentDialog(
            initial = null,
            dismiss = { add = false },
            save = { name, type, units, invested ->
                addHolding(name, type, units, invested, invested)
            }
        )
    }

    if (addIpoDialog) {
        IpoDialog(
            dismiss = { addIpoDialog = false },
            save = {
                addIpo(it)
                addIpoDialog = false
            }
        )
    }

    selected?.let { holding ->
        InvestmentDialog(
            initial = holding,
            dismiss = { selected = null },
            save = { name, type, units, invested ->
                editHolding(
                    holding.copy(
                        name = name,
                        type = type,
                        units = units,
                        valueMinor = invested,
                        costMinor = invested
                    )
                )
            }
        )
    }
}

@Composable
private fun IpoDialog(
    dismiss: () -> Unit,
    save: (PendingIpo) -> Unit
) {
    var name by rememberSaveable { mutableStateOf("") }
    var amount by rememberSaveable { mutableStateOf("") }
    var date by rememberSaveable { mutableStateOf(LocalDate.now().toString()) }
    val saveIpo = rememberDebouncedClick {
        val allotmentDate = runCatching { LocalDate.parse(date) }.getOrNull()
        val amountMinor = ((amount.toDoubleOrNull() ?: 0.0) * 100).toLong()
        if (name.isNotBlank() && allotmentDate != null && amountMinor > 0) {
            save(
                PendingIpo(
                    id = "ipo-${System.currentTimeMillis()}",
                    name = name,
                    amountMinor = amountMinor,
                    allotmentDate = allotmentDate.toString(),
                    allotmentDateMillis = allotmentDate
                        .atStartOfDay(ZoneId.systemDefault())
                        .toInstant()
                        .toEpochMilli()
                )
            )
        }
    }

    AlertDialog(
        onDismissRequest = dismiss,
        title = { Text("Add IPO") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("IPO name") }
                )
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Applied amount") },
                    prefix = { Text("₹ ") }
                )
                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("Allotment date (YYYY-MM-DD)") }
                )
            }
        },
        confirmButton = {
            Button(onClick = saveIpo, colors = ButtonDefaults.buttonColors(WalletGreen)) {
                Text("Save IPO", color = WalletColors.ink)
            }
        },
        dismissButton = {
            TextButton(onClick = dismiss) {
                Text("Cancel")
            }
        }
    )
}

private fun investmentEmoji(type: InvestmentType): String = when (type) {
    InvestmentType.STOCK -> "📈"
    InvestmentType.EQUITY -> "🏢"
    InvestmentType.MUTUAL_FUND -> "🧺"
    InvestmentType.INDEX_FUND -> "🧭"
    InvestmentType.SIP -> "🔁"
}

@Composable
private fun InvestmentDialog(
    initial: Holding?,
    dismiss: () -> Unit,
    save: (String, InvestmentType, String, Long) -> Unit
) {
    var name by remember { mutableStateOf(initial?.name.orEmpty()) }
    var units by remember { mutableStateOf(initial?.units.orEmpty()) }
    var cost by remember { mutableStateOf(initial?.costMinor?.div(100.0)?.toString().orEmpty()) }
    var type by remember { mutableStateOf(initial?.type ?: InvestmentType.MUTUAL_FUND) }
    val saveInvestment = rememberDebouncedClick {
        save(
            name,
            type,
            units,
            ((cost.toDoubleOrNull() ?: 0.0) * 100).toLong()
        )
        dismiss()
    }

    AlertDialog(
        onDismissRequest = dismiss,
        title = { Text(if (initial == null) "Add investment" else "Edit investment") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") }
                )
                Row(Modifier.horizontalScroll(rememberScrollState())) {
                    InvestmentType.entries.forEach {
                        FilterChip(
                            selected = type == it,
                            onClick = { type = it },
                            label = { Text(it.name.replace('_', ' ')) }
                        )
                    }
                }
                OutlinedTextField(
                    value = units,
                    onValueChange = { units = it },
                    label = { Text("Units / quantity") }
                )
                OutlinedTextField(
                    value = cost,
                    onValueChange = { cost = it },
                    label = { Text("Amount invested") },
                    prefix = { Text("₹ ") }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = saveInvestment,
                colors = ButtonDefaults.buttonColors(WalletGreen)
            ) {
                Text("Save", color = WalletColors.ink)
            }
        }
    )
}
