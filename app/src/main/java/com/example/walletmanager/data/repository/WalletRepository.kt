package com.example.walletmanager.data.repository

import androidx.room.withTransaction
import com.example.walletmanager.data.local.AccountEntity
import com.example.walletmanager.data.local.CategoryEntity
import com.example.walletmanager.data.local.HoldingEntity
import com.example.walletmanager.data.local.IpoEntity
import com.example.walletmanager.data.local.NoteEntity
import com.example.walletmanager.data.local.RecurringPaymentEntity
import com.example.walletmanager.data.local.SettingsEntity
import com.example.walletmanager.data.local.TransactionEntity
import com.example.walletmanager.data.local.WalletDatabase
import com.example.walletmanager.data.model.Account
import com.example.walletmanager.data.model.AccountAccent
import com.example.walletmanager.data.model.Currency
import com.example.walletmanager.data.model.Category
import com.example.walletmanager.data.model.Holding
import com.example.walletmanager.data.model.InvestmentType
import com.example.walletmanager.data.model.PendingIpo
import com.example.walletmanager.data.model.RecurringPayment
import com.example.walletmanager.data.model.Transaction
import com.example.walletmanager.data.model.TransactionKind
import com.example.walletmanager.data.model.WalletUiState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.temporal.IsoFields

class WalletRepository(private val database: WalletDatabase) {
    private val dao = database.walletDao()

    fun observeState(): Flow<WalletUiState> = combine(
        combine(
            dao.accounts(),
            dao.transactions(),
            dao.recurringPayments()
        ) { accounts, transactions, plans ->
            Triple(accounts, transactions, plans)
        },
        combine(
            combine(
                dao.holdings(),
                dao.notes(),
                dao.settings()
            ) { holdings, notes, settings ->
                Triple(holdings, notes, settings)
            },
            dao.categories(),
            dao.ipos()
        ) { stored, categories, ipos ->
            Triple(stored, categories, ipos)
        }
    ) { first, second ->
        val accounts = first.first
        val transactions = first.second
        val plans = first.third
        val holdings = second.first.first
        val notes = second.first.second
        val settings = second.first.third
        val categories = second.second
        val ipos = second.third

        WalletUiState(
            accounts = accounts.map {
                Account(
                    id = it.id,
                    name = it.name,
                    type = it.type,
                    balanceMinor = it.balanceMinor,
                    accent = runCatching { AccountAccent.valueOf(it.accent) }.getOrDefault(AccountAccent.Green)
                )
            },
            transactions = transactions.map {
                Transaction(
                    id = it.id,
                    title = it.title,
                    category = it.category,
                    amountMinor = it.amountMinor,
                    kind = runCatching { TransactionKind.valueOf(it.kind) }.getOrDefault(TransactionKind.EXPENSE),
                    accountId = it.accountId,
                    date = it.date,
                    weekOfMonth = it.weekOfMonth,
                    note = it.note,
                    cashbackMinor = it.cashbackMinor,
                    createdAt = it.createdAt
                )
            },
            recurringPayments = plans.map {
                RecurringPayment(
                    id = it.id,
                    name = it.name,
                    amountMinor = it.amountMinor,
                    frequency = it.frequency,
                    due = it.due,
                    accountId = it.accountId,
                    category = it.category,
                    active = it.active,
                    dueDay = it.dueDay,
                    lastProcessedPeriod = it.lastProcessedPeriod,
                    cashbackMinor = it.cashbackMinor,
                    investmentType = runCatching { InvestmentType.valueOf(it.investmentType) }
                        .getOrDefault(InvestmentType.MUTUAL_FUND)
                )
            },
            holdings = holdings.map {
                Holding(
                    id = it.id,
                    name = it.name,
                    type = runCatching { InvestmentType.valueOf(it.type) }.getOrDefault(InvestmentType.MUTUAL_FUND),
                    units = it.units,
                    valueMinor = it.valueMinor,
                    costMinor = it.costMinor,
                    recurringPaymentId = it.recurringPaymentId
                )
            },
            pendingIpos = ipos.map {
                PendingIpo(
                    id = it.id,
                    name = it.name,
                    amountMinor = it.amountMinor,
                    allotmentDate = it.allotmentDate,
                    allotmentDateMillis = it.allotmentDateMillis
                )
            },
            notes = notes.map { it.text },
            categories = categories.map { Category(it.id, it.name, it.parentId) },
            baseCurrency = settings?.baseCurrency?.let { code -> Currency.entries.firstOrNull { it.code == code } } ?: Currency.INR,
            profileName = settings?.profileName?.ifBlank { "Alex" } ?: "Alex",
            profileImageUri = settings?.profileImageUri.orEmpty()
        )
    }

