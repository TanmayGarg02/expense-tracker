package com.example.walletmanager.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextAlign
import com.example.walletmanager.ui.theme.WalletText
import com.example.walletmanager.ui.theme.WalletGreen
import com.example.walletmanager.ui.theme.WalletSurface
import java.time.LocalDate
import java.time.YearMonth

@Composable
fun MonthlyCalendar() {
    val month = remember { YearMonth.now() }
    val today = remember { LocalDate.now() }
    val days = remember(month) {
        val leadingBlankDays = month.atDay(1).dayOfWeek.value - 1
        List(leadingBlankDays) { null } +
            (1..month.lengthOfMonth()).map { month.atDay(it) }
    }
    val weeks = remember(days) {
        days.chunked(7).map { week ->
            week + List(7 - week.size) { null }
        }
    }

    Column(
        Modifier
            .fillMaxWidth()
            .background(WalletSurface, RoundedCornerShape(20.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "${month.month.name.lowercase().replaceFirstChar { it.uppercase() }} ${month.year}",
            style = MaterialTheme.typography.titleMedium
        )
        Row(Modifier.fillMaxWidth()) {
            listOf("M", "T", "W", "T", "F", "S", "S").forEach { day ->
                Text(
                    text = day,
                    modifier = Modifier.weight(1f),
                    color = WalletColors.muted,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
        weeks.forEach { week ->
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                week.forEach { date ->
                    Box(
                        Modifier
                            .weight(1f)
                            .aspectRatio(1.15f)
                            .border(
                                width = 1.dp,
                                color = WalletColors.divider,
                                shape = RoundedCornerShape(8.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        date?.let {
                            Text(
                                text = it.dayOfMonth.toString(),
                                color = if (it == today) WalletGreen else WalletText,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
