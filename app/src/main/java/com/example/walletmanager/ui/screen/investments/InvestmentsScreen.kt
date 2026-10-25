package com.example.walletmanager.ui.screen.investments

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.sp
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.text.input.KeyboardType
import com.example.walletmanager.data.model.Holding
import com.example.walletmanager.data.model.Account
import com.example.walletmanager.data.model.InvestmentType
import com.example.walletmanager.data.model.PendingIpo
import com.example.walletmanager.data.model.OTHER_INVESTMENT_PLATFORM_ID
import com.example.walletmanager.data.model.WalletUiState
import com.example.walletmanager.ui.component.ScreenHeader
import com.example.walletmanager.ui.component.SectionTitle
import com.example.walletmanager.ui.component.SummaryCard
import com.example.walletmanager.ui.component.WalletColors
import com.example.walletmanager.ui.component.walletGlassBorder
import com.example.walletmanager.ui.component.WalletIcon
import com.example.walletmanager.ui.component.WalletActionButton
import com.example.walletmanager.ui.component.WalletConfirmDialog
import com.example.walletmanager.ui.component.WalletModalBottomSheet
import com.example.walletmanager.ui.component.WalletSheetBackButton
import com.example.walletmanager.ui.component.WalletDatePickerField
import com.example.walletmanager.ui.component.formatWalletDate
import com.example.walletmanager.ui.component.formatMoney
import com.example.walletmanager.ui.component.walletDropdownItem
import com.example.walletmanager.ui.component.walletDropdownSurface
import com.example.walletmanager.ui.component.rememberDebouncedClick
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
    addHolding: (String, InvestmentType, Long, String?) -> Unit,
    addPlatform: (String, Long) -> Unit,
    editHolding: (Holding) -> Unit,
    exitHolding: (String, Long, String) -> Unit,
    addIpo: (PendingIpo) -> Unit,
    rejectIpo: (String) -> Unit
) {
    var selectedHolding by remember { mutableStateOf<Holding?>(null) }
    var holdingToExit by remember { mutableStateOf<Holding?>(null) }
    var ipoToDelete by remember { mutableStateOf<PendingIpo?>(null) }
    var addSheet by rememberSaveable { mutableStateOf(false) }
    var addPlatformSheet by rememberSaveable { mutableStateOf(false) }
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
    val platforms = remember(state.accounts) {
        state.accounts.filter { it.type == "Brokerage" }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize(),
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
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Platforms", modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                TextButton(onClick = { addPlatformSheet = true }) {
                    Text("+ Add platform", color = WalletBeige)
                }
            }
        }
        if (platforms.isEmpty()) {
            item {
                Text(
                    "Add a platform such as Zerodha to track its free funds and investments.",
                    color = WalletColors.muted
                )
            }
        } else {
            items(platforms, key = { "platform-${it.id}" }) { platform ->
                val platformHoldings = state.holdings.filter { it.platformAccountId == platform.id }
                PlatformSummaryCard(
                    platform = platform,
                    investedMinor = platformHoldings.sumOf { it.costMinor },
                    state = state
                )
            }
        }
        val platformIds = platforms.mapTo(mutableSetOf()) { it.id }
        val unassignedHoldings = state.holdings.filter {
            it.platformAccountId == null ||
                (it.platformAccountId != OTHER_INVESTMENT_PLATFORM_ID && it.platformAccountId !in platformIds)
        }
        if (unassignedHoldings.isNotEmpty()) {
            item(key = "platform-unassigned") {
                PlatformSummaryCard(
                    platform = null,
                    title = "Needs platform",
                    investedMinor = unassignedHoldings.sumOf { it.costMinor },
                    state = state
                )
            }
        }
        val otherHoldings = state.holdings.filter { it.platformAccountId == OTHER_INVESTMENT_PLATFORM_ID }
        if (otherHoldings.isNotEmpty()) {
            item(key = "platform-other") {
                PlatformSummaryCard(
                    platform = null,
                    title = "Other",
                    investedMinor = otherHoldings.sumOf { it.costMinor },
                    state = state
                )
            }
        }

        if (state.pendingIpos.isNotEmpty()) {
            item {
                SectionTitle("Pending IPOs")
            }
            items(state.pendingIpos, key = { it.id }) { ipo ->
                Card(
                    colors = CardDefaults.cardColors(WalletSurface.copy(alpha = 0.88f)),
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier.walletGlassBorder(RoundedCornerShape(18.dp))
                ) {
                    Row(Modifier.padding(16.dp)) {
                        Column(Modifier.weight(1f)) {
                            Text(ipo.name, fontWeight = FontWeight.Bold)
                            Text(
                                "Allotment ${runCatching { formatWalletDate(LocalDate.parse(ipo.allotmentDate)) }.getOrDefault(ipo.allotmentDate)}",
                                color = WalletColors.muted
                            )
                            Text(formatMoney(ipo.amountMinor, state.balancesVisible, state.baseCurrency), fontWeight = FontWeight.Bold)
                        }
                        WalletActionButton(
                            "Delete",
                            { ipoToDelete = ipo },
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
                                        colors = CardDefaults.cardColors(WalletSurface.copy(alpha = 0.88f)),
                                        shape = RoundedCornerShape(18.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .walletGlassBorder(RoundedCornerShape(18.dp))
                                            .clickable { selectedHolding = holding }
                                    ) {
                                        Column(Modifier.padding(16.dp)) {
                                            Text(holding.name, fontWeight = FontWeight.Bold)
                                            Text(
                                                "${formatInvestmentTypeName(holding.type.displayCategory())} · ${platforms.firstOrNull { it.id == holding.platformAccountId }?.name ?: if (holding.platformAccountId == OTHER_INVESTMENT_PLATFORM_ID) "Other" else "Needs platform"}",
                                                color = WalletColors.muted,
                                                style = MaterialTheme.typography.bodySmall
                                            )
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
                colors = ButtonDefaults.buttonColors(WalletBeige)
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
            platforms = platforms,
            dismiss = { addSheet = false },
            onExit = null,
            save = { name, type, invested, platformAccountId ->
                addHolding(name, type, invested, platformAccountId)
            }
        )
    }

    if (addPlatformSheet) {
        AddPlatformSheet(
            currencySymbol = state.baseCurrency.symbol,
            dismiss = { addPlatformSheet = false },
            save = { name, freeFunds ->
                addPlatform(name, freeFunds)
                addPlatformSheet = false
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

    ipoToDelete?.let { ipo ->
        WalletConfirmDialog(
            title = "Delete IPO?",
            message = "${ipo.name} will be removed from pending IPOs.",
            onDismiss = { ipoToDelete = null },
            onConfirm = {
                rejectIpo(ipo.id)
                ipoToDelete = null
            }
        )
    }

    val currentHolding = selectedHolding
    if (currentHolding != null) {
        InvestmentSheet(
            initial = currentHolding,
            platforms = platforms,
            dismiss = { selectedHolding = null },
            onExit = {
                selectedHolding = null
                holdingToExit = currentHolding
            },
            save = { name, type, invested, platformAccountId ->
                editHolding(
                    currentHolding.copy(
                        name = name,
                        type = type,
                        units = "",
                        valueMinor = invested,
                        costMinor = invested,
                        platformAccountId = platformAccountId
                    )
                )
            }
        )
    }

    holdingToExit?.let { holding ->
        ExitInvestmentSheet(
            holding = holding,
            accounts = state.accounts,
            currencySymbol = state.baseCurrency.symbol,
            dismiss = { holdingToExit = null },
            sell = { proceedsMinor, accountId ->
                exitHolding(holding.id, proceedsMinor, accountId)
                holdingToExit = null
            }
        )
    }
}

@Composable
private fun PlatformSummaryCard(
    platform: Account?,
    title: String? = null,
    investedMinor: Long,
    state: WalletUiState
) {
    Card(
        colors = CardDefaults.cardColors(WalletSurface.copy(alpha = 0.88f)),
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier.fillMaxWidth().walletGlassBorder(RoundedCornerShape(18.dp))
    ) {
        Column(
            Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(title ?: platform?.name.orEmpty(), fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Column(Modifier.weight(1f)) {
                    Text("Free funds", color = WalletColors.muted, style = MaterialTheme.typography.labelMedium)
                    Text(
                        platform?.let { formatMoney(it.balanceMinor, state.balancesVisible, state.baseCurrency) } ?: "—",
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Column(Modifier.weight(1f)) {
                    Text("Invested", color = WalletColors.muted, style = MaterialTheme.typography.labelMedium)
                    Text(
                        formatMoney(investedMinor, state.balancesVisible, state.baseCurrency),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExitInvestmentSheet(
    holding: Holding,
    accounts: List<Account>,
    currencySymbol: String,
    dismiss: () -> Unit,
    sell: (Long, String) -> Unit
) {
    var proceeds by rememberSaveable(holding.id) { mutableStateOf("") }
    var accountId by remember(holding.id, accounts) {
        mutableStateOf(
            holding.platformAccountId?.takeIf { id -> accounts.any { it.id == id } }
                ?: accounts.firstOrNull { it.type == "Brokerage" }?.id
                ?: accounts.firstOrNull()?.id.orEmpty()
        )
    }
    val sellInvestment = rememberDebouncedClick {
        val amountMinor = ((proceeds.toDoubleOrNull() ?: 0.0) * 100).toLong()
        sell(amountMinor, accountId)
    }

    WalletModalBottomSheet(
        onDismissRequest = dismiss,
        contentWindowInsets = { WindowInsets(0, 0, 0, 0) },
        dragHandle = { WalletSheetBackButton(dismiss) },
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = WalletSurface
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Sell / exit ${holding.name}", fontWeight = FontWeight.Bold)
            Text(
                "Enter the money you received. It will be added to the selected account and this investment will be removed. Enter 0 if no money was returned.",
                color = WalletColors.muted
            )
            OutlinedTextField(
                value = proceeds,
                onValueChange = { proceeds = it },
                label = { Text("Sale proceeds") },
                prefix = { Text("$currencySymbol ") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )
            Text("Add proceeds to", color = WalletColors.muted)
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                accounts.forEach { account ->
                    FilterChip(
                        selected = accountId == account.id,
                        onClick = { accountId = account.id },
                        label = { Text(account.name) }
                    )
                }
            }
            Button(
                onClick = sellInvestment,
                enabled = accountId.isNotBlank() && proceeds.toDoubleOrNull()?.let { it >= 0 } == true,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(WalletBeige)
            ) {
                Text("Confirm sale and remove", color = WalletColors.ink, fontWeight = FontWeight.Bold)
            }
            TextButton(onClick = dismiss, modifier = Modifier.fillMaxWidth()) {
                Text("Cancel")
            }
        }
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
            WalletDatePickerField(
                value = runCatching { LocalDate.parse(date) }.getOrDefault(LocalDate.now()),
                label = "Allotment date",
                onDateSelected = { date = it.toString() }
            )
            Button(
                onClick = saveIpo,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(WalletBeige)
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
private fun AddPlatformSheet(
    currencySymbol: String,
    dismiss: () -> Unit,
    save: (String, Long) -> Unit
) {
    var name by rememberSaveable { mutableStateOf("") }
    var freeFunds by rememberSaveable { mutableStateOf("") }
    val savePlatform = rememberDebouncedClick {
        val openingFunds = ((freeFunds.toDoubleOrNull() ?: 0.0).coerceAtLeast(0.0) * 100).toLong()
        save(name.trim(), openingFunds)
    }

    WalletModalBottomSheet(
        onDismissRequest = dismiss,
        contentWindowInsets = { WindowInsets(0, 0, 0, 0) },
        dragHandle = { WalletSheetBackButton(dismiss) },
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = WalletSurface
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Add platform", fontWeight = FontWeight.Bold)
            Text("Track a broker’s available cash separately from its investments.", color = WalletColors.muted)
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Platform name") },
                placeholder = { Text("e.g. Zerodha") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = freeFunds,
                onValueChange = { freeFunds = it },
                label = { Text("Free funds") },
                prefix = { Text("$currencySymbol ") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                onClick = savePlatform,
                enabled = name.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(WalletBeige)
            ) {
                Text("Add platform", color = WalletColors.ink, fontWeight = FontWeight.Bold)
            }
            TextButton(onClick = dismiss, modifier = Modifier.fillMaxWidth()) {
                Text("Cancel")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InvestmentSheet(
    initial: Holding?,
    platforms: List<Account>,
    dismiss: () -> Unit,
    onExit: (() -> Unit)?,
    save: (String, InvestmentType, Long, String?) -> Unit
) {
    var name by remember { mutableStateOf(initial?.name.orEmpty()) }
    var cost by remember { mutableStateOf(initial?.costMinor?.div(100.0)?.toString().orEmpty()) }
    var type by remember {
        mutableStateOf(initial?.type?.displayCategory() ?: InvestmentType.MUTUAL_FUND)
    }
    var expandedDropdown by remember { mutableStateOf(false) }
    var expandedPlatformDropdown by remember { mutableStateOf(false) }
    var platformAccountId by remember(initial) { mutableStateOf(initial?.platformAccountId) }

    val saveInvestment = rememberDebouncedClick {
        save(
            name,
            type,
            ((cost.toDoubleOrNull() ?: 0.0) * 100).toLong(),
            platformAccountId
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
                            modifier = Modifier.walletDropdownItem(selected = option == type, accent = InvestmentPositive),
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    WalletIcon(investmentIcon(option), color = WalletColors.cream, size = 18.dp)
                                    Text(formatInvestmentTypeName(option), color = WalletColors.cream, modifier = Modifier.padding(start = 10.dp))
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

            ExposedDropdownMenuBox(
                expanded = expandedPlatformDropdown,
                onExpandedChange = { expandedPlatformDropdown = it },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = platforms.firstOrNull { it.id == platformAccountId }?.name
                        ?: if (platformAccountId == OTHER_INVESTMENT_PLATFORM_ID) "Other" else "Select platform",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Platform") },
                    trailingIcon = {
                        Box(Modifier.size(width = 48.dp, height = 48.dp), contentAlignment = Alignment.Center) {
                            WalletIcon("chevron_down", color = InvestmentPositive, size = 18.dp)
                        }
                    },
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expandedPlatformDropdown,
                    onDismissRequest = { expandedPlatformDropdown = false },
                    modifier = Modifier.walletDropdownSurface()
                ) {
                    platforms.forEach { platform ->
                        DropdownMenuItem(
                            modifier = Modifier.walletDropdownItem(
                                selected = platform.id == platformAccountId,
                                accent = InvestmentPositive
                            ),
                            text = { Text(platform.name, color = WalletColors.cream) },
                            onClick = {
                                platformAccountId = platform.id
                                expandedPlatformDropdown = false
                            }
                        )
                    }
                    DropdownMenuItem(
                        modifier = Modifier.walletDropdownItem(
                            selected = platformAccountId == OTHER_INVESTMENT_PLATFORM_ID,
                            accent = InvestmentPositive
                        ),
                        text = { Text("Other · real estate and more", color = WalletColors.cream) },
                        onClick = {
                            platformAccountId = OTHER_INVESTMENT_PLATFORM_ID
                            expandedPlatformDropdown = false
                        }
                    )
                }
            }

            if (platforms.isEmpty()) {
                Text("Select Other, or add a brokerage platform from the Investments screen.", color = WalletColors.muted)
            }

            OutlinedTextField(
                value = cost,
                onValueChange = { cost = it },
                label = { Text("Amount invested") },
                prefix = { Text("₹ ") },
                modifier = Modifier.fillMaxWidth()
            )
            if (onExit != null) {
                OutlinedButton(
                    onClick = onExit,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = InvestmentNegative)
                ) {
                    Text("Sell / exit investment")
                }
            }
            Button(
                onClick = saveInvestment,
                enabled = name.isNotBlank() && cost.isNotBlank() && platformAccountId != null,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(WalletBeige)
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