    suspend fun seedIfEmpty() {
        if (dao.accountCount() == 0) {
            dao.insertSettings(SettingsEntity())
        }

        dao.insertCategories(
            listOf(
                CategoryEntity("food", "Food"),
                CategoryEntity("food-lunch", "Lunch", "food"),
                CategoryEntity("food-dinner", "Dinner", "food"),
                CategoryEntity("food-cafe", "Cafe & Coffee", "food"),
                CategoryEntity("food-snacks", "Snacks", "food"),
                CategoryEntity("food-restaurant", "Restaurants", "food"),

                CategoryEntity("social", "Social Life"),
                CategoryEntity("social-outing", "Outings", "social"),
                CategoryEntity("social-party", "Parties", "social"),
                CategoryEntity("social-drinks", "Drinks & Cafe", "social"),

                CategoryEntity("pets", "Pets"),
                CategoryEntity("pets-food", "Pet Food", "pets"),
                CategoryEntity("pets-vet", "Vet & Care", "pets"),

                CategoryEntity("transport", "Transport"),
                CategoryEntity("transport-fuel", "Fuel & Petrol", "transport"),
                CategoryEntity("transport-cab", "Taxi / Cab", "transport"),
                CategoryEntity("transport-transit", "Public Transit", "transport"),

                CategoryEntity("culture", "Culture"),
                CategoryEntity("culture-movies", "Movies & Events", "culture"),
                CategoryEntity("culture-books", "Books & Shows", "culture"),

                CategoryEntity("household", "Household"),
                CategoryEntity("household-groceries", "Groceries", "household"),
                CategoryEntity("household-repairs", "Repairs & Maintenance", "household"),
                CategoryEntity("household-supplies", "Home Supplies", "household"),

                CategoryEntity("shopping", "Shopping"),
                CategoryEntity("shopping-clothes", "Clothing", "shopping"),
                CategoryEntity("shopping-electronics", "Electronics", "shopping"),
                CategoryEntity("shopping-personal", "Personal Care", "shopping"),

                CategoryEntity("health", "Health"),
                CategoryEntity("health-medicines", "Medicines", "health"),
                CategoryEntity("health-doctor", "Doctor & Hospital", "health"),

                CategoryEntity("bills", "Bills"),
                CategoryEntity("bills-electricity", "Electricity", "bills"),
                CategoryEntity("bills-water", "Water & Gas", "bills"),
                CategoryEntity("bills-internet", "Internet & Phone", "bills"),

                CategoryEntity("investments", "Investments"),
                CategoryEntity("investments-sip", "SIP / Funds", "investments"),
                CategoryEntity("investments-stocks", "Stocks", "investments"),

                CategoryEntity("income", "Income"),
                CategoryEntity("income-salary", "Salary", "income"),
                CategoryEntity("income-allowance", "Allowance", "income"),
                CategoryEntity("income-bonus", "Bonus", "income"),
                CategoryEntity("income-cashback", "Cashback", "income"),

                CategoryEntity("other", "Other")
            )
        )
    }

    suspend fun setCurrency(currency: Currency) {
        val settings = dao.settings().first()
        dao.insertSettings(
            SettingsEntity(
                baseCurrency = currency.code,
                profileName = settings?.profileName ?: "Alex",
                profileImageUri = settings?.profileImageUri.orEmpty()
            )
        )
    }

