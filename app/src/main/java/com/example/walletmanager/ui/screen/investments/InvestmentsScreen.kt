package com.example.walletmanager.ui.screen.investments

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.size
import androidx.compose.ui.unit.sp
import androidx.compose.material3.MaterialTheme
import com.example.walletmanager.data.model.Holding
import com.example.walletmanager.data.model.InvestmentType
import com.example.walletmanager.data.model.PendingIpo
import com.example.walletmanager.data.model.WalletUiState
import com.example.walletmanager.ui.component.ScreenHeader
import com.example.walletmanager.ui.component.SectionTitle
import com.example.walletmanager.ui.component.SummaryCard
import com.example.walletmanager.ui.component.WalletColors
import com.example.walletmanager.ui.component.WalletIcon
import com.example.walletmanager.ui.component.WalletActionButton
import com.example.walletmanager.ui.component.WalletModalBottomSheet
import com.example.walletmanager.ui.component.WalletSheetBackButton
import com.example.walletmanager.ui.component.formatMoney
import com.example.walletmanager.ui.component.walletDropdownItem
import com.example.walletmanager.ui.component.walletDropdownSurface
import com.example.walletmanager.ui.component.rememberDebouncedClick
import com.example.walletmanager.ui.theme.WalletBackground
import com.example.walletmanager.ui.theme.WalletBeige
import com.example.walletmanager.ui.theme.WalletSurface
import java.time.LocalDate
import java.time.ZoneId

private val InvestmentPositive = Color(0xFF91A694)
private val InvestmentNegative = Color(0xFFC27D79)

