package com.example.walletmanager.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.walletmanager.data.model.Account
import com.example.walletmanager.data.model.Category
import com.example.walletmanager.data.model.Currency
import com.example.walletmanager.data.model.FixedDeposit
import com.example.walletmanager.data.model.Holding
import com.example.walletmanager.data.model.InvestmentType
import com.example.walletmanager.data.model.PendingIpo
import com.example.walletmanager.data.model.RecurringPayment
import com.example.walletmanager.data.model.Transaction
import com.example.walletmanager.data.model.TransactionKind
import com.example.walletmanager.data.model.WalletUiState
import com.example.walletmanager.data.repository.WalletRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

class WalletViewModel(private val repository: WalletRepository) : ViewModel() {

    private val mutableState = MutableStateFlow(WalletUiState())
    val state: StateFlow<WalletUiState> = mutableState.asStateFlow()
    private val mutableEvents = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val events: SharedFlow<String> = mutableEvents.asSharedFlow()

    init {
        viewModelScope.launch {
            repository.seedIfEmpty()
            repository.processDuePayments(LocalDate.now())
            repository.observeState().collect { stored ->
                mutableState.value = stored.copy(
                    route = mutableState.value.route,
                    balancesVisible = mutableState.value.balancesVisible,
                    filterAccountId = mutableState.value.filterAccountId,
                    filterCategory = mutableState.value.filterCategory,
                    filterKind = mutableState.value.filterKind
                )
            }
        }
    }

    fun navigate(route: String) {
        mutableState.value = mutableState.value.copy(
            route = route,
            message = null
        )
    }

    fun toggleBalances() {
        mutableState.value = mutableState.value.copy(
            balancesVisible = !mutableState.value.balancesVisible
        )
    }

    fun setTransactionFilters(
        accountId: String?,
        category: String?,
        kind: TransactionKind?
    ) {
        mutableState.value = mutableState.value.copy(
            filterAccountId = accountId,
            filterCategory = category,
            filterKind = kind
        )
    }

    fun clearTransactionFilters() {
        setTransactionFilters(null, null, null)
    }

    fun notify(message: String) {
        mutableEvents.tryEmit(message)
    }

    private fun launchAction(action: suspend () -> Unit) = viewModelScope.launch {
        try {
            action()
        } catch (error: Exception) {
            notify(error.message ?: "Something went wrong")
        }
    }

    fun setCurrency(currency: Currency) = launchAction {
        repository.setCurrency(currency)
    }

    fun setProfile(name: String, imageUri: String) = launchAction {
        repository.setProfile(name, imageUri)
    }

    fun wipeData() = launchAction {
        repository.wipeData()
    }

    fun addSampleData() = launchAction {
        val added = repository.addSampleData()
        notify(
            if (added) "Sample data added. Demo entries are labeled in the app."
            else "Sample data is already loaded. Wipe data to reset it."
        )
    }

    fun addAccount(name: String, type: String) = launchAction {
        repository.addAccount(name, type)
    }

    fun addBrokeragePlatform(name: String, openingBalanceMinor: Long) = launchAction {
        repository.addAccount(name, "Brokerage", openingBalanceMinor)
    }

    fun editAccount(account: Account) = launchAction {
        repository.editAccount(account)
    }

    fun deleteAccount(account: Account) = launchAction {
        repository.deleteAccount(account)
    }

    fun addNote(note: String) = launchAction {
        repository.addNote(note)
    }

    fun addCategory(name: String, parentId: String?) = launchAction {
        repository.addCategory(name, parentId)
    }

    fun editCategory(category: Category) = launchAction {
        repository.editCategory(category)
    }

    fun deleteCategory(category: Category) = launchAction {
        repository.deleteCategory(category)
    }

    fun importCsv(csv: String) = launchAction {
        repository.importCsv(csv)
        notify("CSV imported")
    }

    fun addTransaction(
        title: String,
        amountMinor: Long,
        kind: TransactionKind,
        category: String,
        accountId: String,
        note: String = "",
        cashbackMinor: Long = 0
    ) = launchAction {
        repository.addTransaction(title, amountMinor, kind, category, accountId, note, cashbackMinor)
    }

    fun transfer(
        title: String,
        amountMinor: Long,
        sourceAccountId: String,
        destinationAccountId: String,
        note: String,
        feeMinor: Long
    ) = launchAction {
        repository.transfer(title, amountMinor, sourceAccountId, destinationAccountId, note, feeMinor)
    }

    fun editTransaction(transaction: Transaction) = launchAction {
        repository.editTransaction(transaction)
    }

    fun deleteTransaction(transaction: Transaction) = launchAction {
        repository.deleteTransaction(transaction)
    }

    fun addRecurringPayment(
        name: String,
        amountMinor: Long,
        frequency: String,
        cashbackMinor: Long = 0,
        accountId: String,
        category: String = "Bills",
        dueDay: Int = 5,
        investmentType: InvestmentType = InvestmentType.MUTUAL_FUND,
        investmentHoldingId: String? = null,
        investmentPlatformAccountId: String? = null
    ) = launchAction {
        repository.addRecurringPayment(
            name,
            amountMinor,
            frequency,
            accountId,
            category,
            dueDay,
            cashbackMinor,
            investmentType,
            investmentHoldingId,
            investmentPlatformAccountId
        )
    }

    fun editRecurringPayment(payment: RecurringPayment) = launchAction {
        repository.editRecurringPayment(payment)
    }

    fun toggleRecurringPayment(payment: RecurringPayment) = launchAction {
        repository.toggleRecurringPayment(payment)
    }

    fun deleteRecurringPayment(id: String) = launchAction {
        repository.deleteRecurringPayment(id)
    }

    fun addHolding(
        name: String,
        type: InvestmentType,
        investedMinor: Long,
        platformAccountId: String? = null
    ) = launchAction {
        repository.addHolding(name, type, "", investedMinor, investedMinor, platformAccountId)
    }

    fun addIpo(ipo: PendingIpo) = launchAction {
        repository.addIpo(ipo)
    }

    fun confirmIpo(id: String, type: InvestmentType) = launchAction {
        repository.confirmIpo(id, type)
    }

    fun rejectIpo(id: String) = launchAction {
        repository.rejectIpo(id)
    }

    fun addFixedDeposit(deposit: FixedDeposit) = launchAction {
        repository.addFixedDeposit(deposit)
    }

    fun editFixedDeposit(deposit: FixedDeposit) = launchAction {
        repository.editFixedDeposit(deposit)
    }

    fun deleteFixedDeposit(deposit: FixedDeposit) = launchAction {
        repository.deleteFixedDeposit(deposit)
    }

    fun editHolding(holding: Holding) = launchAction {
        repository.editHolding(holding)
    }

    fun exitHolding(holdingId: String, proceedsMinor: Long, destinationAccountId: String) = launchAction {
        repository.exitHolding(holdingId, proceedsMinor, destinationAccountId)
    }
}