    suspend fun setProfile(name: String, imageUri: String) {
        val settings = dao.settings().first()
        dao.insertSettings(
            SettingsEntity(
                baseCurrency = settings?.baseCurrency ?: "INR",
                profileName = name.ifBlank { "Alex" },
                profileImageUri = imageUri
            )
        )
    }

    suspend fun addAccount(name: String, type: String) {
        if (name.isBlank()) return

        dao.insertAccount(
            AccountEntity(
                id = "account-${System.currentTimeMillis()}",
                name = name,
                type = type,
                balanceMinor = 0,
                accent = AccountAccent.Green.name
            )
        )
    }

    suspend fun editAccount(account: Account) {
        if (account.name.isBlank()) return

        dao.insertAccount(
            AccountEntity(
                id = account.id,
                name = account.name,
                type = account.type,
                balanceMinor = account.balanceMinor,
                accent = account.accent.name
            )
        )
    }

    suspend fun deleteAccount(account: Account) {
        database.withTransaction {
            dao.deleteAccountTransactions(account.id)
            dao.deleteAccount(account.id)
        }
    }

    suspend fun addNote(note: String) {
        if (note.isNotBlank()) {
            dao.insertNotes(listOf(NoteEntity(text = note)))
        }
    }

    suspend fun addCategory(name: String, parentId: String?) {
        if (name.isBlank()) return

        dao.insertCategory(
            CategoryEntity(
                id = "category-${System.currentTimeMillis()}",
                name = name,
                parentId = parentId
            )
        )
    }

    suspend fun editCategory(category: Category) {
        if (category.name.isBlank()) return
        dao.insertCategory(
            CategoryEntity(
                id = category.id,
                name = category.name,
                parentId = category.parentId
            )
        )
    }

    suspend fun deleteCategory(category: Category) {
        database.withTransaction {
            dao.deleteSubcategories(category.id)
            dao.deleteCategory(category.id)
        }
    }

    suspend fun addTransaction(
        title: String,
        amountMinor: Long,
        kind: TransactionKind,
        category: String,
        accountId: String,
        note: String = "",
        cashbackMinor: Long = 0
    ) {
        if (amountMinor <= 0 || dao.account(accountId) == null) return
        val delta = if (kind == TransactionKind.EXPENSE) -amountMinor else amountMinor

        val transactionId = "t${System.currentTimeMillis()}"
        val cashback = cashbackMinor.coerceAtLeast(0)

        database.withTransaction {
            dao.insertTransaction(
                TransactionEntity(
                    id = transactionId,
                    title = title.ifBlank { category },
                    category = category,
                    amountMinor = amountMinor,
                    kind = kind.name,
                    accountId = accountId,
                    date = "Just now",
                    weekOfMonth = 4,
                    note = note,
                    cashbackMinor = cashback,
                    createdAt = System.currentTimeMillis()
                )
            )
            dao.adjustBalance(accountId, delta)
            if (cashback > 0) {
                dao.insertTransaction(
                    TransactionEntity(
                        id = "cashback-$transactionId",
                        title = "Cashback · ${title.ifBlank { category }}",
                        category = "Cashback",
                        amountMinor = cashback,
                        kind = TransactionKind.INCOME.name,
                        accountId = accountId,
                        date = "Just now",
                        weekOfMonth = 4,
                        note = "Automatically added cashback",
                        createdAt = System.currentTimeMillis()
                    )
                )
                dao.adjustBalance(accountId, cashback)
            }
        }
    }

