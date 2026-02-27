package com.akshaglobal.smartcallshield

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class SmartCallShieldApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize app-level components
    }
}
