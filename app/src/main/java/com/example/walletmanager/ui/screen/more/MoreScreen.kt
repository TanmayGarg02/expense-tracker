package com.example.walletmanager.ui.screen.more

import android.app.TimePickerDialog
import android.text.format.DateFormat
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.walletmanager.data.model.Currency
import com.example.walletmanager.data.model.SavedNote
import com.example.walletmanager.data.model.WalletUiState
import com.example.walletmanager.notifications.ReminderDraft
import com.example.walletmanager.notifications.ReminderScheduler
import com.example.walletmanager.ui.component.MoreRow
import com.example.walletmanager.ui.component.ScreenHeader
import com.example.walletmanager.ui.component.WalletColors
import com.example.walletmanager.ui.component.WalletConfirmDialog
import com.example.walletmanager.ui.component.WalletPage
import com.example.walletmanager.ui.component.WalletModalBottomSheet
import com.example.walletmanager.ui.component.WalletSheetBackButton
import com.example.walletmanager.ui.component.walletDropdownItem
import com.example.walletmanager.ui.component.walletDropdownSurface
import com.example.walletmanager.ui.theme.WalletGreen
import com.example.walletmanager.ui.theme.WalletGreenAccent
import com.example.walletmanager.ui.theme.WalletBeige
import com.example.walletmanager.ui.theme.WalletSurface
import com.example.walletmanager.ui.theme.WalletSurfaceVariant
import java.io.File
import java.time.LocalTime
import java.time.Month
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Date
import java.util.Locale
import java.util.UUID

