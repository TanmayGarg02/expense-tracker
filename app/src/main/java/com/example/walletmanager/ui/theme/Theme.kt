package com.example.walletmanager.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val WalletDarkColorScheme = darkColorScheme(
    primary = WalletBeige,
    onPrimary = WalletButtonInk,
    primaryContainer = WalletSurfaceVariant,
    onPrimaryContainer = WalletCream,
    secondary = WalletAmber,
    onSecondary = WalletButtonInk,
    secondaryContainer = WalletSurfaceVariant,
    onSecondaryContainer = WalletCream,
    tertiary = WalletRed,
    onTertiary = WalletCream,
    tertiaryContainer = Color(0xFF281416),
    onTertiaryContainer = WalletCream,
    background = WalletBackground,
    onBackground = WalletCream,
    surface = WalletSurface,
    onSurface = WalletCream,
    surfaceVariant = WalletSurfaceVariant,
    onSurfaceVariant = WalletMuted,
    surfaceContainer = WalletSurfaceVariant,
    surfaceContainerHigh = WalletSurfaceVariant,
    surfaceContainerHighest = WalletDivider,
    surfaceContainerLow = Color(0xFF0D0F12),
    surfaceContainerLowest = WalletBlack,
    outline = WalletBeige.copy(alpha = 0.24f),
    outlineVariant = WalletDivider,
    error = WalletRed,
    onError = WalletCream,
    errorContainer = Color(0xFF281416),
    onErrorContainer = WalletCream
)

@Composable
fun WalletManagerTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = WalletDarkColorScheme,
        typography = Typography,
        content = content
    )
}

