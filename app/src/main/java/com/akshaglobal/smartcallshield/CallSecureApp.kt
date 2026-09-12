package com.akshaglobal.smartcallshield

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class CallSecureApp : Application() {
    companion object {
        var isCallShieldEnabled: Boolean = false
    }
}