@Composable
fun MoreScreen(
    state: WalletUiState,
    setCurrency: (Currency) -> Unit,
    addNote: (String) -> Unit,
    editNote: (Long, String) -> Unit,
    deleteNote: (Long) -> Unit,
    export: () -> Unit,
    import: () -> Unit,
    addReminder: (ReminderDraft) -> Unit,
    setProfile: (String, String) -> Unit,
    wipeData: () -> Unit,
    addSampleData: () -> Unit,
    onBack: (() -> Unit)? = null,
    openProfileOnEntry: Boolean = false
) {
    var currencySheet by rememberSaveable { mutableStateOf(false) }
    var noteSheet by rememberSaveable { mutableStateOf(false) }
    var reminderSheet by rememberSaveable { mutableStateOf(false) }
    var profileSheet by rememberSaveable { mutableStateOf(openProfileOnEntry) }
    var wipeSheet by rememberSaveable { mutableStateOf(false) }
    var confirmAddSampleData by rememberSaveable { mutableStateOf(false) }
    var profileImageUri by rememberSaveable { mutableStateOf(state.profileImageUri) }
    val sampleDataLoaded = state.accounts.any { it.id.startsWith("demo-") } ||
        state.transactions.any { it.id.startsWith("demo-") } ||
        state.recurringPayments.any { it.id.startsWith("demo-") } ||
        state.holdings.any { it.id.startsWith("demo-") } ||
        state.pendingIpos.any { it.id.startsWith("demo-") } ||
        state.fixedDeposits.any { it.id.startsWith("demo-") }

    val context = LocalContext.current
    val pickProfileImage = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            runCatching {
                val profileImageFile = File(context.filesDir, "profile_image_${System.currentTimeMillis()}.jpg")
                context.contentResolver.openInputStream(uri)?.use { input ->
                    profileImageFile.outputStream().use { output -> input.copyTo(output) }
                }
                profileImageUri = profileImageFile.toURI().toString()
            }
        }
    }

    WalletPage(PaddingValues()) {
        ScreenHeader(
            "Settings",
            "Your data stays in your hands",
            onBack = onBack,
            onProfileClick = { profileSheet = true }
        )

        Card(
            colors = CardDefaults.cardColors(WalletSurface),
            shape = RoundedCornerShape(22.dp)
        ) {
            Column(
                Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Data and backup", fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = import) {
                        Text("Import CSV")
                    }
                    Button(
                        onClick = export,
                        colors = ButtonDefaults.buttonColors(WalletBeige)
                    ) {
                        Text("Export CSV", color = WalletColors.ink)
                    }
                }
                Text(
                    if (sampleDataLoaded) {
                        "Demo dataset is loaded. Wipe data to reset it."
                    } else {
                        "Add demo accounts, transactions, transfers, plans and investments to explore the app."
                    },
                    color = WalletColors.muted
                )
                OutlinedButton(
                    onClick = { confirmAddSampleData = true },
                    enabled = !sampleDataLoaded,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = WalletColors.cream)
                ) {
                    Text("Add sample data")
                }
                state.message?.let {
                    Text(it, color = WalletGreen)
                }
            }
        }

        MoreRow(
            glyph = "currency",
            title = "Base currency",
            subtitle = "${state.baseCurrency.code} · INR is the default"
        ) {
            currencySheet = true
        }
        MoreRow("profile", "Profile", "${state.profileName} · Add a profile picture") {
            profileSheet = true
        }
        MoreRow("notes", "Notes", "${state.notes.size} saved notes") {
            noteSheet = true
        }
        MoreRow("reminder", "Important reminders", "Only notify me about things I choose") {
            reminderSheet = true
        }
        MoreRow("delete", "Wipe all data", "Delete all accounts, transactions and investments") {
            wipeSheet = true
        }

        Spacer(Modifier.height(70.dp))
    }

    if (currencySheet) {
        CurrencySheet(
            selected = state.baseCurrency,
            dismiss = { currencySheet = false },
            select = {
                setCurrency(it)
                currencySheet = false
            }
        )
    }

    if (noteSheet) {
        NoteSheet(
            notes = state.notes,
            dismiss = { noteSheet = false },
            add = addNote,
            edit = editNote,
            delete = deleteNote
        )
    }

    if (reminderSheet) {
        ReminderSheet(
            dismiss = { reminderSheet = false },
            save = addReminder
        )
    }

    if (profileSheet) {
        ProfileSheet(
            name = state.profileName,
            imageUri = profileImageUri,
            dismiss = { profileSheet = false },
            chooseImage = {
                pickProfileImage.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            },
            save = { name, uri ->
                setProfile(name, uri)
                profileSheet = false
            }
        )
    }

    if (wipeSheet) {
        WipeDataSheet(
            dismiss = { wipeSheet = false },
            wipe = {
                wipeData()
                wipeSheet = false
            }
        )
    }

    if (confirmAddSampleData) {
        WalletConfirmDialog(
            title = "Add sample data?",
            message = "This adds clearly labeled demo accounts, transactions, plans and investments alongside your data. It will not delete or replace your existing data.",
            onDismiss = { confirmAddSampleData = false },
            onConfirm = {
                confirmAddSampleData = false
                addSampleData()
            },
            confirmLabel = "Add sample data"
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WipeDataSheet(
    dismiss: () -> Unit,
    wipe: () -> Unit
) {
    var confirmation by rememberSaveable { mutableStateOf("") }

    WalletModalBottomSheet(
        onDismissRequest = dismiss,
        contentWindowInsets = { WindowInsets(0, 0, 0, 0) },
        dragHandle = { WalletSheetBackButton(dismiss) },
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = WalletSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("Wipe all data?", fontWeight = FontWeight.Bold)
            Text("This permanently removes your accounts, transactions, investments, plans, notes and pending IPOs.", color = WalletColors.muted)
            Text("Type Delete to continue.", fontWeight = FontWeight.Bold)
            OutlinedTextField(
                value = confirmation,
                onValueChange = { confirmation = it },
                label = { Text("Confirmation") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                onClick = wipe,
                enabled = confirmation == "Delete",
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(WalletColors.redAccent)
            ) {
                Text("Wipe data", fontWeight = FontWeight.Bold)
            }
            TextButton(onClick = dismiss, modifier = Modifier.fillMaxWidth()) {
                Text("Cancel")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileSheet(
    name: String,
    imageUri: String,
    dismiss: () -> Unit,
    chooseImage: () -> Unit,
    save: (String, String) -> Unit
) {
    var editedName by rememberSaveable(name) { mutableStateOf(name) }

    WalletModalBottomSheet(
        onDismissRequest = dismiss,
        contentWindowInsets = { WindowInsets(0, 0, 0, 0) },
        dragHandle = { WalletSheetBackButton(dismiss) },
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = WalletSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Edit profile", fontWeight = FontWeight.Bold)
            OutlinedTextField(
                value = editedName,
                onValueChange = { editedName = it },
                label = { Text("Your name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedButton(
                onClick = chooseImage,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (imageUri.isBlank()) "Choose profile picture" else "Change profile picture")
            }
            Button(
                onClick = { save(editedName, imageUri) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(WalletBeige)
            ) {
                Text("Save Profile", color = WalletColors.ink, fontWeight = FontWeight.Bold)
            }
            TextButton(onClick = dismiss, modifier = Modifier.fillMaxWidth()) {
                Text("Cancel")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CurrencySheet(
    selected: Currency,
    dismiss: () -> Unit,
    select: (Currency) -> Unit
) {
    WalletModalBottomSheet(
        onDismissRequest = dismiss,
        contentWindowInsets = { WindowInsets(0, 0, 0, 0) },
        dragHandle = { WalletSheetBackButton(dismiss) },
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = WalletSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("Base currency", fontWeight = FontWeight.Bold)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Currency.entries.forEach {
                    FilterChip(
                        selected = selected == it,
                        onClick = { select(it) },
                        label = { Text("${it.symbol}  ${it.code}") }
                    )
                }
            }
            TextButton(onClick = dismiss, modifier = Modifier.fillMaxWidth()) {
                Text("Done")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NoteSheet(
    notes: List<SavedNote>,
    dismiss: () -> Unit,
    add: (String) -> Unit,
    edit: (Long, String) -> Unit,
    delete: (Long) -> Unit
) {
    var editorVisible by remember { mutableStateOf(false) }
    var editorId by remember { mutableStateOf<Long?>(null) }
    var editorText by remember { mutableStateOf("") }

    WalletModalBottomSheet(
        onDismissRequest = dismiss,
        contentWindowInsets = { WindowInsets(0, 0, 0, 0) },
        dragHandle = null,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = WalletSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 600.dp)
                .padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 28.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                IconButton(
                    onClick = dismiss,
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = WalletColors.cream
                    )
                }
                IconButton(
                    onClick = {
                        editorId = null
                        editorText = ""
                        editorVisible = true
                    },
                    modifier = Modifier.align(Alignment.CenterEnd)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add note", tint = WalletBeige)
                }
                Text("Notes", fontWeight = FontWeight.Bold)
            }

            if (notes.isEmpty()) {
                Text(
                    "No saved notes yet. Tap + to add one.",
                    color = WalletColors.muted,
                    modifier = Modifier.padding(vertical = 20.dp)
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 520.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(notes, key = { it.id }) { note ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(WalletSurfaceVariant)
                                .padding(start = 14.dp, end = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(note.text, modifier = Modifier.weight(1f), color = WalletColors.cream)
                            IconButton(
                                onClick = {
                                    editorId = note.id
                                    editorText = note.text
                                    editorVisible = true
                                }
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit note", tint = WalletColors.muted)
                            }
                            IconButton(onClick = { delete(note.id) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete note", tint = WalletColors.redAccent)
                            }
                        }
                    }
                }
            }
        }
    }

    if (editorVisible) {
        AlertDialog(
            onDismissRequest = { editorVisible = false },
            title = { Text(if (editorId == null) "Add note" else "Edit note") },
            text = {
                OutlinedTextField(
                    value = editorText,
                    onValueChange = { editorText = it },
                    label = { Text("Note") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    enabled = editorText.isNotBlank(),
                    onClick = {
                        val cleanText = editorText.trim()
                        val id = editorId
                        if (id == null) add(cleanText) else edit(id, cleanText)
                        editorVisible = false
                    }
                ) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { editorVisible = false }) { Text("Cancel") }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReminderSheet(
    dismiss: () -> Unit,
    save: (ReminderDraft) -> Unit
) {
    val context = LocalContext.current
    var reminders by remember { mutableStateOf(ReminderScheduler.savedReminders(context)) }
    var editorOpen by rememberSaveable { mutableStateOf(false) }
    var editingId by rememberSaveable { mutableStateOf<String?>(null) }
    var title by rememberSaveable { mutableStateOf("") }
    var message by rememberSaveable { mutableStateOf("") }
    var minutes by rememberSaveable { mutableStateOf("60") }
    var repeatMonthly by rememberSaveable { mutableStateOf(false) }
    var repeatYearly by rememberSaveable { mutableStateOf(false) }
    var monthlyDay by rememberSaveable { mutableStateOf(1) }
    var yearlyMonth by rememberSaveable { mutableStateOf(1) }
    var yearlyDay by rememberSaveable { mutableStateOf(1) }
    var hour by rememberSaveable { mutableStateOf(9) }
    var minute by rememberSaveable { mutableStateOf(0) }
    var dayMenuExpanded by remember { mutableStateOf(false) }
    var monthMenuExpanded by remember { mutableStateOf(false) }
    var yearlyDayMenuExpanded by remember { mutableStateOf(false) }

    WalletModalBottomSheet(
        onDismissRequest = dismiss,
        contentWindowInsets = { WindowInsets(0, 0, 0, 0) },
        dragHandle = null,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = WalletSurface
    ) {
        if (!editorOpen) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 620.dp)
                    .padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 28.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(
                        onClick = dismiss,
                        modifier = Modifier.align(Alignment.CenterStart)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = WalletColors.cream
                        )
                    }
                    IconButton(
                        onClick = {
                            editingId = null
                            title = ""
                            message = ""
                            minutes = "60"
                            repeatMonthly = false
                            repeatYearly = false
                            monthlyDay = 1
                            yearlyMonth = 1
                            yearlyDay = 1
                            hour = 9
                            minute = 0
                            editorOpen = true
                        },
                        modifier = Modifier.align(Alignment.CenterEnd)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add reminder", tint = WalletBeige)
                    }
                    Text("Reminders", fontWeight = FontWeight.Bold)
                }
                if (reminders.isEmpty()) {
                    Text(
                        "No reminders yet. Tap + to create one.",
                        color = WalletColors.muted,
                        modifier = Modifier.padding(vertical = 20.dp)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().heightIn(max = 540.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(reminders, key = { it.id }) { reminder ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(WalletSurfaceVariant)
                                    .padding(start = 14.dp, end = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(Modifier.weight(1f).padding(vertical = 10.dp)) {
                                    Text(reminder.title, color = WalletColors.cream, fontWeight = FontWeight.SemiBold)
                                    Text(reminder.message, color = WalletColors.muted, fontSize = 12.sp)
                                    Text(
                                        when {
                                            reminder.monthlyDay != null -> "Monthly · ${reminder.monthlyDay}${ordinalSuffix(reminder.monthlyDay)}"
                                            reminder.yearlyMonth != null && reminder.yearlyDay != null ->
                                                "Yearly · ${Month.of(reminder.yearlyMonth).getDisplayName(TextStyle.SHORT, Locale.getDefault())} ${reminder.yearlyDay}"
                                            else -> "Once · ${java.text.DateFormat.getDateTimeInstance().format(Date(reminder.triggerAtMillis))}"
                                        },
                                        color = WalletBeige,
                                        fontSize = 11.sp
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        editingId = reminder.id
                                        title = reminder.title
                                        message = reminder.message
                                        repeatMonthly = reminder.monthlyDay != null
                                        repeatYearly = reminder.yearlyMonth != null
                                        monthlyDay = reminder.monthlyDay ?: 1
                                        yearlyMonth = reminder.yearlyMonth ?: 1
                                        yearlyDay = reminder.yearlyDay ?: 1
                                        hour = reminder.hourOfDay
                                        minute = reminder.minute
                                        minutes = ((reminder.triggerAtMillis - System.currentTimeMillis()) / 60_000L)
                                            .coerceAtLeast(1L).toString()
                                        editorOpen = true
                                    }
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit reminder", tint = WalletColors.muted)
                                }
                                IconButton(
                                    onClick = {
                                        ReminderScheduler.cancel(context, reminder.id)
                                        reminders = reminders.filterNot { it.id == reminder.id }
                                    }
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete reminder", tint = WalletColors.redAccent)
                                }
                            }
                        }
                    }
                }
            }
        } else {
          Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Text("Important reminder", fontWeight = FontWeight.Bold)
            Text(
                "Set a one-time reminder or repeat it monthly or yearly.",
                color = WalletColors.muted
            )
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = message,
                onValueChange = { message = it },
                label = { Text("Message") },
                modifier = Modifier.fillMaxWidth()
            )
            Text("Repeat", color = WalletColors.muted)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = !repeatMonthly && !repeatYearly,
                    onClick = {
                        repeatMonthly = false
                        repeatYearly = false
                    },
                    label = { Text("Once") }
                )
                FilterChip(
                    selected = repeatMonthly,
                    onClick = {
                        repeatMonthly = true
                        repeatYearly = false
                    },
                    label = { Text("Monthly") }
                )
                FilterChip(
                    selected = repeatYearly,
                    onClick = {
                        repeatMonthly = false
                        repeatYearly = true
                    },
                    label = { Text("Yearly") }
                )
            }
            if (repeatMonthly) {
                ExposedDropdownMenuBox(
                    expanded = dayMenuExpanded,
                    onExpandedChange = { dayMenuExpanded = it },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = "${monthlyDay}${ordinalSuffix(monthlyDay)} of each month",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Day of month") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = dayMenuExpanded)
                        },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = dayMenuExpanded,
                        onDismissRequest = { dayMenuExpanded = false },
                        modifier = Modifier.walletDropdownSurface()
                    ) {
                        (1..31).forEach { day ->
                            DropdownMenuItem(
                                modifier = Modifier.walletDropdownItem(
                                    selected = day == monthlyDay,
                                    accent = WalletGreenAccent
                                ),
                                text = { Text("${day}${ordinalSuffix(day)}", color = WalletColors.cream) },
                                onClick = {
                                    monthlyDay = day
                                    dayMenuExpanded = false
                                }
                            )
                        }
                    }
                }
                Text(
                    "If a month does not have that day, the reminder runs on its last day.",
                    color = WalletColors.muted
                )
            } else if (repeatYearly) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ExposedDropdownMenuBox(
                        expanded = monthMenuExpanded,
                        onExpandedChange = { monthMenuExpanded = it },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = Month.of(yearlyMonth).getDisplayName(TextStyle.FULL, Locale.getDefault()),
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Month") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = monthMenuExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = monthMenuExpanded,
                            onDismissRequest = { monthMenuExpanded = false },
                            modifier = Modifier.walletDropdownSurface()
                        ) {
                            Month.values().forEach { month ->
                                DropdownMenuItem(
                                    modifier = Modifier.walletDropdownItem(
                                        selected = month.value == yearlyMonth,
                                        accent = WalletGreenAccent
                                    ),
                                    text = {
                                        Text(
                                            month.getDisplayName(TextStyle.FULL, Locale.getDefault()),
                                            color = WalletColors.cream
                                        )
                                    },
                                    onClick = {
                                        yearlyMonth = month.value
                                        yearlyDay = yearlyDay.coerceAtMost(YearMonth.of(2024, month).lengthOfMonth())
                                        monthMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }
                    val daysInMonth = YearMonth.of(2024, yearlyMonth).lengthOfMonth()
                    ExposedDropdownMenuBox(
                        expanded = yearlyDayMenuExpanded,
                        onExpandedChange = { yearlyDayMenuExpanded = it },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = "${yearlyDay}${ordinalSuffix(yearlyDay)}",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Day") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = yearlyDayMenuExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = yearlyDayMenuExpanded,
                            onDismissRequest = { yearlyDayMenuExpanded = false },
                            modifier = Modifier.walletDropdownSurface()
                        ) {
                            (1..daysInMonth).forEach { day ->
                                DropdownMenuItem(
                                    modifier = Modifier.walletDropdownItem(
                                        selected = day == yearlyDay,
                                        accent = WalletGreenAccent
                                    ),
                                    text = { Text("${day}${ordinalSuffix(day)}", color = WalletColors.cream) },
                                    onClick = {
                                        yearlyDay = day
                                        yearlyDayMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
                Text(
                    "If the date does not occur in a year (such as February 29), it runs on the last day of that month.",
                    color = WalletColors.muted
                )
            } else {
                OutlinedTextField(
                    value = minutes,
                    onValueChange = { minutes = it.filter(Char::isDigit) },
                    label = { Text("Remind me in minutes") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            if (repeatMonthly || repeatYearly) {
                OutlinedButton(
                    onClick = {
                        TimePickerDialog(
                            context,
                            { _, selectedHour, selectedMinute ->
                                hour = selectedHour
                                minute = selectedMinute
                            },
                            hour,
                            minute,
                            DateFormat.is24HourFormat(context)
                        ).show()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val time = LocalTime.of(hour, minute)
                        .format(DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault()))
                    Text("Time · $time")
                }
            }
            Button(
                onClick = {
                    val reminder = ReminderDraft(
                        title = title.ifBlank { "Wallet reminder" },
                        message = message.ifBlank { "You have an important money reminder" },
                        triggerAtMillis = if (repeatMonthly || repeatYearly) 0L else
                            System.currentTimeMillis() + (minutes.toLongOrNull() ?: 60) * 60_000,
                        monthlyDay = monthlyDay.takeIf { repeatMonthly },
                        yearlyMonth = yearlyMonth.takeIf { repeatYearly },
                        yearlyDay = yearlyDay.takeIf { repeatYearly },
                        hourOfDay = hour,
                        minute = minute,
                        id = editingId ?: UUID.randomUUID().toString()
                    )
                    save(reminder)
                    reminders = (reminders.filterNot { it.id == reminder.id } + reminder)
                    editorOpen = false
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(WalletBeige)
            ) {
                Text(if (editingId == null) "Set reminder" else "Save reminder", color = WalletColors.ink, fontWeight = FontWeight.Bold)
            }
            TextButton(onClick = { editorOpen = false }, modifier = Modifier.fillMaxWidth()) {
                Text("Cancel")
            }
        }
        }
    }
}

private fun ordinalSuffix(day: Int): String = when (day % 100) {
    11, 12, 13 -> "th"
    else -> when (day % 10) {
        1 -> "st"
        2 -> "nd"
        3 -> "rd"
        else -> "th"
    }
}
