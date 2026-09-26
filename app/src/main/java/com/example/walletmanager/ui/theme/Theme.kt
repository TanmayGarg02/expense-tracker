package com.example.walletmanager.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = WalletGreen,
    secondary = WalletGreen,
    tertiary = WalletGreen,
    background = WalletBackground,
    surface = WalletSurface,
    onBackground = WalletText,
    onSurface = WalletText,
    onPrimary = Color(0xFF10201A)
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
