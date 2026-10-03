package com.example.walletmanager.ui.screen.accounts

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.ModalBottomSheet
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.walletmanager.data.model.FixedDeposit
import com.example.walletmanager.data.model.WalletUiState
import com.example.walletmanager.data.model.calculateFixedDepositMaturity
import com.example.walletmanager.ui.component.AccountRow
import com.example.walletmanager.ui.component.ScreenHeader
import com.example.walletmanager.ui.component.SummaryCard
import com.example.walletmanager.ui.component.WalletColors
import com.example.walletmanager.ui.component.WalletPage
import com.example.walletmanager.ui.component.WalletSheetBackButton
import com.example.walletmanager.ui.component.formatMoney
import com.example.walletmanager.ui.component.walletDropdownItem
import com.example.walletmanager.ui.component.walletDropdownSurface
import com.example.walletmanager.ui.component.rememberDebouncedClick
import com.example.walletmanager.ui.navigation.AccountDetailsRoute
import com.example.walletmanager.ui.navigation.WalletRoute
import com.example.walletmanager.ui.theme.WalletGreen
import com.example.walletmanager.ui.theme.WalletSurface
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
    addAccount: (String, String) -> Unit,
    addFixedDeposit: (FixedDeposit) -> Unit,
    editFixedDeposit: (FixedDeposit) -> Unit,
    deleteFixedDeposit: (FixedDeposit) -> Unit
) {
    var showAddSheet by rememberSaveable { mutableStateOf(false) }
    var showFixedDepositSheet by rememberSaveable { mutableStateOf(false) }
    var fixedDepositsExpanded by rememberSaveable { mutableStateOf(true) }
    var selectedFixedDeposit by remember { mutableStateOf<FixedDeposit?>(null) }

    WalletPage(PaddingValues()) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Your accounts", fontWeight = FontWeight.Bold, fontSize = 24.sp)
                Text("Everything in one view", color = WalletColors.muted, fontSize = 14.sp)
            }
        }
        Spacer(Modifier.height(12.dp))
        SummaryCard("Total across accounts", state.balanceMinor, state)

        state.accounts.forEach { account ->
            AccountRow(
                account = account,
                state = state,
                onClick = { open(AccountDetailsRoute(account.id)) }
            )
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
            Text("🏦 Fixed deposits", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
            Box(contentAlignment = Alignment.Center) {
                Text(
                    if (fixedDepositsExpanded) "▴" else "▾",
                    color = WalletColors.muted,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Column(Modifier.fillMaxWidth().animateContentSize(animationSpec = tween(180))) {
            AnimatedVisibility(
                visible = fixedDepositsExpanded,
                enter = expandVertically(animationSpec = tween(180)) + fadeIn(animationSpec = tween(140)),
                exit = shrinkVertically(animationSpec = tween(180)) + fadeOut(animationSpec = tween(140))
            ) {
                Column {
                    Button(
                        onClick = { showFixedDepositSheet = true },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(WalletGreen)
                    ) {
                        Text("+ FD", color = WalletColors.ink, fontWeight = FontWeight.Bold)
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

        Spacer(Modifier.height(16.dp))
        Button(
            onClick = { showAddSheet = true },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(WalletGreen)
        ) {
            Text("+ Add Bank Account", color = WalletColors.ink, fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.height(70.dp))
    }

    if (showAddSheet) {
        AddAccountSheet(
            dismiss = { showAddSheet = false },
            save = { name, type ->
                addAccount(name, type)
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
    dismiss: () -> Unit,
    save: (String, String) -> Unit
) {
    var name by rememberSaveable { mutableStateOf("") }
    var type by rememberSaveable { mutableStateOf("Bank account") }
    val saveAccount = rememberDebouncedClick {
        save(name, type)
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Add account", fontWeight = FontWeight.Bold)
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
private fun FixedDepositRow(
    deposit: FixedDeposit,
    state: WalletUiState,
    edit: () -> Unit,
    delete: () -> Unit
) {
    val linkedAccount = state.accounts.firstOrNull { it.id == deposit.accountId }
    Card(
        colors = CardDefaults.cardColors(WalletSurface),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(deposit.name, fontWeight = FontWeight.Bold)
                if (linkedAccount != null) {
                    Text(linkedAccount.name, color = WalletGreen, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
            Text(
                "${deposit.interestRateBps / 100.0}% · Matures ${deposit.maturityDate}",
                color = WalletColors.muted
            )
            Text(
                "Maturity ${formatMoney(deposit.maturityAmountMinor, state.balancesVisible, state.baseCurrency)}",
                color = WalletGreen,
                fontWeight = FontWeight.Bold
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                TextButton(onClick = edit) { Text("Edit") }
                TextButton(onClick = delete) { Text("Delete", color = WalletColors.red) }
            }
        }
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
    var maturityDate by rememberSaveable {
        mutableStateOf(initial?.maturityDate ?: LocalDate.now().plusYears(1).toString())
    }
    var maturityAmount by rememberSaveable {
        mutableStateOf(initial?.maturityAmountMinor?.div(100.0)?.toString().orEmpty())
    }
    var userModifiedMaturity by rememberSaveable { mutableStateOf(initial != null) }
    var accountDropdownExpanded by remember { mutableStateOf(false) }
    var validationError by rememberSaveable { mutableStateOf<String?>(null) }
    val decimalPattern = remember { Regex("\\d*(\\.\\d{0,2})?") }

    val selectedAccount = state.accounts.firstOrNull { it.id == accountId }

    fun updateCalculatedMaturity(pStr: String, rStr: String, dStr: String) {
        if (!userModifiedMaturity) {
            val pMinor = ((pStr.toDoubleOrNull() ?: 0.0) * 100).toLong()
            val rBps = ((rStr.toDoubleOrNull() ?: 0.0) * 100).roundToInt()
            if (pMinor > 0 && rBps >= 0 && dStr.isNotBlank()) {
                val calcMinor = calculateFixedDepositMaturity(pMinor, rBps, dStr)
                maturityAmount = (calcMinor / 100.0).toString()
            }
        }
    }

    val saveDeposit = rememberDebouncedClick {
        val principalValue = principal.toBigDecimalOrNull()
        val interestValue = interestRate.toBigDecimalOrNull()
        val maturityValue = maturityAmount.toBigDecimalOrNull()
        val parsedDate = runCatching { LocalDate.parse(maturityDate) }.getOrNull()
        when {
            name.isBlank() -> validationError = "Enter a name for the fixed deposit."
            principalValue == null || principalValue <= BigDecimal.ZERO ->
                validationError = "Principal amount must be a number greater than 0."
            interestValue == null || interestValue < BigDecimal.ZERO ->
                validationError = "Interest rate must be a valid number (0 or more)."
            maturityValue == null || maturityValue <= BigDecimal.ZERO ->
                validationError = "Maturity amount must be a number greater than 0."
            parsedDate == null -> validationError = "Enter a valid maturity date (YYYY-MM-DD)."
            else -> {
                runCatching {
                    val principalMinor = principalValue.toMinorUnits()
                    val interestRateBps = interestValue.toMinorUnits()
                        .coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
                    val enteredAmount = maturityValue.toMinorUnits()
                    val calculatedAmount = calculateFixedDepositMaturity(
                        principalMinor,
                        interestRateBps,
                        parsedDate.toString()
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
                            accountId = accountId
                        )
                    )
                }.onFailure {
                    validationError = "Enter valid numeric values for the deposit."
                }
            }
        }
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
                            modifier = Modifier.walletDropdownItem(),
                            text = { Text(account.name) },
                            onClick = {
                                accountId = account.id
                                accountDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = principal,
                onValueChange = {
                    if (it.matches(decimalPattern)) {
                        principal = it
                        updateCalculatedMaturity(it, interestRate, maturityDate)
                    }
                },
                label = { Text("Principal amount") },
                prefix = { Text("₹ ") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = interestRate,
                onValueChange = {
                    if (it.matches(decimalPattern)) {
                        interestRate = it
                        updateCalculatedMaturity(principal, it, maturityDate)
                    }
                },
                label = { Text("Annual interest rate") },
                suffix = { Text("%") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = maturityDate,
                onValueChange = {
                    maturityDate = it
                    updateCalculatedMaturity(principal, interestRate, it)
                },
                label = { Text("Maturity date (YYYY-MM-DD)") },
                modifier = Modifier.fillMaxWidth()
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
                modifier = Modifier.fillMaxWidth()
            )
            validationError?.let {
                Text(it, color = WalletColors.red, fontSize = 12.sp)
            }
            Text(
                "FD amount is automatically added to the linked bank account's total balance.",
                color = WalletColors.muted
            )
            Button(
                onClick = saveDeposit,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(WalletGreen)
            ) {
                Text("Save fixed deposit", color = WalletColors.ink, fontWeight = FontWeight.Bold)
            }
            TextButton(onClick = dismiss, modifier = Modifier.fillMaxWidth()) {
                Text("Cancel")
            }
        }
    }
}
