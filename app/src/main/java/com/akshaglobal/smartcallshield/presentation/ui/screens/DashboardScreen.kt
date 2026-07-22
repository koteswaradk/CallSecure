package com.akshaglobal.smartcallshield.presentation.ui.screens

import android.Manifest
import android.content.pm.PackageManager
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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.core.net.toUri

import androidx.compose.foundation.layout.size
import com.akshaglobal.smartcallshield.presentation.ui.components.ModeButton
import com.akshaglobal.smartcallshield.presentation.ui.components.StatisticsCard

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
    var showDialPad by rememberSaveable { mutableStateOf(false) }

    // Persistent state for search and dialer
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var dialNumber by rememberSaveable { mutableStateOf("") }

    // Sync lastSelectedMode with currentMode whenever currentMode changes
    LaunchedEffect(currentMode) {
        lastSelectedMode = currentMode
    }

    // Always reload modes and contacts from the database when DashboardScreen is recomposed
    LaunchedEffect(Unit) {
        callModesViewModel.loadModes()
    }
    LaunchedEffect(modes, deviceContacts) {
        callModesViewModel.syncModesAndContacts()
    }

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
                }) { Text("Dial") }
            },
            modifier = Modifier.fillMaxWidth(0.95f)
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showDialPad = !showDialPad },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_call_block), // Using existing dial-like icon
                    contentDescription = "Dial Pad",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    ) { paddingValues ->
        val filteredContacts = remember(searchQuery, deviceContactsList) {
            val filtered = if (searchQuery.isBlank()) deviceContactsList
            else deviceContactsList.filter {
                it.displayName.contains(searchQuery, ignoreCase = true) ||
                it.phoneNumber.contains(searchQuery, ignoreCase = true)
            }
            filtered.distinctBy { it.phoneNumber }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = 4.dp,
                    bottom = paddingValues.calculateBottomPadding() + (if (showDialPad) 400.dp else 16.dp)
                )
            ) {
                // Header
                item {
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
                }

                // Dialogs (Logic only, UI is triggered by state)
                item {
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
                            }
                        )
                    }
                    
                    if (showDisableDialog) {
                        androidx.compose.material3.AlertDialog(
                            onDismissRequest = { showDisableDialog = false },
                            title = { Text("Disable CallShield") },
                            text = {
                                Text("You are disabling the switch. Your phone will receive ALL CALLS WITHOUT ANY CALL FILTERING, including unknown calls. Press OK to disable CallShield.")
                            },
                            confirmButton = {
                                Button(onClick = {
                                    viewModel.toggleAppEnabled()
                                    showDisableDialog = false
                                }) { Text("OK") }
                            },
                            dismissButton = {
                                Button(onClick = { showDisableDialog = false }) { Text("Cancel") }
                            }
                        )
                    }

                    if (showModeChangeDialog && pendingMode != null) {
                        androidx.compose.material3.AlertDialog(
                            onDismissRequest = {
                                showModeChangeDialog = false
                                pendingMode = null
                            },
                            title = { Text("Change Call Mode") },
                            text = {
                                Text("CallShield will be applied to the selected mode. Press OK to switch mode.")
                            },
                            confirmButton = {
                                Button(onClick = {
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
                                }) { Text("OK") }
                            },
                            dismissButton = {
                                Button(onClick = {
                                    showModeChangeDialog = false
                                    pendingMode = null
                                }) { Text("Cancel") }
                            }
                        )
                    }
                }

                item { Spacer(modifier = Modifier.height(16.dp)) }

                // Premium Badge
                if (isPremium) {
                    item {
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
                }

                // Mode Selection
                item {
                    Text(
                        "Call Mode",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    if (currentMode == null) {
                        Box(
                            modifier = Modifier.fillMaxWidth().height(100.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    } else {
                        ModeSelector(
                            currentMode!!,
                            enabled = isAppEnabled,
                            normalEnabled = isAppEnabled,
                            familyEnabled = isAppEnabled && enabledModes["FAMILY"] == true,
                            drivingEnabled = isAppEnabled && enabledModes["DRIVING"] == true,
                            emergencyEnabled = isAppEnabled && enabledModes["EMERGENCY"] == true
                        ) { newMode ->
                            if (!isAppEnabled) return@ModeSelector
                            if (lastSelectedMode != newMode) {
                                pendingMode = newMode
                                showModeChangeDialog = true
                            }
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(24.dp)) }

                // SEARCH CONTACTS HEADER
                item {
                    Text("Search Contacts", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    androidx.compose.material3.OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        label = { Text("Search by name or number") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Search Results List
                items(filteredContacts) { contact ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .background(if (dialNumber == contact.phoneNumber) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.Transparent)
                            .clickable {
                                dialNumberAndCall(contact.phoneNumber)
                            },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_mode_normal),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(contact.displayName.ifEmpty { "Unknown" }, fontWeight = FontWeight.Medium)
                            Text(contact.phoneNumber, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                // Permission Denied Dialog
                item {
                    if (showPermissionDeniedDialog) {
                        androidx.compose.material3.AlertDialog(
                            onDismissRequest = { showPermissionDeniedDialog = false },
                            title = { Text("Permission Required") },
                            text = { Text("Please grant the CALL_PHONE permission to place calls directly from this app.") },
                            confirmButton = {
                                Button(onClick = { showPermissionDeniedDialog = false }) { Text("OK") }
                            }
                        )
                    }
                }
            }

            // Dialer pad UI (Overlay at the bottom)
            if (showDialPad) {
                Card(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(bottom = paddingValues.calculateBottomPadding()),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = dialNumber,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f),
                                maxLines = 1
                            )
                            IconButton(onClick = { showDialPad = false }) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_backspace), // Reuse backspace or add a close icon
                                    contentDescription = "Close",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        
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
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = { dialNumberAndCall(dialNumber) },
                                enabled = dialNumber.isNotBlank(),
                                modifier = Modifier.height(56.dp).width(140.dp)
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_call_block),
                                    contentDescription = "Dial",
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Dial")
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            IconButton(
                                onClick = { if (dialNumber.isNotEmpty()) dialNumber = dialNumber.dropLast(1) }
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_backspace),
                                    contentDescription = "Delete",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ModeSelector(
    currentMode: CallMode,
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
