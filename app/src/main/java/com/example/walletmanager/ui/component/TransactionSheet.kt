package com.example.walletmanager.ui.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import com.example.walletmanager.data.model.Category
import com.example.walletmanager.data.model.Transaction
import com.example.walletmanager.data.model.TransactionKind
import com.example.walletmanager.data.model.WalletUiState
import com.example.walletmanager.ui.theme.WalletBackground
import com.example.walletmanager.ui.theme.WalletGreenAccent
import com.example.walletmanager.ui.theme.WalletRedAccent
import com.example.walletmanager.ui.theme.WalletSurface
import com.example.walletmanager.ui.theme.WalletText
import com.example.walletmanager.ui.theme.WalletMuted
import com.example.walletmanager.ui.theme.WalletAmber
import com.example.walletmanager.ui.theme.WalletBeige
import com.example.walletmanager.ui.theme.WalletSurfaceVariant
import com.example.walletmanager.ui.theme.WalletDivider
import com.example.walletmanager.ui.component.WalletModalBottomSheet
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter

private val ScreenBg = WalletBackground
private val DarkCardBg = WalletSurface
private val RedAccent = WalletRedAccent
private val GreenAccent = WalletGreenAccent
private val AmberAccent = WalletAmber
private val MutedText = WalletMuted
private val LineColor = WalletDivider
private val CategoryPanelHeaderBg = WalletSurfaceVariant
private val CategoryPanelBg = WalletSurface
private val CategoryPanelLeftBg = WalletBackground
private val CreamText = WalletText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionSheet(
    state: WalletUiState,
    initialTransaction: Transaction? = null,
    initialKind: TransactionKind = TransactionKind.EXPENSE,
    initialTransfer: Boolean = false,
    dismiss: () -> Unit,
    onSave: (title: String, amountMinor: Long, kind: TransactionKind, category: String, accountId: String, note: String, cashbackMinor: Long) -> Unit,
    onTransfer: ((title: String, amountMinor: Long, sourceAccountId: String, destinationAccountId: String, note: String, feeMinor: Long) -> Unit)? = null,
    onDelete: ((Transaction) -> Unit)? = null,
    onAddCategory: ((name: String, parentId: String?) -> Unit)? = null,
    onEditCategory: ((Category) -> Unit)? = null,
    onDeleteCategory: ((Category) -> Unit)? = null
) {
    val isEditing = initialTransaction != null
    var kind by rememberSaveable { mutableStateOf(initialTransaction?.kind ?: initialKind) }

    var dateText by rememberSaveable {
        mutableStateOf(
            initialTransaction?.date ?: LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy (E) HH:mm"))
        )
    }
    var amountText by rememberSaveable {
        mutableStateOf(
            if (initialTransaction != null) {
                (initialTransaction.amountMinor / 100.0).let { if (it % 1.0 == 0.0) it.toLong().toString() else it.toString() }
            } else ""
        )
    }
    var title by rememberSaveable { mutableStateOf(initialTransaction?.title.orEmpty()) }
    var accountId by rememberSaveable {
        mutableStateOf(initialTransaction?.accountId ?: state.accounts.firstOrNull()?.id.orEmpty())
    }
    var destinationAccountId by rememberSaveable {
        mutableStateOf(state.accounts.getOrNull(1)?.id.orEmpty())
    }
    var transferFeeText by rememberSaveable { mutableStateOf("") }
    var note by rememberSaveable { mutableStateOf(initialTransaction?.note.orEmpty()) }
    var cashbackText by rememberSaveable {
        mutableStateOf(
            if (initialTransaction != null && initialTransaction.cashbackMinor > 0) {
                (initialTransaction.cashbackMinor / 100.0).toString()
            } else ""
        )
    }

    var selectedCategoryName by rememberSaveable {
        mutableStateOf(
            initialTransaction?.category ?: if (initialKind == TransactionKind.INCOME) {
                "Income · Salary"
            } else {
                "Food · Lunch"
            }
        )
    }

    var showCategoryPanel by rememberSaveable { mutableStateOf(true) }
    var selectedParentId by rememberSaveable { mutableStateOf<String?>("food") }
    var accountMenu by remember { mutableStateOf(false) }
    var destinationAccountMenu by remember { mutableStateOf(false) }
    var addCategoryDialog by rememberSaveable { mutableStateOf(false) }
    var categoryManager by rememberSaveable { mutableStateOf(false) }
    var categoryToEdit by remember { mutableStateOf<Category?>(null) }
    var categoryToDelete by remember { mutableStateOf<Category?>(null) }
    var confirmTransactionDelete by rememberSaveable { mutableStateOf(false) }
    var activePanel by rememberSaveable { mutableStateOf("category") }
    var transferMode by rememberSaveable { mutableStateOf(initialTransfer) }
    var noteFocused by remember { mutableStateOf(false) }
    val noteBringIntoViewRequester = remember { BringIntoViewRequester() }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(activePanel) {
        if (activePanel != "note") {
            focusManager.clearFocus(force = true)
            keyboardController?.hide()
        }
    }

    LaunchedEffect(note, noteFocused, state.notes) {
        if (noteFocused && note.isNotBlank()) {
            noteBringIntoViewRequester.bringIntoView()
        }
    }

    val activeColor = when {
        transferMode -> AmberAccent
        kind == TransactionKind.EXPENSE -> RedAccent
        else -> GreenAccent
    }
    val panelMinHeight = LocalConfiguration.current.screenHeightDp.dp * 0.4f
    val compactInputStyle = MaterialTheme.typography.bodySmall.copy(
        fontSize = 13.sp,
        lineHeight = 18.sp
    )

    val selectedAccount = state.accounts.firstOrNull { it.id == accountId }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val snackbarController = LocalWalletSnackbarController.current
    val haptics = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()
    val saveShake = remember { Animatable(0f) }

    WalletModalBottomSheet(
        onDismissRequest = dismiss,
        modifier = Modifier.statusBarsPadding(),
        contentWindowInsets = { WindowInsets(0, 0, 0, 0) },
        sheetState = sheetState,
        containerColor = ScreenBg
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .imePadding()
        ) {
          Column(
              modifier = Modifier
                  .fillMaxWidth()
                  .fillMaxHeight()
                  .background(ScreenBg)
          ) {
            // Top Navigation Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = dismiss) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = CreamText
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = when (kind) {
                            TransactionKind.EXPENSE -> if (transferMode) "Transfer" else "Expense"
                            TransactionKind.INCOME -> "Income"
                        },
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = CreamText
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isEditing && onDelete != null) {
                        IconButton(onClick = { confirmTransactionDelete = true }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = RedAccent
                            )
                        }
                    }
                }
            }

            // Top Segmented Tab Buttons (Income, Expense, Transfer)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 3.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Income Tab
                TabButton(
                    label = "Income",
                    isSelected = kind == TransactionKind.INCOME,
                    activeColor = GreenAccent,
                    onClick = {
                        kind = TransactionKind.INCOME
                        transferMode = false
                        transferFeeText = ""
                        selectedParentId = "income"
                        selectedCategoryName = "Income · Salary"
                    },
                    modifier = Modifier.weight(1f)
                )
                // Expense Tab
                TabButton(
                    label = "Expense",
                    isSelected = kind == TransactionKind.EXPENSE && !transferMode,
                    activeColor = RedAccent,
                    onClick = {
                        kind = TransactionKind.EXPENSE
                        transferMode = false
                        transferFeeText = ""
                        selectedParentId = "food"
                        selectedCategoryName = "Food · Lunch"
                    },
                    modifier = Modifier.weight(1f)
                )
                // Transfer Tab (maps to Expense for now)
                TabButton(
                    label = "Transfer",
                    isSelected = transferMode,
                    activeColor = AmberAccent,
                    onClick = {
                        kind = TransactionKind.EXPENSE
                        transferMode = true
                        transferFeeText = ""
                        selectedCategoryName = "Transfer"
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(12.dp))

            // Form Rows (Date, Amount, Category, Account, Note)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp, vertical = 3.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Date Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            activePanel = "date"
                            showCategoryPanel = true
                        },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Date",
                        color = MutedText,
                        fontSize = 13.sp,
                        modifier = Modifier.width(72.dp)
                    )
                    Text(
                        text = runCatching {
                            val parsed = LocalDateTime.parse(
                                dateText,
                                DateTimeFormatter.ofPattern("dd/MM/yyyy (E) HH:mm")
                            )
                            "${formatWalletDate(parsed.toLocalDate())} · ${parsed.format(DateTimeFormatter.ofPattern("h:mm a"))}"
                        }.getOrDefault(dateText),
                        color = CreamText,
                        fontSize = 13.sp,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Amount Row
                HorizontalDivider(color = LineColor.copy(alpha = 0.7f), thickness = 0.5.dp)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            focusManager.clearFocus(force = true)
                            keyboardController?.hide()
                            activePanel = "amount"
                            showCategoryPanel = true
                        },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Amount",
                        color = MutedText,
                        fontSize = 13.sp,
                        modifier = Modifier.width(72.dp)
                    )
                    Box(
                        Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = amountText,
                            onValueChange = {},
                            enabled = false,
                            textStyle = compactInputStyle,
                            placeholder = {
                                Text("0.00", color = MutedText.copy(alpha = 0.4f), fontSize = 13.sp, lineHeight = 18.sp)
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = activeColor,
                                unfocusedBorderColor = LineColor,
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                disabledTextColor = activeColor,
                                focusedTextColor = activeColor,
                                unfocusedTextColor = activeColor
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        )
                    }
                }

                // Category Row
                HorizontalDivider(color = LineColor.copy(alpha = 0.7f), thickness = 0.5.dp)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Category",
                        color = MutedText,
                        fontSize = 13.sp,
                        modifier = Modifier.width(72.dp)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showCategoryPanel = true
                                activePanel = "category"
                            }
                    ) {
                        OutlinedTextField(
                            value = if (transferMode) "Transfer" else selectedCategoryName,
                            onValueChange = {},
                            readOnly = true,
                            enabled = false,
                            textStyle = compactInputStyle,
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledBorderColor = if (showCategoryPanel) activeColor else LineColor,
                                disabledTextColor = CreamText,
                                disabledContainerColor = Color.Transparent
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        )
                    }
                }

                // Account Row
                HorizontalDivider(color = LineColor.copy(alpha = 0.7f), thickness = 0.5.dp)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Account",
                        color = MutedText,
                        fontSize = 13.sp,
                        modifier = Modifier.width(72.dp)
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { accountMenu = !accountMenu }
                            .semantics(mergeDescendants = true) {
                                contentDescription = selectedAccount?.name ?: "Choose account"
                                role = Role.Button
                            }
                    ) {
                        OutlinedTextField(
                            value = selectedAccount?.let {
                                "${it.name} · ${formatMoney(it.balanceMinor, true, state.baseCurrency)}"
                            } ?: "Choose account",
                            onValueChange = {},
                            readOnly = true,
                            enabled = false,
                            singleLine = true,
                            maxLines = 1,
                            textStyle = compactInputStyle,
                            modifier = Modifier
                                .fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledBorderColor = LineColor,
                                disabledTextColor = CreamText,
                                disabledContainerColor = Color.Transparent
                            )
                        )
                        DropdownMenu(
                            expanded = accountMenu,
                            onDismissRequest = { accountMenu = false },
                            modifier = Modifier.walletDropdownSurface()
                        ) {
                            state.accounts.forEach { acc ->
                                DropdownMenuItem(
                                    modifier = Modifier.walletDropdownItem(
                                        selected = acc.id == accountId,
                                        accent = if (kind == TransactionKind.INCOME) GreenAccent else RedAccent
                                    ),
                                    text = {
                                        Text(
                                            "${acc.name} · ${formatMoney(acc.balanceMinor, true, state.baseCurrency)}",
                                            fontSize = 12.sp,
                                            color = if (acc.id == accountId && kind == TransactionKind.INCOME) ScreenBg else CreamText
                                        )
                                    },
                                    onClick = {
                                        accountId = acc.id
                                        if (destinationAccountId == acc.id) {
                                            destinationAccountId = state.accounts.firstOrNull { it.id != acc.id }?.id.orEmpty()
                                        }
                                        accountMenu = false
                                    }
                                )
                            }
                        }
                    }
                }

                if (transferMode) {
                    HorizontalDivider(color = LineColor.copy(alpha = 0.7f), thickness = 0.5.dp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "To account",
                            color = MutedText,
                            fontSize = 13.sp,
                            modifier = Modifier.width(72.dp)
                        )
                        Box(
                            Modifier
                                .weight(1f)
                                .clickable { destinationAccountMenu = !destinationAccountMenu }
                        ) {
                            val destination = state.accounts.firstOrNull { it.id == destinationAccountId }
                            OutlinedTextField(
                                value = destination?.let {
                                    "${it.name} · ${formatMoney(it.balanceMinor, true, state.baseCurrency)}"
                                } ?: "Choose account",
                                onValueChange = {},
                                enabled = false,
                                singleLine = true,
                                maxLines = 1,
                                textStyle = compactInputStyle,
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    disabledBorderColor = LineColor,
                                    disabledTextColor = CreamText,
                                    disabledContainerColor = Color.Transparent
                                )
                            )
                            DropdownMenu(
                                expanded = destinationAccountMenu,
                                onDismissRequest = { destinationAccountMenu = false },
                                modifier = Modifier.walletDropdownSurface()
                            ) {
                                state.accounts
                                    .filter { it.id != accountId }
                                    .forEach { acc ->
                                        DropdownMenuItem(
                                            modifier = Modifier.walletDropdownItem(
                                                selected = acc.id == destinationAccountId,
                                                accent = if (kind == TransactionKind.INCOME) GreenAccent else RedAccent
                                            ),
                                            text = { Text(acc.name, color = if (acc.id == destinationAccountId && kind == TransactionKind.INCOME) ScreenBg else CreamText) },
                                            onClick = {
                                                destinationAccountId = acc.id
                                                destinationAccountMenu = false
                                            }
                                        )
                                    }
                            }
                        }
                    }
                    HorizontalDivider(color = LineColor.copy(alpha = 0.7f), thickness = 0.5.dp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Fee (optional)",
                            color = MutedText,
                            fontSize = 10.sp,
                            maxLines = 1,
                            softWrap = false,
                            modifier = Modifier.width(72.dp)
                        )
                        OutlinedTextField(
                            value = transferFeeText,
                            onValueChange = { transferFeeText = it },
                            placeholder = {
                                Text(
                                    "0",
                                    color = MutedText.copy(alpha = 0.5f),
                                    fontSize = 13.sp,
                                    lineHeight = 18.sp
                                )
                            },
                            prefix = { Text("${state.baseCurrency.symbol} ", fontSize = 13.sp) },
                            singleLine = true,
                            textStyle = compactInputStyle,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = activeColor,
                                unfocusedBorderColor = LineColor,
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedTextColor = CreamText,
                                unfocusedTextColor = CreamText
                            ),
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        )
                    }
                }

                if (kind == TransactionKind.EXPENSE) {
                    HorizontalDivider(color = LineColor.copy(alpha = 0.7f), thickness = 0.5.dp)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                focusManager.clearFocus(force = true)
                                keyboardController?.hide()
                                activePanel = "cashback"
                                showCategoryPanel = true
                            },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Cashback",
                            color = MutedText,
                            fontSize = 13.sp,
                            modifier = Modifier.width(72.dp)
                        )
                        Box(
                            Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = cashbackText,
                                onValueChange = {},
                                enabled = false,
                                textStyle = compactInputStyle,
                                placeholder = {
                                    Text("Optional", color = MutedText.copy(alpha = 0.4f), fontSize = 13.sp, lineHeight = 18.sp)
                                },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = GreenAccent,
                                    unfocusedBorderColor = LineColor,
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    disabledTextColor = GreenAccent,
                                    focusedTextColor = GreenAccent,
                                    unfocusedTextColor = GreenAccent
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                            )
                        }
                    }
                }

                // Keep the note field last so it can sit directly above the keyboard while editing.
                HorizontalDivider(color = LineColor.copy(alpha = 0.7f), thickness = 0.5.dp)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = "Note",
                        color = MutedText,
                        fontSize = 13.sp,
                        modifier = Modifier.width(72.dp).padding(top = 14.dp)
                    )
                    Column(
                        Modifier
                            .weight(1f)
                            .bringIntoViewRequester(noteBringIntoViewRequester)
                    ) {
                        OutlinedTextField(
                            value = note,
                            onValueChange = { note = it },
                            textStyle = compactInputStyle,
                            placeholder = {
                                Text("Write note...", color = MutedText.copy(alpha = 0.4f), fontSize = 13.sp, lineHeight = 18.sp)
                            },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = if (activePanel == "note") activeColor else LineColor,
                                unfocusedBorderColor = LineColor,
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedTextColor = CreamText,
                                unfocusedTextColor = CreamText
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .onFocusChanged { focusState ->
                                    noteFocused = focusState.isFocused
                                    if (focusState.isFocused) {
                                        activePanel = "note"
                                        showCategoryPanel = false
                                    }
                                }
                        )
                        val matchingNotes = remember(state.notes, note) {
                            if (note.isBlank()) emptyList()
                            else state.notes
                                .filter { it.text.contains(note, ignoreCase = true) }
                                .map { it.text }
                        }
                        if (noteFocused && matchingNotes.isNotEmpty()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 160.dp)
                                    .padding(top = 6.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(WalletSurfaceVariant)
                                    .border(1.dp, WalletBeige.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                                    .verticalScroll(rememberScrollState())
                            ) {
                                Text(
                                    text = "Saved note suggestions · ${matchingNotes.size}",
                                    color = WalletBeige,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp)
                                )
                                HorizontalDivider(color = LineColor.copy(alpha = 0.7f), thickness = 0.5.dp)
                                matchingNotes.forEach { matchingNote ->
                                    DropdownMenuItem(
                                        text = { Text(matchingNote, color = CreamText, fontSize = 13.sp) },
                                        onClick = {
                                            note = matchingNote
                                            noteFocused = false
                                            focusManager.clearFocus(force = true)
                                            keyboardController?.hide()
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(4.dp))

            // Bottom Category Split Panel
            if (showCategoryPanel && activePanel != "note") {
                Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(panelMinHeight)
                    .background(CategoryPanelBg)
                ) {
                    // Panel Header Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(CategoryPanelHeaderBg)
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = when (activePanel) {
                                "date" -> "Choose date"
                                "amount" -> "Enter amount"
                                "cashback" -> "Enter cashback"
                                "note" -> ""
                                else -> "Category"
                            },
                            color = CreamText,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (activePanel == "category") {
                                IconButton(onClick = { categoryManager = true }) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Edit Category",
                                        tint = CreamText
                                    )
                                }
                            }
                            IconButton(onClick = { showCategoryPanel = false }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close Panel",
                                    tint = CreamText
                                )
                            }
                        }
                    }

                    AnimatedContent(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        targetState = activePanel,
                        transitionSpec = {
                            (fadeIn(tween(90, easing = FastOutSlowInEasing)) +
                                slideInVertically(tween(110, easing = FastOutSlowInEasing)) { it / 14 }) togetherWith
                                (fadeOut(tween(75, easing = FastOutSlowInEasing)) +
                                    slideOutVertically(tween(90, easing = FastOutSlowInEasing)) { -it / 18 })
                        },
                        label = "transaction-input-panel"
                    ) { panel ->
                        if (panel == "category") {
                        // Split Columns (Left Parents, Right Subcategories)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight()
                        ) {
                            // Left Column: Parent Categories
                            val parents = remember(state.categories, kind) {
                                state.categories.filter {
                                    it.parentId == null &&
                                            (kind == TransactionKind.INCOME && it.id == "income" ||
                                                    kind == TransactionKind.EXPENSE && it.id != "income")
                                }
                            }
                            val activeParent = parents.firstOrNull { it.id == selectedParentId } ?: parents.firstOrNull()

                            Column(
                                modifier = Modifier
                                    .width(148.dp)
                                    .fillMaxHeight()
                                    .background(CategoryPanelLeftBg)
                                    .verticalScroll(rememberScrollState())
                            ) {
                                parents.forEach { parent ->
                                    val isSelected = parent.id == activeParent?.id
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                selectedParentId = parent.id
                                                selectedCategoryName = parent.name
                                            }
                                            .background(if (isSelected) DarkCardBg else Color.Transparent)
                                            .padding(horizontal = 16.dp, vertical = 14.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            WalletIcon(categoryIconName(parent.name), size = 16.dp, color = WalletColors.cream)
                                            Spacer(Modifier.width(10.dp))
                                            Text(
                                                text = parent.name,
                                                color = CreamText,
                                                fontSize = 13.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                            )
                                        }
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                            contentDescription = null,
                                            tint = MutedText,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    HorizontalDivider(color = LineColor.copy(alpha = 0.5f), thickness = 0.5.dp)
                                }
                            }

                            // Right Column: Subcategories of Active Parent
                            val subcategories = remember(state.categories, activeParent) {
                                if (activeParent == null) emptyList()
                                else state.categories.filter { it.parentId == activeParent.id }
                            }

                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .background(CategoryPanelBg)
                                    .verticalScroll(rememberScrollState())
                            ) {
                                if (activeParent != null) {
                                    val isParentOnlySelected = selectedCategoryName == activeParent.name
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                selectedCategoryName = activeParent.name
                                            }
                                            .background(if (isParentOnlySelected) DarkCardBg else Color.Transparent)
                                            .padding(horizontal = 20.dp, vertical = 14.dp)
                                    ) {
                                        Text(
                                            text = "${activeParent.name} (Main category)",
                                            color = if (isParentOnlySelected) activeColor else CreamText,
                                            fontSize = 13.sp,
                                            fontWeight = if (isParentOnlySelected) FontWeight.Bold else FontWeight.SemiBold
                                        )
                                    }
                                    HorizontalDivider(color = LineColor.copy(alpha = 0.5f), thickness = 0.5.dp)
                                }
                                subcategories.forEach { sub ->
                                    val isSubSelected = selectedCategoryName == "${activeParent?.name} · ${sub.name}" || selectedCategoryName == sub.name
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                selectedCategoryName = "${activeParent?.name} · ${sub.name}"
                                            }
                                            .background(if (isSubSelected) DarkCardBg else Color.Transparent)
                                            .padding(horizontal = 20.dp, vertical = 14.dp)
                                    ) {
                                        Text(
                                            text = sub.name,
                                            color = if (isSubSelected) activeColor else CreamText,
                                            fontSize = 13.sp,
                                            fontWeight = if (isSubSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                    HorizontalDivider(color = LineColor.copy(alpha = 0.3f), thickness = 0.5.dp)
                                }
                            }
                        }
                    } else {
                        when (panel) {
                            "date" -> DatePanel(
                                initialDate = runCatching {
                                    LocalDateTime.parse(
                                        dateText,
                                        DateTimeFormatter.ofPattern("dd/MM/yyyy (E) HH:mm")
                                    ).toLocalDate()
                                }.getOrDefault(LocalDate.now())
                            ) { selectedDate ->
                                dateText = selectedDate.atTime(LocalTime.now()).format(
                                    DateTimeFormatter.ofPattern("dd/MM/yyyy (E) HH:mm")
                                )
                                activePanel = "category"
                            }
                            "amount" -> NumberPad(
                                value = amountText,
                                onValueChange = { amountText = it },
                                onDone = { activePanel = "category" }
                            )
                            "cashback" -> NumberPad(
                                value = cashbackText,
                                onValueChange = { cashbackText = it },
                                onDone = { activePanel = "category" }
                            )
                            "note" -> Unit
                        }
                    }
                    }
                }
            }

            // Save Action Button
            Button(
                onClick = {
                    val amountMinor = ((amountText.toDoubleOrNull() ?: 0.0) * 100).toLong()
                    val cashbackMinor = ((cashbackText.toDoubleOrNull() ?: 0.0) * 100).toLong()
                    val feeAmount = transferFeeText.toDoubleOrNull()
                    val feeMinor = ((feeAmount ?: 0.0) * 100).toLong()
                    val validationMessage = when {
                        amountMinor <= 0 -> "Enter an amount greater than zero"
                        transferMode && transferFeeText.isNotBlank() && (feeAmount == null || feeAmount < 0) -> "Enter a valid transfer fee"
                        state.accounts.none { it.id == accountId } -> "Choose an account"
                        !transferMode && selectedCategoryName.isBlank() -> "Choose a category"
                        transferMode && state.accounts.none { it.id == destinationAccountId && it.id != accountId } ->
                            "Choose a different destination account"
                        transferMode && onTransfer == null -> "Transfers are unavailable right now"
                        else -> null
                    }
                    if (validationMessage != null) {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        coroutineScope.launch { snackbarController.show(validationMessage) }
                        coroutineScope.launch {
                            saveShake.snapTo(0f)
                            listOf(-10f, 10f, -7f, 7f, -3f, 3f, 0f).forEach { offset ->
                                saveShake.animateTo(
                                    targetValue = offset,
                                    animationSpec = tween(28, easing = FastOutSlowInEasing)
                                )
                            }
                        }
                        return@Button
                    }
                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    if (transferMode) {
                        onTransfer?.invoke(title, amountMinor, accountId, destinationAccountId, note, feeMinor)
                    } else {
                        onSave(
                            title.ifBlank { selectedCategoryName },
                            amountMinor,
                            kind,
                            selectedCategoryName,
                            accountId,
                            note,
                            cashbackMinor
                        )
                    }
                    dismiss()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(46.dp)
                    .graphicsLayer { translationX = saveShake.value },
                colors = ButtonDefaults.buttonColors(containerColor = WalletBeige),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    text = if (isEditing) "Update Transaction" else {
                        "Save ${if (transferMode) "Transfer" else kind.name.lowercase().replaceFirstChar { it.uppercase() }}"
                    },
                    color = WalletColors.ink,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
          }
          WalletSnackbarHost(
              modifier = Modifier
                  .align(Alignment.BottomCenter)
                  .fillMaxWidth()
                  .padding(horizontal = 16.dp, vertical = 76.dp)
          )
        }
    }

    if (confirmTransactionDelete && initialTransaction != null && onDelete != null) {
        WalletConfirmDialog(
            title = "Delete transaction?",
            message = "${initialTransaction.title} will be removed.",
            onDismiss = { confirmTransactionDelete = false },
            onConfirm = {
                onDelete(initialTransaction)
                confirmTransactionDelete = false
                dismiss()
            }
        )
    }

    if (categoryManager && onAddCategory != null) {
        CategoryManagerSheet(
            categories = state.categories,
            dismiss = { categoryManager = false },
            add = {
                categoryManager = false
                addCategoryDialog = true
            },
            edit = {
                categoryManager = false
                categoryToEdit = it
            },
            delete = { categoryToDelete = it }
        )
    }

    categoryToDelete?.let { category ->
        WalletConfirmDialog(
            title = "Delete category?",
            message = "${category.name} will be removed.",
            onDismiss = { categoryToDelete = null },
            onConfirm = {
                onDeleteCategory?.invoke(category)
                categoryToDelete = null
            }
        )
    }

    if (addCategoryDialog && onAddCategory != null) {
        AddCategorySheet(
            categories = state.categories,
            dismiss = { addCategoryDialog = false },
            save = { name, parentId ->
                onAddCategory(name, parentId)
                addCategoryDialog = false
            }
        )
    }

    val currentCategoryToEdit = categoryToEdit
    if (currentCategoryToEdit != null && onEditCategory != null) {
        AddCategorySheet(
            categories = state.categories,
            initial = currentCategoryToEdit,
            dismiss = { categoryToEdit = null },
            save = { name, parentId ->
                onEditCategory(currentCategoryToEdit.copy(name = name, parentId = parentId))
                categoryToEdit = null
            }
        )
    }
}

