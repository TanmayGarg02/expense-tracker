package com.example.walletmanager.ui.component

import android.content.Context
import android.os.Build
import android.view.WindowManager
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogWindowProvider
import com.example.walletmanager.ui.theme.WalletSurface
import kotlin.math.roundToInt

/** A compact app sheet with a softly translucent surface and platform backdrop blur. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletModalBottomSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    dragHandle: @Composable (() -> Unit)? = null,
    contentWindowInsets: @Composable () -> WindowInsets = { WindowInsets(0, 0, 0, 0) },
    containerColor: Color = WalletSurface,
    content: @Composable ColumnScope.() -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        contentWindowInsets = contentWindowInsets,
        sheetState = sheetState,
        dragHandle = dragHandle,
        containerColor = containerColor.copy(alpha = containerColor.alpha * 0.84f),
        tonalElevation = 0.dp
    ) {
        WalletWindowBackdropBlur()
        content()
    }
}

@Composable
private fun WalletWindowBackdropBlur() {
    val view = LocalView.current
    val context = LocalContext.current
    val blurRadiusPx = with(LocalDensity.current) { 52.dp.toPx().roundToInt() }
    val window = (view.parent as? DialogWindowProvider)?.window
    val canBlurBehind = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
        (context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager)
            ?.isCrossWindowBlurEnabled == true

    DisposableEffect(window, canBlurBehind, blurRadiusPx) {
        if (window == null || !canBlurBehind || Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            onDispose { }
        } else {
            val originalFlags = window.attributes.flags
            val originalRadius = window.attributes.blurBehindRadius
            window.addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
            window.attributes = window.attributes.apply {
                blurBehindRadius = blurRadiusPx
            }
            onDispose {
                window.attributes = window.attributes.apply {
                    blurBehindRadius = originalRadius
                }
                if (originalFlags and WindowManager.LayoutParams.FLAG_BLUR_BEHIND == 0) {
                    window.clearFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
                }
            }
        }
    }
}
