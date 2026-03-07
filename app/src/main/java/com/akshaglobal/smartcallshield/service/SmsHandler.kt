package com.akshaglobal.smartcallshield.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import kotlinx.coroutines.launch

class SmsHandler : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        // Placeholder: actual implementation will send SMS when triggered by driving mode
        if (context == null || intent == null) return

        val action = intent.action
        Log.d(TAG, "Received SMS handler action: $action")
    }

    fun sendAutoReplySms(context: Context, phoneNumber: String, message: String) {
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
            try {
                val subscriptionManager = context.getSystemService(android.telephony.SubscriptionManager::class.java)
                val subId = try {
                    if (androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_PHONE_STATE) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                        subscriptionManager.activeSubscriptionInfoList?.firstOrNull()?.subscriptionId
                    } else null
                } catch (e: SecurityException) { null }
                @Suppress("DEPRECATION")
                val smsManager = if (subId != null) android.telephony.SmsManager.getSmsManagerForSubscriptionId(subId) else android.telephony.SmsManager.getDefault()
                val parts = smsManager.divideMessage(message)
                smsManager.sendMultipartTextMessage(phoneNumber, null, ArrayList(parts), null, null)
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
