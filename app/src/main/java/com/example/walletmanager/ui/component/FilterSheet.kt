package com.example.walletmanager.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.walletmanager.data.model.Account
import com.example.walletmanager.data.model.Transaction
import com.example.walletmanager.data.model.TransactionKind
import com.example.walletmanager.data.model.WalletUiState
import java.util.Locale
import java.time.LocalDate
import java.time.YearMonth

private val HeaderBg = Color(0xFF2B3046)
private val TopDonutBg = Color(0xFF1E202B)
private val FilterContentBg = Color(0xFF181A22)
private val IncomeBlue = Color(0xFF2196F3)
private val ExpenseRed = Color(0xFFE53935)
private val TabUnderlineColor = Color(0xFFEF5350)
private val LineColor = Color(0xFF2C2F3D)
private val TextMuted = Color(0xFF9E9E9E)

enum class FilterTab {
    INCOME,
    EXPENSES,
    ACCOUNT
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterSheet(
    state: WalletUiState,
    selectedFilterName: String,
    dismiss: () -> Unit,
    onApplyFilter: (String) -> Unit
) {
    var activeTab by rememberSaveable { mutableStateOf(FilterTab.ACCOUNT) }
    var selectedItems by remember {
        mutableStateOf(
            if (selectedFilterName == "All") emptySet<String>() else setOf(selectedFilterName)
        )
    }
    var selectedMonthValue by rememberSaveable {
        mutableStateOf(YearMonth.now().toString())
    }
    val selectedMonth = remember(selectedMonthValue) {
        YearMonth.parse(selectedMonthValue)
    }
    val monthTransactions = remember(state.transactions, selectedMonthValue) {
        state.transactions.filter { transaction ->
            runCatching {
                YearMonth.from(
                    LocalDate.parse(transaction.date.substringBefore(" "))
                )
            }.getOrNull() == selectedMonth
        }
    }

    // Dynamic Calculations directly from state (Single Source of Truth)
    val totalIncome = remember(monthTransactions) {
        monthTransactions.filter { it.kind == TransactionKind.INCOME }.sumOf { it.amountMinor }
    }
    val totalExpense = remember(monthTransactions) {
        monthTransactions.filter { it.kind == TransactionKind.EXPENSE }.sumOf { it.amountMinor }
    }
    val netTotal = totalIncome - totalExpense

    val incomePercent = if (totalIncome + totalExpense > 0) {
        (totalIncome.toFloat() / (totalIncome + totalExpense) * 100).toInt()
    } else 0
    val expensePercent = if (totalIncome + totalExpense > 0) {
        (totalExpense.toFloat() / (totalIncome + totalExpense) * 100).toInt()
    } else if (totalExpense > 0) 100 else 0
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = dismiss,
        contentWindowInsets = { WindowInsets(0, 0, 0, 0) },
        dragHandle = { WalletSheetBackButton(dismiss) },
        sheetState = sheetState,
        containerColor = FilterContentBg,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .background(FilterContentBg)
        ) {
            // 1. Top Header Bar (< Sept 2026 > ... X)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(HeaderBg)
                    .padding(vertical = 8.dp, horizontal = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = {
                                selectedMonthValue = selectedMonth.minusMonths(1).toString()
                            }
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                contentDescription = "Previous Month",
                                tint = Color.White
                            )
                        }
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = selectedMonth.month.name
                                .lowercase(Locale.getDefault())
                                .replaceFirstChar { it.uppercase() } + " ${selectedMonth.year}",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                        Spacer(Modifier.width(6.dp))
                        IconButton(
                            onClick = {
                                selectedMonthValue = selectedMonth.plusMonths(1).toString()
                            }
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = "Next Month",
                                tint = Color.White
                            )
                        }
                    }

                    IconButton(onClick = dismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White
                        )
                    }
                }

                Text(
                    text = "Select items that you want to filter.",
                    color = TextMuted,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            // 2. Donut Charts Summary Section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(TopDonutBg)
                    .padding(vertical = 18.dp, horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Income Donut
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Income", color = TextMuted, fontSize = 13.sp)
                    Spacer(Modifier.height(8.dp))
                    DonutRing(
                        color = IncomeBlue,
                        percentage = incomePercent,
                        text = "$incomePercent%"
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = formatMoney(totalIncome, state.balancesVisible, state.baseCurrency),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                // Expenses Donut
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Expenses", color = TextMuted, fontSize = 13.sp)
                    Spacer(Modifier.height(8.dp))
                    DonutRing(
                        color = ExpenseRed,
                        percentage = expensePercent,
                        text = "$expensePercent%"
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = formatMoney(totalExpense, state.balancesVisible, state.baseCurrency),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                // Total Calculation
                Column(horizontalAlignment = Alignment.End) {
                    Text("Total", color = TextMuted, fontSize = 13.sp)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = (if (netTotal < 0) "- " else "") + formatMoney(Math.abs(netTotal), state.balancesVisible, state.baseCurrency),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )
                }
            }

            // 3. Middle Filter Tabs (INCOME, EXPENSES, ACCOUNT)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(TopDonutBg)
            ) {
                FilterTabItem(
                    title = "INCOME",
                    isActive = activeTab == FilterTab.INCOME,
                    onClick = { activeTab = FilterTab.INCOME },
                    modifier = Modifier.weight(1f)
                )
                FilterTabItem(
                    title = "EXPENSES",
                    isActive = activeTab == FilterTab.EXPENSES,
                    onClick = { activeTab = FilterTab.EXPENSES },
                    modifier = Modifier.weight(1f)
                )
                FilterTabItem(
                    title = "ACCOUNT",
                    isActive = activeTab == FilterTab.ACCOUNT,
                    onClick = { activeTab = FilterTab.ACCOUNT },
                    modifier = Modifier.weight(1f)
                )
            }

            HorizontalDivider(color = LineColor, thickness = 1.dp)

            // 4. Sub-header Table Bar ([ ] All | Income/Expenses/Transfer-In/Out)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(FilterContentBg)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = selectedItems.isEmpty(),
                        onCheckedChange = { isChecked ->
                            if (isChecked) {
                                selectedItems = emptySet()
                                onApplyFilter("All")
                            }
                        },
                        colors = CheckboxDefaults.colors(uncheckedColor = TextMuted, checkedColor = IncomeBlue)
                    )
                    Text("All", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                }

                if (activeTab == FilterTab.ACCOUNT) {
                    Column(horizontalAlignment = Alignment.End) {
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Text("Income", color = IncomeBlue, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            Text("Expenses", color = ExpenseRed, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Text("Transfer-In", color = TextMuted, fontSize = 11.sp)
                            Text("Transfer-Out", color = TextMuted, fontSize = 11.sp)
                        }
                    }
                }
            }

            HorizontalDivider(color = LineColor, thickness = 0.5.dp)

            // 5. Categorized List Content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
            ) {
                when (activeTab) {
                    FilterTab.ACCOUNT -> {
                        // Group Accounts by Type (Cash, Bank Accounts, Cards, Pay)
                        val groupedAccounts = remember(state.accounts) {
                            state.accounts.groupBy { it.type }
                        }

                        groupedAccounts.forEach { (typeGroup, accounts) ->
                            Text(
                                text = typeGroup,
                                color = TextMuted,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(top = 14.dp, bottom = 6.dp)
                            )

                            accounts.forEach { acc ->
                                val accIncome = remember(monthTransactions, acc.id) {
                                    monthTransactions
                                        .filter { it.accountId == acc.id && it.kind == TransactionKind.INCOME }
                                        .sumOf { it.amountMinor }
                                }
                                val accExpense = remember(monthTransactions, acc.id) {
                                    monthTransactions
                                        .filter { it.accountId == acc.id && it.kind == TransactionKind.EXPENSE }
                                        .sumOf { it.amountMinor }
                                }

                                val isChecked = selectedItems.contains(acc.name) || selectedItems.contains(acc.id)

                                AccountFilterRow(
                                    account = acc,
                                    incomeMinor = accIncome,
                                    expenseMinor = accExpense,
                                    isChecked = isChecked,
                                    state = state,
                                    onToggle = {
                                        selectedItems = if (isChecked) {
                                            selectedItems - acc.name - acc.id
                                        } else {
                                            selectedItems + acc.name
                                        }
                                        onApplyFilter(if (selectedItems.isEmpty()) "All" else selectedItems.first())
                                    }
                                )
                                HorizontalDivider(color = LineColor.copy(alpha = 0.5f), thickness = 0.5.dp)
                            }
                        }
                    }

                    FilterTab.EXPENSES -> {
                        val expenseTransactions = remember(monthTransactions) {
                            monthTransactions.filter { it.kind == TransactionKind.EXPENSE }
                        }
                        val categoryTotals = remember(expenseTransactions) {
                            expenseTransactions.groupBy { it.category }
                                .mapValues { entry -> entry.value.sumOf { it.amountMinor } }
                        }

                        state.categories.filter { it.parentId == null }.forEach { parent ->
                            val parentTotal = categoryTotals[parent.name] ?: 0L
                            val isChecked = selectedItems.contains(parent.name)

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onApplyFilter(parent.name)
                                        dismiss()
                                    }
                                    .padding(vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(
                                        checked = isChecked,
                                        onCheckedChange = {
                                            onApplyFilter(parent.name)
                                            dismiss()
                                        },
                                        colors = CheckboxDefaults.colors(uncheckedColor = TextMuted, checkedColor = ExpenseRed)
                                    )
                                    Text(parent.name, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                                }
                                Text(
                                    text = formatMoney(parentTotal, state.balancesVisible, state.baseCurrency),
                                    color = ExpenseRed,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                            HorizontalDivider(color = LineColor.copy(alpha = 0.4f), thickness = 0.5.dp)
                        }
                    }

                    FilterTab.INCOME -> {
                        val incomeTransactions = remember(monthTransactions) {
                            monthTransactions.filter { it.kind == TransactionKind.INCOME }
                        }
                        val categoryTotals = remember(incomeTransactions) {
                            incomeTransactions.groupBy { it.category }
                                .mapValues { entry -> entry.value.sumOf { it.amountMinor } }
                        }

                        listOf("Salary", "Business", "Investments", "Bonus", "Cashback", "Income").forEach { catName ->
                            val catTotal = categoryTotals[catName] ?: 0L
                            val isChecked = selectedItems.contains(catName)

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onApplyFilter(catName)
                                        dismiss()
                                    }
                                    .padding(vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(
                                        checked = isChecked,
                                        onCheckedChange = {
                                            onApplyFilter(catName)
                                            dismiss()
                                        },
                                        colors = CheckboxDefaults.colors(uncheckedColor = TextMuted, checkedColor = IncomeBlue)
                                    )
                                    Text(catName, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                                }
                                Text(
                                    text = formatMoney(catTotal, state.balancesVisible, state.baseCurrency),
                                    color = IncomeBlue,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                            HorizontalDivider(color = LineColor.copy(alpha = 0.4f), thickness = 0.5.dp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DonutRing(
    color: Color,
    percentage: Int,
    text: String
) {
    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(100.dp)) {
        Canvas(modifier = Modifier.size(90.dp)) {
            val strokeWidth = 14.dp.toPx()
            // Track background ring
            drawArc(
                color = color.copy(alpha = 0.2f),
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = strokeWidth)
            )
            // Progress arc
            val sweep = (percentage / 100f) * 360f
            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = sweep,
                useCenter = false,
                style = Stroke(width = strokeWidth)
            )
        }
        Text(
            text = text,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
    }
}

@Composable
private fun FilterTabItem(
    title: String,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            color = if (isActive) Color.White else TextMuted,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
            fontSize = 14.sp
        )
        Spacer(Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.5.dp)
                .background(if (isActive) TabUnderlineColor else Color.Transparent)
        )
    }
}

@Composable
private fun AccountFilterRow(
    account: Account,
    incomeMinor: Long,
    expenseMinor: Long,
    isChecked: Boolean,
    state: WalletUiState,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = isChecked,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(uncheckedColor = TextMuted, checkedColor = IncomeBlue)
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = account.name,
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Column(horizontalAlignment = Alignment.End) {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = formatMoney(incomeMinor, state.balancesVisible, state.baseCurrency),
                    color = IncomeBlue,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = formatMoney(expenseMinor, state.balancesVisible, state.baseCurrency),
                    color = ExpenseRed,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = formatMoney(0L, state.balancesVisible, state.baseCurrency),
                    color = TextMuted,
                    fontSize = 11.sp
                )
                Text(
                    text = formatMoney(0L, state.balancesVisible, state.baseCurrency),
                    color = TextMuted,
                    fontSize = 11.sp
                )
            }
        }
    }
}
