package com.akshaglobal.smartcallshield.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.akshaglobal.smartcallshield.R
import com.akshaglobal.smartcallshield.data.preferences.PreferencesManager

@AndroidEntryPoint
class DrivingModeService : Service() {

    @Inject
    lateinit var preferencesManager: PreferencesManager

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "DrivingModeService created")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "DrivingModeService started")

        // Create foreground notification
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Driving Mode Active")
            .setContentText("Incoming calls will be handled safely")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .build()

        startForeground(NOTIFICATION_ID, notification)

        // Start monitoring driving mode
        startDrivingModeMonitoring()

        return START_STICKY
    }

    private fun startDrivingModeMonitoring() {
        scope.launch {
            try {
                // Monitor driving mode settings
                preferencesManager.drivingModeEnabled.collect { enabled ->
                    if (!enabled) {
                        stopSelf()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error monitoring driving mode", e)
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
        Log.d(TAG, "DrivingModeService destroyed")
    }

    companion object {
        private const val TAG = "DrivingModeService"
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "driving_mode_channel"
    }
}

