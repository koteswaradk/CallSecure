package com.akshaglobal.smartcallshield.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
import android.util.Log
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import com.akshaglobal.smartcallshield.data.model.CallLogEntity
import com.akshaglobal.smartcallshield.data.repository.CallLogRepository
import com.akshaglobal.smartcallshield.domain.usecase.HandleCallUseCase
import com.akshaglobal.smartcallshield.domain.usecase.CallDecision
import dagger.hilt.android.EntryPointAccessors
import com.akshaglobal.smartcallshield.di.CallInterceptorEntryPoint
import com.akshaglobal.smartcallshield.util.PhoneNumberUtils
import com.akshaglobal.smartcallshield.data.preferences.PreferencesManager

class CallInterceptor : BroadcastReceiver() {

    companion object {
        private const val TAG = "CallInterceptor"
        @Volatile
        private var isCurrentlyRinging = false
        @Volatile
        private var currentRingingNumber: String? = null
        @Volatile
        private var currentRingingDecision: CallDecision? = null
        @Volatile
        private var hasLoggedCurrentCall = false
        
        // Use a single CoroutineScope for the interceptor
        private val interceptorScope = CoroutineScope(Dispatchers.Default)
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return

        // Get dependencies dynamically from Hilt
        val entryPoint = EntryPointAccessors.fromApplication(context.applicationContext, CallInterceptorEntryPoint::class.java)
        val handleCallUseCase = entryPoint.handleCallUseCase()
        val callLogRepository = entryPoint.callLogRepository()
        val preferencesManager = entryPoint.preferencesManager()

        when (intent.action) {
            TelephonyManager.ACTION_PHONE_STATE_CHANGED -> {
                val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
                @Suppress("DEPRECATION")
                val incomingNumberRaw = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)
                val incomingNumber = PhoneNumberUtils.normalize(incomingNumberRaw)

                Log.d(TAG, "Phone State changed to: $state for number: $incomingNumber")

                when (state) {
                    TelephonyManager.EXTRA_STATE_RINGING -> {
                        // Ignore empty numbers if we are already ringing or have logged this call
                        if (incomingNumber.isBlank() && isCurrentlyRinging) {
                            Log.d(TAG, "Empty number during ringing, ignoring.")
                            return
                        }

                        // Avoid handling the same call multiple times
                        if (currentRingingNumber != incomingNumber || !isCurrentlyRinging) {
                            isCurrentlyRinging = true
                            currentRingingNumber = incomingNumber
                            hasLoggedCurrentCall = false
                            
                            val pendingResult = goAsync()
                            interceptorScope.launch {
                                try {
                                    handleIncomingCall(context, incomingNumber, handleCallUseCase, callLogRepository, preferencesManager)
                                } finally {
                                    pendingResult.finish()
                                }
                            }
                        } else {
                            Log.d(TAG, "Already handling ringing for $incomingNumber, skipping duplicate broadcast.")
                        }
                    }
                    TelephonyManager.EXTRA_STATE_OFFHOOK -> {
                        // User answered the call
                        if (isCurrentlyRinging && currentRingingDecision == CallDecision.ALLOW && !hasLoggedCurrentCall) {
                            val answeredNumber = currentRingingNumber ?: incomingNumber
                            val pendingResult = goAsync()
                            interceptorScope.launch {
                                try {
                                    logAnsweredCall(answeredNumber, callLogRepository, preferencesManager)
                                } finally {
                                    pendingResult.finish()
                                }
                            }
                            hasLoggedCurrentCall = true
                        }
                        isCurrentlyRinging = false
                    }
                    TelephonyManager.EXTRA_STATE_IDLE -> {
                        // If it was ringing and we haven't logged it yet, it's a missed call
                        if (isCurrentlyRinging && !hasLoggedCurrentCall && currentRingingDecision != null) {
                            val isAllowedDecision = currentRingingDecision == CallDecision.ALLOW
                            
                            if (isAllowedDecision) {
                                val missedNumber = currentRingingNumber ?: ""
                                val pendingResult = goAsync()
                                interceptorScope.launch {
                                    try {
                                        logMissedCall(missedNumber, callLogRepository, preferencesManager)
                                    } finally {
                                        pendingResult.finish()
                                    }
                                }
                            }
                        }
                        isCurrentlyRinging = false
                        currentRingingNumber = null
                        currentRingingDecision = null
                        hasLoggedCurrentCall = false
                    }
                }
            }
        }
    }

    private suspend fun logMissedCall(phoneNumber: String, callLogRepository: CallLogRepository, preferencesManager: PreferencesManager) {
        if (phoneNumber.isBlank()) return
        try {
            if (!preferencesManager.isAppEnabled.first()) return

            Log.d(TAG, "Logging missed call for: $phoneNumber")
            callLogRepository.addCallLog(
                CallLogEntity(
                    phoneNumber = phoneNumber,
                    timestamp = System.currentTimeMillis(),
                    callType = com.akshaglobal.smartcallshield.data.model.CallType.INCOMING.ordinal,
                    duration = 0, // Missed
                    isSpam = false,
                    wasBlocked = false
                )
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error logging missed call", e)
        }
    }

    private suspend fun logAnsweredCall(phoneNumber: String, callLogRepository: CallLogRepository, preferencesManager: PreferencesManager) {
        if (phoneNumber.isBlank()) return
        
        try {
            if (!preferencesManager.isAppEnabled.first()) return

            Log.d(TAG, "Logging answered call for: $phoneNumber")
            callLogRepository.addCallLog(
                CallLogEntity(
                    phoneNumber = phoneNumber,
                    timestamp = System.currentTimeMillis(),
                    callType = com.akshaglobal.smartcallshield.data.model.CallType.INCOMING.ordinal,
                    duration = 1, // Set > 0 to count as answered/allowed
                    isSpam = false,
                    wasBlocked = false
                )
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error logging answered call", e)
        }
    }

    private suspend fun handleIncomingCall(context: Context, phoneNumber: String?, handleCallUseCase: HandleCallUseCase, callLogRepository: CallLogRepository, preferencesManager: PreferencesManager) {
        if (phoneNumber.isNullOrBlank()) return

        try {
            val appEnabled = preferencesManager.isAppEnabled.first()
            if (!appEnabled) return

            val decision = handleCallUseCase.invoke(phoneNumber)
            currentRingingDecision = decision

            // Log ONLY if blocked. 
            // Allowed calls are logged in OFFHOOK when answered.
            val isBlockedDecision = decision == CallDecision.REJECT || 
                                  decision == CallDecision.SILENT
            
            if (isBlockedDecision) {
                Log.d(TAG, "Logging blocked call: $phoneNumber")
                callLogRepository.addCallLog(
                    CallLogEntity(
                        phoneNumber = phoneNumber,
                        timestamp = System.currentTimeMillis(),
                        callType = com.akshaglobal.smartcallshield.data.model.CallType.INCOMING.ordinal,
                        duration = 0,
                        isSpam = isBlockedDecision,
                        wasBlocked = isBlockedDecision
                    )
                )
                hasLoggedCurrentCall = true
            }

            when (decision) {
                CallDecision.REJECT -> {
                    Log.d(TAG, "Rejecting call from: $phoneNumber")
                    rejectCall(context)
                }
                CallDecision.SILENT -> {
                    Log.d(TAG, "Silencing call from: $phoneNumber")
                    muteCall(context)
                }
                CallDecision.ALLOW -> {
                    Log.d(TAG, "Allowing call from: $phoneNumber, waiting for answer to log.")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error handling incoming call", e)
        }
    }

    private fun rejectCall(context: Context) {
        try {
            val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as? android.telecom.TelecomManager
            if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.ANSWER_PHONE_CALLS) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                @Suppress("DEPRECATION")
                telecomManager?.endCall()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error rejecting call", e)
        }
    }

    private fun muteCall(context: Context) {
        try {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? android.media.AudioManager
            audioManager?.setRingerMode(android.media.AudioManager.RINGER_MODE_SILENT)
        } catch (e: Exception) {
            Log.e(TAG, "Error muting call", e)
        }
    }
}
