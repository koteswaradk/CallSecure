package com.akshaglobal.smartcallshield.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
import android.util.Log
import androidx.core.app.ActivityCompat
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
import com.akshaglobal.smartcallshield.utils.SmsSender
import com.akshaglobal.smartcallshield.data.preferences.PreferencesManager
import com.akshaglobal.smartcallshield.data.contacts.DeviceContactsProvider
import com.akshaglobal.smartcallshield.service.TFLiteSpamDetector

class CallInterceptor : BroadcastReceiver() {

    private lateinit var handleCallUseCase: HandleCallUseCase
    private lateinit var callLogRepository: CallLogRepository
    private lateinit var smsSender: SmsSender
    private lateinit var preferencesManager: PreferencesManager
    private lateinit var deviceContactsProvider: DeviceContactsProvider
    private lateinit var contactRepository: com.akshaglobal.smartcallshield.data.repository.ContactRepository
    private lateinit var drivingModeLogRepository: com.akshaglobal.smartcallshield.data.repository.DrivingModeLogRepository

    private var ringStartTime: Long = 0
    private var ringCount: Int = 0
    private var lastIncomingNumber: String? = null

    // Track last SMS sent time per phone number for driving mode auto-reply
    private val lastSmsSentMap: MutableMap<String, Long> = mutableMapOf()

    private var spamDetector: TFLiteSpamDetector? = null

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return

        // obtain Hilt dependencies via EntryPoint
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

        // Show toast if app is set as default dialer
        val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as? android.telecom.TelecomManager
        val packageName = context.packageName
        if (telecomManager != null && telecomManager.defaultDialerPackage == packageName) {
            android.widget.Toast.makeText(context, "SmartCallShield is now the default dialer", android.widget.Toast.LENGTH_SHORT).show()
        }

        // Request permissions if not granted
        val permissions = arrayOf(android.Manifest.permission.SEND_SMS, android.Manifest.permission.READ_PHONE_STATE)
        permissions.forEach { perm ->
            if (ContextCompat.checkSelfPermission(context, perm) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                // Request permission (if in Activity context)
                if (context is android.app.Activity) {
                    ActivityCompat.requestPermissions(context, permissions, 1001)
                } else {
                    Log.w(TAG, "Permission $perm not granted. SMS may not be sent.")
                }
            }
        }

