package com.akshaglobal.smartcallshield.utils

import android.content.Context
import android.telephony.SmsManager
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SmsSender @Inject constructor(@ApplicationContext private val context: Context) {
    private val TAG = "SmsSender"

    fun sendSms(phoneNumber: String, message: String) {
        try {
            @Suppress("DEPRECATION")
            val subscriptionManager = android.telephony.SubscriptionManager.from(context)
            val subId = try {
                if (androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_PHONE_STATE) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                    subscriptionManager.activeSubscriptionInfoList?.firstOrNull()?.subscriptionId
                } else null
            } catch (_: SecurityException) { null }
            @Suppress("DEPRECATION")
            val smsManager: SmsManager = if (subId != null) SmsManager.getSmsManagerForSubscriptionId(subId) else SmsManager.getDefault()
            val parts = smsManager.divideMessage(message)
            smsManager.sendMultipartTextMessage(phoneNumber, null, parts, null, null)
            Log.d(TAG, "Sent SMS to $phoneNumber")
        } catch (e: Exception) {
            Log.e(TAG, "Error sending SMS to $phoneNumber", e)
        }
    }
}