@Composable
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
fun InvestmentsScreen(
    state: WalletUiState,
    addHolding: (String, InvestmentType, Long) -> Unit,
    editHolding: (Holding) -> Unit,
    addIpo: (PendingIpo) -> Unit,
    rejectIpo: (String) -> Unit
) {
    var selectedHolding by remember { mutableStateOf<Holding?>(null) }
    var addSheet by rememberSaveable { mutableStateOf(false) }
    var addIpoSheet by rememberSaveable { mutableStateOf(false) }
    var expandedTypes by rememberSaveable {
        mutableStateOf(InvestmentType.categories.map { it.name }.toSet())
    }
    val holdingsByType = remember(state.holdings) {
        InvestmentType.categories.associateWith { type ->
            state.holdings.filter { it.type.displayCategory() == type }
        }
    }
    val totalInvested = remember(state.holdings) {
        state.holdings.sumOf { it.costMinor }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(WalletBackground),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 70.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            ScreenHeader("Investments", "Stocks, equities, mutual funds and SIPs")
        }
        item {
            SummaryCard("Total invested", totalInvested, state)
        }
        item {
            Text(
                "Track every stock, mutual fund, equity, index fund and SIP without unit hassle.",
                color = WalletColors.muted
            )
        }

        if (state.pendingIpos.isNotEmpty()) {
            item {
                SectionTitle("Pending IPOs")
            }
            items(state.pendingIpos, key = { it.id }) { ipo ->
                Card(
                    colors = CardDefaults.cardColors(WalletSurface),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Row(Modifier.padding(16.dp)) {
                        Column(Modifier.weight(1f)) {
                            Text(ipo.name, fontWeight = FontWeight.Bold)
                            Text("Allotment ${ipo.allotmentDate}", color = WalletColors.muted)
                            Text(formatMoney(ipo.amountMinor, state.balancesVisible, state.baseCurrency), fontWeight = FontWeight.Bold)
                        }
                        WalletActionButton(
                            "Delete",
                            { rejectIpo(ipo.id) },
                            destructive = true,
                            destructiveColor = InvestmentNegative
                        )
                    }
                }
            }
        }

        InvestmentType.categories.forEach { type ->
            val holdings = holdingsByType[type].orEmpty()
            if (holdings.isNotEmpty()) {
                item(key = "category-${type.name}") {
                    val expanded = type.name in expandedTypes
                    Column(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    expandedTypes = if (expanded) {
                                        expandedTypes - type.name
                                    } else {
                                        expandedTypes + type.name
                                    }
                                }
                                .padding(horizontal = 2.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            WalletIcon(investmentIcon(type), color = WalletBeige, size = 19.dp)
                            Text(
                                formatInvestmentTypeName(type).uppercase(),
                                modifier = Modifier.weight(1f).padding(start = 10.dp, end = 4.dp),
                                style = MaterialTheme.typography.labelLarge.copy(letterSpacing = 1.1.sp),
                                color = WalletColors.cream,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                formatMoney(holdings.sumOf { it.costMinor }, state.balancesVisible, state.baseCurrency),
                                color = InvestmentPositive,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Box(
                                modifier = Modifier.size(width = 38.dp, height = 32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                WalletIcon(if (expanded) "chevron_up" else "chevron_down", color = WalletColors.muted, size = 18.dp)
                            }
                        }
                        AnimatedVisibility(
                            visible = expanded,
                            enter = expandVertically(
                                animationSpec = tween(140, easing = FastOutSlowInEasing)
                            ) + fadeIn(animationSpec = tween(100, easing = FastOutSlowInEasing)),
                            exit = shrinkVertically(
                                animationSpec = tween(110, easing = FastOutSlowInEasing)
                            ) + fadeOut(animationSpec = tween(80, easing = FastOutSlowInEasing))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                holdings.forEach { holding ->
                                    Card(
                                        colors = CardDefaults.cardColors(WalletSurface),
                                        shape = RoundedCornerShape(18.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { selectedHolding = holding }
                                    ) {
                                        Column(Modifier.padding(16.dp)) {
                                            Text(holding.name, fontWeight = FontWeight.Bold)
                                            if (holding.recurringPaymentId != null) {
                                                Text("Ongoing SIP", color = InvestmentPositive)
                                            }
                                            Text(
                                                "Invested: ${formatMoney(holding.costMinor, state.balancesVisible, state.baseCurrency)}",
                                                color = WalletColors.muted
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Button(
                onClick = { addSheet = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(InvestmentPositive)
            ) {
                Text("+ Add investment", color = WalletColors.ink, fontWeight = FontWeight.Bold)
            }
        }
        item {
            OutlinedButton(
                onClick = { addIpoSheet = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("+ Add IPO")
            }
        }
    }

    if (addSheet) {
        InvestmentSheet(
            initial = null,
            dismiss = { addSheet = false },
            save = { name, type, invested ->
                addHolding(name, type, invested)
            }
        )
    }

    if (addIpoSheet) {
        IpoSheet(
            dismiss = { addIpoSheet = false },
            save = {
                addIpo(it)
                addIpoSheet = false
            }
        )
    }

    val currentHolding = selectedHolding
    if (currentHolding != null) {
        InvestmentSheet(
            initial = currentHolding,
            dismiss = { selectedHolding = null },
            save = { name, type, invested ->
                editHolding(
                    currentHolding.copy(
                        name = name,
                        type = type,
                        units = "",
                        valueMinor = invested,
                        costMinor = invested
                    )
                )
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun IpoSheet(
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
            Text("Add IPO", fontWeight = FontWeight.Bold)
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("IPO name") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("Applied amount") },
                prefix = { Text("₹ ") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = date,
                onValueChange = { date = it },
                label = { Text("Allotment date (YYYY-MM-DD)") },
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                onClick = saveIpo,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(InvestmentPositive)
            ) {
                Text("Save IPO", color = WalletColors.ink, fontWeight = FontWeight.Bold)
            }
            TextButton(
                onClick = dismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cancel")
            }
        }
    }
}

fun investmentIcon(type: InvestmentType): String = when (type) {
    InvestmentType.STOCK, InvestmentType.EQUITY, InvestmentType.INDEX_FUND, InvestmentType.SIP -> "chart"
    InvestmentType.MUTUAL_FUND, InvestmentType.OTHER -> "category"
    InvestmentType.INSURANCE -> "shield"
    InvestmentType.GOLD -> "coin"
}

fun formatInvestmentTypeName(type: InvestmentType): String = when (type) {
    InvestmentType.STOCK -> "Stocks & Equities"
    InvestmentType.EQUITY -> "Direct Equity"
    InvestmentType.MUTUAL_FUND -> "Mutual Funds"
    InvestmentType.INDEX_FUND -> "Index Funds & ETFs"
    InvestmentType.SIP -> "SIP Investments"
    InvestmentType.INSURANCE -> "Insurance & Health Cover"
    InvestmentType.GOLD -> "Gold"
    InvestmentType.OTHER -> "Other financial plans"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InvestmentSheet(
    initial: Holding?,
    dismiss: () -> Unit,
    save: (String, InvestmentType, Long) -> Unit
) {
    var name by remember { mutableStateOf(initial?.name.orEmpty()) }
    var cost by remember { mutableStateOf(initial?.costMinor?.div(100.0)?.toString().orEmpty()) }
    var type by remember {
        mutableStateOf(initial?.type?.displayCategory() ?: InvestmentType.MUTUAL_FUND)
    }
    var expandedDropdown by remember { mutableStateOf(false) }

    val saveInvestment = rememberDebouncedClick {
        save(
            name,
            type,
            ((cost.toDoubleOrNull() ?: 0.0) * 100).toLong()
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
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                if (initial == null) "Add investment" else "Edit investment",
                fontWeight = FontWeight.Bold
            )
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Investment name") },
                modifier = Modifier.fillMaxWidth()
            )

            // Category Selection Dropdown
            ExposedDropdownMenuBox(
                expanded = expandedDropdown,
                onExpandedChange = { expandedDropdown = it },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = formatInvestmentTypeName(type),
                    onValueChange = {},
                    readOnly = true,
                    leadingIcon = { WalletIcon(investmentIcon(type), color = WalletColors.muted, size = 18.dp) },
                    label = { Text("Investment category") },
                    trailingIcon = {
                        Box(
                            modifier = Modifier
                                .size(width = 48.dp, height = 48.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            WalletIcon("chevron_down", color = InvestmentPositive, size = 18.dp)
                        }
                    },
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expandedDropdown,
                    onDismissRequest = { expandedDropdown = false },
                    modifier = Modifier.walletDropdownSurface()
                ) {
                    InvestmentType.categories.forEach { option ->
                        DropdownMenuItem(
                            modifier = Modifier.walletDropdownItem(),
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    WalletIcon(investmentIcon(option), color = WalletColors.cream, size = 18.dp)
                                    Text(formatInvestmentTypeName(option), modifier = Modifier.padding(start = 10.dp))
                                }
                            },
                            onClick = {
                                type = option
                                expandedDropdown = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = cost,
                onValueChange = { cost = it },
                label = { Text("Amount invested") },
                prefix = { Text("₹ ") },
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                onClick = saveInvestment,
                enabled = name.isNotBlank() && cost.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(InvestmentPositive)
            ) {
                Text("Save Investment", color = WalletColors.ink, fontWeight = FontWeight.Bold)
            }
            TextButton(
                onClick = dismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cancel")
            }
        }
    }
}
