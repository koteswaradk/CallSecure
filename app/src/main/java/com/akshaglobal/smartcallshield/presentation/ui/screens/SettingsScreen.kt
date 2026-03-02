package com.akshaglobal.smartcallshield.presentation.ui.screens

import android.Manifest
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
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import com.akshaglobal.smartcallshield.presentation.viewmodel.SettingsViewModel
import com.akshaglobal.smartcallshield.presentation.viewmodel.CallModesViewModel
import com.google.accompanist.permissions.isGranted
import java.util.Locale

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    isAppEnabled: Boolean // <-- Add this parameter
) {
    val spamDetectionEnabled by viewModel.spamDetectionEnabled.collectAsState()
    val drivingModeEnabled by viewModel.drivingModeEnabled.collectAsState()
    val drivingModeAutoReply by viewModel.drivingModeAutoReply.collectAsState()
    val spamConfidenceThreshold by viewModel.spamConfidenceThreshold.collectAsState()
    val autoRejectSpam by viewModel.autoRejectSpam.collectAsState()

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

        // Synchronize spam detection and auto-reject spam with app switch
        LaunchedEffect(isAppEnabled) {
            if (isAppEnabled) {
                if (!spamDetectionEnabled) viewModel.setSpamDetectionEnabled(true)
                if (!autoRejectSpam) viewModel.setAutoRejectSpam(true)
            } else {
                if (spamDetectionEnabled) viewModel.setSpamDetectionEnabled(false)
                if (autoRejectSpam) viewModel.setAutoRejectSpam(false)
            }
        }

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
            onToggle = { viewModel.setDrivingModeEnabled(it) }
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
            enabled = !drivingModeEnabled // Disable when driving mode is enabled
        )

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
