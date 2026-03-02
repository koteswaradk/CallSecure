package com.akshaglobal.smartcallshield.presentation.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.akshaglobal.smartcallshield.R
import com.akshaglobal.smartcallshield.data.model.CallMode
import com.akshaglobal.smartcallshield.presentation.viewmodel.CallModesViewModel
import com.akshaglobal.smartcallshield.presentation.viewmodel.DashboardViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import com.akshaglobal.smartcallshield.SmartCallShieldApp

@Composable
fun DashboardScreen(viewModel: DashboardViewModel = hiltViewModel(), callModesViewModel: CallModesViewModel = hiltViewModel()) {
    val currentMode by viewModel.currentMode.collectAsState()
    val isPremium by viewModel.isPremium.collectAsState()
    val blockedCount by viewModel.blockedCount.collectAsState()
    val spamCount by viewModel.spamCount.collectAsState()
    val drivingReplies by viewModel.drivingRepliesCount.collectAsState()
    val modes by callModesViewModel.modes.collectAsState()
    val enabledModes by callModesViewModel.enabledModes.collectAsState()
    var showModeErrorDialog by remember { mutableStateOf<String?>(null) }
    var showEnableDialog by remember { mutableStateOf(false) }
    var showWarningDialog by remember { mutableStateOf(false) }
    var showDisableDialog by remember { mutableStateOf(false) }
    var showModeChangeDialog by remember { mutableStateOf(false) }
    var pendingMode: CallMode? by remember { mutableStateOf(null) }
    val isAppEnabled by viewModel.isAppEnabled.collectAsState()
    val deviceContacts by callModesViewModel.deviceContacts.collectAsState()

    // Always allow the switch to be toggled
    // Enable Normal mode button if NORMAL mode exists

    // Always reload modes and contacts from the database when DashboardScreen is recomposed
    LaunchedEffect(Unit) {
        callModesViewModel.loadModes()
    }
    LaunchedEffect(modes, deviceContacts) {
        callModesViewModel.syncModesAndContacts()
    }

    // Add a loading state: wait until modes are loaded and currentMode is not null
    val isLoading = modes.isEmpty() || currentMode == null

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "SmartAICallShield",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            // Switch is always enabled
            Switch(
                checked = isAppEnabled,
                onCheckedChange = { checked ->
                    if (checked) {
                        showWarningDialog = true
                    } else {
                        showDisableDialog = true
                    }
                },
                enabled = true
            )
        }
        if (showWarningDialog) {
            androidx.compose.material3.AlertDialog(
                onDismissRequest = {},
                title = { Text("Enable CallShield") },
                text = {
                    Text("You are altering your phone's default call receiving behavior, which may alter your incoming calls by blocking spam, automated, and unknown calls. Press OK to enable CallShield. Normal mode will be set by default.")
                },
                confirmButton = {
                    Button(onClick = {
                        viewModel.toggleAppEnabled()
                        viewModel.setMode(CallMode.NORMAL)
                        showWarningDialog = false
                        SmartCallShieldApp.isCallShieldEnabled = true
                    }) { Text("OK") }
                },
                dismissButton = {
                    Button(onClick = {
                        showWarningDialog = false
                        SmartCallShieldApp.isCallShieldEnabled = false
                    }) { Text("Cancel") }
                },
                modifier = Modifier.fillMaxWidth(0.95f)
            )
        }
        if (showDisableDialog) {
            androidx.compose.material3.AlertDialog(
                onDismissRequest = {},
                title = { Text("Disable CallShield") },
                text = {
                    Text("You are disabling the switch. Your phone will receive ALL CALLS WITHOUT ANY CALL FILTERING, including unknown calls. Press OK to disable CallShield.")
                },
                confirmButton = {
                    Button(onClick = {
                        viewModel.toggleAppEnabled()
                        // Optionally, clear the mode or set to a disabled state
                        showDisableDialog = false
                    }) { Text("OK") }
                },
                dismissButton = {
                    Button(onClick = {
                        showDisableDialog = false
                    }) { Text("Cancel") }
                },
                modifier = Modifier.fillMaxWidth(0.95f)
            )
        }
        if (showEnableDialog) {
            androidx.compose.material3.AlertDialog(
                onDismissRequest = { showEnableDialog = false },
                title = { Text("Enable CallShield") },
                text = {
                    Text("To enable CallShield, you must first create a 'NORMAL' mode in Call Modes Management.")
                },
                confirmButton = {
                    Button(onClick = { showEnableDialog = false }) {
                        Text("Close")
                    }
                },
                dismissButton = {},
                modifier = Modifier.fillMaxWidth(0.95f)
            )
        }
        if (showModeChangeDialog && pendingMode != null) {
            androidx.compose.material3.AlertDialog(
                onDismissRequest = {},
                title = { Text("Change Call Mode") },
                text = {
                    Text("CallShield will be applied to the selected mode. Press OK to switch mode.")
                },
                confirmButton = {
                    Button(onClick = {
                        viewModel.setMode(pendingMode!!)
                        showModeChangeDialog = false
                        pendingMode = null
                    }) { Text("OK") }
                },
                dismissButton = {
                    Button(onClick = {
                        showModeChangeDialog = false
                        pendingMode = null
                    }) { Text("Cancel") }
                },
                modifier = Modifier.fillMaxWidth(0.95f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Premium Badge
        if (isPremium) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFFD700)
                )
            ) {
                Text(
                    "✨ Premium Features Unlocked",
                    modifier = Modifier.padding(16.dp),
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Mode Selection
        Text(
            "Call Mode",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        // ModeSelector: All modes disabled if switch is off. Normal enabled if switch is on. Others only if contacts exist.
        ModeSelector(
            currentMode,
            enabled = isAppEnabled,
            normalEnabled = isAppEnabled,
            familyEnabled = isAppEnabled && enabledModes["FAMILY"] == true,
            drivingEnabled = isAppEnabled && enabledModes["DRIVING"] == true,
            emergencyEnabled = isAppEnabled && enabledModes["EMERGENCY"] == true
        ) { newMode ->
            if (!isAppEnabled) return@ModeSelector // Don't allow mode change if app is disabled
            val modeName = when (newMode) {
                CallMode.FAMILY -> "FAMILY"
                CallMode.DRIVING -> "DRIVING"
                CallMode.EMERGENCY -> "EMERGENCY"
                else -> "NORMAL"
            }
            val modeEntity = modes.find { it.name.equals(modeName, ignoreCase = true) }
            val hasContacts = enabledModes[modeName] == true
            if (modeName != "NORMAL" && !hasContacts) {
                showModeErrorDialog = "No contacts found for $modeName mode. Please add contacts to this category in the contacts table."
                return@ModeSelector
            }
            if (modeEntity == null && modeName != "NORMAL") {
                showModeErrorDialog = "No $modeName mode found. Please create it in Call Modes Management."
                return@ModeSelector
            }
            // Show confirmation dialog for mode change
            if (currentMode != newMode) {
                pendingMode = newMode
                showModeChangeDialog = true
            }
        }
        if (showModeChangeDialog && pendingMode != null) {
            androidx.compose.material3.AlertDialog(
                onDismissRequest = {},
                title = { Text("Change Call Mode") },
                text = {
                    Text("CallShield will be applied to the selected mode. Press OK to switch mode.")
                },
                confirmButton = {
                    Button(onClick = {
                        viewModel.setMode(pendingMode!!)
                        showModeChangeDialog = false
                        pendingMode = null
                    }) { Text("OK") }
                },
                dismissButton = {
                    Button(onClick = {
                        showModeChangeDialog = false
                        pendingMode = null
                    }) { Text("Cancel") }
                },
                modifier = Modifier.fillMaxWidth(0.95f)
            )
        }
        if (showModeErrorDialog != null) {
            androidx.compose.material3.AlertDialog(
                onDismissRequest = { showModeErrorDialog = null },
                title = { Text("Mode Selection Error") },
                text = { Text(showModeErrorDialog ?: "") },
                confirmButton = {
                    Button(onClick = { showModeErrorDialog = null }) { Text("Close") }
                },
                dismissButton = {},
                modifier = Modifier.fillMaxWidth(0.95f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Statistics Cards
        Text(
            "Today's Statistics",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        StatisticsCard(
            title = "Blocked Calls",
            value = blockedCount.toString(),
            icon = R.drawable.ic_launcher_foreground,
            backgroundColor = Color(0xFFFFEBEE)
        )

        Spacer(modifier = Modifier.height(12.dp))

        StatisticsCard(
            title = "Spam Prevented",
            value = spamCount.toString(),
            icon = R.drawable.ic_launcher_foreground,
            backgroundColor = Color(0xFFE8F5E9)
        )

        Spacer(modifier = Modifier.height(12.dp))

        StatisticsCard(
            title = "Driving Mode Replies",
            value = drivingReplies.toString(),
            icon = R.drawable.ic_launcher_foreground,
            backgroundColor = Color(0xFFE3F2FD)
        )

        // Debug printout for enabledModes
        println("[DEBUG] enabledModes: $enabledModes")
    }
}

@Composable
private fun ModeSelector(
    currentMode: CallMode?,
    enabled: Boolean,
    normalEnabled: Boolean = false,
    familyEnabled: Boolean = false,
    drivingEnabled: Boolean = false,
    emergencyEnabled: Boolean = false,
    onModeSelected: (CallMode) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        ModeButton(
            label = "Normal",
            iconRes = R.drawable.ic_mode_normal,
            isSelected = currentMode == CallMode.NORMAL,
            enabled = enabled && normalEnabled,
            onClick = { onModeSelected(CallMode.NORMAL) }
        )
        ModeButton(
            label = "Family",
            iconRes = R.drawable.ic_mode_family,
            isSelected = currentMode == CallMode.FAMILY,
            enabled = enabled && familyEnabled,
            onClick = { onModeSelected(CallMode.FAMILY) }
        )
        ModeButton(
            label = "Driving",
            iconRes = R.drawable.ic_mode_driving,
            isSelected = currentMode == CallMode.DRIVING,
            enabled = enabled && drivingEnabled,
            onClick = { onModeSelected(CallMode.DRIVING) }
        )
        ModeButton(
            label = "Emergency",
            iconRes = R.drawable.ic_mode_emergency,
            isSelected = currentMode == CallMode.EMERGENCY,
            enabled = enabled && emergencyEnabled,
            onClick = { onModeSelected(CallMode.EMERGENCY) }
        )
    }
}

@Composable
private fun ModeButton(
    label: String,
    iconRes: Int,
    isSelected: Boolean,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .width(85.dp)
            .height(85.dp)
            .padding(4.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.LightGray
        )
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = label,
                modifier = Modifier
                    .width(36.dp)
                    .height(36.dp),
                tint = if (isSelected && enabled) Color.Unspecified else Color.White
            )
        }
    }
}

@Composable
private fun StatisticsCard(
    title: String,
    value: String,
    icon: Int,
    backgroundColor: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(title, fontSize = 14.sp, color = Color.Gray)
                Text(value, fontSize = 28.sp, fontWeight = FontWeight.Bold)
            }
            Icon(
                painter = painterResource(id = icon),
                contentDescription = title,
                modifier = Modifier
                    .width(40.dp)
                    .height(40.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}
