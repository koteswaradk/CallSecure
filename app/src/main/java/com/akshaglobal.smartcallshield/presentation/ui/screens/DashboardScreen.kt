package com.example.smartcallshield

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.telecom.TelecomManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.net.toUri

// Helper composable to request READ_CONTACTS permission at runtime
@Composable
private fun RequestContactsPermissionIfNeeded(onPermissionResult: (Boolean) -> Unit) {
    val context = LocalContext.current
    val permissionState = remember { mutableStateOf(false) }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            permissionState.value = granted
            onPermissionResult(granted)
        }
    )
    LaunchedEffect(Unit) {
        val granted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED
        if (!granted) {
            permissionLauncher.launch(Manifest.permission.READ_CONTACTS)
        } else {
            permissionState.value = true
            onPermissionResult(true)
        }
    }
}

// Helper composable to request SEND_SMS permission at runtime
@Composable
private fun RequestSendSmsPermissionIfNeeded(onPermissionResult: (Boolean) -> Unit) {
    val context = LocalContext.current
    val permissionState = remember { mutableStateOf(false) }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            permissionState.value = granted
            onPermissionResult(granted)
        }
    )
    LaunchedEffect(Unit) {
        val granted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.SEND_SMS
        ) == PackageManager.PERMISSION_GRANTED
        if (!granted) {
            permissionLauncher.launch(Manifest.permission.SEND_SMS)
        } else {
            permissionState.value = true
            onPermissionResult(true)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen() {
    var hasContactsPermission by remember { mutableStateOf(false) }
    var hasSendSmsPermission by remember { mutableStateOf(false) }
    // Request permissions at the start
    RequestContactsPermissionIfNeeded { granted ->
        hasContactsPermission = granted
    }
    RequestSendSmsPermissionIfNeeded { granted ->
        hasSendSmsPermission = granted
    }

    var isDefaultDialer by remember { mutableStateOf(false) }
    var pendingDialNumber by remember { mutableStateOf("") }
    var showNotDefaultDialerDialog by remember { mutableStateOf(false) }
    var showIntentNotHandledDialog by remember { mutableStateOf(false) }
    var attemptedSetDefaultDialer by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val telecomManager = remember { context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager }
    var currentDefaultDialer by remember { mutableStateOf(telecomManager.defaultDialerPackage ?: "(none)") }

    val defaultDialerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = {
            val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager
            isDefaultDialer = telecomManager.defaultDialerPackage == context.packageName
            currentDefaultDialer = telecomManager.defaultDialerPackage ?: "(none)"
            Log.d("DIALER_FLOW", "Result callback: isDefaultDialer=$isDefaultDialer, pendingDialNumber=$pendingDialNumber, attemptedSetDefaultDialer=$attemptedSetDefaultDialer")
            if (isDefaultDialer && pendingDialNumber.isNotBlank()) {
                // Now default, dial the number
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                    val intent = Intent(Intent.ACTION_CALL).apply {
                        data = ("tel:" + pendingDialNumber).toUri()
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    Log.d("DIALER_FLOW", "Calling number: $pendingDialNumber")
                    context.startActivity(intent)
                } else {
                    val intent = Intent(Intent.ACTION_DIAL).apply {
                        data = ("tel:" + pendingDialNumber).toUri()
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    Log.d("DIALER_FLOW", "Opening dialer for number: $pendingDialNumber")
                    context.startActivity(intent)
                }
                pendingDialNumber = ""
                attemptedSetDefaultDialer = false
            } else if (!isDefaultDialer && pendingDialNumber.isNotBlank() && attemptedSetDefaultDialer) {
                // User did not set as default, fallback to system dialer
                val intent = Intent(Intent.ACTION_DIAL).apply {
                    data = ("tel:" + pendingDialNumber).toUri()
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                try {
                    context.startActivity(intent)
                } catch (e: Exception) {
                    showNotDefaultDialerDialog = true
                }
                pendingDialNumber = ""
                attemptedSetDefaultDialer = false
            } else if (!isDefaultDialer && pendingDialNumber.isNotBlank()) {
                // Show feedback dialog
                showNotDefaultDialerDialog = true
                attemptedSetDefaultDialer = false
            }
        }
    )

    // Request background permission for Android 10+
    val requestBackgroundPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            Log.d("PERMISSION", "Background permission granted: $granted")
        }
    )
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val bgPerm = Manifest.permission.ACCESS_BACKGROUND_LOCATION
            val granted = ContextCompat.checkSelfPermission(context, bgPerm) == PackageManager.PERMISSION_GRANTED
            if (!granted) {
                requestBackgroundPermissionLauncher.launch(bgPerm)
            }
        }
    }

    Column(Modifier.padding(16.dp)) {
        Text("Current default dialer: $currentDefaultDialer", style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = {
                pendingDialNumber = "+1234567890"
                attemptedSetDefaultDialer = true
                val intent = Intent(TelecomManager.ACTION_CHANGE_DEFAULT_DIALER).apply {
                    putExtra(TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME, context.packageName)
                }
                // Check if there is an activity to handle the intent
                val pm = context.packageManager
                val canHandle = intent.resolveActivity(pm) != null
                if (canHandle) {
                    defaultDialerLauncher.launch(intent)
                } else {
                    showIntentNotHandledDialog = true
                }
            },
            modifier = Modifier.padding(16.dp)
        ) {
            Text("Set as Default Dialer and Dial")
        }
    }

    if (showNotDefaultDialerDialog) {
        AlertDialog(
            onDismissRequest = { showNotDefaultDialerDialog = false },
            title = { Text("Not Default Dialer") },
            text = { Text("You must set SmartCallShield as the default phone app to place calls directly. Please try again and select SmartCallShield as the default dialer.") },
            confirmButton = {
                Button(onClick = { showNotDefaultDialerDialog = false }) { Text("OK") }
            }
        )
    }
    if (showIntentNotHandledDialog) {
        AlertDialog(
            onDismissRequest = { showIntentNotHandledDialog = false },
            title = { Text("Device Restriction") },
            text = { Text("Your device does not allow changing the default dialer or does not support the required intent. This is a device/OS limitation. You can try to set SmartCallShield as the default dialer manually in system settings.") },
            confirmButton = {
                Button(onClick = {
                    showIntentNotHandledDialog = false
                    // Open system settings for default apps
                    val intent = Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    try {
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        // Fallback for some OEMs
                        val fallbackIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = ("package:" + context.packageName).toUri()
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        context.startActivity(fallbackIntent)
                    }
                }) { Text("Go to Settings") }
            },
            dismissButton = {
                Button(onClick = { showIntentNotHandledDialog = false }) { Text("Cancel") }
            }
        )
    }
}
