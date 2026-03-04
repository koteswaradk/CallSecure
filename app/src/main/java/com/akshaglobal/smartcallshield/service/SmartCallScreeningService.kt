package com.akshaglobal.smartcallshield.service

import android.telecom.CallScreeningService
import android.telecom.Call
import com.akshaglobal.smartcallshield.domain.usecase.HandleCallUseCase
import com.akshaglobal.smartcallshield.domain.usecase.CallDecision
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.runBlocking

@AndroidEntryPoint
class SmartCallScreeningService : CallScreeningService() {
    @Inject
    lateinit var handleCallUseCase: HandleCallUseCase

    override fun onScreenCall(callDetails: Call.Details) {
        android.util.Log.d("SmartCallScreeningService", "onScreenCall invoked for: ${callDetails.handle?.schemeSpecificPart}")
        val phoneNumber = callDetails.handle?.schemeSpecificPart ?: ""
        val shouldBlock = runBlocking {
            handleCallUseCase(phoneNumber) == CallDecision.REJECT
        }
        android.util.Log.d("SmartCallScreeningService", "shouldBlock=$shouldBlock for $phoneNumber")

        // Fallback: If not default dialer, only silence the call
        val telecomManager = getSystemService(TELECOM_SERVICE) as android.telecom.TelecomManager
        val isDefaultDialer = telecomManager.defaultDialerPackage == packageName
        val response = if (shouldBlock) {
            if (isDefaultDialer) {
                CallResponse.Builder()
                    .setDisallowCall(true)
                    .setRejectCall(true)
                    .setSkipCallLog(false)
                    .setSkipNotification(false)
                    .build()
            } else {
                CallResponse.Builder()
                    .setDisallowCall(false)
                    .setRejectCall(false)
                    .setSilenceCall(true)
                    .setSkipCallLog(false)
                    .setSkipNotification(false)
                    .build()
            }
        } else {
            CallResponse.Builder()
                .setDisallowCall(false)
                .setRejectCall(false)
                .setSkipCallLog(false)
                .setSkipNotification(false)
                .build()
        }
        android.util.Log.d("SmartCallScreeningService", "respondToCall: disallow=${response.disallowCall} reject=${response.rejectCall} silence=${response.silenceCall}")
        respondToCall(callDetails, response)
    }
}
