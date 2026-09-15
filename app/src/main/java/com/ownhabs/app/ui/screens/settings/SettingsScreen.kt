package com.ownhabs.app.ui.screens.settings

import android.content.Intent
import android.net.Uri
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.ownhabs.app.data.backup.BackupManager
import com.ownhabs.app.data.backup.ImportMode
import com.ownhabs.app.data.backup.ImportResult
import com.ownhabs.app.ui.strings.Lang
import com.ownhabs.app.ui.strings.LocalLang
import com.ownhabs.app.ui.strings.LocalStrings
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    backupManager: BackupManager,
    onBack: () -> Unit,
    onLanguageChange: (Lang) -> Unit
) {
    val strings = LocalStrings.current
    val currentLang = LocalLang.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var pendingImportUri by remember { mutableStateOf<Uri?>(null) }
    var showImportModeDialog by remember { mutableStateOf(false) }

    // Ξαναελέγχει κάθε φορά που η οθόνη ξαναγίνεται ορατή (π.χ. επιστροφή από τις ρυθμίσεις συστήματος).
    var isBatteryExempted by remember {
        mutableStateOf(
            (context.getSystemService(android.content.Context.POWER_SERVICE) as PowerManager)
                .isIgnoringBatteryOptimizations(context.packageName)
        )
    }
    val lifecycleOwner = androidx.compose.ui.platform.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                isBatteryExempted = (context.getSystemService(android.content.Context.POWER_SERVICE) as PowerManager)
                    .isIgnoringBatteryOptimizations(context.packageName)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val ok = backupManager.exportTo(uri)
                statusMessage = if (ok) "✅" else "⚠️"
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            pendingImportUri = uri
            showImportModeDialog = true
        }
    }

    if (showImportModeDialog && pendingImportUri != null) {
        AlertDialog(
            onDismissRequest = { showImportModeDialog = false },
            title = { Text(if (currentLang == Lang.EL) "Τρόπος Εισαγωγής" else "Import Mode") },
            text = {
                Text(
                    if (currentLang == Lang.EL)
                        "«Αντικατάσταση» διαγράφει τα τρέχοντα τοπικά δεδομένα. «Συγχώνευση» ενώνει τις εγγραφές χωρίς διπλότυπα."
                    else
                        "\"Overwrite\" deletes your current local data. \"Merge\" combines records without duplicates."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val uri = pendingImportUri!!
                    showImportModeDialog = false
                    scope.launch {
                        val result = backupManager.importFrom(uri, ImportMode.OVERWRITE)
                        statusMessage = describeResult(result, currentLang)
                    }
                }) { Text(if (currentLang == Lang.EL) "Αντικατάσταση" else "Overwrite") }
            },
            dismissButton = {
                TextButton(onClick = {
                    val uri = pendingImportUri!!
                    showImportModeDialog = false
                    scope.launch {
                        val result = backupManager.importFrom(uri, ImportMode.MERGE)
                        statusMessage = describeResult(result, currentLang)
                    }
                }) { Text(if (currentLang == Lang.EL) "Συγχώνευση" else "Merge") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(strings.settingsTitle) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = strings.back) }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(strings.settingsPrivacyBanner, style = MaterialTheme.typography.bodyMedium)

            Card {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(strings.settingsBatteryTitle, style = MaterialTheme.typography.titleMedium)
                    Text(strings.settingsBatteryDesc, style = MaterialTheme.typography.bodyMedium)
                    if (isBatteryExempted) {
                        Text(strings.settingsBatteryAlreadyOk, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                    } else {
                        Button(onClick = { openBatteryOptimizationSettings(context) }) {
                            Text(strings.settingsBatteryButton)
                        }
                    }
                }
            }

            Card {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(strings.settingsLanguageTitle, style = MaterialTheme.typography.titleMedium)
                    Text(strings.settingsLanguageDesc, style = MaterialTheme.typography.bodyMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        FilterChip(
                            selected = currentLang == Lang.EL,
                            onClick = { onLanguageChange(Lang.EL) },
                            label = { Text("🇬🇷 Ελληνικά") }
                        )
                        FilterChip(
                            selected = currentLang == Lang.EN,
                            onClick = { onLanguageChange(Lang.EN) },
                            label = { Text("🇬🇧 English") }
                        )
                    }
                }
            }

            Card {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(strings.settingsExportTitle, style = MaterialTheme.typography.titleMedium)
                    Text(strings.settingsExportDesc, style = MaterialTheme.typography.bodyMedium)
                    Button(onClick = {
                        val fileName = "ownhabs_backup_${SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date())}.json"
                        exportLauncher.launch(fileName)
                    }) { Text(strings.settingsExportButton) }
                }
            }

            Card {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(strings.settingsImportTitle, style = MaterialTheme.typography.titleMedium)
                    Text(strings.settingsImportDesc, style = MaterialTheme.typography.bodyMedium)
                    Button(onClick = { importLauncher.launch(arrayOf("application/json")) }) {
                        Text(strings.settingsImportButton)
                    }
                }
            }

            statusMessage?.let {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(it, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

private fun openBatteryOptimizationSettings(context: android.content.Context) {
    // Προτίμηση: απευθείας διάλογος εξαίρεσης για ΑΥΤΗ την εφαρμογή.
    runCatching {
        val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
            data = Uri.parse("package:${context.packageName}")
        }
        context.startActivity(intent)
        return
    }
    // Fallback 1: γενική λίστα εξαιρέσεων μπαταρίας (αν κάποιο OEM μπλοκάρει το άμεσο intent).
    runCatching {
        context.startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
        return
    }
    // Fallback 2: σελίδα λεπτομερειών της ίδιας της εφαρμογής.
    runCatching {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.parse("package:${context.packageName}")
        }
        context.startActivity(intent)
    }
}

private fun describeResult(result: ImportResult, lang: Lang): String = when (result) {
    is ImportResult.Success ->
        if (lang == Lang.EL) "Εισήχθησαν ${result.habitsImported} συνήθειες και ${result.logsImported} καταγραφές."
        else "Imported ${result.habitsImported} habits and ${result.logsImported} logs."
    is ImportResult.InvalidFile ->
        if (lang == Lang.EL) "Μη έγκυρο αρχείο: ${result.reason}"
        else "Invalid file: ${result.reason}"
}
