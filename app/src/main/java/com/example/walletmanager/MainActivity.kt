package com.example.walletmanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.walletmanager.ui.navigation.WalletNavGraph
import com.example.walletmanager.ui.theme.WalletManagerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { WalletManagerTheme { WalletNavGraph() } }
    }
}
