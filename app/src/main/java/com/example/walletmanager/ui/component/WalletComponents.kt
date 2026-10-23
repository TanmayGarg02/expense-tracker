package com.example.walletmanager.ui.component

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.Image
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.composed
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import com.example.walletmanager.data.model.Account
import com.example.walletmanager.data.model.AccountAccent
import com.example.walletmanager.data.model.Currency
import com.example.walletmanager.data.model.Transaction
import com.example.walletmanager.data.model.TransactionKind
import com.example.walletmanager.data.model.WalletUiState
import com.example.walletmanager.ui.theme.WalletBackground
import com.example.walletmanager.ui.theme.WalletGreen
import com.example.walletmanager.ui.theme.WalletGreenAccent
import com.example.walletmanager.ui.theme.WalletRed
import com.example.walletmanager.ui.theme.WalletRedAccent
import com.example.walletmanager.ui.theme.WalletSurface
import com.example.walletmanager.ui.theme.WalletText
import com.example.walletmanager.ui.theme.WalletMuted
import com.example.walletmanager.ui.theme.WalletAmber
import com.example.walletmanager.ui.theme.WalletDivider
import com.example.walletmanager.ui.theme.WalletBeige
import com.example.walletmanager.ui.theme.WalletCream
import com.example.walletmanager.ui.theme.WalletSage
import com.example.walletmanager.ui.theme.WalletBlack
import com.example.walletmanager.ui.theme.WalletButtonInk
import androidx.compose.ui.text.font.FontFamily
import java.util.Locale
import kotlinx.coroutines.launch

import androidx.compose.runtime.rememberUpdatedState

fun Modifier.debounceClickable(
    intervalMillis: Long = 300L,
    onClick: () -> Unit
): Modifier = composed {
    val currentOnClick by rememberUpdatedState(onClick)
    var lastClickAt by remember { mutableLongStateOf(0L) }
    clickable {
        val now = System.currentTimeMillis()
        if (now - lastClickAt >= intervalMillis) {
            lastClickAt = now
            currentOnClick()
        }
    }
}

@Composable
fun rememberDebouncedClick(
    intervalMillis: Long = 300L,
    onClick: () -> Unit
): () -> Unit {
    val currentOnClick by rememberUpdatedState(onClick)
    var lastClickAt by remember { mutableLongStateOf(0L) }

    return remember {
        {
            val now = System.currentTimeMillis()
            if (now - lastClickAt >= intervalMillis) {
                lastClickAt = now
                currentOnClick()
            }
        }
    }
}

object WalletColors {
    val muted = WalletMuted
    val red = WalletRed
    val green = WalletGreen
    val redAccent = WalletRedAccent
    val greenAccent = WalletGreenAccent
    val amber = WalletAmber
    val blue = Color(0xFF64B5F6)
    val purple = Color(0xFFB388FF)
    val ink = WalletButtonInk
    val cream = WalletCream
    val beige = WalletBeige
    val sage = WalletSage
    val black = WalletBlack
    val divider = WalletDivider
}

private val WalletMenuShape = RoundedCornerShape(18.dp)
private val WalletMenuItemShape = RoundedCornerShape(12.dp)
val WalletPageBackgroundBrush = Brush.verticalGradient(
    listOf(
        Color(0xFF12151A),
        WalletBackground,
        Color(0xFF121316)
    )
)

fun Modifier.walletGlassBorder(shape: RoundedCornerShape, accent: Color? = null): Modifier = border(
    BorderStroke(
        1.dp,
        Brush.linearGradient(
            if (accent == null) {
                listOf(
                    WalletCream.copy(alpha = 0.32f),
                    WalletBeige.copy(alpha = 0.11f),
                    WalletCream.copy(alpha = 0.18f)
                )
            } else {
                    listOf(accent.copy(alpha = 0.58f), WalletCream.copy(alpha = 0.14f), accent.copy(alpha = 0.30f))
            }
        )
    ),
    shape
)

fun Modifier.walletGlassSurface(shape: RoundedCornerShape): Modifier =
    clip(shape)
        .background(
            Brush.verticalGradient(
                listOf(
                    WalletCream.copy(alpha = 0.10f),
                    WalletSurface.copy(alpha = 0.86f),
                    WalletSurface.copy(alpha = 0.76f)
                )
            )
        )
        .walletGlassBorder(shape)

fun Modifier.walletDropdownSurface(): Modifier =
    shadow(10.dp, WalletMenuShape)
        .clip(WalletMenuShape)
        .background(WalletSurface.copy(alpha = 0.76f))
        .border(1.dp, WalletColors.cream.copy(alpha = 0.28f), WalletMenuShape)

