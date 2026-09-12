package com.akshaglobal.smartcallshield.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
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
    private var stateMonitorStarted = false

    override fun onCreate() {
        super.onCreate()
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand invoked")

        scope.launch {
            val appEnabled = preferencesManager.isAppEnabled.first()
            if (!appEnabled) {
                notificationManager.cancel(NOTIFICATION_ID)
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
                return@launch
            }

            val initialMode = preferencesManager.currentMode.first()
            val initialNotification = createNotification(initialMode)
            try {
                startForeground(NOTIFICATION_ID, initialNotification)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start foreground service", e)
            }

            if (!stateMonitorStarted) {
                monitorStateAndModes()
                stateMonitorStarted = true
            }
        }

        return START_STICKY
    }

    private fun monitorStateAndModes() {
        scope.launch {
            combine(
                preferencesManager.isAppEnabled,
                preferencesManager.currentMode
            ) { appEnabled, mode ->
                StateConfig(appEnabled, mode)
            }.collect { config ->
                if (!config.appEnabled) {
                    Log.d(TAG, "App disabled, clearing notifications and stopping service")
                    notificationManager.cancel(NOTIFICATION_ID)
                    stopForeground(STOP_FOREGROUND_REMOVE)
                    stopSelf()
                } else {
                    Log.d(TAG, "Updating notification for mode: ${config.mode}")
                    val updatedNotification = createNotification(config.mode)
                    notificationManager.notify(NOTIFICATION_ID, updatedNotification)
                }
            }
        }
    }

    private fun createNotification(mode: String): Notification {
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
        val title = "CallSecure: $modeName Mode"
        
        val description = when (mode.uppercase()) {
            "FAMILY" -> getString(R.string.mode_family_desc)
            "EMERGENCY" -> getString(R.string.mode_emergency_desc)
            else -> getString(R.string.notif_call_protection_desc)
        }

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(description)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setLargeIcon(BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher))
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
        val mode: String
    )

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Call Protection",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Shows that CallSecure is active in the background"
            setShowBadge(false)
        }
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        notificationManager.cancel(NOTIFICATION_ID)
        stateMonitorStarted = false
        scope.cancel()
        Log.d(TAG, "CallProtectionService destroyed")
    }

    companion object {
        private const val TAG = "CallProtectionService"
        private const val NOTIFICATION_ID = 2
        private const val CHANNEL_ID = "call_protection_channel"
    }
}
