package com.akshaglobal.smartcallshield

import android.Manifest
import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.akshaglobal.smartcallshield.data.repository.CallLogRepository
import dagger.hilt.android.AndroidEntryPoint
import com.akshaglobal.smartcallshield.presentation.ui.navigation.MainNavigation
import com.akshaglobal.smartcallshield.presentation.ui.screens.IntroScreen
import com.akshaglobal.smartcallshield.presentation.ui.screens.PermissionDisclosureScreen
import com.akshaglobal.smartcallshield.presentation.ui.screens.SplashScreen
import com.akshaglobal.smartcallshield.presentation.ui.theme.CallSecureTheme
import com.akshaglobal.smartcallshield.service.ai.FirstLaunchTrainer
import com.akshaglobal.smartcallshield.service.ai.SpamDetectionModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject lateinit var callLogRepository: CallLogRepository
    @Inject lateinit var preferencesManager: com.akshaglobal.smartcallshield.data.preferences.PreferencesManager
    @Inject lateinit var spamDetectionModel: SpamDetectionModel

    private var isInitialized = false

    private val requiredPermissions = arrayOf(
        Manifest.permission.READ_CALL_LOG,
        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.READ_CONTACTS,
        Manifest.permission.CALL_PHONE,
        Manifest.permission.INTERNET,
        Manifest.permission.ACCESS_NETWORK_STATE
    ).plus(
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            emptyArray()
        }
    )
        .plus(
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(Manifest.permission.ANSWER_PHONE_CALLS)
        } else {
            emptyArray()
        }
    )

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        Log.d(TAG, "Permissions result received")
        // Initialize app regardless of whether all permissions are granted
        // Essential logic inside initializeApp should check for specific permissions
        initializeApp()
        
        val anyDenied = permissions.entries.filter { !it.value }
        if (anyDenied.isNotEmpty()) {
            Log.w(TAG, "Some permissions denied: ${anyDenied.map { it.key }}")
        }
    }

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Always call initializeApp, it will handle internal checks
        initializeApp()

        // Request call screening role if needed (Android 10+)
        val roleManager = getSystemService(Context.ROLE_SERVICE) as RoleManager
        if (!roleManager.isRoleHeld(RoleManager.ROLE_CALL_SCREENING)) {
            val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING)
            startActivity(intent)
        }

        val prefs = getSharedPreferences("smartcallshield_prefs", Context.MODE_PRIVATE)
        val introShown = prefs.getBoolean("intro_shown", false)
        val disclosureShown = prefs.getBoolean("disclosure_shown", false)

        setContent {
            val windowSizeClass = calculateWindowSizeClass(this)
            CallSecureTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var currentScreen by remember { 
                        mutableStateOf("splash") 
                    }

                    when (currentScreen) {
                        "splash" -> SplashScreen(onComplete = { 
                            currentScreen = if (!introShown) "intro" else if (!disclosureShown) "disclosure" else "navigation"
                        })
                        "intro" -> IntroScreen(
                            context = this,
                            onFinish = {
                                prefs.edit().putBoolean("intro_shown", true).apply()
                                currentScreen = "disclosure"
                            }
                        )
                        "disclosure" -> PermissionDisclosureScreen(
                            onGetStarted = {
                                prefs.edit().putBoolean("disclosure_shown", true).apply()
                                requestAppPermissions()
                                currentScreen = "navigation"
                            }
                        )
                        "navigation" -> MainNavigation(windowSizeClass = windowSizeClass)
                    }
                }
            }
        }
    }

    private fun requestAppPermissions() {
        permissionLauncher.launch(requiredPermissions)
    }

    private fun initializeApp() {
        if (isInitialized) return
        isInitialized = true

        Log.d(TAG, "Initializing app")

        // Manage foreground services based on preferences
        manageServices()

        // Register call receiver
        registerCallReceiver()

        // TensorFlow training on first launch
        val prefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val modelTrained = prefs.getBoolean("model_trained", false)
        if (!modelTrained) {
            CoroutineScope(Dispatchers.IO).launch {
                val trainer = FirstLaunchTrainer(this@MainActivity, callLogRepository)
                trainer.trainModelOnFirstLaunch()
                prefs.edit().putBoolean("model_trained", true).apply()
            }
        }
    }

    private fun manageServices() {
        lifecycleScope.launch {
            // Observe overall app enabled state
            preferencesManager.isAppEnabled.collect { appEnabled ->
                Log.d(TAG, "Service State Update: appEnabled=$appEnabled")
                
                val intent = Intent(this@MainActivity, com.akshaglobal.smartcallshield.service.CallProtectionService::class.java)
                if (appEnabled) {
                    startForegroundService(intent)
                } else {
                    stopService(intent)
                }
            }
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
