package com.akshaglobal.smartcallshield.presentation.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.provider.Settings
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.runtime.saveable.rememberSaveable
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
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.core.net.toUri

import androidx.compose.foundation.layout.size

@Composable
fun DashboardScreen(viewModel: DashboardViewModel = hiltViewModel(), callModesViewModel: CallModesViewModel = hiltViewModel()) {
    // Add this state to remember the last dialed number for fallback
    var lastDialAttemptedNumber by remember { mutableStateOf("") }
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

    var showDialerDialog by remember { mutableStateOf(false) }
    val context = androidx.compose.ui.platform.LocalContext.current
    val telecomManager = context.getSystemService(android.content.Context.TELECOM_SERVICE) as android.telecom.TelecomManager
    var isDefaultDialer by remember { mutableStateOf(telecomManager.defaultDialerPackage == context.packageName) }
    val deviceContactsList = deviceContacts // Already collected from ViewModel
    var showPermissionDeniedDialog by remember { mutableStateOf(false) }

    // Permission launcher for CALL_PHONE
    val callPhonePermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            if (!granted) {
                showPermissionDeniedDialog = true
            }
        }
    )

    // Helper to check permission
    fun hasCallPhonePermission(): Boolean {
        return androidx.core.content.ContextCompat.checkSelfPermission(
            context, Manifest.permission.CALL_PHONE
        ) == PackageManager.PERMISSION_GRANTED
    }

    // State to store a pending number to dial after default dialer is set (robust to process death)
    var pendingDialNumber by rememberSaveable { mutableStateOf("") }
    // Launcher for default dialer intent
    val defaultDialerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = {
            val telecomManager = context.getSystemService(android.content.Context.TELECOM_SERVICE) as android.telecom.TelecomManager
            isDefaultDialer = telecomManager.defaultDialerPackage == context.packageName
            Log.d("DIALER_FLOW", "Result callback: isDefaultDialer=$isDefaultDialer, pendingDialNumber=$pendingDialNumber")
            if (isDefaultDialer && pendingDialNumber.isNotBlank()) {
                if (androidx.core.content.ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                    val intent = android.content.Intent(android.content.Intent.ACTION_CALL).apply {
                        data = ("tel:" + pendingDialNumber).toUri()
                        flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    Log.d("DIALER_FLOW", "Calling number: $pendingDialNumber")
                    context.startActivity(intent)
                } else {
                    val intent = android.content.Intent(android.content.Intent.ACTION_DIAL).apply {
                        data = ("tel:" + pendingDialNumber).toUri()
                        flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    Log.d("DIALER_FLOW", "Opening dialer for number: $pendingDialNumber")
                    context.startActivity(intent)
                }
                pendingDialNumber = ""
            }
        }
    )
    var showSetDefaultDialerDialog by remember { mutableStateOf(false) }

    // Move dialNumberAndCall to this scope so it is accessible everywhere in the dialog
    val dialNumberAndCall: (String) -> Unit = { number ->
        if (number.isBlank()) {
            // Do nothing if blank
        } else {
            lastDialAttemptedNumber = number
            if (!isDefaultDialer) {
                pendingDialNumber = number // Store for after default dialer set
                showSetDefaultDialerDialog = true
            } else if (isDefaultDialer) {
                if (hasCallPhonePermission()) {
                    val intent = android.content.Intent(android.content.Intent.ACTION_CALL).apply {
                        data = ("tel:" + number).toUri()
                        flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(intent)
                } else {
                    // Request permission
                    callPhonePermissionLauncher.launch(Manifest.permission.CALL_PHONE)
                }
            } else {
                val intent = android.content.Intent(android.content.Intent.ACTION_DIAL).apply {
                    data = ("tel:" + number).toUri()
                    flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            }
        }
    }

    // Show dialog to prompt user to set as default dialer
    if (showSetDefaultDialerDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showSetDefaultDialerDialog = false },
            title = { Text("Set as Default Dialer") },
            text = { Text("To place calls directly, please set SmartCallShield as your device's default phone app.") },
            confirmButton = {
                Button(onClick = {
                    showSetDefaultDialerDialog = false
                    val dialerIntent = android.content.Intent(android.telecom.TelecomManager.ACTION_CHANGE_DEFAULT_DIALER).apply {
                        putExtra(android.telecom.TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME, context.packageName)
                    }
                    defaultDialerLauncher.launch(dialerIntent)
                }) { Text("Set as Default") }
            },
            dismissButton = {
                Button(onClick = {
                    showSetDefaultDialerDialog = false
                    pendingDialNumber = "" // Clear pending if cancelled
                    if (lastDialAttemptedNumber.isNotBlank()) {
                        val intent = android.content.Intent(android.content.Intent.ACTION_DIAL).apply {
                            data = ("tel:" + lastDialAttemptedNumber).toUri()
                            flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        context.startActivity(intent)
                    }
                }) { Text("Cancel") }
            },
            modifier = Modifier.fillMaxWidth(0.95f)
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
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

            Spacer(modifier = Modifier.height(24.dp))

            // --- Place dialer button and dialog here, below Today's Statistics ---
            Button(
                onClick = { showDialerDialog = true },
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 24.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_call_block),
                    contentDescription = "Open Dialer",
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Open Dialer")
            }
            if (showDialerDialog) {
                Dialog(onDismissRequest = { showDialerDialog = false }) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        var searchQuery by remember { mutableStateOf("") }
                        var dialNumber by remember { mutableStateOf("") }
                        val filteredContacts = remember(searchQuery, deviceContactsList) {
                            val filtered = if (searchQuery.isBlank()) deviceContactsList
                            else deviceContactsList.filter {
                                it.displayName.contains(searchQuery, ignoreCase = true) ||
                                it.phoneNumber.contains(searchQuery, ignoreCase = true)
                            }
                            filtered.distinctBy { it.phoneNumber }
                        }
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("Search Contacts", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            androidx.compose.material3.OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                label = { Text("Search by name or number") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            LazyColumn(
                                modifier = Modifier.heightIn(max = 150.dp).fillMaxWidth()
                            ) {
                                items(filteredContacts) { contact ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                            .background(if (dialNumber == contact.phoneNumber) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.Transparent)
                                            .clickable {
                                                dialNumberAndCall(contact.phoneNumber)
                                                showDialerDialog = false
                                            },
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.ic_mode_normal),
                                            contentDescription = null,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(contact.displayName.ifEmpty { "Unknown" }, fontWeight = FontWeight.Medium)
                                            Text(contact.phoneNumber, fontSize = 13.sp, color = Color.Gray)
                                        }
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = dialNumber,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                maxLines = 1
                            )
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                val dialPadRows = listOf(
                                    listOf("1", "2", "3"),
                                    listOf("4", "5", "6"),
                                    listOf("7", "8", "9"),
                                    listOf("*", "0", "#")
                                )
                                dialPadRows.forEach { row ->
                                    Row(
                                        horizontalArrangement = Arrangement.SpaceEvenly,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        row.forEach { symbol ->
                                            Button(
                                                onClick = { dialNumber += symbol },
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .padding(4.dp)
                                                    .height(56.dp),
                                                shape = MaterialTheme.shapes.medium,
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = MaterialTheme.colorScheme.surface,
                                                    contentColor = MaterialTheme.colorScheme.primary
                                                ),
                                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                                            ) {
                                                Text(symbol, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                                Row(
                                    horizontalArrangement = Arrangement.End,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Spacer(modifier = Modifier.weight(1f))
                                    Button(
                                        onClick = { if (dialNumber.isNotEmpty()) dialNumber = dialNumber.dropLast(1) },
                                        enabled = dialNumber.isNotEmpty(),
                                        modifier = Modifier
                                            .padding(4.dp)
                                            .height(48.dp),
                                        shape = MaterialTheme.shapes.medium,
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.surface,
                                            contentColor = MaterialTheme.colorScheme.error
                                        ),
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error)
                                    ) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.ic_backspace),
                                            contentDescription = "Delete",
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp),
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Button(
                                        onClick = {
                                            dialNumberAndCall(dialNumber)
                                            showDialerDialog = false
                                        },
                                        enabled = dialNumber.isNotBlank(),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.primary,
                                            contentColor = Color.White
                                        ),
                                        modifier = Modifier
                                            .height(56.dp)
                                            .width(120.dp)
                                    ) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.ic_call_block),
                                            contentDescription = "Dial",
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Dial")
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                Button(
                                    onClick = { showDialerDialog = false },
                                    modifier = Modifier.width(120.dp)
                                ) {
                                    Text("Close")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Show dialog if permission denied
    if (showPermissionDeniedDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showPermissionDeniedDialog = false },
            title = { Text("Permission Required") },
            text = { Text("Please grant the CALL_PHONE permission to place calls directly from this app.") },
            confirmButton = {
                Button(onClick = { showPermissionDeniedDialog = false }) { Text("OK") }
            },
            modifier = Modifier.fillMaxWidth(0.95f)
        )
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
                Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold)
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
