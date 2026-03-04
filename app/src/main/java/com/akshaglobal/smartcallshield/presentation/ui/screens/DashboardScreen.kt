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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.foundation.layout.Box

@Composable
fun DashboardScreen(viewModel: DashboardViewModel = hiltViewModel(), callModesViewModel: CallModesViewModel = hiltViewModel()) {
    val currentMode by viewModel.currentMode.collectAsState()
    val isAppEnabled by viewModel.isAppEnabled.collectAsState()
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
    val deviceContacts by callModesViewModel.deviceContacts.collectAsState()
    // Fix: lastSelectedMode should be declared here, nullable, and initialized with currentMode
    var lastSelectedMode by remember { mutableStateOf<CallMode?>(currentMode) }

    // Sync lastSelectedMode with currentMode whenever currentMode changes
    LaunchedEffect(currentMode) {
        lastSelectedMode = currentMode
    }

    // Always allow the switch to be toggled
    // Enable Normal mode button if NORMAL mode exists

    // Always reload modes and contacts from the database when DashboardScreen is recomposed
    LaunchedEffect(Unit) {
        callModesViewModel.loadModes()
    }
    LaunchedEffect(modes, deviceContacts) {
        callModesViewModel.syncModesAndContacts()
    }

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
            // Switch is always enabled so user can interact
            Switch(
                checked = isAppEnabled,
                onCheckedChange = { checked ->
                    if (checked && !isAppEnabled) {
                        showEnableDialog = true
                    } else if (!checked && isAppEnabled) {
                        showDisableDialog = true
                    }
                },
                enabled = true // Always enabled for user interaction
            )
        }
        // Show dialog to enable switch on first attempt
        if (showEnableDialog) {
            androidx.compose.material3.AlertDialog(
                onDismissRequest = { showEnableDialog = false },
                title = { Text("Enable CallShield") },
                text = {
                    Text("To enable CallShield, you must confirm. Normal mode will be set by default.")
                },
                confirmButton = {
                    Button(onClick = {
                        viewModel.toggleAppEnabled()
                        viewModel.setMode(CallMode.NORMAL)
                        callModesViewModel.createOrActivateMode("NORMAL")
                        showEnableDialog = false
                        SmartCallShieldApp.isCallShieldEnabled = true
                    }) { Text("OK") }
                },
                dismissButton = {
                    Button(onClick = { showEnableDialog = false }) { Text("Cancel") }
                },
                modifier = Modifier.fillMaxWidth(0.95f)
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
        // Guard to prevent multiple dialogs from being triggered in rapid succession
        var dialogInProgress by remember { mutableStateOf(false) }
        if (showModeChangeDialog && pendingMode != null) {
            androidx.compose.material3.AlertDialog(
                onDismissRequest = {
                    showModeChangeDialog = false
                    pendingMode = null
                    dialogInProgress = false
                },
                title = { Text("Change Call Mode") },
                text = {
                    Text("CallShield will be applied to the selected mode. Press OK to switch mode.")
                },
                confirmButton = {
                    Button(onClick = {
                        // Only update mode and setActiveMode here
                        viewModel.setMode(pendingMode!!)
                        val modeName = when (pendingMode) {
                            CallMode.FAMILY -> "FAMILY"
                            CallMode.DRIVING -> "DRIVING"
                            CallMode.EMERGENCY -> "EMERGENCY"
                            else -> "NORMAL"
                        }
                        val modeEntity = modes.find { it.name.equals(modeName, ignoreCase = true) }
                        if (modeEntity != null) {
                            callModesViewModel.setActiveMode(modeEntity.id)
                        }
                        lastSelectedMode = pendingMode // pendingMode is not null here
                        showModeChangeDialog = false
                        pendingMode = null
                        dialogInProgress = false
                    }) { Text("OK") }
                },
                dismissButton = {
                    Button(onClick = {
                        showModeChangeDialog = false
                        pendingMode = null
                        dialogInProgress = false
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

        // Show loading indicator if currentMode is not loaded (null or not set)
        if (currentMode == null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            return
        }

        // ModeSelector: All modes disabled if app is off. Normal enabled only if app is enabled. Others only if contacts exist and app is enabled.
        ModeSelector(
            currentMode, // use currentMode for selection
            enabled = isAppEnabled,
            normalEnabled = isAppEnabled,
            familyEnabled = isAppEnabled && enabledModes["FAMILY"] == true,
            drivingEnabled = isAppEnabled && enabledModes["DRIVING"] == true,
            emergencyEnabled = isAppEnabled && enabledModes["EMERGENCY"] == true
        ) { newMode ->
            if (!isAppEnabled) return@ModeSelector
            // Only show dialog if not already showing, not in progress, and mode is actually changing
            if (!showModeChangeDialog && !dialogInProgress && lastSelectedMode != newMode) {
                dialogInProgress = true
                pendingMode = newMode
                showModeChangeDialog = true
            }
        }
        // Only render the dialog if showModeChangeDialog is true and pendingMode is not null
        if (showModeChangeDialog && pendingMode != null) {
            androidx.compose.material3.AlertDialog(
                onDismissRequest = {
                    showModeChangeDialog = false
                    pendingMode = null
                    dialogInProgress = false
                },
                title = { Text("Change Call Mode") },
                text = {
                    Text("CallShield will be applied to the selected mode. Press OK to switch mode.")
                },
                confirmButton = {
                    Button(onClick = {
                        // Only update mode and setActiveMode here
                        viewModel.setMode(pendingMode!!)
                        val modeName = when (pendingMode) {
                            CallMode.FAMILY -> "FAMILY"
                            CallMode.DRIVING -> "DRIVING"
                            CallMode.EMERGENCY -> "EMERGENCY"
                            else -> "NORMAL"
                        }
                        val modeEntity = modes.find { it.name.equals(modeName, ignoreCase = true) }
                        if (modeEntity != null) {
                            callModesViewModel.setActiveMode(modeEntity.id)
                        }
                        lastSelectedMode = pendingMode
                        showModeChangeDialog = false
                        pendingMode = null
                        dialogInProgress = false
                    }) { Text("OK") }
                },
                dismissButton = {
                    Button(onClick = {
                        showModeChangeDialog = false
                        pendingMode = null
                        dialogInProgress = false
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
            icon = R.drawable.ic_blocked_call, // new blocked call icon
            backgroundColor = Color(0xFFFFEBEE)
        )

        Spacer(modifier = Modifier.height(12.dp))

        StatisticsCard(
            title = "Spam Prevented",
            value = spamCount.toString(),
            icon = R.drawable.ic_call_block, // new spam blocked icon (vector)
            backgroundColor = Color(0xFFE8F5E9)
        )

        Spacer(modifier = Modifier.height(12.dp))

        StatisticsCard(
            title = "Driving Mode Replies",
            value = drivingReplies.toString(),
            icon = R.drawable.ic_driving_message, // car + message
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