fun Modifier.walletDropdownItem(): Modifier =
    padding(horizontal = 8.dp, vertical = 4.dp)
        .clip(WalletMenuItemShape)
        .background(WalletBackground.copy(alpha = 0.35f))
        .border(1.dp, WalletColors.cream.copy(alpha = 0.12f), WalletMenuItemShape)

data class ProfileHeaderState(
    val name: String = "Alex",
    val imageUri: String = ""
)

val LocalProfileHeader = staticCompositionLocalOf { ProfileHeaderState() }

fun formatMoney(
    minor: Long,
    visible: Boolean,
    currency: Currency
) = if (!visible) {
    "••••"
} else {
    currency.symbol + String.format(Locale.US, "%,.2f", minor / 100.0 * currency.inrRate)
}

@Composable
fun WalletPage(
    padding: PaddingValues,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(padding)
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
        content = {
            content()
            Spacer(Modifier.height(84.dp))
        }
    )
}

@Composable
fun WalletSheetBackButton(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(38.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Text("‹", fontSize = 32.sp, color = WalletText)
        }
    }
}

@Composable
fun ScreenHeader(
    title: String,
    subtitle: String,
    onBack: (() -> Unit)? = null,
    profileName: String? = null,
    profileImageUri: String? = null,
    onProfileClick: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val profile = LocalProfileHeader.current
    val visibleProfileName = profileName ?: profile.name
    val visibleProfileImageUri = profileImageUri ?: profile.imageUri
    val profileImage = remember(visibleProfileImageUri, context) {
        visibleProfileImageUri.takeIf { it.isNotBlank() }?.let { uri ->
            runCatching {
                val parsedUri = Uri.parse(uri)
                if (parsedUri.scheme == "file") {
                    parsedUri.path?.let { BitmapFactory.decodeFile(it)?.asImageBitmap() }
                } else {
                    context.contentResolver.openInputStream(parsedUri)?.use { input ->
                        BitmapFactory.decodeStream(input)?.asImageBitmap()
                    }
                }
            }.getOrNull()
        }
    }
    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (onBack != null) {
            IconButton(onClick = onBack) {
                Text("‹", fontSize = 34.sp, color = WalletText)
            }
        }
        Column(Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subtitle,
                color = WalletColors.muted,
                modifier = Modifier.padding(top = 3.dp)
            )
        }
        Box(
            Modifier
                .size(44.dp)
                .walletGlassSurface(RoundedCornerShape(50.dp))
                .clickable(
                    enabled = onProfileClick != null,
                    onClick = { onProfileClick?.invoke() }
                ),
            contentAlignment = Alignment.Center
        ) {
            if (profileImage != null) {
                Image(
                    bitmap = profileImage,
                    contentDescription = "Profile picture",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Text(
                    visibleProfileName.firstOrNull()?.uppercase() ?: "A",
                    color = WalletGreen,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(top = 10.dp, bottom = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(5.dp)
                .clip(CircleShape)
                .background(WalletBeige)
        )
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelMedium.copy(
                letterSpacing = 1.6.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.SemiBold
            ),
            color = WalletBeige
        )
    }
}

@Composable
fun SummaryCard(
    label: String,
    amount: Long,
    state: WalletUiState,
    amountColor: Color? = null
) {
    val color = amountColor ?: when {
        label.contains("Expense", ignoreCase = true) || label.contains("Spent", ignoreCase = true) -> WalletColors.red
        label.contains("Income", ignoreCase = true) || label.contains("Saved", ignoreCase = true) -> {
            if (amount >= 0) WalletGreen else WalletColors.red
        }
        amount < 0 -> WalletColors.red
        else -> WalletCream
    }
    val accentColor = when {
        label.contains("Expense", ignoreCase = true) || label.contains("Spent", ignoreCase = true) -> WalletRedAccent
        label.contains("Income", ignoreCase = true) || label.contains("Saved", ignoreCase = true) -> {
            if (amount >= 0) WalletGreenAccent else WalletRedAccent
        }
        amount < 0 -> WalletRedAccent
        else -> color
    }
    Card(
        colors = CardDefaults.cardColors(Color.Transparent),
        shape = RoundedCornerShape(22.dp),
        modifier = Modifier.walletGlassBorder(RoundedCornerShape(22.dp))
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .background(
                    Brush.linearGradient(
                        listOf(accentColor.copy(alpha = 0.20f), WalletSurface.copy(alpha = 0.86f), WalletSurface.copy(alpha = 0.78f))
                    )
                )
                .padding(18.dp)
        ) {
            Text(
                label.uppercase(),
                color = WalletColors.muted,
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                letterSpacing = 1.2.sp
            )
            Spacer(Modifier.height(4.dp))
            Text(
                formatMoney(amount, state.balancesVisible, state.baseCurrency),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = color
            )
        }
    }
}

