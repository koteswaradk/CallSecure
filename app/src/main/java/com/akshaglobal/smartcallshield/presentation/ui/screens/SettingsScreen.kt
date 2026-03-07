package com.akshaglobal.smartcallshield.presentation.ui.screens

import android.Manifest
import android.content.Intent
import android.os.Environment
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import com.akshaglobal.smartcallshield.presentation.viewmodel.SettingsViewModel
import com.akshaglobal.smartcallshield.presentation.viewmodel.CallModesViewModel
import com.google.accompanist.permissions.isGranted
import java.util.Locale
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import android.graphics.pdf.PdfDocument
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    isAppEnabled: Boolean?, // Accept nullable for loading state
    callModesViewModel: CallModesViewModel = hiltViewModel() // <-- Inject CallModesViewModel
) {
    val spamDetectionEnabled by viewModel.spamDetectionEnabled.collectAsState()
    val drivingModeEnabled by viewModel.drivingModeEnabled.collectAsState()
    val drivingModeAutoReply by viewModel.drivingModeAutoReply.collectAsState()
    val spamConfidenceThreshold by viewModel.spamConfidenceThreshold.collectAsState()
    val autoRejectSpam by viewModel.autoRejectSpam.collectAsState()
    val modesState = callModesViewModel.modes.collectAsState(initial = emptyList())
    val modes = modesState.value

    val drivingModeExists = modes.any { it.name.equals("Driving", ignoreCase = true) }

    if (isAppEnabled == null) {
        // Show loading indicator while state is loading
        androidx.compose.material3.CircularProgressIndicator(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 64.dp)
        )
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Header
        Text(
            "Settings",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Spam Detection Section
        SettingsSectionHeader("Spam Detection")
        SettingCard(
            title = "Enable Spam Detection",
            description = "Use AI to detect spam calls",
            isEnabled = spamDetectionEnabled,
            onToggle = { viewModel.setSpamDetectionEnabled(it) },
            enabled = isAppEnabled // Only enable if app is enabled
        )

        Spacer(modifier = Modifier.height(8.dp))

        SettingCard(
            title = "Auto-Reject Spam",
            description = "Automatically reject detected spam calls",
            isEnabled = autoRejectSpam,
            onToggle = { viewModel.setAutoRejectSpam(it) },
            enabled = isAppEnabled // Only enable if app is enabled
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            "Confidence Threshold: ${String.format(Locale.getDefault(), "%.1f", spamConfidenceThreshold * 100)}%",
            modifier = Modifier.padding(8.dp)
        )
        Slider(
            value = spamConfidenceThreshold,
            onValueChange = { viewModel.setSpamConfidenceThreshold(it) },
            valueRange = 0.5f..1.0f,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Driving Mode Section
        SettingsSectionHeader("Driving Mode")
        SettingCard(
            title = "Enable Driving Mode",
            description = "Automatically handle calls while driving",
            isEnabled = drivingModeEnabled,
            onToggle = { viewModel.setDrivingModeEnabled(it) },
            enabled = isAppEnabled && drivingModeExists // Enable only if app is enabled and Driving mode exists
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            "Auto-Reply Message",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(8.dp)
        )
        TextField(
            value = drivingModeAutoReply,
            onValueChange = { viewModel.setDrivingModeAutoReply(it) },
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .padding(8.dp),
            label = { Text("Reply message") },
            maxLines = 5,
            enabled = !drivingModeExists, // Enable only if Driving mode does not exist
            placeholder = { if (drivingModeExists) Text("Edit in Driving Mode setup") else Text("Enter auto-reply message") }
        )
        if (drivingModeExists) {
            Text(
                "To edit the auto-reply message, remove the Driving mode from Call Modes Management.",
                color = Color.Gray,
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Call Modes Management Section
        SettingsSectionHeader("Call Modes Management")
        CallModeManagementCard()

        Spacer(modifier = Modifier.height(24.dp))

        // Privacy Section
        SettingsSectionHeader("Privacy & Data")
        SettingCard(
            title = "Analytics",
            description = "Help improve the app with usage data",
            isEnabled = true,
            onToggle = { }
        )

        Spacer(modifier = Modifier.height(8.dp))

        SettingCard(
            title = "Cloud Sync (Premium)",
            description = "Sync your settings across devices",
            isEnabled = false,
            onToggle = {
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // --- Export Data Section ---
        ExportDataSection()

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        title,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .padding(vertical = 12.dp)
            .padding(start = 8.dp)
    )
}

@Composable
private fun SettingCard(
    title: String,
    description: String,
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit,
    enabled: Boolean = true // <-- Add enabled parameter
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text(description, fontSize = 12.sp, color = Color.Gray)
            }
            Switch(
                checked = isEnabled,
                onCheckedChange = onToggle,
                enabled = enabled // <-- Control enabled state
            )
        }
    }
}

@Composable
private fun CallModeManagementCard() {
    var showModeDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Create & Manage Modes", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text("Setup Normal, Family, Driving, Emergency modes", fontSize = 12.sp, color = Color.Gray)
            }
            Button(
                onClick = { showModeDialog = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Setup", fontSize = 12.sp)
                Spacer(modifier = Modifier.width(4.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Setup modes")
            }
        }
    }

    if (showModeDialog) {
        CreateModeAlertDialog(
            onDismiss = { showModeDialog = false }
        )
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun CreateModeAlertDialog(
    onDismiss: () -> Unit,
    callModesViewModel: CallModesViewModel = hiltViewModel()
) {
    val deviceContacts by callModesViewModel.deviceContacts.collectAsState()
    val contactsPermissionState = rememberPermissionState(Manifest.permission.READ_CONTACTS)

    var selectedMode by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedContacts by remember { mutableStateOf<Set<String>>(emptySet()) }
    var showModeDropdown by remember { mutableStateOf(false) }

    val predefinedModes = listOf("Family", "Driving", "Emergency")

    // Request permission and load contacts on first load
    LaunchedEffect(Unit) {
        if (!contactsPermissionState.status.isGranted) {
            contactsPermissionState.launchPermissionRequest()
        } else {
            callModesViewModel.loadDeviceContacts()
        }
    }

    // Reload contacts when permission is granted
    LaunchedEffect(contactsPermissionState.status.isGranted) {
        if (contactsPermissionState.status.isGranted) {
            callModesViewModel.loadDeviceContacts()
        }
    }

    // Filter contacts based on search query and remove duplicates
    val uniquePhoneNumbers = remember(deviceContacts) {
        deviceContacts.distinctBy { it.phoneNumber }
    }

    val filteredContacts = remember(uniquePhoneNumbers, searchQuery) {
        if (searchQuery.isBlank()) {
            uniquePhoneNumbers
        } else {
            uniquePhoneNumbers.filter {
                it.displayName.contains(searchQuery, ignoreCase = true) ||
                        it.phoneNumber.contains(searchQuery)
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Create & Manage Call Modes", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Mode Selection Dropdown
                Text(
                    "Select Call Mode",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                OutlinedButton(
                    onClick = { showModeDropdown = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Text(selectedMode ?: "Choose a mode", modifier = Modifier.weight(1f))
                    Text("▼", fontSize = 12.sp)
                }

                DropdownMenu(
                    expanded = showModeDropdown,
                    onDismissRequest = { showModeDropdown = false },
                    modifier = Modifier.widthIn(max = 250.dp).fillMaxWidth()
                ) {
                    predefinedModes.forEach { mode ->
                        DropdownMenuItem(
                            text = { Text(mode) },
                            onClick = {
                                selectedMode = mode
                                showModeDropdown = false
                            }
                        )
                    }
                }

                // Search Bar
                if (selectedMode != null) {
                    Text(
                        "Search Contacts",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp, top = 16.dp)
                    )

                    TextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        label = { Text("Search by name or number") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        singleLine = true
                    )

                    // Contacts List
                    Text(
                        "Select Contacts (${selectedContacts.size} selected)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 300.dp)
                    ) {
                        items(filteredContacts) { contact ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        contact.displayName,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        contact.phoneNumber,
                                        fontSize = 12.sp,
                                        color = Color.Gray
                                    )
                                }
                                Checkbox(
                                    checked = selectedContacts.contains(contact.phoneNumber),
                                    onCheckedChange = { isSelected ->
                                        selectedContacts = if (isSelected) {
                                            selectedContacts + contact.phoneNumber
                                        } else {
                                            selectedContacts - contact.phoneNumber
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (selectedMode != null && selectedContacts.isNotEmpty()) {
                        callModesViewModel.createMode(selectedMode!!, selectedContacts)
                        onDismiss()
                    }
                },
                enabled = selectedMode != null && selectedContacts.isNotEmpty()
            ) {
                Text("Create Mode")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        modifier = Modifier
            .fillMaxWidth(0.95f)
    )
}

@Composable
fun ExportDataSection() {
    val context = androidx.compose.ui.platform.LocalContext.current
    var showExportDialog by remember { mutableStateOf(false) }
    var exportType by remember { mutableStateOf<String?>(null) }

    Text(
        "Export Data",
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .padding(vertical = 12.dp)
            .padding(start = 8.dp)
    )

    Text("Export your call history and analytics report.", fontSize = 14.sp, color = Color.Gray)
    Spacer(modifier = Modifier.height(8.dp))

    Row(Modifier.fillMaxWidth()) {
        Button(
            onClick = {
                exportType = "call_history"
                showExportDialog = true
            },
            modifier = Modifier.weight(1f).padding(end = 4.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text("Export Call History")
        }
        Button(
            onClick = {
                exportType = "analytics"
                showExportDialog = true
            },
            modifier = Modifier.weight(1f).padding(start = 4.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text("Export Analytics")
        }
    }

    if (showExportDialog && exportType != null) {
        ExportDialog(
            exportType = exportType!!,
            onDismiss = { showExportDialog = false },
            onExport = { method: String ->
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val fileName = "SmartCallShield.pdf"
                        val documentsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
                        val smartCallShieldDir = File(documentsDir, "SmartCallShield")
                        if (!smartCallShieldDir.exists()) {
                            smartCallShieldDir.mkdirs()
                        }
                        val file = File(smartCallShieldDir, fileName)

                        // --- PDF GENERATION WITH ANDROID PdfDocument ---
                        val pdfDocument = PdfDocument()
                        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size
                        val page = pdfDocument.startPage(pageInfo)
                        val canvas = page.canvas
                        val paint = android.graphics.Paint()
                        paint.textSize = 24f
                        paint.isFakeBoldText = true
                        canvas.drawText(
                            if (exportType == "call_history") "Call History" else "Analytics Report",
                            40f, 60f, paint
                        )
                        paint.textSize = 12f
                        paint.isFakeBoldText = false
                        canvas.drawText("Exported on: ${java.time.LocalDateTime.now()}", 40f, 90f, paint)
                        // Table header
                        var y = 130f
                        paint.textSize = 14f
                        paint.isFakeBoldText = true
                        if (exportType == "call_history") {
                            canvas.drawText("Number", 40f, y, paint)
                            canvas.drawText("Type", 180f, y, paint)
                            canvas.drawText("Timestamp", 300f, y, paint)
                            canvas.drawText("Duration", 480f, y, paint)
                            y += 20f
                            paint.isFakeBoldText = false
                            // TODO: Fill call history rows from ViewModel/Repository
                            // Example row:
                            // canvas.drawText("+911234567890", 40f, y, paint)
                            // canvas.drawText("Incoming", 180f, y, paint)
                            // canvas.drawText("2026-03-05 10:00", 300f, y, paint)
                            // canvas.drawText("00:01:23", 480f, y, paint)
                        } else {
                            canvas.drawText("Metric", 40f, y, paint)
                            canvas.drawText("Value", 300f, y, paint)
                            y += 20f
                            paint.isFakeBoldText = false
                            // TODO: Fill analytics rows from ViewModel/Repository
                            // Example row:
                            // canvas.drawText("Total Calls", 40f, y, paint)
                            // canvas.drawText("123", 300f, y, paint)
                        }
                        pdfDocument.finishPage(page)
                        val outputStream = FileOutputStream(file)
                        pdfDocument.writeTo(outputStream)
                        pdfDocument.close()
                        outputStream.close()
                        if (method == "download") {
                            CoroutineScope(Dispatchers.Main).launch {
                                Toast.makeText(context, "PDF saved: ${file.absolutePath}", Toast.LENGTH_LONG).show()
                            }
                        } else if (method == "email") {
                            val uri = FileProvider.getUriForFile(
                                context,
                                context.packageName + ".provider",
                                file
                            )
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "application/pdf"
                                putExtra(Intent.EXTRA_SUBJECT, if (exportType == "call_history") "Call History" else "Analytics Report")
                                putExtra(Intent.EXTRA_TEXT, "Please find attached the exported ${if (exportType == "call_history") "call history" else "analytics report"} as PDF.")
                                putExtra(Intent.EXTRA_STREAM, uri)
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            CoroutineScope(Dispatchers.Main).launch {
                                context.startActivity(Intent.createChooser(intent, "Send Email"))
                            }
                        }
                    } catch (e: Exception) {
                        CoroutineScope(Dispatchers.Main).launch {
                            Toast.makeText(context, "Export failed: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        )
    }
}

@Composable
fun ExportDialog(exportType: String, onDismiss: () -> Unit, onExport: (String) -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Export ${if (exportType == "call_history") "Call History" else "Analytics Report"}", fontWeight = FontWeight.Bold)
        },
        text = {
            Column {
                Text("How would you like to export?", fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = { onExport("download"); onDismiss() }, modifier = Modifier.fillMaxWidth()) {
                    Text("Download PDF File")
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = { onExport("email"); onDismiss() }, modifier = Modifier.fillMaxWidth()) {
                    Text("Send via Email")
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            Button(onClick = onDismiss) { Text("Cancel") }
        },
        modifier = Modifier.fillMaxWidth(0.95f)
    )
}
