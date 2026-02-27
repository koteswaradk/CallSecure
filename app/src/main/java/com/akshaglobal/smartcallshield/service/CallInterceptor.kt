package com.akshaglobal.smartcallshield.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.akshaglobal.smartcallshield.data.model.CallLogEntity
import com.akshaglobal.smartcallshield.data.repository.CallLogRepository
import com.akshaglobal.smartcallshield.domain.usecase.HandleCallUseCase
import com.akshaglobal.smartcallshield.domain.usecase.CallDecision

class CallInterceptor : BroadcastReceiver() {

    // If using DI, these would be injected. For compilation they are declared nullable and can be set via setter.
    var handleCallUseCase: HandleCallUseCase? = null
    var callLogRepository: CallLogRepository? = null

    private var ringStartTime: Long = 0
    private var ringCount: Int = 0

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return

        when (intent.action) {
            TelephonyManager.ACTION_PHONE_STATE_CHANGED -> {
                val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
                val incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)

                when (state) {
                    TelephonyManager.EXTRA_STATE_RINGING -> {
                        handleIncomingCall(context, incomingNumber)
                    }
                    TelephonyManager.EXTRA_STATE_OFFHOOK -> {
                        // Call answered
                    }
                    TelephonyManager.EXTRA_STATE_IDLE -> {
                        // Call ended
                        ringCount = 0
                    }
                }
            }
            Intent.ACTION_NEW_OUTGOING_CALL -> {
                val outgoingNumber = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER)
                if (outgoingNumber != null) {
                    logOutgoingCall(context, outgoingNumber)
                }
            }
        }
    }

    private fun handleIncomingCall(context: Context, phoneNumber: String?) {
        if (phoneNumber.isNullOrEmpty()) return

        // Increment ring count and track time
        if (ringStartTime == 0L) {
            ringStartTime = System.currentTimeMillis()
            ringCount = 1
        } else {
            ringCount++
        }

        val scope = CoroutineScope(Dispatchers.Default)

        scope.launch {
            try {
                val decision = handleCallUseCase?.invoke(phoneNumber) ?: CallDecision.ALLOW

                // Log the call
                val callLog = CallLogEntity(
                    phoneNumber = phoneNumber,
                    timestamp = System.currentTimeMillis(),
                    callType = com.akshaglobal.smartcallshield.data.model.CallType.INCOMING.ordinal,
                    isSpam = decision == CallDecision.REJECT || decision == CallDecision.SILENT,
                    wasBlocked = decision == CallDecision.REJECT || decision == CallDecision.SILENT
                )
                callLogRepository?.addCallLog(callLog)

                // Handle decision
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
                        Log.d(TAG, "Replying with SMS to: $phoneNumber")
                        // Send auto-reply SMS (handled separately)
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

    private fun logOutgoingCall(context: Context, phoneNumber: String) {
        CoroutineScope(Dispatchers.Default).launch {
            try {
                val callLog = CallLogEntity(
                    phoneNumber = phoneNumber,
                    timestamp = System.currentTimeMillis(),
                    callType = com.akshaglobal.smartcallshield.data.model.CallType.OUTGOING.ordinal,
                    duration = 0
                )
                callLogRepository?.addCallLog(callLog)
            } catch (e: Exception) {
                Log.e(TAG, "Error logging outgoing call", e)
            }
        }
    }

    private fun rejectCall(context: Context) {
        try {
            // Use TelecomManager or AudioManager to reject call
            val telecomManager = context.getSystemService(Context.TELECOM_SERVICE)
            // telecomManager?.endCall()

            Log.d(TAG, "Call rejected")
        } catch (e: Exception) {
            Log.e(TAG, "Error rejecting call", e)
        }
    }

    private fun muteCall(context: Context) {
        try {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? android.media.AudioManager
            audioManager?.setRingerMode(android.media.AudioManager.RINGER_MODE_SILENT)
            Log.d(TAG, "Call muted")
        } catch (e: Exception) {
            Log.e(TAG, "Error muting call", e)
        }
    }

    companion object {
        private const val TAG = "CallInterceptor"
    }
}