    suspend fun transfer(
        title: String,
        amountMinor: Long,
        sourceAccountId: String,
        destinationAccountId: String,
        note: String
    ) {
        if (
            amountMinor <= 0 ||
            sourceAccountId == destinationAccountId ||
            dao.account(sourceAccountId) == null ||
            dao.account(destinationAccountId) == null
        ) return

        val timestamp = System.currentTimeMillis()
        database.withTransaction {
            dao.insertTransaction(
                TransactionEntity(
                    id = "transfer-out-$timestamp",
                    title = title.ifBlank { "Transfer" },
                    category = "Transfer",
                    amountMinor = amountMinor,
                    kind = TransactionKind.EXPENSE.name,
                    accountId = sourceAccountId,
                    date = "Just now",
                    weekOfMonth = 4,
                    note = note,
                    createdAt = timestamp
                )
            )
            dao.insertTransaction(
                TransactionEntity(
                    id = "transfer-in-$timestamp",
                    title = title.ifBlank { "Transfer" },
                    category = "Transfer",
                    amountMinor = amountMinor,
                    kind = TransactionKind.INCOME.name,
                    accountId = destinationAccountId,
                    date = "Just now",
                    weekOfMonth = 4,
                    note = note,
                    createdAt = timestamp
                )
            )
            dao.adjustBalance(sourceAccountId, -amountMinor)
            dao.adjustBalance(destinationAccountId, amountMinor)
        }
    }

    suspend fun editTransaction(transaction: Transaction) {
        if (transaction.amountMinor <= 0) return
        if (dao.account(transaction.accountId) == null) return

        val previous = dao.transaction(transaction.id) ?: return
        val previousDelta = if (previous.kind == TransactionKind.EXPENSE.name) {
            -previous.amountMinor
        } else {
            previous.amountMinor
        }
        val updatedDelta = if (transaction.kind == TransactionKind.EXPENSE) {
            -transaction.amountMinor
        } else {
            transaction.amountMinor
        }

        database.withTransaction {
            dao.adjustBalance(previous.accountId, -previousDelta)
            dao.insertTransaction(
                TransactionEntity(
                    id = transaction.id,
                    title = transaction.title.ifBlank { transaction.category },
                    category = transaction.category,
                    amountMinor = transaction.amountMinor,
                    kind = transaction.kind.name,
                    accountId = transaction.accountId,
                    date = transaction.date,
                    weekOfMonth = transaction.weekOfMonth,
                    note = transaction.note,
                    cashbackMinor = transaction.cashbackMinor,
                    createdAt = transaction.createdAt
                )
            )
            dao.adjustBalance(transaction.accountId, updatedDelta)
            if (previous.cashbackMinor > 0) {
                dao.deleteTransaction("cashback-${previous.id}")
                dao.adjustBalance(previous.accountId, -previous.cashbackMinor)
            }
            if (transaction.cashbackMinor > 0) {
                dao.insertTransaction(
                    TransactionEntity(
                        id = "cashback-${transaction.id}",
                        title = "Cashback · ${transaction.title}",
                        category = "Cashback",
                        amountMinor = transaction.cashbackMinor,
                        kind = TransactionKind.INCOME.name,
                        accountId = transaction.accountId,
                        date = transaction.date,
                        weekOfMonth = transaction.weekOfMonth,
                        note = "Automatically added cashback",
                        createdAt = transaction.createdAt
                    )
                )
                dao.adjustBalance(transaction.accountId, transaction.cashbackMinor)
            }
        }
    }

    suspend fun deleteTransaction(transaction: Transaction) {
        val stored = dao.transaction(transaction.id) ?: return
        val delta = if (stored.kind == TransactionKind.EXPENSE.name) {
            -stored.amountMinor
        } else {
            stored.amountMinor
        }

        database.withTransaction {
            dao.deleteTransaction(transaction.id)
            dao.adjustBalance(stored.accountId, -delta)
            if (stored.cashbackMinor > 0) {
                dao.deleteTransaction("cashback-${stored.id}")
                dao.adjustBalance(stored.accountId, -stored.cashbackMinor)
            }
        }
    }

