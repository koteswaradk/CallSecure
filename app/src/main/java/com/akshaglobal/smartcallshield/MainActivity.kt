package com.akshaglobal.smartcallshield

import android.Manifest
import android.app.ActivityManager
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.akshaglobal.smartcallshield.data.repository.CallLogRepository
import com.akshaglobal.smartcallshield.presentation.ui.navigation.MainNavigation
import com.akshaglobal.smartcallshield.presentation.ui.screens.IntroScreen
import com.akshaglobal.smartcallshield.presentation.ui.screens.PermissionDisclosureScreen
import com.akshaglobal.smartcallshield.presentation.ui.screens.SplashScreen
import com.akshaglobal.smartcallshield.presentation.ui.theme.CallSecureTheme
import com.akshaglobal.smartcallshield.service.ai.FirstLaunchTrainer
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject lateinit var callLogRepository: CallLogRepository
    @Inject lateinit var preferencesManager: com.akshaglobal.smartcallshield.data.preferences.PreferencesManager

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

        initializeApp()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = getSystemService(Context.ROLE_SERVICE) as? RoleManager
            if (roleManager != null && !roleManager.isRoleHeld(RoleManager.ROLE_CALL_SCREENING)) {
                val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING)
                startActivity(intent)
            }
        }

        val prefs = getSharedPreferences("callsecure_prefs", Context.MODE_PRIVATE)
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

        manageServices()

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
            preferencesManager.isAppEnabled.collect { appEnabled ->
                val intent = Intent(this@MainActivity, com.akshaglobal.smartcallshield.service.CallProtectionService::class.java)
                if (appEnabled) {
                    if (!isServiceRunning()) {
                        Log.d(TAG, "Starting protection service")
                        startForegroundService(intent)
                    }
                } else {
                    Log.d(TAG, "Stopping protection service")
                    stopService(intent)
                }
            }
        }
    }

    private fun isServiceRunning(): Boolean {
        val manager = getSystemService(ActivityManager::class.java)
        val services = manager?.getRunningServices(Integer.MAX_VALUE) ?: return false
        val serviceName = com.akshaglobal.smartcallshield.service.CallProtectionService::class.java.name
        return services.any { it.service.className == serviceName }
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}
