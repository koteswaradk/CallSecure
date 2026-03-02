package com.akshaglobal.smartcallshield.service

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.SmsManager
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import com.akshaglobal.smartcallshield.data.repository.DrivingModeLogRepository
import com.akshaglobal.smartcallshield.data.preferences.PreferencesManager

class SmsHandler : BroadcastReceiver() {

    var drivingModeRepository: DrivingModeLogRepository? = null
    var preferencesManager: PreferencesManager? = null

    override fun onReceive(context: Context?, intent: Intent?) {
        // Placeholder: actual implementation will send SMS when triggered by driving mode
        if (context == null || intent == null) return

        val action = intent.action
        Log.d(TAG, "Received SMS handler action: $action")
    }

    suspend fun sendAutoReplySms(context: Context, phoneNumber: String, message: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val smsManager = android.telephony.SmsManager.getDefault()
                val parts = smsManager.divideMessage(message)
                smsManager.sendMultipartTextMessage(phoneNumber, null, parts, null, null)
                Log.d(TAG, "Auto-reply sent to $phoneNumber")
            } catch (e: Exception) {
                Log.e(TAG, "Error sending auto-reply SMS", e)
            }
        }
    }

    companion object {
        private const val TAG = "SmsHandler"
    }
}
