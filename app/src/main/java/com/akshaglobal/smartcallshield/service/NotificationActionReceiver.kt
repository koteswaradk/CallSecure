package com.akshaglobal.smartcallshield.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.akshaglobal.smartcallshield.data.preferences.PreferencesManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class NotificationActionReceiver : BroadcastReceiver() {

    @Inject
    lateinit var preferencesManager: PreferencesManager

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACTION_DISABLE_PROTECTION) {
            Log.d("NotificationReceiver", "Disabling protection via notification button")
            CoroutineScope(Dispatchers.IO).launch {
                preferencesManager.setAppEnabled(false)
            }
        }
    }

    companion object {
        const val ACTION_DISABLE_PROTECTION = "com.akshaglobal.smartcallshield.ACTION_DISABLE_PROTECTION"
    }
}
