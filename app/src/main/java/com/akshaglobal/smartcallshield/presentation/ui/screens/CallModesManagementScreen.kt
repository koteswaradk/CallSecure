package com.akshaglobal.smartcallshield.presentation.ui.screens

import android.Manifest
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import com.akshaglobal.smartcallshield.data.model.DeviceContact
import com.akshaglobal.smartcallshield.presentation.viewmodel.CallModesViewModel
import com.google.accompanist.permissions.isGranted


@Preview
@Composable
private fun CallModesManagementScreenPreview() {
    CallModesManagementScreen()
}
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CallModesManagementScreen(viewModel: CallModesViewModel = hiltViewModel()) {
    val context = LocalContext.current
    // Remove showToast lambda logic, only use toastEvent collector
    LaunchedEffect(viewModel) {
        viewModel.toastEvent.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    val deviceContacts by viewModel.deviceContacts.collectAsState()
    val modes by viewModel.modes.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    var showCreateModeDialog by remember { mutableStateOf(false) }

    val contactsPermissionState = rememberPermissionState(Manifest.permission.READ_CONTACTS)

    // Request permission on first load
    LaunchedEffect(Unit) {
        if (!contactsPermissionState.status.isGranted) {
            contactsPermissionState.launchPermissionRequest()
        } else {
            viewModel.loadDeviceContacts()
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        // Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Call Modes",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                IconButton(
                    onClick = { showCreateModeDialog = true }
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Create Mode",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // Predefined Modes Section Header
        item {
            Text(
                "Preset Modes",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        // Preset Mode Cards
        val presetModes = listOf(
            Pair("Normal", "Allow all calls"),
            Pair("Family", "Only family contacts"),
            Pair("Driving", "Family & Emergency only"),
            Pair("Emergency", "Emergency contacts only")
        )

        items(presetModes) { (modeName, description) ->
            PresetModeCard(
                modeName = modeName,
                description = description,
                onSelect = {
                    viewModel.createPredefinedMode(modeName)
                }
            )
        }

        // Manage Contacts Section
        if (modes.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    "Manage Contacts in Modes",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            item {
                if (modes.size > 0) {
                    TabRow(selectedTabIndex = selectedTab.coerceIn(0, modes.size - 1)) {
                        modes.forEachIndexed { index, mode ->
                            Tab(
                                selected = selectedTab == index,
                                onClick = { selectedTab = index },
                                text = { Text(mode.name, fontSize = 12.sp) }
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))

                // Contact List for Selected Mode
                if (selectedTab < modes.size) {
                    ContactsSelectionListLazy(
                        mode = modes[selectedTab],
                        deviceContacts = deviceContacts,
                        viewModel = viewModel
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (showCreateModeDialog) {
        CreateModeDialog(
            onCreate = { modeName, selectedContacts ->
                viewModel.createMode(modeName, selectedContacts.map { it.phoneNumber }.toSet())
                showCreateModeDialog = false
            },
            onDismiss = { showCreateModeDialog = false },
            deviceContacts = deviceContacts
        )
    }
}

@Composable
private fun PresetModeCard(
    modeName: String,
    description: String,
    onSelect: () -> Unit
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
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(modeName, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text(description, fontSize = 12.sp, color = Color.Gray)
            }
            Button(onClick = onSelect) {
                Text("Create")
            }
        }
    }
}

@Composable
private fun ContactsSelectionListLazy(
    mode: com.akshaglobal.smartcallshield.data.model.ModeEntity,
    deviceContacts: List<DeviceContact>,
    viewModel: CallModesViewModel
) {
    var selectedContacts by remember { mutableStateOf(setOf<String>()) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .height(300.dp)
    ) {
        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {
            items(deviceContacts) { contact ->
                ContactSelectionCard(
                    contact = contact,
                    isSelected = selectedContacts.contains(contact.phoneNumber),
                    onToggled = { isSelected ->
                        selectedContacts = if (isSelected) {
                            selectedContacts + contact.phoneNumber
                        } else {
                            selectedContacts - contact.phoneNumber
                        }
                    }
                )
            }
        }

        // Save Button
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = {
                selectedContacts.forEach { phoneNumber ->
                    val contact = deviceContacts.find { it.phoneNumber == phoneNumber }
                    contact?.let {
                        viewModel.addContactToModeByPhone(mode.id, phoneNumber)
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Text("Save Contacts to ${mode.name}")
        }
    }
}

@Composable
private fun ContactSelectionCard(
    contact: DeviceContact,
    isSelected: Boolean,
    onToggled: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.White
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(contact.displayName, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text(contact.phoneNumber, fontSize = 12.sp, color = Color.Gray)
            }
            Checkbox(
                checked = isSelected,
                onCheckedChange = onToggled
            )
        }
    }
}

@Composable
private fun CreateModeDialog(
    onCreate: (String, List<DeviceContact>) -> Unit,
    onDismiss: () -> Unit,
    deviceContacts: List<DeviceContact>
) {
    var modeName by remember { mutableStateOf("") }
    var selectedContacts by remember { mutableStateOf<Set<String>>(emptySet()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create New Mode") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                TextField(
                    value = modeName,
                    onValueChange = { modeName = it },
                    label = { Text("Mode Name") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                )

                Text("Select Contacts", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    items(deviceContacts) { contact ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(contact.displayName, fontSize = 12.sp)
                                Text(contact.phoneNumber, fontSize = 10.sp, color = Color.Gray)
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
        },
        confirmButton = {
            Button(
                onClick = {
                    val contactsToAdd = deviceContacts.filter { selectedContacts.contains(it.phoneNumber) }
                    onCreate(modeName, contactsToAdd)
                },
                enabled = modeName.isNotBlank()
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
