package com.example.walletmanager.di

import com.example.walletmanager.data.local.WalletDatabase
import com.example.walletmanager.data.repository.WalletRepository
import com.example.walletmanager.ui.WalletViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val walletModule = module {
    single { WalletDatabase.getInstance(get()) }
    single { WalletRepository(get()) }
    viewModel { WalletViewModel(get()) }
}
