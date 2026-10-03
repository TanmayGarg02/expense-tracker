package com.example.walletmanager.ui.component

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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.walletmanager.data.model.Category
import com.example.walletmanager.data.model.Transaction
import com.example.walletmanager.data.model.TransactionKind
import com.example.walletmanager.data.model.WalletUiState
import com.example.walletmanager.ui.theme.WalletGreen
import java.time.LocalDateTime
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter

private val ScreenBg = Color(0xFF1B1D22)
private val DarkCardBg = Color(0xFF24262E)
private val RedAccent = Color(0xFFEF5350)
private val GreenAccent = Color(0xFF66BB6A)
private val BlueAccent = Color(0xFF42A5F5)
private val MutedText = Color(0xFF9E9E9E)
private val LineColor = Color(0xFF32353E)
private val CategoryPanelHeaderBg = Color(0xFF383B44)
private val CategoryPanelBg = Color(0xFF262830)
private val CategoryPanelLeftBg = Color(0xFF202228)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionSheet(
    state: WalletUiState,
    initialTransaction: Transaction? = null,
    initialKind: TransactionKind = TransactionKind.EXPENSE,
    initialTransfer: Boolean = false,
    dismiss: () -> Unit,
    onSave: (title: String, amountMinor: Long, kind: TransactionKind, category: String, accountId: String, note: String, cashbackMinor: Long) -> Unit,
    onTransfer: ((title: String, amountMinor: Long, sourceAccountId: String, destinationAccountId: String, note: String) -> Unit)? = null,
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
    var addCategoryDialog by rememberSaveable { mutableStateOf(false) }
    var categoryManager by rememberSaveable { mutableStateOf(false) }
    var categoryToEdit by remember { mutableStateOf<Category?>(null) }
    var activePanel by rememberSaveable { mutableStateOf("category") }
    var transferMode by rememberSaveable { mutableStateOf(initialTransfer) }

    val activeColor = when (kind) {
        TransactionKind.EXPENSE -> RedAccent
        TransactionKind.INCOME -> GreenAccent
    }
    val compactInputStyle = MaterialTheme.typography.bodySmall.copy(
        fontSize = 13.sp,
        lineHeight = 18.sp
    )

    val selectedAccount = state.accounts.firstOrNull { it.id == accountId }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = dismiss,
        contentWindowInsets = { WindowInsets(0, 0, 0, 0) },
        sheetState = sheetState,
        containerColor = ScreenBg,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
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
                            tint = Color.White
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
                        color = Color.White
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isEditing && onDelete != null && initialTransaction != null) {
                        IconButton(onClick = {
                            onDelete(initialTransaction)
                            dismiss()
                        }) {
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
                    label = "💰 Income",
                    isSelected = kind == TransactionKind.INCOME,
                    activeColor = GreenAccent,
                    onClick = {
                        kind = TransactionKind.INCOME
                        transferMode = false
                        selectedParentId = "income"
                        selectedCategoryName = "Income · Salary"
                    },
                    modifier = Modifier.weight(1f)
                )
                // Expense Tab
                TabButton(
                    label = "💸 Expense",
                    isSelected = kind == TransactionKind.EXPENSE,
                    activeColor = RedAccent,
                    onClick = {
                        kind = TransactionKind.EXPENSE
                        transferMode = false
                        selectedParentId = "food"
                        selectedCategoryName = "Food · Lunch"
                    },
                    modifier = Modifier.weight(1f)
                )
                // Transfer Tab (maps to Expense for now)
                TabButton(
                    label = "🔄 Transfer",
                    isSelected = transferMode,
                    activeColor = BlueAccent,
                    onClick = {
                        kind = TransactionKind.EXPENSE
                        transferMode = true
                        selectedCategoryName = "Transfer"
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            // Form Rows (Date, Amount, Category, Account, Note)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 3.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Date Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { activePanel = "date" },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Date",
                        color = MutedText,
                        fontSize = 13.sp,
                        modifier = Modifier.width(72.dp)
                    )
                    Text(
                        text = dateText,
                        color = Color.White,
                        fontSize = 13.sp,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Amount Row
                HorizontalDivider(color = LineColor.copy(alpha = 0.7f), thickness = 0.5.dp)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { activePanel = "amount" },
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
                                disabledTextColor = Color.White,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
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
                                disabledTextColor = Color.White,
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
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = selectedAccount?.let {
                                "${it.name} · ${formatMoney(it.balanceMinor, true, state.baseCurrency)}"
                            } ?: "Choose account",
                            onValueChange = {},
                            readOnly = true,
                            enabled = false,
                            textStyle = compactInputStyle,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { accountMenu = !accountMenu },
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledBorderColor = LineColor,
                                disabledTextColor = Color.White,
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
                                    modifier = Modifier.walletDropdownItem(),
                                    text = {
                                        Text(
                                            "${acc.name} · ${formatMoney(acc.balanceMinor, true, state.baseCurrency)}",
                                            fontSize = 12.sp
                                        )
                                    },
                                    onClick = {
                                        accountId = acc.id
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
                        Box(Modifier.fillMaxWidth()) {
                            val destination = state.accounts.firstOrNull { it.id == destinationAccountId }
                            OutlinedTextField(
                                value = destination?.let {
                                    "${it.name} · ${formatMoney(it.balanceMinor, true, state.baseCurrency)}"
                                } ?: "Choose account",
                                onValueChange = {},
                                enabled = false,
                                textStyle = compactInputStyle,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { accountMenu = !accountMenu },
                                colors = OutlinedTextFieldDefaults.colors(
                                    disabledBorderColor = LineColor,
                                    disabledTextColor = Color.White,
                                    disabledContainerColor = Color.Transparent
                                )
                            )
                            DropdownMenu(
                                expanded = accountMenu,
                                onDismissRequest = { accountMenu = false },
                                modifier = Modifier.walletDropdownSurface()
                            ) {
                                state.accounts
                                    .filter { it.id != accountId }
                                    .forEach { acc ->
                                        DropdownMenuItem(
                                            modifier = Modifier.walletDropdownItem(),
                                            text = { Text(acc.name) },
                                            onClick = {
                                                destinationAccountId = acc.id
                                                accountMenu = false
                                            }
                                        )
                                    }
                            }
                        }
                    }
                }

                // Note Row
                HorizontalDivider(color = LineColor.copy(alpha = 0.7f), thickness = 0.5.dp)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Note",
                        color = MutedText,
                        fontSize = 13.sp,
                        modifier = Modifier.width(72.dp)
                    )
                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        textStyle = compactInputStyle,
                        placeholder = {
                            Text("Write note...", color = MutedText.copy(alpha = 0.4f), fontSize = 13.sp, lineHeight = 18.sp)
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = activeColor,
                            unfocusedBorderColor = LineColor,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .onFocusChanged { if (it.isFocused) activePanel = "note" }
                    )
                }

                if (kind == TransactionKind.EXPENSE) {
                    HorizontalDivider(color = LineColor.copy(alpha = 0.7f), thickness = 0.5.dp)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { activePanel = "cashback" },
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
                                    focusedBorderColor = activeColor,
                                    unfocusedBorderColor = LineColor,
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    disabledTextColor = Color.White,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(4.dp))

            // Bottom Category Split Panel
            if (showCategoryPanel) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
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
                                "note" -> "Previous notes"
                                else -> "Category"
                            },
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (activePanel == "category") {
                                IconButton(onClick = { categoryManager = true }) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Edit Category",
                                        tint = Color.White
                                    )
                                }
                            }
                            IconButton(onClick = { showCategoryPanel = false }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close Panel",
                                    tint = Color.White
                                )
                            }
                        }
                    }

                    if (activePanel == "category") {
                        // Split Columns (Left Parents, Right Subcategories)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
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
                                                selectedParentId = parent.id
                                                selectedCategoryName = parent.name
                                            }
                                            .background(if (isSelected) DarkCardBg else Color.Transparent)
                                            .padding(horizontal = 16.dp, vertical = 14.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(categoryEmoji(parent.name), fontSize = 14.sp)
                                            Spacer(Modifier.width(10.dp))
                                            Text(
                                                text = parent.name,
                                                color = Color.White,
                                                fontSize = 13.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                            )
                                        }
                                        Icon(
                                            imageVector = Icons.Default.KeyboardArrowRight,
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
                                                selectedCategoryName = activeParent.name
                                            }
                                            .background(if (isParentOnlySelected) DarkCardBg else Color.Transparent)
                                            .padding(horizontal = 20.dp, vertical = 14.dp)
                                    ) {
                                        Text(
                                            text = "${activeParent.name} (Main category)",
                                            color = if (isParentOnlySelected) activeColor else Color.White,
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
                                                selectedCategoryName = "${activeParent?.name} · ${sub.name}"
                                            }
                                            .background(if (isSubSelected) DarkCardBg else Color.Transparent)
                                            .padding(horizontal = 20.dp, vertical = 14.dp)
                                    ) {
                                        Text(
                                            text = sub.name,
                                            color = if (isSubSelected) activeColor else Color.White,
                                            fontSize = 13.sp,
                                            fontWeight = if (isSubSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                    HorizontalDivider(color = LineColor.copy(alpha = 0.3f), thickness = 0.5.dp)
                                }
                            }
                        }
                    } else {
                        when (activePanel) {
                            "date" -> DatePanel { selectedDate ->
                                dateText = selectedDate
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
                            "note" -> NotePanel(
                                notes = state.notes,
                                onSelect = { note = it }
                            )
                        }
                    }
                }
            }

            // Save Action Button
            Button(
                onClick = {
                    val amountMinor = ((amountText.toDoubleOrNull() ?: 0.0) * 100).toLong()
                    val cashbackMinor = ((cashbackText.toDoubleOrNull() ?: 0.0) * 100).toLong()
                    if (amountMinor > 0) {
                        if (transferMode && onTransfer != null) {
                            onTransfer(
                                title,
                                amountMinor,
                                accountId,
                                destinationAccountId,
                                note
                            )
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
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(46.dp),
                colors = ButtonDefaults.buttonColors(containerColor = activeColor),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    text = if (isEditing) "Update Transaction" else "Save ${kind.name.lowercase().replaceFirstChar { it.uppercase() }}",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
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
            delete = { onDeleteCategory?.invoke(it) }
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
private fun DatePanel(onSelect: (String) -> Unit) {
    val month = remember { YearMonth.now() }
    val days = remember(month) {
        val leading = month.atDay(1).dayOfWeek.value - 1
        List(leading) { null } + (1..month.lengthOfMonth()).map { month.atDay(it) }
    }
    Column(
        Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            "${month.month.name.lowercase().replaceFirstChar { it.uppercase() }} ${month.year}",
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
        days.chunked(7).forEach { week ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                week.forEach { date ->
                    Box(
                        Modifier
                            .weight(1f)
                            .height(38.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (date == LocalDate.now()) DarkCardBg else Color.Transparent)
                            .clickable {
                                date?.let {
                                    onSelect(
                                        it.atTime(LocalTime.now()).format(
                                            DateTimeFormatter.ofPattern("dd/MM/yyyy (E) HH:mm")
                                        )
                                    )
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(date?.dayOfMonth?.toString().orEmpty(), color = Color.White)
                    }
                }
                repeat(7 - week.size) {
                    Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun NumberPad(
    value: String,
    onValueChange: (String) -> Unit,
    onDone: () -> Unit
) {
    val keys = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", ".", "0", "Done")
    Column(
        Modifier
            .fillMaxWidth()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        keys.chunked(3).forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { key ->
                    Button(
                        onClick = {
                            if (key == "Done") {
                                onDone()
                            } else if (key != "." || !value.contains('.')) {
                                onValueChange(value + key)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = DarkCardBg)
                    ) {
                        Text(key, color = Color.White, fontSize = 16.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun NotePanel(notes: List<String>, onSelect: (String) -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (notes.isEmpty()) {
            Text("Your previous notes will appear here.", color = MutedText)
        } else {
            notes.take(8).forEach { note ->
                TextButton(onClick = { onSelect(note) }) {
                    Text(note, color = Color.White)
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
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (isSelected) activeColor else Color.White,
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

    ModalBottomSheet(
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
                color = Color.White,
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
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
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
                        color = if (parentId == null) GreenAccent else Color.White
                    )
                }
                categories
                    .filter { it.parentId == null && it.id != initial?.id }
                    .forEach { category ->
                        TextButton(onClick = { parentId = category.id }) {
                            Text(
                                if (parentId == category.id) "${category.name} ✓" else category.name,
                                color = if (parentId == category.id) GreenAccent else Color.White
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
                colors = ButtonDefaults.buttonColors(containerColor = GreenAccent),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Save Category", color = Color.Black, fontWeight = FontWeight.Bold)
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
    ModalBottomSheet(
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
            Text("Manage Categories", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Button(
                onClick = add,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = GreenAccent),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("+ Add category or subcategory", color = Color.Black, fontWeight = FontWeight.Bold)
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
                            color = Color.White,
                            fontWeight = if (category.parentId == null) FontWeight.Bold else FontWeight.Normal
                        )
                        TextButton(onClick = { edit(category) }) {
                            Text("Edit", color = BlueAccent)
                        }
                        TextButton(onClick = { delete(category) }) {
                            Text("Delete", color = RedAccent)
                        }
                    }
                    HorizontalDivider(color = LineColor.copy(alpha = 0.5f), thickness = 0.5.dp)
                }
            }

            TextButton(onClick = dismiss, modifier = Modifier.fillMaxWidth()) {
                Text("Done", color = Color.White)
            }
        }
    }
}
