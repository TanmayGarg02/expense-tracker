package com.example.walletmanager.ui.component

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.composed
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.walletmanager.data.model.Account
import com.example.walletmanager.data.model.AccountAccent
import com.example.walletmanager.data.model.Currency
import com.example.walletmanager.data.model.Transaction
import com.example.walletmanager.data.model.TransactionKind
import com.example.walletmanager.data.model.WalletUiState
import com.example.walletmanager.ui.theme.WalletBackground
import com.example.walletmanager.ui.theme.WalletGreen
import com.example.walletmanager.ui.theme.WalletSurface
import com.example.walletmanager.ui.theme.WalletText
import java.util.Locale

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
    val muted = Color(0xFF95A39E)
    val red = Color(0xFFFF7777)
    val amber = Color(0xFFFFC56E)
    val blue = Color(0xFF7DBBFF)
    val purple = Color(0xFFB79AFF)
    val ink = Color(0xFF10201A)
    val divider = Color(0xFF2D3835)
}

fun categoryEmoji(name: String): String = when (name.substringBefore(" · ").lowercase()) {
    "food" -> "🍜"
    "social life", "social" -> "👫"
    "pets" -> "🐶"
    "transport", "travel" -> "🚕"
    "culture", "entertainment" -> "🖼"
    "household" -> "🪑"
    "shopping" -> "🛍"
    "health" -> "🏥"
    "bills" -> "💡"
    "investments" -> "📈"
    "income", "salary" -> "💵"
    "cashback" -> "💸"
    else -> "📦"
}

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
            .background(WalletBackground)
            .verticalScroll(rememberScrollState())
            .padding(padding)
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
        content = content
    )
}

@Composable
fun ScreenHeader(
    title: String,
    subtitle: String,
    onBack: (() -> Unit)? = null,
    profileName: String = "Alex",
    profileImageUri: String = ""
) {
    val context = LocalContext.current
    val profileImage = remember(profileImageUri, context) {
        profileImageUri.takeIf { it.isNotBlank() }?.let { uri ->
            runCatching {
                context.contentResolver.openInputStream(Uri.parse(uri))?.use { input ->
                    BitmapFactory.decodeStream(input)?.asImageBitmap()
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
                .size(42.dp)
                .clip(CircleShape)
                .background(WalletSurface),
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
                    profileName.firstOrNull()?.uppercase() ?: "T",
                    color = WalletGreen,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
    )
}

@Composable
fun SummaryCard(label: String, amount: Long, state: WalletUiState) {
    Card(colors = CardDefaults.cardColors(WalletSurface), shape = RoundedCornerShape(22.dp)) {
        Column(Modifier.padding(18.dp)) {
            Text(label, color = WalletColors.muted)
            Text(
                formatMoney(amount, state.balancesVisible, state.baseCurrency),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun MoreRow(glyph: String, title: String, subtitle: String = "", onClick: () -> Unit = {}) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(WalletSurface)
            .debounceClickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(WalletGreen.copy(alpha = .12f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = glyph,
                color = WalletGreen,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
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
fun BalanceCard(
    state: WalletUiState,
    toggleBalances: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(WalletGreen),
        shape = RoundedCornerShape(26.dp),
        modifier = Modifier.shadow(12.dp, RoundedCornerShape(26.dp))
    ) {
        Column(
            Modifier.padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(15.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "NET BALANCE THIS MONTH",
                    color = WalletColors.ink.copy(alpha = .65f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = toggleBalances) {
                    Text(
                        text = if (state.balancesVisible) "◉" else "—",
                        color = WalletColors.ink
                    )
                }
            }

            Text(
                text = formatMoney(state.monthlyNetMinor, state.balancesVisible, state.baseCurrency),
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = WalletColors.ink
            )

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Metric("Income", state.monthlyIncomeMinor, state, Modifier.weight(1f))
                Metric("Spent", state.monthlyExpenseMinor, state, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun Metric(
    label: String,
    amount: Long,
    state: WalletUiState,
    modifier: Modifier
) {
    Column(
        modifier
            .clip(RoundedCornerShape(14.dp))
            .background(WalletColors.ink.copy(alpha = .12f))
            .padding(12.dp)
    ) {
        Text(
            text = label,
            color = WalletColors.ink.copy(alpha = .65f),
            fontSize = 12.sp
        )
        Text(
            text = formatMoney(amount, state.balancesVisible, state.baseCurrency),
            color = WalletColors.ink,
            fontWeight = FontWeight.Bold
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
    Column(
        modifier.debounceClickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            Modifier
                .size(48.dp)
                .shadow(6.dp, CircleShape)
                .clip(CircleShape)
                .background(color.copy(alpha = .13f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = glyph,
                color = color,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Text(
            text = label,
            color = WalletColors.muted,
            fontSize = 12.sp,
            modifier = Modifier.padding(top = 7.dp)
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
            .padding(vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(13.dp))
                .background(if (isIncome) WalletGreen.copy(alpha = .14f) else WalletColors.red.copy(alpha = .12f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (isIncome) "💰" else categoryEmoji(item.category),
                color = if (isIncome) WalletGreen else WalletColors.red,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(Modifier.width(12.dp))

        Column(Modifier.weight(1f)) {
            Text(item.title, fontWeight = FontWeight.SemiBold)
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
                color = if (isIncome) WalletGreen else WalletText
            )
            Text(
                text = item.date,
                color = WalletColors.muted,
                fontSize = 12.sp
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
        AccountAccent.Green -> WalletGreen
        AccountAccent.Purple -> WalletColors.purple
        AccountAccent.Amber -> WalletColors.amber
    }

    Card(
        colors = CardDefaults.cardColors(WalletSurface),
        shape = RoundedCornerShape(22.dp),
        onClick = onClick
    ) {
        Row(
            Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(color.copy(alpha = .15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (account.type == "Cash") state.baseCurrency.symbol else "▣",
                    color = color,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.width(13.dp))

            Column(Modifier.weight(1f)) {
                Text(account.name, fontWeight = FontWeight.Bold)
                Text(account.type, color = WalletColors.muted, fontSize = 12.sp)
            }

            Text(
                text = formatMoney(account.balanceMinor, state.balancesVisible, state.baseCurrency),
                fontWeight = FontWeight.Bold,
                color = if (account.balanceMinor < 0) WalletColors.red else WalletText
            )
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
            color = WalletGreen,
            style = Stroke(4.dp.toPx(), cap = StrokeCap.Round)
        )
        drawCircle(
            color = WalletGreen,
            radius = 5.dp.toPx(),
            center = Offset(size.width, size.height * points.last())
        )
    }
}
