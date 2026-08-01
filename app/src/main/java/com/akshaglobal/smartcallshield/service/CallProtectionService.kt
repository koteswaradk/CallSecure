package com.akshaglobal.smartcallshield.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.akshaglobal.smartcallshield.MainActivity
import com.akshaglobal.smartcallshield.R
import com.akshaglobal.smartcallshield.data.preferences.PreferencesManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@AndroidEntryPoint
class CallProtectionService : Service() {

    @Inject
    lateinit var preferencesManager: PreferencesManager

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private lateinit var notificationManager: NotificationManager

    override fun onCreate() {
        super.onCreate()
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand invoked")

        // Sync check to avoid starting foreground if app is disabled
        val appEnabled = runBlocking { preferencesManager.isAppEnabled.first() }
        if (!appEnabled) {
            stopSelf()
            return START_NOT_STICKY
        }

        val initialMode = runBlocking { preferencesManager.currentMode.first() }
        val drivingEnabled = runBlocking { preferencesManager.drivingModeEnabled.first() }
        val autoReplyEnabled = runBlocking { preferencesManager.drivingModeAutoReplyEnabled.first() }

        val initialNotification = createNotification(initialMode, drivingEnabled, autoReplyEnabled)
        try {
            startForeground(NOTIFICATION_ID, initialNotification)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start foreground service", e)
        }

        monitorStateAndModes()

        return START_STICKY
    }

    private fun monitorStateAndModes() {
        scope.launch {
            combine(
                preferencesManager.isAppEnabled,
                preferencesManager.currentMode,
                preferencesManager.drivingModeEnabled,
                preferencesManager.drivingModeAutoReplyEnabled
            ) { appEnabled, mode, drivingEnabled, autoReplyEnabled ->
                StateConfig(appEnabled, mode, drivingEnabled, autoReplyEnabled)
            }.collect { config ->
                if (!config.appEnabled) {
                    Log.d(TAG, "App disabled, stopping CallProtectionService")
                    stopForeground(STOP_FOREGROUND_REMOVE)
                    stopSelf()
                } else {
                    Log.d(TAG, "Updating notification for mode: ${config.mode}")
                    val updatedNotification = createNotification(config.mode, config.drivingEnabled, config.autoReplyEnabled)
                    notificationManager.notify(NOTIFICATION_ID, updatedNotification)
                }
            }
        }
    }

    private fun createNotification(mode: String, drivingEnabled: Boolean, autoReplyEnabled: Boolean): Notification {
        val contentIntent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        val contentPendingIntent = PendingIntent.getActivity(
            this,
            0,
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val disableIntent = Intent(this, NotificationActionReceiver::class.java).apply {
            action = NotificationActionReceiver.ACTION_DISABLE_PROTECTION
        }
        val disablePendingIntent = PendingIntent.getBroadcast(
            this,
            0,
            disableIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val modeName = mode.lowercase().replaceFirstChar { it.uppercase() }
        val title = "DriveShield: $modeName Mode"
        
        val description = when (mode.uppercase()) {
            "DRIVING" -> {
                if (drivingEnabled && autoReplyEnabled) {
                    getString(R.string.notif_driving_mode_desc)
                } else {
                    getString(R.string.notif_driving_mode_no_reply_desc)
                }
            }
            "FAMILY" -> getString(R.string.mode_family_desc)
            "EMERGENCY" -> getString(R.string.mode_emergency_desc)
            else -> getString(R.string.notif_call_protection_desc)
        }

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(description)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(contentPendingIntent)
            .setOngoing(true)
            .setAutoCancel(false)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .addAction(
                R.drawable.ic_call_block,
                getString(R.string.notif_action_disable),
                disablePendingIntent
            )
            .build()
    }

    private data class StateConfig(
        val appEnabled: Boolean,
        val mode: String,
        val drivingEnabled: Boolean,
        val autoReplyEnabled: Boolean
    )

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Call Protection",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Shows that SmartCallShield is active in the background"
            setShowBadge(false)
        }
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        notificationManager.cancel(NOTIFICATION_ID)
        scope.cancel()
        Log.d(TAG, "CallProtectionService destroyed")
    }

    companion object {
        private const val TAG = "CallProtectionService"
        private const val NOTIFICATION_ID = 2
        private const val CHANNEL_ID = "call_protection_channel"
    }
}
