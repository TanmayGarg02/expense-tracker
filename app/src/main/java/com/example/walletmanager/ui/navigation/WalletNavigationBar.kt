package com.example.walletmanager.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.walletmanager.ui.component.WalletColors
import com.example.walletmanager.ui.theme.WalletGreen
import com.example.walletmanager.ui.theme.WalletSurface
import com.kyant.backdrop.backdrops.LayerBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur

@Composable
fun WalletNavigationBar(
    currentRoute: String?,
    backdrop: LayerBackdrop,
    onNavigate: (WalletRoute) -> Unit
) {
    val screenWidthDp = LocalConfiguration.current.screenWidthDp
    val labelFontSize = (screenWidthDp / 33f).coerceIn(10f, 12f).sp

    val destinations = listOf(
        Triple(HomeRoute, "🏠", "Home"),
        Triple(AccountsRoute, "💳", "Accounts"),
        Triple(StatsRoute, "📊", "Stats"),
        Triple(InvestmentsRoute, "📈", "Investments"),
        Triple(MoreRoute(), "⚙️", "Settings")
    )

    NavigationBar(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .drawBackdrop(
                backdrop = backdrop,
                shape = { RoundedCornerShape(28.dp) },
                effects = { blur(18.dp.toPx()) },
                onDrawSurface = {
                    drawRect(WalletSurface.copy(alpha = 0.92f))
                }
            )
            .shadow(12.dp, RoundedCornerShape(28.dp))
            .clip(RoundedCornerShape(28.dp))
            .border(
                1.dp,
                WalletColors.cream.copy(alpha = 0.18f),
                RoundedCornerShape(28.dp)
            ),
        containerColor = WalletSurface.copy(alpha = 0.82f),
        tonalElevation = 0.dp,
        windowInsets = WindowInsets(0, 0, 0, 0)
    ) {
        destinations.forEach { (route, glyph, label) ->
            val selected = currentRoute?.substringBefore("?") == route::class.qualifiedName
            NavigationBarItem(
                selected = selected,
                onClick = { onNavigate(route) },
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        if (selected) {
                            WalletGreen.copy(alpha = 0.28f)
                        } else {
                            WalletSurface.copy(alpha = 0f)
                        }
                    ),
                icon = { Text(text = glyph, fontSize = 19.sp) },
                label = { Text(label, fontSize = labelFontSize) },
                alwaysShowLabel = true,
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = WalletSurface.copy(alpha = 0f),
                    selectedIconColor = WalletColors.cream,
                    unselectedIconColor = WalletColors.muted,
                    selectedTextColor = WalletColors.cream,
                    unselectedTextColor = WalletColors.muted
                )
            )
        }
    }
}