    suspend fun addRecurringPayment(
        name: String,
        amountMinor: Long,
        frequency: String,
        accountId: String,
        category: String,
        dueDay: Int,
        cashbackMinor: Long,
        investmentType: InvestmentType
    ) {
        if (name.isBlank() || amountMinor <= 0 || dao.account(accountId) == null) return

        val paymentId = "p${System.currentTimeMillis()}"
        dao.insertRecurringPayment(
            RecurringPaymentEntity(
                id = paymentId,
                name = name,
                amountMinor = amountMinor,
                frequency = frequency,
                due = "Day $dueDay",
                accountId = accountId,
                category = category,
                active = true,
                dueDay = dueDay,
                lastProcessedPeriod = "",
                cashbackMinor = cashbackMinor.coerceAtLeast(0),
                investmentType = investmentType.name
            )
        )
        if (category == "Investments") {
            dao.insertHolding(
                HoldingEntity(
                    id = "sip-$paymentId",
                    name = name,
                    type = investmentType.name,
                    units = "Ongoing SIP",
                    valueMinor = 0,
                    costMinor = 0,
                    recurringPaymentId = paymentId
                )
            )
        }
    }

    suspend fun editRecurringPayment(p: RecurringPayment) {
        if (p.name.isBlank() || p.amountMinor <= 0 || dao.account(p.accountId) == null) return

        dao.insertRecurringPayment(
            RecurringPaymentEntity(
                p.id,
                p.name,
                p.amountMinor,
                p.frequency,
                p.due,
                p.accountId,
                p.category,
                p.active,
                p.dueDay,
                p.lastProcessedPeriod,
                p.cashbackMinor,
                p.investmentType.name
            )
        )
        if (p.category == "Investments" && dao.holdingForRecurringPayment(p.id) == null) {
            dao.insertHolding(
                HoldingEntity(
                    id = "sip-${p.id}",
                    name = p.name,
                    type = InvestmentType.SIP.name,
                    units = "Ongoing SIP",
                    valueMinor = 0,
                    costMinor = 0,
                    recurringPaymentId = p.id
                )
            )
        }
    }

    suspend fun toggleRecurringPayment(p: RecurringPayment) = editRecurringPayment(p.copy(active = !p.active))

    suspend fun addHolding(
        name: String,
        type: InvestmentType,
        units: String,
        valueMinor: Long,
        costMinor: Long
    ) {
        if (name.isNotBlank() && valueMinor > 0) {
            dao.insertHolding(
                HoldingEntity(
                    id = "h${System.currentTimeMillis()}",
                    name = name,
                    type = type.name,
                    units = units,
                    valueMinor = valueMinor,
                    costMinor = costMinor,
                    recurringPaymentId = null
                )
            )
        }
    }

    suspend fun addIpo(ipo: PendingIpo) {
        if (ipo.name.isBlank() || ipo.amountMinor <= 0) return
        dao.insertIpo(
            IpoEntity(
                id = ipo.id,
                name = ipo.name,
                amountMinor = ipo.amountMinor,
                allotmentDate = ipo.allotmentDate,
                allotmentDateMillis = ipo.allotmentDateMillis
            )
        )
    }

    suspend fun confirmIpo(id: String, type: InvestmentType) {
        val ipo = dao.ipo(id) ?: return
        database.withTransaction {
            dao.insertHolding(
                HoldingEntity(
                    id = "holding-$id",
                    name = ipo.name,
                    type = type.name,
                    units = "IPO allotment",
                    valueMinor = ipo.amountMinor,
                    costMinor = ipo.amountMinor,
                    recurringPaymentId = null
                )
            )
            dao.deleteIpo(id)
        }
    }

    suspend fun rejectIpo(id: String) {
        dao.deleteIpo(id)
    }

    suspend fun wipeData() {
        database.withTransaction {
            dao.clearAccounts()
            dao.clearTransactions()
            dao.clearRecurringPayments()
            dao.clearHoldings()
            dao.clearIpos()
            dao.clearNotes()
            dao.clearCategories()
            dao.clearSettings()
        }
        seedIfEmpty()
    }

    suspend fun editHolding(h: Holding) = dao.insertHolding(
        HoldingEntity(
            h.id,
            h.name,
            h.type.name,
            h.units,
            h.valueMinor,
            h.costMinor,
            h.recurringPaymentId
        )
    )

