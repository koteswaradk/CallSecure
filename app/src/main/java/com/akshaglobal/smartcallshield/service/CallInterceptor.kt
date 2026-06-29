package com.akshaglobal.smartcallshield.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
import android.util.Log
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import com.akshaglobal.smartcallshield.data.model.CallLogEntity
import com.akshaglobal.smartcallshield.data.repository.CallLogRepository
import com.akshaglobal.smartcallshield.domain.usecase.HandleCallUseCase
import com.akshaglobal.smartcallshield.domain.usecase.CallDecision
import dagger.hilt.android.EntryPointAccessors
import com.akshaglobal.smartcallshield.di.CallInterceptorEntryPoint
import com.akshaglobal.smartcallshield.util.PhoneNumberUtils
import com.akshaglobal.smartcallshield.utils.SmsSender
import com.akshaglobal.smartcallshield.data.preferences.PreferencesManager
import com.akshaglobal.smartcallshield.data.contacts.DeviceContactsProvider

class CallInterceptor : BroadcastReceiver() {

    private lateinit var handleCallUseCase: HandleCallUseCase
    private lateinit var callLogRepository: CallLogRepository
    private lateinit var smsSender: SmsSender
    private lateinit var preferencesManager: PreferencesManager
    private lateinit var deviceContactsProvider: DeviceContactsProvider
    private lateinit var contactRepository: com.akshaglobal.smartcallshield.data.repository.ContactRepository
    private lateinit var drivingModeLogRepository: com.akshaglobal.smartcallshield.data.repository.DrivingModeLogRepository

    private var spamDetector: TFLiteSpamDetector? = null

    companion object {
        private const val TAG = "CallInterceptor"
        @Volatile
        private var isCurrentlyRinging = false
        @Volatile
        private var currentRingingNumber: String? = null
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return

        val entryPoint = EntryPointAccessors.fromApplication(context.applicationContext, CallInterceptorEntryPoint::class.java)
        handleCallUseCase = entryPoint.handleCallUseCase()
        callLogRepository = entryPoint.callLogRepository()
        smsSender = entryPoint.smsSender()
        preferencesManager = entryPoint.preferencesManager()
        deviceContactsProvider = entryPoint.deviceContactsProvider()
        contactRepository = entryPoint.contactRepository()
        drivingModeLogRepository = entryPoint.drivingModeLogRepository()

        if (spamDetector == null) {
            spamDetector = TFLiteSpamDetector(context)
        }

        when (intent.action) {
            TelephonyManager.ACTION_PHONE_STATE_CHANGED -> {
                val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
                @Suppress("DEPRECATION")
                val incomingNumberRaw = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)
                val incomingNumber = PhoneNumberUtils.normalize(incomingNumberRaw)

                when (state) {
                    TelephonyManager.EXTRA_STATE_RINGING -> {
                        // Avoid handling the same call multiple times (Android sends multiple RINGING broadcasts)
                        if (currentRingingNumber != incomingNumber) {
                            isCurrentlyRinging = true
                            currentRingingNumber = incomingNumber
                            handleIncomingCall(context, incomingNumber)
                        } else {
                            Log.d(TAG, "Already handling ringing for $incomingNumber, skipping duplicate broadcast.")
                        }
                    }
                    TelephonyManager.EXTRA_STATE_OFFHOOK, TelephonyManager.EXTRA_STATE_IDLE -> {
                        isCurrentlyRinging = false
                        currentRingingNumber = null
                    }
                }
            }
        }
    }

    private fun handleIncomingCall(context: Context, phoneNumber: String?) {
        if (phoneNumber == null) return

        val scope = CoroutineScope(Dispatchers.Default)
        scope.launch {
            try {
                val appEnabled = preferencesManager.isAppEnabled.first()
                if (!appEnabled) return@launch

                val decision = handleCallUseCase.invoke(phoneNumber)
                val e164Number = PhoneNumberUtils.normalize(phoneNumber)

                // Log the call
                callLogRepository.addCallLog(
                    CallLogEntity(
                        phoneNumber = phoneNumber,
                        timestamp = System.currentTimeMillis(),
                        callType = com.akshaglobal.smartcallshield.data.model.CallType.INCOMING.ordinal,
                        isSpam = decision == CallDecision.REJECT || decision == CallDecision.SILENT,
                        wasBlocked = decision == CallDecision.REJECT || decision == CallDecision.SILENT
                    )
                )

                when (decision) {
                    CallDecision.REJECT -> {
                        Log.d(TAG, "Rejecting call from: $phoneNumber")
                        rejectCall(context)
                    }
                    CallDecision.SILENT -> {
                        Log.d(TAG, "Silencing call from: $phoneNumber")
                        muteCall(context)
                    }
                    CallDecision.REPLY_SMS -> {
                        Log.d(TAG, "Waiting 5 seconds before replying with SMS and rejecting call from: $phoneNumber")
                        
                        // Wait for 5 seconds
                        delay(5000)
                        
                        // Check if the phone is still ringing and it's the same number
                        // (User might have answered or caller might have hung up)
                        if (isCurrentlyRinging && currentRingingNumber == e164Number) {
                            val replyMessage = preferencesManager.drivingModeAutoReply.first()
                            
                            if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.SEND_SMS) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                                try {
                                    smsSender.sendSms(e164Number, replyMessage)
                                    Log.d(TAG, "Auto-reply SMS sent to $e164Number after 5s delay")
                                    
                                    drivingModeLogRepository.addDrivingModeLog(
                                        com.akshaglobal.smartcallshield.data.model.DrivingModeLogEntity(
                                            phoneNumber = e164Number,
                                            contactName = "", 
                                            smsMessage = replyMessage,
                                            timestamp = System.currentTimeMillis(),
                                            status = "SENT"
                                        )
                                    )
                                } catch (e: Exception) {
                                    Log.e(TAG, "Failed to send auto-reply SMS", e)
                                }
                            }
                            rejectCall(context)
                        } else {
                            Log.d(TAG, "Call from $phoneNumber no longer ringing or changed state, skipping auto-reply.")
                        }
                    }
                    CallDecision.ALLOW -> {
                        Log.d(TAG, "Allowing call from: $phoneNumber")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error handling incoming call", e)
            }
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
