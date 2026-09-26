package com.example.walletmanager

import android.app.Application
import com.example.walletmanager.di.walletModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class WalletApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@WalletApplication)
            modules(walletModule)
        }
    }
}