    suspend fun processDuePayments(today: LocalDate) {
        val due = dao.currentRecurringPayments().filter {
            val period = when (it.frequency) {
                "Weekly" -> "${today.year}-W${today.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR)}"
                "Yearly" -> today.year.toString()
                else -> "${today.year}-${today.monthValue}"
            }
            val isDue = when (it.frequency) {
                "Weekly" -> it.dueDay <= today.dayOfWeek.value
                else -> it.dueDay <= today.dayOfMonth
            }
            it.active &&
                isDue &&
                it.lastProcessedPeriod != period &&
                dao.account(it.accountId) != null
        }
        if (due.isEmpty()) return

        database.withTransaction {
            due.forEach { p ->
                val period = when (p.frequency) {
                    "Weekly" -> "${today.year}-W${today.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR)}"
                    "Yearly" -> today.year.toString()
                    else -> "${today.year}-${today.monthValue}"
                }
                dao.insertTransaction(
                    TransactionEntity(
                        id = "auto-${p.id}-$period",
                        title = p.name,
                        category = p.category,
                        amountMinor = p.amountMinor,
                        kind = TransactionKind.EXPENSE.name,
                        accountId = p.accountId,
                        date = today.toString(),
                        weekOfMonth = 4,
                        note = "",
                        cashbackMinor = p.cashbackMinor,
                        createdAt = System.currentTimeMillis()
                    )
                )
                dao.adjustBalance(p.accountId, -p.amountMinor)
                if (p.cashbackMinor > 0) {
                    dao.insertTransaction(
                        TransactionEntity(
                            id = "cashback-auto-${p.id}-$period",
                            title = "Cashback · ${p.name}",
                            category = "Cashback",
                            amountMinor = p.cashbackMinor,
                            kind = TransactionKind.INCOME.name,
                            accountId = p.accountId,
                            date = today.toString(),
                            weekOfMonth = 4,
                            note = "Automatically added cashback",
                            createdAt = System.currentTimeMillis()
                        )
                    )
                    dao.adjustBalance(p.accountId, p.cashbackMinor)
                }
                if (p.category == "Investments") {
                    val holding = dao.holdingForRecurringPayment(p.id)
                    dao.insertHolding(
                        holding?.copy(
                            type = runCatching { InvestmentType.valueOf(p.investmentType) }
                                .getOrDefault(InvestmentType.MUTUAL_FUND).name,
                            valueMinor = holding.valueMinor + p.amountMinor,
                            costMinor = holding.costMinor + p.amountMinor
                        ) ?: HoldingEntity(
                            id = "sip-${p.id}",
                            name = p.name,
                            type = runCatching { InvestmentType.valueOf(p.investmentType) }
                                .getOrDefault(InvestmentType.MUTUAL_FUND).name,
                            units = "Ongoing SIP",
                            valueMinor = p.amountMinor,
                            costMinor = p.amountMinor,
                            recurringPaymentId = p.id
                        )
                    )
                }
                dao.insertRecurringPayment(p.copy(lastProcessedPeriod = period))
            }
        }
    }

    fun exportCsv(state: WalletUiState): String {
        val out = StringBuilder("record_type,id,name,kind,amount_minor,account_id,account_type,category,date,frequency,next_due,investment_type,units,current_value_minor,cost_minor\n")

        state.accounts.forEach {
            out.appendLine(csv(listOf("ACCOUNT", it.id, it.name, "", it.balanceMinor.toString(), "", it.type, "", "", "", "", "", "", "", "")))
        }
        state.transactions.forEach {
            out.appendLine(csv(listOf("TRANSACTION", it.id, it.title, it.kind.name, it.amountMinor.toString(), it.accountId, "", it.category, it.date, "", "", "", "", "", "")))
        }
        state.recurringPayments.forEach {
            out.appendLine(csv(listOf("RECURRING", it.id, it.name, "", it.amountMinor.toString(), it.accountId, "", it.category, "", it.frequency, it.due, "", "", "", "")))
        }
        state.holdings.forEach {
            out.appendLine(csv(listOf("INVESTMENT", it.id, it.name, "", "", "", "", "", "", "", "", it.type.name, it.units, it.valueMinor.toString(), it.costMinor.toString())))
        }

        return out.toString()
    }

