package com.akshaglobal.smartcallshield.presentation.ui.screens

import android.Manifest
import android.os.Environment
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.FileProvider
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import com.akshaglobal.smartcallshield.presentation.viewmodel.CallModesViewModel
import com.google.accompanist.permissions.isGranted
import java.util.Locale
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import android.graphics.pdf.PdfDocument
import java.io.File
import java.io.FileOutputStream

@Composable
fun CallModeManagementCard() {
    var showModeDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Create & Manage Modes", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text("Setup Normal, Family, Driving, Emergency modes", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
fun CreateModeAlertDialog(
    onDismiss: () -> Unit,
    callModesViewModel: CallModesViewModel = androidx.hilt.navigation.compose.hiltViewModel()
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
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        ),
        title = {
            Text("Create & Manage Call Modes", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .imePadding()
                    .padding(horizontal = 16.dp)
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

                // Search Bar and List
                if (selectedMode != null) {
                    Text(
                        "Search Contacts",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp, top = 8.dp)
                    )

                    TextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        label = { Text("Search by name or number") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        singleLine = true
                    )

                    // Contacts List
                    Text(
                        "Select Contacts (${selectedContacts.size} selected)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false)
                            .heightIn(max = 250.dp)
                    ) {
                        items(filteredContacts) { contact ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
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
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
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
