package com.example.walletmanager.ui.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.walletmanager.ui.theme.WalletBeige
import com.example.walletmanager.ui.theme.WalletSurface
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

fun formatWalletDate(date: LocalDate): String =
    date.format(DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.getDefault()))

@Composable
fun WalletDatePickerField(
    value: LocalDate,
    label: String,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    var showPicker by remember { mutableStateOf(false) }
    Box(modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = formatWalletDate(value),
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { WalletIcon("calendar", color = WalletColors.muted, size = 18.dp) },
            modifier = Modifier.fillMaxWidth()
        )
        Box(
            Modifier
                .matchParentSize()
                .clip(RoundedCornerShape(4.dp))
                .clickable { showPicker = true }
                .semantics {
                    contentDescription = "$label, ${formatWalletDate(value)}"
                    role = Role.Button
                }
        )
    }

    if (showPicker) {
        Dialog(onDismissRequest = { showPicker = false }) {
            Surface(
                modifier = Modifier.widthIn(max = 380.dp),
                shape = RoundedCornerShape(24.dp),
                color = WalletSurface
            ) {
                WalletCalendarPicker(
                    initialDate = value,
                    onDateSelected = {
                        onDateSelected(it)
                        showPicker = false
                    }
                )
            }
        }
    }
}

@Composable
fun WalletCalendarPicker(
    initialDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    var visibleMonth by remember(initialDate) { mutableStateOf(YearMonth.from(initialDate)) }
    var selectedDate by remember(initialDate) { mutableStateOf(initialDate) }
    var expandedMenu by remember { mutableStateOf<String?>(null) }
    val locale = Locale.getDefault()
    Column(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(animationSpec = tween(160))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "‹",
                color = WalletBeige,
                fontSize = 30.sp,
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable { visibleMonth = visibleMonth.minusMonths(1) }
                    .padding(horizontal = 12.dp, vertical = 2.dp)
            )
            Spacer(Modifier.weight(1f))
            Box {
                Text(
                    visibleMonth.month.getDisplayName(TextStyle.FULL, locale),
                    color = WalletColors.cream,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { expandedMenu = "month" }
                )
                DropdownMenu(
                    expanded = expandedMenu == "month",
                    onDismissRequest = { expandedMenu = null },
                    modifier = Modifier.walletDropdownSurface()
                ) {
                    java.time.Month.values().forEach { month ->
                        DropdownMenuItem(
                            modifier = Modifier.walletDropdownItem(
                                selected = month == visibleMonth.month,
                                accent = WalletBeige
                            ),
                            text = { Text(month.getDisplayName(TextStyle.FULL, locale), color = WalletColors.cream) },
                            onClick = {
                                visibleMonth = visibleMonth.withMonth(month.value)
                                expandedMenu = null
                            }
                        )
                    }
                }
            }
            Box(Modifier.padding(start = 8.dp)) {
                Text(
                    visibleMonth.year.toString(),
                    color = WalletColors.cream,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { expandedMenu = "year" }
                )
                DropdownMenu(
                    expanded = expandedMenu == "year",
                    onDismissRequest = { expandedMenu = null },
                    modifier = Modifier.walletDropdownSurface()
                ) {
                    (LocalDate.now().year - 100..LocalDate.now().year + 25).forEach { year ->
                        DropdownMenuItem(
                            modifier = Modifier.walletDropdownItem(
                                selected = year == visibleMonth.year,
                                accent = WalletBeige
                            ),
                            text = { Text(year.toString(), color = WalletColors.cream) },
                            onClick = {
                                visibleMonth = visibleMonth.withYear(year)
                                expandedMenu = null
                            }
                        )
                    }
                }
            }
            Spacer(Modifier.weight(1f))
            Text(
                "›",
                color = WalletBeige,
                fontSize = 30.sp,
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable { visibleMonth = visibleMonth.plusMonths(1) }
                    .padding(horizontal = 12.dp, vertical = 2.dp)
            )
        }

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            java.time.DayOfWeek.values().forEach { day ->
                Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text(
                        day.getDisplayName(TextStyle.SHORT, locale).take(1),
                        color = WalletColors.muted,
                        fontSize = 12.sp
                    )
                }
            }
        }

        AnimatedContent(targetState = visibleMonth, label = "calendar-month") { month ->
            val days = remember(month) {
                val leadingDays = month.atDay(1).dayOfWeek.value - 1
                List(leadingDays) { null } + (1..month.lengthOfMonth()).map { month.atDay(it) }
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                days.chunked(7).forEach { week ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        week.forEach { date ->
                            val selected = date == selectedDate
                            val today = date == LocalDate.now()
                            Box(
                                Modifier
                                    .weight(1f)
                                    .height(40.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when {
                                            selected -> WalletBeige
                                            today -> WalletBeige.copy(alpha = 0.16f)
                                            else -> Color.Transparent
                                        }
                                    )
                                    .clickable(enabled = date != null) {
                                        date?.let {
                                            selectedDate = it
                                            onDateSelected(it)
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    date?.dayOfMonth?.toString().orEmpty(),
                                    color = if (selected) WalletColors.ink else WalletColors.cream,
                                    fontWeight = if (selected || today) FontWeight.SemiBold else FontWeight.Normal
                                )
                            }
                        }
                        repeat(7 - week.size) { Spacer(Modifier.weight(1f)) }
                    }
                }
            }
        }
    }
}
