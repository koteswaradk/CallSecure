package com.akshaglobal.smartcallshield.service

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
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class CallProtectionService : Service() {

    @Inject
    lateinit var preferencesManager: PreferencesManager

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand invoked")

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

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.notif_call_protection_active))
            .setContentText(getString(R.string.notif_call_protection_desc))
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(contentPendingIntent)
            .setOngoing(true)
            .setAutoCancel(false)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .addAction(
                R.drawable.ic_call_block, // Using an existing icon as a placeholder
                getString(R.string.notif_action_disable),
                disablePendingIntent
            )
            .build()

        try {
            startForeground(NOTIFICATION_ID, notification)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start foreground service", e)
        }

        monitorAppState()

        return START_STICKY
    }

    private fun monitorAppState() {
        scope.launch {
            preferencesManager.isAppEnabled.collect { enabled ->
                if (!enabled) {
                    Log.d(TAG, "App disabled, stopping CallProtectionService")
                    stopSelf()
                }
            }
        }
    }

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
        scope.cancel()
        Log.d(TAG, "CallProtectionService destroyed")
    }

    companion object {
        private const val TAG = "CallProtectionService"
        private const val NOTIFICATION_ID = 2
        private const val CHANNEL_ID = "call_protection_channel"
    }
}
