package com.example.walletmanager.ui.component

import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier

/** App-wide snackbar state and message entry point. */
class WalletSnackbarController internal constructor() {
    internal val hostState = SnackbarHostState()

    suspend fun show(message: String) {
        hostState.showSnackbar(message)
    }
}

val LocalWalletSnackbarController = staticCompositionLocalOf<WalletSnackbarController> {
    error("WalletSnackbarController was not provided")
}

@Composable
fun rememberWalletSnackbarController(): WalletSnackbarController = remember {
    WalletSnackbarController()
}

/** Reusable host for the shared snackbar controller, including modal surfaces. */
@Composable
fun WalletSnackbarHost(modifier: Modifier = Modifier) {
    SnackbarHost(
        hostState = LocalWalletSnackbarController.current.hostState,
        modifier = modifier
    )
}
