package com.example.walletmanager.data.model

import java.time.YearMonth
import java.time.ZoneId

enum class Currency(
    val code: String,
    val symbol: String,
    val inrRate: Double
) {
    INR("INR", "₹", 1.0),
    USD("USD", "$", 0.012),
    EUR("EUR", "€", 0.011),
    GBP("GBP", "£", 0.0095)
}

enum class TransactionKind {
    INCOME,
    EXPENSE
}

const val OTHER_INVESTMENT_PLATFORM_ID = "other"

enum class AccountAccent {
    Green,
    Purple,
    Amber,
    Pink,
    Blue
}

enum class InvestmentType {
    STOCK,
    EQUITY,
    MUTUAL_FUND,
    INDEX_FUND,
    SIP,
    INSURANCE,
    GOLD,
    OTHER;

    fun displayCategory(): InvestmentType = when (this) {
        EQUITY -> STOCK
        SIP -> MUTUAL_FUND
        GOLD, INSURANCE -> OTHER
        else -> this
    }

    companion object {
        val categories = listOf(STOCK, MUTUAL_FUND, INDEX_FUND, OTHER)
    }
}

data class Account(
    val id: String,
    val name: String,
    val type: String,
    val balanceMinor: Long,
    val accent: AccountAccent
)

data class Category(
    val id: String,
    val name: String,
    val parentId: String? = null
) {
    val displayName: String
        get() = name
}

data class Transaction(
    val id: String,
    val title: String,
    val category: String,
    val amountMinor: Long,
    val kind: TransactionKind,
    val accountId: String,
    val date: String,
    val weekOfMonth: Int = 1,
    val note: String = "",
    val cashbackMinor: Long = 0,
    val createdAt: Long = 0
)

data class RecurringPayment(
    val id: String,
    val name: String,
    val amountMinor: Long,
    val frequency: String,
    val due: String,
    val accountId: String = "bank",
    val category: String = "Bills",
    val active: Boolean = true,
    val dueDay: Int = 5,
    val lastProcessedPeriod: String = "",
    val cashbackMinor: Long = 0,
    val investmentType: InvestmentType = InvestmentType.MUTUAL_FUND,
    val investmentHoldingId: String? = null,
    val investmentPlatformAccountId: String? = null
)

data class Holding(
    val id: String,
    val name: String,
    val type: InvestmentType,
    val units: String,
    val valueMinor: Long,
    val costMinor: Long,
    val recurringPaymentId: String? = null,
    val platformAccountId: String? = null
)

data class PendingIpo(
    val id: String,
    val name: String,
    val amountMinor: Long,
    val allotmentDate: String,
    val allotmentDateMillis: Long
)

data class FixedDeposit(
    val id: String,
    val name: String,
    val principalMinor: Long,
    val interestRateBps: Int,
    val maturityDate: String,
    val maturityDateMillis: Long,
    val maturityAmountMinor: Long,
    val accountId: String = "",
    val startDate: String = ""
)

fun calculateFixedDepositMaturity(
    principalMinor: Long,
    interestRateBps: Int,
    maturityDate: String,
    today: String = java.time.LocalDate.now().toString()
): Long {
    val days = runCatching {
        java.time.temporal.ChronoUnit.DAYS.between(
            java.time.LocalDate.parse(today),
            java.time.LocalDate.parse(maturityDate)
        ).coerceAtLeast(0)
    }.getOrDefault(0)
    val interest = principalMinor * interestRateBps * days / (10000L * 365L)
    return principalMinor + interest
}

data class WalletUiState(
    val route: String = "home",
    val baseCurrency: Currency = Currency.INR,
    val balancesVisible: Boolean = true,
    val accounts: List<Account> = emptyList(),
    val transactions: List<Transaction> = emptyList(),
    val recurringPayments: List<RecurringPayment> = emptyList(),
    val holdings: List<Holding> = emptyList(),
    val pendingIpos: List<PendingIpo> = emptyList(),
    val fixedDeposits: List<FixedDeposit> = emptyList(),
    val notes: List<String> = emptyList(),
    val categories: List<Category> = emptyList(),
    val profileName: String = "Alex",
    val profileImageUri: String = "",
    val filterAccountId: String? = null,
    val filterCategory: String? = null,
    val filterKind: TransactionKind? = null,
    val message: String? = null
) {
    val balanceMinor: Long
        get() = accounts.sumOf { it.balanceMinor }

    val incomeMinor: Long
        get() = transactions.filter { it.kind == TransactionKind.INCOME }.sumOf { it.amountMinor }

    val expenseMinor: Long
        get() = transactions.filter { it.kind == TransactionKind.EXPENSE }.sumOf { it.amountMinor }

    val monthlyIncomeMinor: Long
        get() = transactions
            .filter { it.createdAt >= currentMonthStart() && it.kind == TransactionKind.INCOME }
            .sumOf { it.amountMinor }

    val monthlyExpenseMinor: Long
        get() = transactions
            .filter { it.createdAt >= currentMonthStart() && it.kind == TransactionKind.EXPENSE }
            .sumOf { it.amountMinor }

    val monthlyNetMinor: Long
        get() = monthlyIncomeMinor - monthlyExpenseMinor

    val portfolioMinor: Long
        get() = holdings.sumOf { it.costMinor }

    val gainMinor: Long
        get() = holdings.sumOf { it.valueMinor - it.costMinor }

    private fun currentMonthStart(): Long = YearMonth
        .now()
        .atDay(1)
        .atStartOfDay(ZoneId.systemDefault())
        .toInstant()
        .toEpochMilli()
}