        when (intent.action) {
            TelephonyManager.ACTION_PHONE_STATE_CHANGED -> {
                val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
                @Suppress("DEPRECATION")
                val incomingNumberRaw = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)
                val incomingNumber = PhoneNumberUtils.normalize(incomingNumberRaw)

                when (state) {
                    TelephonyManager.EXTRA_STATE_RINGING -> {
                        // If new incoming number, reset state
                        if (lastIncomingNumber != incomingNumber) {
                            ringStartTime = System.currentTimeMillis()
                            ringCount = 1
                            lastIncomingNumber = incomingNumber
                        } else {
                            ringCount++
                        }
                        handleIncomingCall(context, incomingNumber)
                    }
                    TelephonyManager.EXTRA_STATE_OFFHOOK -> {
                        // Call answered, reset state and allow new SMS for this number next time
                        ringCount = 0
                        ringStartTime = 0
                        lastIncomingNumber?.let { lastSmsSentMap.remove(it) }
                        lastIncomingNumber = null
                    }
                    TelephonyManager.EXTRA_STATE_IDLE -> {
                        // Call ended, check if we need to send auto-reply SMS for driving mode
                        if (lastIncomingNumber != null) {
                            checkAndSendDrivingAutoReply(context, lastIncomingNumber!!)
                        }
                        ringCount = 0
                        ringStartTime = 0
                        lastIncomingNumber?.let { lastSmsSentMap.remove(it) }
                        lastIncomingNumber = null
                    }
                }
            }
            @Suppress("DEPRECATION")
            Intent.ACTION_NEW_OUTGOING_CALL -> {
                val outgoingNumber = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER)
                if (outgoingNumber != null) {
                    logOutgoingCall(context, outgoingNumber)
                }
            }
        }
    }

    private fun handleIncomingCall(context: Context, phoneNumber: String?) {
        if (phoneNumber == null) return

        // Only perform filtering/blocking if app is enabled
        val scope = CoroutineScope(Dispatchers.Default)
        scope.launch {
            val appEnabled = try { preferencesManager.isAppEnabled.first() } catch (e: Exception) { true }
            if (!appEnabled) {
                Log.d(TAG, "[APP ENABLED CHECK] App is disabled, allowing all calls. No filtering or blocking.")
                // Optionally, log the call as allowed
                val callLog = CallLogEntity(
                    phoneNumber = phoneNumber,
                    timestamp = System.currentTimeMillis(),
                    callType = com.akshaglobal.smartcallshield.data.model.CallType.INCOMING.ordinal,
                    isSpam = false,
                    wasBlocked = false
                )
                callLogRepository.addCallLog(callLog)
                return@launch
            }

            // Use improved TFLiteSpamDetector prediction
            val prediction = spamDetector?.predict(phoneNumber) ?: 0
            // 0: safe, 1: spam, 2: robocall, 3: unknown
            when (prediction) {
                1 -> {
                    Log.d(TAG, "[SPAM DETECTION] Call flagged as SPAM. Blocking call.")
                    rejectCall(context)
                    // Log as blocked
                    val callLog = CallLogEntity(
                        phoneNumber = phoneNumber,
                        timestamp = System.currentTimeMillis(),
                        callType = com.akshaglobal.smartcallshield.data.model.CallType.INCOMING.ordinal,
                        isSpam = true,
                        wasBlocked = true
                    )
                    callLogRepository.addCallLog(callLog)
                    return@launch
                }
                2 -> {
                    Log.d(TAG, "[SPAM DETECTION] Call flagged as ROBOCALL. Blocking call.")
                    rejectCall(context)
                    val callLog = CallLogEntity(
                        phoneNumber = phoneNumber,
                        timestamp = System.currentTimeMillis(),
                        callType = com.akshaglobal.smartcallshield.data.model.CallType.INCOMING.ordinal,
                        isSpam = true,
                        wasBlocked = true
                    )
                    callLogRepository.addCallLog(callLog)
                    return@launch
                }
                3 -> {
                    Log.d(TAG, "[SPAM DETECTION] Call flagged as UNKNOWN. Silencing call.")
                    muteCall(context)
                    val callLog = CallLogEntity(
                        phoneNumber = phoneNumber,
                        timestamp = System.currentTimeMillis(),
                        callType = com.akshaglobal.smartcallshield.data.model.CallType.INCOMING.ordinal,
                        isSpam = false,
                        wasBlocked = false
                    )
                    callLogRepository.addCallLog(callLog)
                    return@launch
                }
                else -> Log.d(TAG, "[SPAM DETECTION] Call is safe.")
            }

            // If not blocked, proceed with business logic
            try {
                val decision = handleCallUseCase.invoke(phoneNumber)

                // Log the call
                val callLog = CallLogEntity(
                    phoneNumber = phoneNumber,
                    timestamp = System.currentTimeMillis(),
                    callType = com.akshaglobal.smartcallshield.data.model.CallType.INCOMING.ordinal,
                    isSpam = decision == CallDecision.REJECT || decision == CallDecision.SILENT,
                    wasBlocked = decision == CallDecision.REJECT || decision == CallDecision.SILENT
                )
                callLogRepository.addCallLog(callLog)

                // --- DRIVING MODE AUTO-REPLY LOGIC ---
                val drivingEnabled = preferencesManager.drivingModeEnabled.first()
                val autoReplyMessage = preferencesManager.drivingModeAutoReply.first()
                val currentMode = preferencesManager.currentMode.first().uppercase()
                var isDrivingContact = false
                var drivingContacts: List<com.akshaglobal.smartcallshield.data.model.ContactEntity> = emptyList()
                val normalizedNumber = phoneNumber.replace(Regex("[^0-9]"), "")
                val e164Number = PhoneNumberUtils.normalize(phoneNumber)
                if (drivingEnabled && currentMode == "DRIVING") {
                    drivingContacts = contactRepository.getContactsByCategory("DRIVING").first()
                    isDrivingContact = drivingContacts.any { c: com.akshaglobal.smartcallshield.data.model.ContactEntity ->
                        val contactNormalized = c.phoneNumber.replace(Regex("[^0-9]"), "")
                        contactNormalized.endsWith(normalizedNumber) || normalizedNumber.endsWith(contactNormalized)
                    }
                    Log.d(TAG, "[DRIVING MODE] isDrivingContact=$isDrivingContact, ringCount=$ringCount, autoReplyMessage='$autoReplyMessage'")
                }
                // Check if app is default SMS app
                val defaultSmsPackage = android.provider.Telephony.Sms.getDefaultSmsPackage(context)
                if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.SEND_SMS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                    Log.w(TAG, "[DRIVING MODE] SEND_SMS permission not granted. Cannot send auto-reply.")
                    android.widget.Toast.makeText(context, "SMS permission not granted", android.widget.Toast.LENGTH_LONG).show()
                } else if (defaultSmsPackage != null && defaultSmsPackage != context.packageName) {
                    Log.w(TAG, "[DRIVING MODE] App is not the default SMS app. Cannot send SMS reliably.")
                    android.widget.Toast.makeText(context, "Set SmartCallShield as default SMS app for auto-reply", android.widget.Toast.LENGTH_LONG).show()
                } else if (drivingEnabled && currentMode == "DRIVING" && isDrivingContact && ringCount == 1) {
                    val lastSent = lastSmsSentMap[phoneNumber] ?: 0L
                    if (System.currentTimeMillis() - lastSent >= 10000) { // Prevent spamming within 10 seconds
                        try {
                            Log.d(TAG, "[DRIVING MODE] Attempting to send auto-reply SMS to $e164Number: $autoReplyMessage")
                            smsSender.sendSms(e164Number, autoReplyMessage)
                            Log.d(TAG, "[DRIVING MODE] Auto-reply SMS sent to $e164Number: $autoReplyMessage (immediate on ring)")
                            val drivingLog = com.akshaglobal.smartcallshield.data.model.DrivingModeLogEntity(
                                phoneNumber = e164Number,
                                contactName = drivingContacts.find { c: com.akshaglobal.smartcallshield.data.model.ContactEntity ->
                                    val contactNormalized = c.phoneNumber.replace(Regex("[^0-9]"), "")
                                    contactNormalized.endsWith(normalizedNumber) || normalizedNumber.endsWith(contactNormalized)
                                }?.displayName ?: "",
                                smsMessage = autoReplyMessage,
                                timestamp = System.currentTimeMillis(),
                                status = if (lastSent == 0L) "SENT" else "RESEND"
                            )
                            drivingModeLogRepository.addDrivingModeLog(drivingLog)
                            lastSmsSentMap[phoneNumber] = System.currentTimeMillis()
                        } catch (e: Exception) {
                            Log.e(TAG, "[DRIVING MODE] Failed to send auto-reply SMS", e)
                            android.widget.Toast.makeText(context, "Failed to send auto-reply SMS", android.widget.Toast.LENGTH_LONG).show()
                        }
                    }
                }
                // --- END DRIVING MODE AUTO-REPLY LOGIC ---

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
                        val defaultMsg = preferencesManager.drivingModeAutoReply.first()
                        smsSender.sendSms(e164Number, defaultMsg)
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
                val normalized = PhoneNumberUtils.normalize(phoneNumber)
                val callLog = CallLogEntity(
                    phoneNumber = normalized,
                    timestamp = System.currentTimeMillis(),
                    callType = com.akshaglobal.smartcallshield.data.model.CallType.OUTGOING.ordinal,
                    duration = 0
                )
                callLogRepository.addCallLog(callLog)
            } catch (e: Exception) {
                Log.e(TAG, "Error logging outgoing call", e)
            }
        }
    }

    private fun rejectCall(context: Context) {
        try {
            val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as? android.telecom.TelecomManager
            // Check for required permission before calling endCall
            if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.ANSWER_PHONE_CALLS) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                @Suppress("DEPRECATION")
                // Deprecated: No public alternative for rejecting calls in non-system dialer apps
                telecomManager?.endCall()
                Log.d(TAG, "Call rejected")
            } else {
                Log.w(TAG, "Missing ANSWER_PHONE_CALLS permission. Cannot reject call.")
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException rejecting call", e)
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

    // New function to check and send driving mode auto-reply on call end
    private fun checkAndSendDrivingAutoReply(context: Context, phoneNumber: String) {
        val scope = CoroutineScope(Dispatchers.Default)
        scope.launch {
            try {
                val drivingEnabled = preferencesManager.drivingModeEnabled.first()
                val autoReplyMessage = preferencesManager.drivingModeAutoReply.first()
                val currentMode = preferencesManager.currentMode.first().uppercase()
                var isDrivingContact = false
                var drivingContacts: List<com.akshaglobal.smartcallshield.data.model.ContactEntity> = emptyList()
                val normalizedNumber = phoneNumber.replace(Regex("[^0-9]"), "")
                val e164Number = PhoneNumberUtils.normalize(phoneNumber)
                if (drivingEnabled && currentMode == "DRIVING") {
                    drivingContacts = contactRepository.getContactsByCategory("DRIVING").first()
                    isDrivingContact = drivingContacts.any { c: com.akshaglobal.smartcallshield.data.model.ContactEntity ->
                        val contactNormalized = c.phoneNumber.replace(Regex("[^0-9]"), "")
                        contactNormalized.endsWith(normalizedNumber) || normalizedNumber.endsWith(contactNormalized)
                    }
                }
                val lastSent = lastSmsSentMap[phoneNumber] ?: 0L
                if (drivingEnabled && currentMode == "DRIVING" && isDrivingContact) {
                    // Only send if not already sent for this call
                    if (lastSent == 0L) {
                        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.SEND_SMS) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                            val defaultSmsPackage = android.provider.Telephony.Sms.getDefaultSmsPackage(context)
                            if (defaultSmsPackage != null && defaultSmsPackage != context.packageName) {
                                Log.w(TAG, "[DRIVING MODE] App is not the default SMS app. Cannot send SMS reliably.")
                                android.widget.Toast.makeText(context, "Set SmartCallShield as default SMS app for auto-reply", android.widget.Toast.LENGTH_LONG).show()
                            } else {
                                try {
                                    smsSender.sendSms(e164Number, autoReplyMessage)
                                    Log.d(TAG, "[DRIVING MODE] Auto-reply SMS sent to $e164Number: $autoReplyMessage (on call end)")
                                    val drivingLog = com.akshaglobal.smartcallshield.data.model.DrivingModeLogEntity(
                                        phoneNumber = e164Number,
                                        contactName = drivingContacts.find { c: com.akshaglobal.smartcallshield.data.model.ContactEntity ->
                                            val contactNormalized = c.phoneNumber.replace(Regex("[^0-9]"), "")
                                            contactNormalized.endsWith(normalizedNumber) || normalizedNumber.endsWith(contactNormalized)
                                        }?.displayName ?: "",
                                        smsMessage = autoReplyMessage,
                                        timestamp = System.currentTimeMillis(),
                                        status = if (lastSent == 0L) "SENT" else "RESEND"
                                    )
                                    drivingModeLogRepository.addDrivingModeLog(drivingLog)
                                    lastSmsSentMap[phoneNumber] = System.currentTimeMillis()
                                } catch (e: Exception) {
                                    Log.e(TAG, "[DRIVING MODE] Failed to send auto-reply SMS on call end", e)
                                    android.widget.Toast.makeText(context, "Failed to send auto-reply SMS", android.widget.Toast.LENGTH_LONG).show()
                                }
                            }
                        } else {
                            Log.w(TAG, "[DRIVING MODE] SEND_SMS permission not granted. Cannot send auto-reply.")
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in checkAndSendDrivingAutoReply", e)
            }
        }
    }

    companion object {
        private const val TAG = "CallInterceptor"
    }
}
