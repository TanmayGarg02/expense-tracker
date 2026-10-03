package com.example.walletmanager.ui.screen.more

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.walletmanager.data.model.Currency
import com.example.walletmanager.data.model.WalletUiState
import com.example.walletmanager.notifications.ReminderDraft
import com.example.walletmanager.ui.component.MoreRow
import com.example.walletmanager.ui.component.ScreenHeader
import com.example.walletmanager.ui.component.WalletColors
import com.example.walletmanager.ui.component.WalletPage
import com.example.walletmanager.ui.component.WalletSheetBackButton
import com.example.walletmanager.ui.theme.WalletGreen
import com.example.walletmanager.ui.theme.WalletSurface
import java.io.File

@Composable
fun MoreScreen(
    state: WalletUiState,
    setCurrency: (Currency) -> Unit,
    addNote: (String) -> Unit,
    export: () -> Unit,
    import: () -> Unit,
    addReminder: (ReminderDraft) -> Unit,
    setProfile: (String, String) -> Unit,
    wipeData: () -> Unit,
    onBack: (() -> Unit)? = null,
    openProfileOnEntry: Boolean = false
) {
    var currencySheet by rememberSaveable { mutableStateOf(false) }
    var noteSheet by rememberSaveable { mutableStateOf(false) }
    var reminderSheet by rememberSaveable { mutableStateOf(false) }
    var profileSheet by rememberSaveable { mutableStateOf(openProfileOnEntry) }
    var wipeSheet by rememberSaveable { mutableStateOf(false) }
    var profileImageUri by rememberSaveable { mutableStateOf(state.profileImageUri) }

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
                        colors = ButtonDefaults.buttonColors(WalletGreen)
                    ) {
                        Text("Export CSV", color = WalletColors.ink)
                    }
                }
                state.message?.let {
                    Text(it, color = WalletGreen)
                }
            }
        }

        MoreRow(
            glyph = "💱",
            title = "Base currency",
            subtitle = "${state.baseCurrency.code} · INR is the default"
        ) {
            currencySheet = true
        }
        MoreRow("👤", "Profile", "${state.profileName} · Add a profile picture") {
            profileSheet = true
        }
        MoreRow("📝", "Notes", "${state.notes.size} saved notes") {
            noteSheet = true
        }
        MoreRow("🔔", "Important reminders", "Only notify me about things I choose") {
            reminderSheet = true
        }
        MoreRow("🎨", "Appearance", "Dark theme with green and red signals")
        MoreRow("🗑️", "Wipe all data", "Delete all accounts, transactions and investments") {
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
            dismiss = { noteSheet = false },
            save = {
                addNote(it)
                noteSheet = false
            }
        )
    }

    if (reminderSheet) {
        ReminderSheet(
            dismiss = { reminderSheet = false },
            save = {
                addReminder(it)
                reminderSheet = false
            }
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WipeDataSheet(
    dismiss: () -> Unit,
    wipe: () -> Unit
) {
    var confirmation by rememberSaveable { mutableStateOf("") }

    ModalBottomSheet(
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
                colors = ButtonDefaults.buttonColors(WalletColors.red)
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

    ModalBottomSheet(
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
                Text(if (imageUri.isBlank()) "🖼️ Choose profile picture" else "🖼️ Change profile picture")
            }
            Button(
                onClick = { save(editedName, imageUri) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(WalletGreen)
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
    ModalBottomSheet(
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
    dismiss: () -> Unit,
    save: (String) -> Unit
) {
    var value by rememberSaveable { mutableStateOf("") }

    ModalBottomSheet(
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
            Text("Add note", fontWeight = FontWeight.Bold)
            OutlinedTextField(
                value = value,
                onValueChange = { value = it },
                label = { Text("Note") },
                minLines = 3,
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                onClick = {
                    save(value)
                    dismiss()
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(WalletGreen)
            ) {
                Text("Save note", color = WalletColors.ink, fontWeight = FontWeight.Bold)
            }
            TextButton(onClick = dismiss, modifier = Modifier.fillMaxWidth()) {
                Text("Cancel")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReminderSheet(
    dismiss: () -> Unit,
    save: (ReminderDraft) -> Unit
) {
    var title by rememberSaveable { mutableStateOf("") }
    var message by rememberSaveable { mutableStateOf("") }
    var minutes by rememberSaveable { mutableStateOf("60") }

    ModalBottomSheet(
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
            Text("Important reminder", fontWeight = FontWeight.Bold)
            Text(
                "Use this for a real deadline, payment, or action. Wallet Manager will not send routine notifications.",
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
            OutlinedTextField(
                value = minutes,
                onValueChange = { minutes = it.filter(Char::isDigit) },
                label = { Text("Remind me in minutes") },
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                onClick = {
                    save(
                        ReminderDraft(
                            title = title.ifBlank { "Wallet reminder" },
                            message = message.ifBlank { "You have an important money reminder" },
                            triggerAtMillis = System.currentTimeMillis() + (minutes.toLongOrNull() ?: 60) * 60_000
                        )
                    )
                    dismiss()
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(WalletGreen)
            ) {
                Text("Set reminder", color = WalletColors.ink, fontWeight = FontWeight.Bold)
            }
            TextButton(onClick = dismiss, modifier = Modifier.fillMaxWidth()) {
                Text("Cancel")
            }
        }
    }
}
