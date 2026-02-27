package com.akshaglobal.smartcallshield

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import dagger.hilt.android.AndroidEntryPoint
import com.akshaglobal.smartcallshield.presentation.ui.navigation.MainNavigation
import com.akshaglobal.smartcallshield.presentation.ui.theme.SmartCallShieldTheme
import com.akshaglobal.smartcallshield.service.DrivingModeService

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val requiredPermissions = arrayOf(
        Manifest.permission.READ_CALL_LOG,
        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.SEND_SMS,
        Manifest.permission.READ_CONTACTS,
        Manifest.permission.CALL_PHONE,
        Manifest.permission.WRITE_CALL_LOG,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.INTERNET,
        Manifest.permission.ACCESS_NETWORK_STATE
    ).plus(
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(Manifest.permission.SCHEDULE_EXACT_ALARM)
        } else {
            emptyArray()
        }
    ).plus(
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(Manifest.permission.ANSWER_PHONE_CALLS)
        } else {
            emptyArray()
        }
    )

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            Log.d(TAG, "All permissions granted")
            initializeApp()
        } else {
            Log.w(TAG, "Some permissions denied")
            permissions.forEach { (permission, granted) ->
                if (!granted) {
                    Log.w(TAG, "Permission denied: $permission")
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Request permissions
        permissionLauncher.launch(requiredPermissions)

        setContent {
            SmartCallShieldTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainNavigation()
                }
            }
        }
    }

    private fun initializeApp() {
        Log.d(TAG, "Initializing app")

        // Start driving mode service if enabled
        startDrivingModeIfNeeded()

        // Register call receiver
        registerCallReceiver()
    }

    private fun startDrivingModeIfNeeded() {
        // Check preferences and start service if needed
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(Intent(this, DrivingModeService::class.java))
        } else {
            startService(Intent(this, DrivingModeService::class.java))
        }
    }

    private fun registerCallReceiver() {
        // Call receiver is registered via AndroidManifest
        Log.d(TAG, "Call receiver registered via manifest")
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}

