package com.akshaglobal.smartcallshield.di

import com.akshaglobal.smartcallshield.domain.usecase.HandleCallUseCase
import com.akshaglobal.smartcallshield.data.repository.CallLogRepository
import com.akshaglobal.smartcallshield.utils.SmsSender
import com.akshaglobal.smartcallshield.data.preferences.PreferencesManager
import com.akshaglobal.smartcallshield.data.contacts.DeviceContactsProvider
import com.akshaglobal.smartcallshield.data.repository.ContactRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface CallInterceptorEntryPoint {
    fun handleCallUseCase(): HandleCallUseCase
    fun callLogRepository(): CallLogRepository
    fun smsSender(): SmsSender
    fun preferencesManager(): PreferencesManager
    fun deviceContactsProvider(): DeviceContactsProvider
    fun contactRepository(): ContactRepository
    fun drivingModeLogRepository(): com.akshaglobal.smartcallshield.data.repository.DrivingModeLogRepository
}
