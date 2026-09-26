package com.example.walletmanager.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.walletmanager.data.model.Account
import com.example.walletmanager.data.model.Category
import com.example.walletmanager.data.model.Currency
import com.example.walletmanager.data.model.Holding
import com.example.walletmanager.data.model.InvestmentType
import com.example.walletmanager.data.model.PendingIpo
import com.example.walletmanager.data.model.RecurringPayment
import com.example.walletmanager.data.model.Transaction
import com.example.walletmanager.data.model.TransactionKind
import com.example.walletmanager.data.model.WalletUiState
import com.example.walletmanager.data.repository.WalletRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

class WalletViewModel(private val repository: WalletRepository) : ViewModel() {

    private val mutableState = MutableStateFlow(WalletUiState())
    val state: StateFlow<WalletUiState> = mutableState.asStateFlow()

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

    fun setCurrency(currency: Currency) = viewModelScope.launch {
        repository.setCurrency(currency)
    }

    fun setProfile(name: String, imageUri: String) = viewModelScope.launch {
        repository.setProfile(name, imageUri)
    }

    fun wipeData() = viewModelScope.launch {
        repository.wipeData()
    }

    fun addAccount(name: String, type: String) = viewModelScope.launch {
        repository.addAccount(name, type)
    }

    fun editAccount(account: Account) = viewModelScope.launch {
        repository.editAccount(account)
    }

    fun deleteAccount(account: Account) = viewModelScope.launch {
        repository.deleteAccount(account)
    }

    fun addNote(note: String) = viewModelScope.launch {
        repository.addNote(note)
    }

    fun addCategory(name: String, parentId: String?) = viewModelScope.launch {
        repository.addCategory(name, parentId)
    }

    fun editCategory(category: Category) = viewModelScope.launch {
        repository.editCategory(category)
    }

    fun deleteCategory(category: Category) = viewModelScope.launch {
        repository.deleteCategory(category)
    }

    fun importCsv(csv: String) = viewModelScope.launch {
        repository.importCsv(csv)
    }

    fun addTransaction(
        title: String,
        amountMinor: Long,
        kind: TransactionKind,
        category: String,
        accountId: String,
        note: String = "",
        cashbackMinor: Long = 0
    ) = viewModelScope.launch {
        repository.addTransaction(title, amountMinor, kind, category, accountId, note, cashbackMinor)
    }

    fun transfer(
        title: String,
        amountMinor: Long,
        sourceAccountId: String,
        destinationAccountId: String,
        note: String
    ) = viewModelScope.launch {
        repository.transfer(title, amountMinor, sourceAccountId, destinationAccountId, note)
    }

    fun editTransaction(transaction: Transaction) = viewModelScope.launch {
        repository.editTransaction(transaction)
    }

    fun deleteTransaction(transaction: Transaction) = viewModelScope.launch {
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
        investmentType: InvestmentType = InvestmentType.MUTUAL_FUND
    ) = viewModelScope.launch {
        repository.addRecurringPayment(
            name,
            amountMinor,
            frequency,
            accountId,
            category,
            dueDay,
            cashbackMinor,
            investmentType
        )
    }

    fun editRecurringPayment(payment: RecurringPayment) = viewModelScope.launch {
        repository.editRecurringPayment(payment)
    }

    fun toggleRecurringPayment(payment: RecurringPayment) = viewModelScope.launch {
        repository.toggleRecurringPayment(payment)
    }

    fun addHolding(
        name: String,
        type: InvestmentType,
        units: String,
        valueMinor: Long,
        costMinor: Long
    ) = viewModelScope.launch {
        repository.addHolding(name, type, units, valueMinor, costMinor)
    }

    fun addIpo(ipo: PendingIpo) = viewModelScope.launch {
        repository.addIpo(ipo)
    }

    fun confirmIpo(id: String, type: InvestmentType) = viewModelScope.launch {
        repository.confirmIpo(id, type)
    }

    fun rejectIpo(id: String) = viewModelScope.launch {
        repository.rejectIpo(id)
    }

    fun editHolding(holding: Holding) = viewModelScope.launch {
        repository.editHolding(holding)
    }
}