@Composable
private fun DatePanel(initialDate: LocalDate, onSelect: (LocalDate) -> Unit) {
    WalletCalendarPicker(initialDate = initialDate, onDateSelected = onSelect)
}

@Composable
private fun NumberPad(
    value: String,
    onValueChange: (String) -> Unit,
    onDone: () -> Unit
) {
    val haptics = LocalHapticFeedback.current
    val rows = listOf(
        listOf("1", "2", "3", "⌫"),
        listOf("4", "5", "6", "C"),
        listOf("7", "8", "9", "−"),
        listOf("", "0", ".", "Done")
    )
    Column(
        Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .padding(vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(7.dp, Alignment.CenterVertically)
    ) {
        rows.forEach { row ->
            Row(
                Modifier.widthIn(max = 288.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                row.forEach { key ->
                    if (key.isEmpty()) {
                        Spacer(Modifier.width(64.dp).height(48.dp))
                    } else {
                        val isDone = key == "Done"
                        Box(
                            modifier = Modifier
                                .width(if (isDone) 72.dp else 64.dp)
                                .height(48.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isDone) WalletBeige else DarkCardBg)
                                .clickable {
                                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    when (key) {
                                        "Done" -> onDone()
                                        "⌫" -> onValueChange(value.dropLast(1))
                                        "C" -> onValueChange("")
                                        "−" -> onValueChange(if (value.startsWith("-")) value.drop(1) else "-$value")
                                        "." -> if (!value.contains('.')) onValueChange(if (value.isEmpty() || value == "-") "${value}0." else "$value.")
                                        else -> onValueChange(value + key)
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = key,
                                color = if (isDone) WalletColors.ink else CreamText,
                                fontSize = if (key.length > 1) 13.sp else 18.sp,
                                fontWeight = if (isDone) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TabButton(
    label: String,
    isSelected: Boolean,
    activeColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(DarkCardBg)
            .then(
                if (isSelected) Modifier.border(1.5.dp, activeColor, RoundedCornerShape(10.dp))
                else Modifier
            )
            .clickable(onClick = onClick)
            .semantics { selected = isSelected }
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (isSelected) activeColor else CreamText,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            fontSize = 13.sp
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddCategorySheet(
    categories: List<Category>,
    initial: Category? = null,
    dismiss: () -> Unit,
    save: (String, String?) -> Unit
) {
    var name by rememberSaveable { mutableStateOf(initial?.name.orEmpty()) }
    var parentId by rememberSaveable { mutableStateOf(initial?.parentId) }

    WalletModalBottomSheet(
        onDismissRequest = dismiss,
        contentWindowInsets = { WindowInsets(0, 0, 0, 0) },
        dragHandle = { WalletSheetBackButton(dismiss) },
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = CategoryPanelBg
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.5f)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                if (initial == null) "Add category" else "Edit category",
                color = CreamText,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Category name") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GreenAccent,
                    unfocusedBorderColor = LineColor,
                    focusedTextColor = CreamText,
                    unfocusedTextColor = CreamText
                ),
                modifier = Modifier.fillMaxWidth()
            )
            Text("Parent category (leave unselected for top-level)", color = MutedText, fontSize = 12.sp)
            Row(
                Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextButton(onClick = { parentId = null }) {
                    Text(
                        if (parentId == null) "Top level ✓" else "Top level",
                        color = if (parentId == null) GreenAccent else CreamText
                    )
                }
                categories
                    .filter { it.parentId == null && it.id != initial?.id }
                    .forEach { category ->
                        TextButton(onClick = { parentId = category.id }) {
                            Text(
                                if (parentId == category.id) "${category.name} ✓" else category.name,
                                color = if (parentId == category.id) GreenAccent else CreamText
                            )
                        }
                    }
            }

            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        save(name, parentId)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = WalletBeige),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Save Category", color = WalletColors.ink, fontWeight = FontWeight.Bold)
            }
            TextButton(onClick = dismiss, modifier = Modifier.fillMaxWidth()) {
                Text("Cancel", color = MutedText)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryManagerSheet(
    categories: List<Category>,
    dismiss: () -> Unit,
    add: () -> Unit,
    edit: (Category) -> Unit,
    delete: (Category) -> Unit
) {
    WalletModalBottomSheet(
        onDismissRequest = dismiss,
        contentWindowInsets = { WindowInsets(0, 0, 0, 0) },
        dragHandle = { WalletSheetBackButton(dismiss) },
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = CategoryPanelBg
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.5f)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("Manage Categories", color = CreamText, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Button(
                onClick = add,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = WalletBeige),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("+ Add category or subcategory", color = WalletColors.ink, fontWeight = FontWeight.Bold)
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                categories.forEach { category ->
                    Row(
                        Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (category.parentId == null) category.name else "  └ ${category.name}",
                            modifier = Modifier.weight(1f),
                            color = CreamText,
                            fontWeight = if (category.parentId == null) FontWeight.Bold else FontWeight.Normal
                        )
                        WalletActionButton("Edit", { edit(category) })
                        WalletActionButton("Delete", { delete(category) }, destructive = true)
                    }
                    HorizontalDivider(color = LineColor.copy(alpha = 0.5f), thickness = 0.5.dp)
                }
            }

            TextButton(onClick = dismiss, modifier = Modifier.fillMaxWidth()) {
                Text("Done", color = CreamText)
            }
        }
    }
}
