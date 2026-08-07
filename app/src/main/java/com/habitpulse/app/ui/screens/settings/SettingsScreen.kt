package com.habitpulse.app.ui.screens.settings

import android.net.Uri
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
import com.habitpulse.app.data.backup.BackupManager
import com.habitpulse.app.data.backup.ImportMode
import com.habitpulse.app.data.backup.ImportResult
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(backupManager: BackupManager, onBack: () -> Unit) {
    val scope = rememberCoroutineScope()
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var pendingImportUri by remember { mutableStateOf<Uri?>(null) }
    var showImportModeDialog by remember { mutableStateOf(false) }

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val ok = backupManager.exportTo(uri)
                statusMessage = if (ok) "Η εξαγωγή ολοκληρώθηκε επιτυχώς." else "Η εξαγωγή απέτυχε."
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
            title = { Text("Τρόπος Εισαγωγής") },
            text = {
                Text("«Αντικατάσταση» διαγράφει τα τρέχοντα τοπικά δεδομένα. «Συγχώνευση» ενώνει τις εγγραφές βάσει UUID/Timestamp χωρίς διπλότυπα.")
            },
            confirmButton = {
                TextButton(onClick = {
                    val uri = pendingImportUri!!
                    showImportModeDialog = false
                    scope.launch {
                        val result = backupManager.importFrom(uri, ImportMode.OVERWRITE)
                        statusMessage = describeResult(result)
                    }
                }) { Text("Αντικατάσταση") }
            },
            dismissButton = {
                TextButton(onClick = {
                    val uri = pendingImportUri!!
                    showImportModeDialog = false
                    scope.launch {
                        val result = backupManager.importFrom(uri, ImportMode.MERGE)
                        statusMessage = describeResult(result)
                    }
                }) { Text("Συγχώνευση") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ρυθμίσεις & Τοπικό Backup") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Πίσω") }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Η εφαρμογή είναι 100% τοπική (Local-First). Δεν υπάρχει backend ή cloud server — όλα τα δεδομένα σου μένουν στη συσκευή σου.",
                style = MaterialTheme.typography.bodyMedium
            )

            Card {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Εξαγωγή Δεδομένων", style = MaterialTheme.typography.titleMedium)
                    Text("Αποθηκεύει όλες τις συνήθειες, το ιστορικό και τα σήματα σε ένα αρχείο .json.", style = MaterialTheme.typography.bodyMedium)
                    Button(onClick = {
                        val fileName = "habitpulse_backup_${SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date())}.json"
                        exportLauncher.launch(fileName)
                    }) { Text("Εξαγωγή σε .json") }
                }
            }

            Card {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Εισαγωγή Δεδομένων", style = MaterialTheme.typography.titleMedium)
                    Text("Φόρτωσε ένα αρχείο .json από άλλη συσκευή ή παλιό backup.", style = MaterialTheme.typography.bodyMedium)
                    Button(onClick = { importLauncher.launch(arrayOf("application/json")) }) {
                        Text("Επιλογή αρχείου .json")
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

private fun describeResult(result: ImportResult): String = when (result) {
    is ImportResult.Success -> "Εισήχθησαν ${result.habitsImported} συνήθειες και ${result.logsImported} καταγραφές."
    is ImportResult.InvalidFile -> "Μη έγκυρο αρχείο: ${result.reason}"
}