    suspend fun importCsv(csv: String) {
        val rows = parseCsv(csv)
        if (rows.size < 2) return

        val header = rows.first()
        val data = rows.drop(1)
        fun value(row: List<String>, key: String) = row.getOrNull(header.indexOf(key)).orEmpty()

        val accounts = data
            .filter { value(it, "record_type") == "ACCOUNT" }
            .map {
                AccountEntity(
                    value(it, "id"),
                    value(it, "name"),
                    value(it, "account_type"),
                    value(it, "amount_minor").toLongOrNull() ?: 0,
                    AccountAccent.Green.name
                )
            }
        val transactions = data
            .filter { value(it, "record_type") == "TRANSACTION" }
            .map {
                TransactionEntity(
                    value(it, "id"),
                    value(it, "name"),
                    value(it, "category"),
                    value(it, "amount_minor").toLongOrNull() ?: 0,
                    value(it, "kind"),
                    value(it, "account_id"),
                    value(it, "date"),
                    1,
                    createdAt = System.currentTimeMillis()
                )
            }
        val plans = data
            .filter { value(it, "record_type") == "RECURRING" }
            .map {
                RecurringPaymentEntity(
                    value(it, "id"),
                    value(it, "name"),
                    value(it, "amount_minor").toLongOrNull() ?: 0,
                    value(it, "frequency"),
                    value(it, "next_due"),
                    value(it, "account_id").ifBlank { "bank" },
                    value(it, "category").ifBlank { "Bills" },
                    true,
                    5,
                    ""
                )
            }
        val holdings = data
            .filter { value(it, "record_type") == "INVESTMENT" }
            .map {
                HoldingEntity(
                    value(it, "id"),
                    value(it, "name"),
                    value(it, "investment_type").ifBlank { InvestmentType.MUTUAL_FUND.name },
                    value(it, "units"),
                    value(it, "current_value_minor").toLongOrNull() ?: 0,
                    value(it, "cost_minor").toLongOrNull() ?: 0,
                    null
                )
            }

        database.withTransaction {
            if (accounts.isNotEmpty()) {
                dao.clearAccounts()
                dao.insertAccounts(accounts)
            }
            if (transactions.isNotEmpty()) {
                dao.clearTransactions()
                dao.insertTransactions(transactions)
            }
            if (plans.isNotEmpty()) {
                dao.clearRecurringPayments()
                dao.insertRecurringPayments(plans)
            }
            if (holdings.isNotEmpty()) {
                dao.clearHoldings()
                dao.insertHoldings(holdings)
            }
        }
    }

    private fun csv(fields: List<String>) = fields.joinToString(",") {
        "\"${it.replace("\"", "\"\"")}\""
    }

    private fun parseCsv(input: String): List<List<String>> {
        val rows = mutableListOf<List<String>>()
        val row = mutableListOf<String>()
        val field = StringBuilder()
        var quoted = false
        var index = 0

        while (index < input.length) {
            when (val character = input[index]) {
                '\"' -> {
                    if (quoted && input.getOrNull(index + 1) == '\"') {
                        field.append('\"')
                        index++
                    } else {
                        quoted = !quoted
                    }
                }

                ',' -> if (quoted) {
                    field.append(character)
                } else {
                    row += field.toString().trim()
                    field.clear()
                }

                '\n' -> if (quoted) {
                    field.append(character)
                } else {
                    row += field.toString().trimEnd('\r')
                    field.clear()
                    if (row.any { it.isNotBlank() }) {
                        rows += row.toList()
                    }
                    row.clear()
                }

                else -> field.append(character)
            }
            index++
        }

        if (field.isNotEmpty() || row.isNotEmpty()) {
            row += field.toString()
            rows += row.toList()
        }

        return rows
    }
}
