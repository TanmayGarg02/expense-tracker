package com.example.walletmanager.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = WalletGreen,
    secondary = Color(0xFFD9B27C),
    tertiary = WalletRed,
    background = WalletBackground,
    surface = WalletSurface,
    onBackground = WalletText,
    onSurface = WalletText,
    onPrimary = Color(0xFF1C1815)
)

@Composable
fun WalletManagerTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}

