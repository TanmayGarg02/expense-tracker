package com.example.walletmanager.ui.screen.accounts

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.walletmanager.data.model.FixedDeposit
import com.example.walletmanager.data.model.AccountAccent
import com.example.walletmanager.data.model.WalletUiState
import com.example.walletmanager.data.model.calculateFixedDepositMaturity
import com.example.walletmanager.ui.component.TransactionRow
import com.example.walletmanager.ui.component.WalletColors
import com.example.walletmanager.ui.component.WalletActionButton
import com.example.walletmanager.ui.component.WalletConfirmDialog
import com.example.walletmanager.ui.component.walletGlassBorder
import com.example.walletmanager.ui.component.WalletIcon
import com.example.walletmanager.ui.component.WalletPage
import com.example.walletmanager.ui.component.WalletModalBottomSheet
import com.example.walletmanager.ui.component.WalletSheetBackButton
import com.example.walletmanager.ui.component.WalletDatePickerField
import com.example.walletmanager.ui.component.formatWalletDate
import com.example.walletmanager.ui.component.formatMoney
import com.example.walletmanager.ui.component.AmountGroupingVisualTransformation
import com.example.walletmanager.ui.component.walletDropdownItem
import com.example.walletmanager.ui.component.walletDropdownSurface
import com.example.walletmanager.ui.component.rememberDebouncedClick
import com.example.walletmanager.ui.navigation.AccountDetailsRoute
import com.example.walletmanager.ui.navigation.WalletRoute
import com.example.walletmanager.ui.theme.WalletBeige
import com.example.walletmanager.ui.theme.WalletCream
import com.example.walletmanager.ui.theme.WalletGreen
import com.example.walletmanager.ui.theme.WalletGreenAccent
import com.example.walletmanager.ui.theme.WalletSurface
import com.example.walletmanager.ui.theme.WalletSurfaceVariant
import java.time.LocalDate
import java.time.ZoneId
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.roundToInt

