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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.walletmanager.ui.component.WalletColors
import com.example.walletmanager.ui.component.WalletIcon
import com.example.walletmanager.ui.theme.WalletBeige
import com.example.walletmanager.ui.theme.WalletSurface
import com.kyant.backdrop.backdrops.LayerBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun WalletNavigationBar(
    currentRoute: String?,
    backdrop: LayerBackdrop,
    onNavigate: (WalletRoute) -> Unit
) {
    val screenWidthDp = LocalConfiguration.current.screenWidthDp
    val labelFontSize = (screenWidthDp / 32f).coerceIn(10f, 12f).sp
    val investmentsLabelFontSize = (screenWidthDp / 34f).coerceIn(10.5f, 12.5f).sp

    val destinations = listOf(
        Triple(HomeRoute, "home", "Home"),
        Triple(AccountsRoute, "accounts", "Accounts"),
        Triple(StatsRoute, "stats", "Stats"),
        Triple(InvestmentsRoute, "investments", "Investments"),
        Triple(MoreRoute(), "more", "Settings")
    )

    NavigationBar(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .drawBackdrop(
                backdrop = backdrop,
                shape = { RoundedCornerShape(28.dp) },
                effects = { blur(44.dp.toPx()) },
                onDrawSurface = {
                    drawRoundRect(
                        brush = Brush.verticalGradient(
                            listOf(
                                WalletSurface.copy(alpha = 0.90f),
                                WalletSurface.copy(alpha = 0.84f)
                            )
                        ),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(28.dp.toPx())
                    )
                }
            )
            .shadow(12.dp, RoundedCornerShape(28.dp))
            .clip(RoundedCornerShape(28.dp))
            .border(
                1.dp,
                WalletColors.cream.copy(alpha = 0.18f),
                RoundedCornerShape(28.dp)
            ),
        containerColor = androidx.compose.ui.graphics.Color.Transparent,
        tonalElevation = 0.dp,
        windowInsets = WindowInsets(0, 0, 0, 0)
    ) {
        destinations.forEach { (route, glyph, label) ->
            val selected = currentRoute?.substringBefore("?") == route::class.qualifiedName
            CompositionLocalProvider(LocalRippleConfiguration provides null) {
                NavigationBarItem(
                    selected = selected,
                    onClick = { onNavigate(route) },
                    modifier = Modifier
                        .weight(
                            when (label) {
                                "Accounts", "Settings" -> 1.15f
                                "Investments" -> 1.25f
                                else -> 1f
                            }
                        )
                        .clip(RoundedCornerShape(24.dp))
                        .background(
                            if (selected) {
                                WalletBeige.copy(alpha = 0.22f)
                            } else {
                                WalletSurface.copy(alpha = 0f)
                            }
                        ),
                    icon = { WalletIcon(glyph, color = if (selected) WalletBeige else WalletColors.muted, size = 20.dp) },
                    label = {
                        Text(
                            text = label,
                            fontSize = if (label == "Investments") investmentsLabelFontSize else labelFontSize,
                            maxLines = 1,
                            softWrap = false,
                            textAlign = TextAlign.Center
                        )
                    },
                    alwaysShowLabel = true,
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = WalletSurface.copy(alpha = 0f),
                        selectedIconColor = WalletBeige,
                        unselectedIconColor = WalletColors.muted,
                        selectedTextColor = WalletBeige,
                        unselectedTextColor = WalletColors.muted
                    )
                )
            }
        }
    }
}