@Composable
fun MoreRow(glyph: String, title: String, subtitle: String = "", onClick: () -> Unit = {}) {
    Row(
        Modifier
            .fillMaxWidth()
            .walletGlassSurface(RoundedCornerShape(18.dp))
            .debounceClickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(WalletGreenAccent.copy(alpha = .12f)),
            contentAlignment = Alignment.Center
        ) {
            WalletIcon(glyph, color = WalletGreenAccent, size = 21.dp)
        }
        Column(
            Modifier
                .padding(start = 14.dp)
                .weight(1f)
        ) {
            Text(title, fontWeight = FontWeight.SemiBold)
            if (subtitle.isNotBlank()) {
                Text(subtitle, color = WalletColors.muted, fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun WalletActionButton(
    label: String,
    onClick: () -> Unit,
    destructive: Boolean = false,
    destructiveColor: Color = WalletColors.red,
    compact: Boolean = false
) {
    val actionColor = if (destructive) destructiveColor else WalletColors.beige
    OutlinedButton(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(
            1.dp,
            (if (destructive) WalletColors.redAccent else actionColor).copy(alpha = 0.7f)
        ),
        contentPadding = PaddingValues(horizontal = if (compact) 8.dp else 10.dp, vertical = 0.dp),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = actionColor)
    ) {
        Text(
            label,
            style = if (compact) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun WalletConfirmDialog(
    title: String,
    message: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    confirmLabel: String = "Delete"
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, style = MaterialTheme.typography.titleMedium) },
        text = { Text(message, style = MaterialTheme.typography.bodyMedium, color = WalletColors.muted) },
        confirmButton = {
            Button(
                onClick = onConfirm,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = WalletColors.redAccent)
            ) {
                Text(confirmLabel)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Cancel")
            }
        },
        shape = RoundedCornerShape(18.dp),
        containerColor = WalletSurface
    )
}

@Composable
fun BalanceCard(
    state: WalletUiState,
    toggleBalances: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(WalletSurface.copy(alpha = 0.86f)),
        shape = RoundedCornerShape(26.dp),
        modifier = Modifier
            .shadow(12.dp, RoundedCornerShape(26.dp))
            .walletGlassBorder(RoundedCornerShape(26.dp))
    ) {
        Column(
            Modifier.padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(15.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "NET BALANCE THIS MONTH",
                    color = WalletColors.muted,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.2.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = toggleBalances) {
                    Text(
                        text = if (state.balancesVisible) "◉" else "—",
                        color = WalletBeige,
                        fontSize = 16.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Text(
                text = formatMoney(state.monthlyNetMinor, state.balancesVisible, state.baseCurrency),
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = if (state.monthlyNetMinor >= 0) WalletGreen else WalletColors.red
            )

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Metric("Income", state.monthlyIncomeMinor, state, WalletGreenAccent, Modifier.weight(1f))
                Metric("Spent", state.monthlyExpenseMinor, state, WalletRedAccent, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun Metric(
    label: String,
    amount: Long,
    state: WalletUiState,
    amountColor: Color,
    modifier: Modifier
) {
    val textColor = when (amountColor) {
        WalletGreenAccent -> WalletGreen
        WalletRedAccent -> WalletRed
        else -> amountColor
    }
    Column(
        modifier
            .clip(RoundedCornerShape(14.dp))
            .background(
                Brush.linearGradient(
                    listOf(amountColor.copy(alpha = 0.17f), WalletBlack.copy(alpha = .45f))
                )
            )
            .border(1.dp, amountColor.copy(alpha = 0.24f), RoundedCornerShape(14.dp))
            .padding(12.dp)
    ) {
        Text(
            text = label.uppercase(),
            color = WalletColors.muted,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.sp
        )
        Text(
            text = formatMoney(amount, state.balancesVisible, state.baseCurrency),
            color = textColor,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            fontSize = 15.sp
        )
    }
}

@Composable
fun QuickAction(
    glyph: String,
    label: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier
) {
    val hapticFeedback = LocalHapticFeedback.current
    val clickScope = rememberCoroutineScope()
    val iconScale = remember { Animatable(1f) }
    val debouncedClick = rememberDebouncedClick(onClick = onClick)

    Column(
        modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    clickScope.launch {
                        iconScale.animateTo(1.12f, tween(50, easing = FastOutSlowInEasing))
                        iconScale.animateTo(1f, tween(75, easing = FastOutSlowInEasing))
                    }
                    debouncedClick()
                }
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            Modifier
                .size(38.dp)
                .scale(iconScale.value)
                .shadow(4.dp, CircleShape)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(color.copy(alpha = 0.32f), color.copy(alpha = 0.08f), WalletSurface.copy(alpha = 0.8f))
                    )
                )
                .border(1.dp, color.copy(alpha = 0.28f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            WalletIcon(glyph, color = color, size = 19.dp)
        }
        Text(
            text = label,
            color = WalletColors.muted,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
fun TransactionRow(
    item: Transaction,
    accounts: List<Account>,
    currency: Currency,
    onClick: () -> Unit = {}
) {
    val account = accounts.firstOrNull { it.id == item.accountId }?.name ?: "Account"
    val isIncome = item.kind == TransactionKind.INCOME

    Row(
        Modifier
            .debounceClickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(13.dp))
                .background(
                    Brush.radialGradient(
                        if (isIncome) {
                            listOf(WalletGreenAccent.copy(alpha = 0.28f), WalletGreenAccent.copy(alpha = 0.07f), WalletSurface)
                        } else {
                            listOf(WalletColors.redAccent.copy(alpha = 0.26f), WalletColors.redAccent.copy(alpha = 0.06f), WalletSurface)
                        }
                    )
                )
                .border(1.dp, if (isIncome) WalletGreenAccent.copy(alpha = .28f) else WalletColors.redAccent.copy(alpha = .22f), RoundedCornerShape(13.dp)),
            contentAlignment = Alignment.Center
        ) {
            WalletIcon(
                name = if (isIncome) "income" else categoryIconName(item.category),
                color = if (isIncome) WalletGreenAccent else WalletColors.redAccent,
                size = 19.dp
            )
        }

        Spacer(Modifier.width(12.dp))

        Column(Modifier.weight(1f)) {
            Text(item.title, fontWeight = FontWeight.SemiBold, color = WalletCream)
            Text(
                text = "${item.category} · $account",
                color = WalletColors.muted,
                fontSize = 12.sp
            )
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = (if (isIncome) "+" else "−") + formatMoney(item.amountMinor, true, currency),
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = if (isIncome) WalletGreen else WalletColors.red
            )
            Text(
                text = item.date,
                color = WalletColors.muted,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
fun AccountRow(
    account: Account,
    state: WalletUiState,
    onClick: () -> Unit = {}
) {
    val color = when (account.accent) {
        AccountAccent.Green -> WalletGreenAccent
        AccountAccent.Purple -> Color(0xFFB29ACB)
        AccountAccent.Amber -> Color(0xFFC7A56F)
        AccountAccent.Pink -> Color(0xFFD18FA2)
        AccountAccent.Blue -> Color(0xFF86A9CE)
    }

    Card(
        colors = CardDefaults.cardColors(Color.Transparent),
        shape = RoundedCornerShape(22.dp),
        modifier = Modifier.walletGlassBorder(RoundedCornerShape(22.dp), color),
        onClick = onClick
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .background(
                    Brush.linearGradient(
                        listOf(color.copy(alpha = 0.22f), WalletSurface.copy(alpha = 0.92f), WalletSurface)
                    )
                )
                .padding(horizontal = 16.dp, vertical = 15.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        Brush.radialGradient(
                            listOf(color.copy(alpha = 0.34f), color.copy(alpha = 0.08f), WalletSurface)
                        )
                    )
                    .border(1.dp, color.copy(alpha = 0.28f), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (account.type == "Cash") state.baseCurrency.symbol else "▣",
                    color = color,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.width(13.dp))

            Column(
                Modifier
                    .weight(1f)
                    .padding(end = 12.dp)
            ) {
                Text(
                    account.name,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    account.type,
                    color = WalletColors.muted,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = formatMoney(account.balanceMinor, state.balancesVisible, state.baseCurrency),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = if (account.balanceMinor < 0) WalletColors.red else WalletText,
                    maxLines = 1
                )
                Spacer(Modifier.height(4.dp))
                Text("Balance", color = WalletColors.muted, fontSize = 11.sp)
            }
        }
    }
}

@Composable
fun Sparkline() {
    val points = remember {
        listOf(.75f, .61f, .68f, .48f, .53f, .37f, .42f, .18f, .27f)
    }

    Canvas(
        Modifier
            .fillMaxWidth()
            .height(54.dp)
    ) {
        val path = Path()
        points.forEachIndexed { index, point ->
            val x = size.width * index / points.lastIndex
            val y = size.height * point

            if (index == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }

        drawPath(
            path = path,
            color = WalletGreenAccent,
            style = Stroke(4.dp.toPx(), cap = StrokeCap.Round)
        )
        drawCircle(
            color = WalletGreenAccent,
            radius = 5.dp.toPx(),
            center = Offset(size.width, size.height * points.last())
        )
    }
}
