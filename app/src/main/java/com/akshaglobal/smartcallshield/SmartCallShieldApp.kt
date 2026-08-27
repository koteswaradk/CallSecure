package com.akshaglobal.smartcallshield

import android.app.Application
import android.content.Context
import android.database.Cursor
import android.provider.CallLog
import android.util.Log
import com.google.android.gms.ads.MobileAds
import dagger.hilt.android.HiltAndroidApp
import com.akshaglobal.smartcallshield.service.TFLiteSpamDetector
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@HiltAndroidApp
class SmartCallShieldApp : Application() {

   companion object {
       var isCallShieldEnabled: Boolean = false
   }

    override fun onCreate() {
        super.onCreate()
        
        CoroutineScope(Dispatchers.IO).launch {
            // Defer Mobile Ads initialization
            MobileAds.initialize(this@SmartCallShieldApp) {}

            // Only run heavy initialization if not already done
            val prefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            val initialScanDone = prefs.getBoolean("initial_spam_scan_done", false)
            if (!initialScanDone) {
                runSpamDetectionOnCallHistory()
                prefs.edit().putBoolean("initial_spam_scan_done", true).apply()
            }
        }
    }

    private fun runSpamDetectionOnCallHistory() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val detector = TFLiteSpamDetector(applicationContext)
                val callLogUri = CallLog.Calls.CONTENT_URI
                val projection = arrayOf(CallLog.Calls.NUMBER)
                val cursor: Cursor? = contentResolver.query(callLogUri, projection, null, null, null)
                val seenNumbers = mutableSetOf<String>()
                cursor?.use {
                    val numberIndex = it.getColumnIndex(CallLog.Calls.NUMBER)
                    while (it.moveToNext()) {
                        val number = it.getString(numberIndex)
                        if (number != null && seenNumbers.add(number)) {
                            val prediction = detector.predict(number)
                            // TODO: Store prediction result in DB for future blocking/analytics
                            Log.d("SmartCallShieldApp", "Initial prediction for $number: $prediction")
                        }
                    }
                }
                detector.close()
            } catch (e: Exception) {
                Log.e("SmartCallShieldApp", "Error running spam detection on call history", e)
            }
        }
    }
}
