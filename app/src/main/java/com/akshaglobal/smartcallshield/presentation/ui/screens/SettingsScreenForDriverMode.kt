package com.akshaglobal.smartcallshield.presentation.ui.screens

import SettingsViewModel
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Column
import androidx.compose.ui.Alignment

@Composable
fun SettingsScreenForDrivingMode(viewModel: SettingsViewModel = hiltViewModel()) {
    val hasDrivingContacts by viewModel.hasDrivingContacts.collectAsState()
    val drivingModeEnabled by viewModel.drivingModeEnabled.collectAsState()
    val showDrivingAlert = remember { mutableStateOf(false) }
    val showNoContactsAlert = remember { mutableStateOf(false) }

    // Driving Mode Section
    SettingsSectionHeader("Driving Mode")
    SettingCard(
        title = "Enable Driving Mode",
        description = "Automatically handle calls while driving",
        isEnabled = drivingModeEnabled,
        onToggle = { enabled ->
            if (enabled) {
                showDrivingAlert.value = true
            } else {
                viewModel.setDrivingModeEnabled(false)
            }
        }
    )
    if (showDrivingAlert.value) {
        AlertDialog(
            onDismissRequest = { showDrivingAlert.value = false },
            title = { Text("Driving Mode") },
            text = { Text("DRIVING MODE AUTO REPLY will be enabled if you did not pick the call for three to five ringing") },
            confirmButton = {
                Button(onClick = {
                    showDrivingAlert.value = false
                    if (hasDrivingContacts) {
                        viewModel.setDrivingModeEnabled(true)
                    } else {
                        showNoContactsAlert.value = true
                    }
                }) { Text("OK") }
            },
            dismissButton = {
                Button(onClick = { showDrivingAlert.value = false }) { Text("Cancel") }
            }
        )
    }
    if (showNoContactsAlert.value) {
        AlertDialog(
            onDismissRequest = { showNoContactsAlert.value = false },
            title = { Text("Driving Mode") },
            text = { Text("Add the contacts to the DRIVING MODE") },
            confirmButton = {
                Button(onClick = { showNoContactsAlert.value = false }) { Text("OK") }
            }
        )
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(vertical = 12.dp)
    )
}

@Composable
private fun SettingCard(
    title: String,
    description: String,
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    androidx.compose.material3.Card(
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(title)
                Text(description, style = androidx.compose.material3.MaterialTheme.typography.bodySmall)
            }
            androidx.compose.material3.Switch(
                checked = isEnabled,
                onCheckedChange = onToggle
            )
        }
    }
}