private fun BigDecimal.toMinorUnits(): Long =
    movePointRight(2).setScale(0, RoundingMode.HALF_UP).longValueExact()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountsScreen(
    state: WalletUiState,
    open: (WalletRoute) -> Unit,
    addAccount: (String, String, Long) -> Unit,
    addFixedDeposit: (FixedDeposit) -> Unit,
    editFixedDeposit: (FixedDeposit) -> Unit,
    deleteFixedDeposit: (FixedDeposit) -> Unit
) {
    var showAddSheet by rememberSaveable { mutableStateOf(false) }
    var showFixedDepositSheet by rememberSaveable { mutableStateOf(false) }
    var fixedDepositsExpanded by rememberSaveable { mutableStateOf(false) }
    var selectedFixedDeposit by remember { mutableStateOf<FixedDeposit?>(null) }
    val fixedDepositChevronRotation by animateFloatAsState(
        targetValue = if (fixedDepositsExpanded) 180f else 0f,
        animationSpec = tween(260, easing = FastOutSlowInEasing),
        label = "fixed-deposit-chevron"
    )

    WalletPage(PaddingValues()) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Accounts", fontWeight = FontWeight.Bold, fontSize = 24.sp)
                Text("${state.accounts.size} accounts", color = WalletColors.muted, fontSize = 13.sp)
            }
            OutlinedButton(
                onClick = { showAddSheet = true },
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 13.dp, vertical = 7.dp),
                border = BorderStroke(1.dp, WalletBeige.copy(alpha = 0.45f))
            ) {
                Text("+", color = WalletBeige, fontSize = 22.sp, lineHeight = 22.sp)
            }
        }

        if (state.accounts.isEmpty()) {
            Text("Add an account to get started.", color = WalletColors.muted)
        } else {
            val displayedAccents = mutableSetOf<AccountAccent>()
            Row(
                Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                state.accounts.forEach { account ->
                    val accent = if (account.accent in displayedAccents) {
                        AccountAccent.values().firstOrNull { it !in displayedAccents } ?: account.accent
                    } else account.accent
                    displayedAccents += accent
                    WalletAccountCard(
                        account = account.copy(accent = accent),
                        state = state,
                        onClick = { open(AccountDetailsRoute(account.id)) }
                    )
                }
            }
        }

        Text("Transactions", fontWeight = FontWeight.Bold, fontSize = 20.sp)
        if (state.transactions.isEmpty()) {
            Text("Your recent transactions will appear here.", color = WalletColors.muted)
        } else {
            Column(Modifier.fillMaxWidth()) {
                state.transactions.take(5).forEach { transaction ->
                    TransactionRow(
                        item = transaction,
                        accounts = state.accounts,
                        currency = state.baseCurrency
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(WalletSurface)
                .clickable { fixedDepositsExpanded = !fixedDepositsExpanded }
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Fixed deposits", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
            Box(contentAlignment = Alignment.Center) {
                WalletIcon(
                    "chevron_down",
                    color = WalletColors.muted,
                    size = 18.dp,
                    modifier = Modifier.graphicsLayer { rotationZ = fixedDepositChevronRotation }
                )
            }
        }
        Column(Modifier.fillMaxWidth()) {
            AnimatedVisibility(
                visible = fixedDepositsExpanded,
                enter = expandVertically(
                    animationSpec = tween(280, easing = FastOutSlowInEasing)
                ) + fadeIn(animationSpec = tween(200, easing = FastOutSlowInEasing)),
                exit = shrinkVertically(
                    animationSpec = tween(240, easing = FastOutSlowInEasing)
                ) + fadeOut(animationSpec = tween(180, easing = FastOutSlowInEasing))
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = { showFixedDepositSheet = true },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(WalletBeige)
                    ) {
                        Text("+ Add fixed deposit", color = WalletColors.ink, fontWeight = FontWeight.Bold)
                    }
                    state.fixedDeposits.forEach { deposit ->
                        FixedDepositRow(
                            deposit = deposit,
                            state = state,
                            edit = { selectedFixedDeposit = deposit },
                            delete = { deleteFixedDeposit(deposit) }
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(70.dp))
    }

    if (showAddSheet) {
        AddAccountSheet(
            currency = state.baseCurrency,
            dismiss = { showAddSheet = false },
            save = { name, type, openingBalanceMinor ->
                addAccount(name, type, openingBalanceMinor)
                showAddSheet = false
            }
        )
    }

    if (showFixedDepositSheet) {
        FixedDepositSheet(
            state = state,
            initial = null,
            dismiss = { showFixedDepositSheet = false },
            save = {
                addFixedDeposit(it)
                showFixedDepositSheet = false
            }
        )
    }

    val currentDeposit = selectedFixedDeposit
    if (currentDeposit != null) {
        FixedDepositSheet(
            state = state,
            initial = currentDeposit,
            dismiss = { selectedFixedDeposit = null },
            save = {
                editFixedDeposit(it)
                selectedFixedDeposit = null
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddAccountSheet(
    currency: com.example.walletmanager.data.model.Currency,
    dismiss: () -> Unit,
    save: (String, String, Long) -> Unit
) {
    var name by rememberSaveable { mutableStateOf("") }
    var type by rememberSaveable { mutableStateOf("Bank account") }
    var openingBalance by rememberSaveable { mutableStateOf("") }
    var validationError by rememberSaveable { mutableStateOf<String?>(null) }
    val decimalPattern = remember { Regex("\\d*(\\.\\d{0,2})?") }
    val saveAccount = rememberDebouncedClick {
        val balanceValue = openingBalance.toBigDecimalOrNull()
        when {
            name.isBlank() -> validationError = "Enter a name for the account."
            balanceValue == null || balanceValue < BigDecimal.ZERO ->
                validationError = "Enter a valid current balance."
            else -> runCatching {
                save(name.trim(), type, balanceValue.toMinorUnits())
            }.onFailure {
                validationError = "Enter a valid current balance."
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Add account", fontWeight = FontWeight.Bold)
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Account name") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = openingBalance,
                onValueChange = { if (it.matches(decimalPattern)) openingBalance = it },
                label = { Text("Current balance") },
                prefix = { Text("₹ ") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                visualTransformation = AmountGroupingVisualTransformation(currency),
                modifier = Modifier.fillMaxWidth()
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
                Text("Add account", color = WalletColors.ink, fontWeight = FontWeight.Bold)
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

@Composable
private fun WalletAccountCard(
    account: com.example.walletmanager.data.model.Account,
    state: WalletUiState,
    onClick: () -> Unit
) {
    val isBankAccount = account.type.equals("Bank account", ignoreCase = true) ||
        account.type.equals("Bank", ignoreCase = true)
    val fixedDepositBalance = state.fixedDeposits
        .filter { it.accountId == account.id }
        .sumOf { it.principalMinor }
    val displayedFixedDepositBalance = fixedDepositBalance.takeIf { isBankAccount } ?: 0L
    val liquidBalance = (account.balanceMinor - displayedFixedDepositBalance).coerceAtLeast(0)
    val accent = when (account.accent) {
        AccountAccent.Green -> WalletGreenAccent
        AccountAccent.Purple -> Color(0xFFB29ACB)
        AccountAccent.Amber -> Color(0xFFC7A56F)
        AccountAccent.Pink -> Color(0xFFD18FA2)
        AccountAccent.Blue -> Color(0xFF86A9CE)
    }
    val shape = RoundedCornerShape(22.dp)

    Box(
        Modifier
            .width(292.dp)
            .height(202.dp)
            .clip(shape)
            .background(
                Brush.linearGradient(
                    listOf(accent.copy(alpha = 0.26f), WalletSurfaceVariant.copy(alpha = 0.94f), WalletSurface)
                )
            )
            .walletGlassBorder(shape, accent)
            .clickable(onClick = onClick)
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val center = Offset(size.width * 0.92f, size.height * 0.32f)
            for (ring in 0..5) {
                drawCircle(
                    color = WalletCream.copy(alpha = 0.035f + ring * 0.006f),
                    radius = size.minDimension * (0.28f + ring * 0.09f),
                    center = center,
                    style = Stroke(width = 1.dp.toPx())
                )
            }
        }
        Column(
            Modifier.fillMaxSize().padding(19.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(11.dp))
                        .background(WalletCream.copy(alpha = 0.09f))
                        .border(1.dp, WalletCream.copy(alpha = 0.17f), RoundedCornerShape(11.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    WalletIcon("accounts", color = accent, size = 20.dp)
                }
                Column(Modifier.padding(start = 11.dp).weight(1f)) {
                    Text(account.type.uppercase(), color = WalletColors.cream, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Text(state.baseCurrency.code, color = WalletColors.muted, fontSize = 11.sp)
                }
                Text("WALLET", color = WalletColors.muted, fontSize = 10.sp, letterSpacing = 1.sp)
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("LIQUID BALANCE", color = accent, fontSize = 10.sp, letterSpacing = 1.sp, fontWeight = FontWeight.Bold)
                Text(
                    formatMoney(liquidBalance, state.balancesVisible, state.baseCurrency),
                    color = WalletColors.cream,
                    fontSize = 27.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1
                )
                if (displayedFixedDepositBalance > 0) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Fixed deposits", color = WalletColors.muted, fontSize = 12.sp)
                        Text(
                            formatMoney(displayedFixedDepositBalance, state.balancesVisible, state.baseCurrency),
                            color = WalletColors.cream,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.Bottom) {
                Column(Modifier.weight(1f)) {
                    Text("ACCOUNT", color = WalletColors.muted, fontSize = 9.sp, letterSpacing = 1.2.sp)
                    Text(account.name, color = WalletColors.cream, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }
                Box(
                    Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(accent.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(account.name.firstOrNull()?.uppercase() ?: "W", color = accent, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun FixedDepositRow(
    deposit: FixedDeposit,
    state: WalletUiState,
    edit: () -> Unit,
    delete: () -> Unit
) {
    var confirmDelete by rememberSaveable(deposit.id) { mutableStateOf(false) }
    val linkedAccount = state.accounts.firstOrNull { it.id == deposit.accountId }
    Card(
        colors = CardDefaults.cardColors(WalletSurface.copy(alpha = 0.88f)),
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier.walletGlassBorder(RoundedCornerShape(18.dp))
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(deposit.name, fontWeight = FontWeight.Bold)
                if (linkedAccount != null) {
                    Text(linkedAccount.name, color = WalletGreen, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
            Text(
                "${deposit.interestRateBps / 100.0}% · Deposited ${runCatching { formatWalletDate(LocalDate.parse(deposit.startDate)) }.getOrDefault(deposit.startDate)} · Matures ${runCatching { formatWalletDate(LocalDate.parse(deposit.maturityDate)) }.getOrDefault(deposit.maturityDate)}",
                color = WalletColors.muted
            )
            Text(
                "Maturity ${formatMoney(deposit.maturityAmountMinor, state.balancesVisible, state.baseCurrency)}",
                color = WalletGreen,
                fontWeight = FontWeight.Bold
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                WalletActionButton("Edit", edit)
                WalletActionButton("Delete", { confirmDelete = true }, destructive = true)
            }
        }
    }
    if (confirmDelete) {
        WalletConfirmDialog(
            title = "Delete fixed deposit?",
            message = "This fixed deposit will be removed.",
            onDismiss = { confirmDelete = false },
            onConfirm = {
                confirmDelete = false
                delete()
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FixedDepositSheet(
    state: WalletUiState,
    initial: FixedDeposit?,
    dismiss: () -> Unit,
    save: (FixedDeposit) -> Unit
) {
    var name by rememberSaveable { mutableStateOf(initial?.name.orEmpty()) }
    var accountId by rememberSaveable {
        mutableStateOf(initial?.accountId ?: state.accounts.firstOrNull()?.id.orEmpty())
    }
    var principal by rememberSaveable {
        mutableStateOf(initial?.principalMinor?.div(100.0)?.toString().orEmpty())
    }
    var interestRate by rememberSaveable {
        mutableStateOf(initial?.interestRateBps?.div(100.0)?.toString().orEmpty())
    }
    var startDate by rememberSaveable {
        mutableStateOf(initial?.startDate?.takeIf { it.isNotBlank() } ?: LocalDate.now().toString())
    }
    var maturityDate by rememberSaveable {
        mutableStateOf(initial?.maturityDate ?: LocalDate.now().plusYears(1).toString())
    }
    var maturityAmount by rememberSaveable {
        mutableStateOf(initial?.maturityAmountMinor?.div(100.0)?.toString().orEmpty())
    }
    var userModifiedMaturity by rememberSaveable { mutableStateOf(false) }
    var accountDropdownExpanded by remember { mutableStateOf(false) }
    var validationError by rememberSaveable { mutableStateOf<String?>(null) }
    val decimalPattern = remember { Regex("\\d*(\\.\\d{0,2})?") }

    val selectedAccount = state.accounts.firstOrNull { it.id == accountId }

    fun updateCalculatedMaturity(pStr: String, rStr: String, startStr: String, maturityStr: String) {
        if (!userModifiedMaturity) {
            val pMinor = ((pStr.toDoubleOrNull() ?: 0.0) * 100).toLong()
            val rBps = ((rStr.toDoubleOrNull() ?: 0.0) * 100).roundToInt()
            val parsedStart = runCatching { LocalDate.parse(startStr) }.getOrNull()
            val parsedMaturity = runCatching { LocalDate.parse(maturityStr) }.getOrNull()
            if (pMinor > 0 && rBps >= 0 && parsedStart != null && parsedMaturity != null && !parsedMaturity.isBefore(parsedStart)) {
                val calcMinor = calculateFixedDepositMaturity(pMinor, rBps, parsedMaturity.toString(), parsedStart.toString())
                maturityAmount = (calcMinor / 100.0).toString()
            }
        }
    }

    val saveDeposit = rememberDebouncedClick {
        val principalValue = principal.toBigDecimalOrNull()
        val interestValue = interestRate.toBigDecimalOrNull()
        val maturityValue = maturityAmount.toBigDecimalOrNull()
        val parsedStartDate = runCatching { LocalDate.parse(startDate) }.getOrNull()
        val parsedDate = runCatching { LocalDate.parse(maturityDate) }.getOrNull()
        when {
            name.isBlank() -> validationError = "Enter a name for the fixed deposit."
            principalValue == null || principalValue <= BigDecimal.ZERO ->
                validationError = "Principal amount must be a number greater than 0."
            interestValue == null || interestValue < BigDecimal.ZERO ->
                validationError = "Interest rate must be a valid number (0 or more)."
            parsedStartDate == null -> validationError = "Enter a valid deposit date (YYYY-MM-DD)."
            maturityValue == null || maturityValue <= BigDecimal.ZERO ->
                validationError = "Maturity amount must be a number greater than 0."
            parsedDate == null -> validationError = "Enter a valid maturity date (YYYY-MM-DD)."
            parsedDate.isBefore(parsedStartDate) -> validationError = "Maturity date must be on or after the deposit date."
            else -> {
                runCatching {
                    val principalMinor = principalValue.toMinorUnits()
                    val interestRateBps = interestValue.toMinorUnits()
                        .coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
                    val enteredAmount = maturityValue.toMinorUnits()
                    val calculatedAmount = calculateFixedDepositMaturity(
                        principalMinor,
                        interestRateBps,
                        parsedDate.toString(),
                        parsedStartDate.toString()
                    )
                    validationError = null
                    save(
                        FixedDeposit(
                            id = initial?.id ?: "fd-${System.currentTimeMillis()}",
                            name = name.trim(),
                            principalMinor = principalMinor,
                            interestRateBps = interestRateBps,
                            maturityDate = parsedDate.toString(),
                            maturityDateMillis = parsedDate
                                .atStartOfDay(ZoneId.systemDefault())
                                .toInstant()
                                .toEpochMilli(),
                            maturityAmountMinor = enteredAmount.takeIf { it > 0 } ?: calculatedAmount,
                            accountId = accountId,
                            startDate = parsedStartDate.toString()
                        )
                    )
                }.onFailure {
                    validationError = "Enter valid numeric values for the deposit."
                }
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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                if (initial == null) "Add fixed deposit" else "Edit fixed deposit",
                fontWeight = FontWeight.Bold
            )
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("FD name") },
                modifier = Modifier.fillMaxWidth()
            )

            // Bank Account Selection Dropdown
            ExposedDropdownMenuBox(
                expanded = accountDropdownExpanded,
                onExpandedChange = { accountDropdownExpanded = it },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = selectedAccount?.name ?: "Select Bank Account",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Bank Account") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = accountDropdownExpanded) },
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = accountDropdownExpanded,
                    onDismissRequest = { accountDropdownExpanded = false },
                    modifier = Modifier.walletDropdownSurface()
                ) {
                    state.accounts.forEach { account ->
                        DropdownMenuItem(
                            modifier = Modifier.walletDropdownItem(
                                selected = account.id == accountId,
                                accent = WalletGreenAccent
                            ),
                            text = { Text(account.name, color = WalletColors.cream) },
                            onClick = {
                                accountId = account.id
                                accountDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            WalletDatePickerField(
                value = runCatching { LocalDate.parse(startDate) }.getOrDefault(LocalDate.now()),
                label = "Deposit date",
                onDateSelected = {
                    startDate = it.toString()
                    updateCalculatedMaturity(principal, interestRate, startDate, maturityDate)
                }
            )
            OutlinedTextField(
                value = principal,
                onValueChange = {
                    if (it.matches(decimalPattern)) {
                        principal = it
                        updateCalculatedMaturity(it, interestRate, startDate, maturityDate)
                    }
                },
                label = { Text("Principal amount") },
                prefix = { Text("₹ ") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                visualTransformation = AmountGroupingVisualTransformation(state.baseCurrency),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = interestRate,
                onValueChange = {
                    if (it.matches(decimalPattern)) {
                        interestRate = it
                        updateCalculatedMaturity(principal, it, startDate, maturityDate)
                    }
                },
                label = { Text("Annual interest rate") },
                suffix = { Text("%") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )
            WalletDatePickerField(
                value = runCatching { LocalDate.parse(maturityDate) }
                    .getOrDefault(LocalDate.now().plusYears(1)),
                label = "Maturity date",
                onDateSelected = {
                    maturityDate = it.toString()
                    updateCalculatedMaturity(principal, interestRate, startDate, maturityDate)
                }
            )
            OutlinedTextField(
                value = maturityAmount,
                onValueChange = {
                    if (it.matches(decimalPattern)) {
                        maturityAmount = it
                        userModifiedMaturity = true
                    }
                },
                label = { Text("Maturity amount (editable for tax/discrepancies)") },
                prefix = { Text("₹ ") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                visualTransformation = AmountGroupingVisualTransformation(state.baseCurrency),
                modifier = Modifier.fillMaxWidth()
            )
            validationError?.let {
                Text(it, color = WalletColors.red, fontSize = 12.sp)
            }
            Text(
                "FD principal is included in the linked account's total balance.",
                color = WalletColors.muted
            )
            Button(
                onClick = saveDeposit,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(WalletBeige)
            ) {
                Text("Save fixed deposit", color = WalletColors.ink, fontWeight = FontWeight.Bold)
            }
            TextButton(onClick = dismiss, modifier = Modifier.fillMaxWidth()) {
                Text("Cancel")
            }
        }
    }
}
