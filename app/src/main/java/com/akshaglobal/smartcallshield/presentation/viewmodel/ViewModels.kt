package com.akshaglobal.smartcallshield.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.akshaglobal.smartcallshield.data.model.CallLogEntity
import com.akshaglobal.smartcallshield.data.model.CallMode
import com.akshaglobal.smartcallshield.data.model.CallStatistics
import com.akshaglobal.smartcallshield.data.model.ContactEntity
import com.akshaglobal.smartcallshield.data.preferences.PreferencesManager
import com.akshaglobal.smartcallshield.data.repository.AnalyticsRepository
import com.akshaglobal.smartcallshield.data.repository.CallLogRepository
import com.akshaglobal.smartcallshield.data.repository.ContactRepository
import com.akshaglobal.smartcallshield.domain.usecase.GetAnalyticsUseCase
import com.akshaglobal.smartcallshield.domain.usecase.ManageContactsUseCase
import com.akshaglobal.smartcallshield.domain.usecase.GetCallHistoryUseCase
import com.akshaglobal.smartcallshield.data.model.CallType

enum class TrendFilter { TODAY, WEEK, MONTH, OVERALL }

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager,
    private val analyticsRepository: AnalyticsRepository,
    private val getAnalyticsUseCase: GetAnalyticsUseCase
) : ViewModel() {

    private val _currentMode = MutableStateFlow<CallMode?>(null)
    val currentMode = _currentMode.asStateFlow()

    private val _isAppEnabled = MutableStateFlow(true)
    val isAppEnabled = _isAppEnabled.asStateFlow()

    private val _isPremium = MutableStateFlow(false)
    val isPremium = _isPremium.asStateFlow()

    private val _statistics = MutableStateFlow(CallStatistics())
    val statistics = _statistics.asStateFlow()

    private val _blockedCount = MutableStateFlow(0L)
    val blockedCount = _blockedCount.asStateFlow()

    private val _spamCount = MutableStateFlow(0L)
    val spamCount = _spamCount.asStateFlow()

    private val _drivingRepliesCount = MutableStateFlow(0L)
    val drivingRepliesCount = _drivingRepliesCount.asStateFlow()

    private val _drivingModeEnabled = MutableStateFlow(false)
    val drivingModeEnabled = _drivingModeEnabled.asStateFlow()

    init {
        viewModelScope.launch {
            // Observe mode changes
            preferencesManager.currentMode.collect { mode ->
                _currentMode.value = try {
                    CallMode.valueOf(mode)
                } catch (e: Exception) {
                    CallMode.NORMAL
                }
            }
        }

        viewModelScope.launch {
            // Observe app enabled status
            preferencesManager.isAppEnabled.collect { enabled ->
                _isAppEnabled.value = enabled
            }
        }

        viewModelScope.launch {
            // Observe premium status
            preferencesManager.isPremium.collect { premium ->
                _isPremium.value = premium
            }
        }

        viewModelScope.launch {
            // Observe blocked calls
            getAnalyticsUseCase.getBlockedCallsCount().collect { count ->
                _blockedCount.value = count
            }
        }

        viewModelScope.launch {
            // Observe spam calls
            getAnalyticsUseCase.getSpamCallsCount().collect { count ->
                _spamCount.value = count
            }
        }

        viewModelScope.launch {
            // Observe driving mode replies
            getAnalyticsUseCase.getDrivingModeRepliesCount().collect { count ->
                _drivingRepliesCount.value = count
            }
        }

        viewModelScope.launch {
            // Combine statistics
            analyticsRepository.getCallStatistics().collect { stats ->
                _statistics.value = stats.copy(
                    blockedCalls = _blockedCount.value,
                    spamCallsPrevented = _spamCount.value,
                    drivingModeRepliesSent = _drivingRepliesCount.value
                )
            }
        }
    }

    fun setMode(mode: CallMode) {
        viewModelScope.launch {
            preferencesManager.setCurrentMode(mode.name)
            _currentMode.value = mode
            // Enable driving mode in preferences if DRIVING is selected, disable otherwise
            if (mode == CallMode.DRIVING) {
                preferencesManager.setDrivingModeEnabled(true)
                _drivingModeEnabled.value = true
            } else {
                preferencesManager.setDrivingModeEnabled(false)
                _drivingModeEnabled.value = false
            }
        }
    }

    fun toggleAppEnabled() {
        viewModelScope.launch {
            val newState = !_isAppEnabled.value
            preferencesManager.setAppEnabled(newState)
            _isAppEnabled.value = newState
        }
    }
}

@HiltViewModel
class ContactsViewModel @Inject constructor(
    private val manageContactsUseCase: ManageContactsUseCase,
    private val contactRepository: ContactRepository
) : ViewModel() {

    private val _allContacts = MutableStateFlow<List<ContactEntity>>(emptyList())
    val allContacts = _allContacts.asStateFlow()

    private val _whitelistContacts = MutableStateFlow<List<ContactEntity>>(emptyList())
    val whitelistContacts = _whitelistContacts.asStateFlow()

    private val _blacklistContacts = MutableStateFlow<List<ContactEntity>>(emptyList())
    val blacklistContacts = _blacklistContacts.asStateFlow()

    private val _emergencyContacts = MutableStateFlow<List<ContactEntity>>(emptyList())
    val emergencyContacts = _emergencyContacts.asStateFlow()

    init {
        viewModelScope.launch {
            manageContactsUseCase.getAllContacts().collect { contacts ->
                _allContacts.value = contacts
            }
        }

        viewModelScope.launch {
            manageContactsUseCase.getWhitelistedContacts().collect { contacts ->
                _whitelistContacts.value = contacts
            }
        }

        viewModelScope.launch {
            manageContactsUseCase.getBlacklistedContacts().collect { contacts ->
                _blacklistContacts.value = contacts
            }
        }

        viewModelScope.launch {
            manageContactsUseCase.getEmergencyContacts().collect { contacts ->
                _emergencyContacts.value = contacts
            }
        }
    }

    fun addContact(contact: ContactEntity) {
        viewModelScope.launch {
            manageContactsUseCase.addContact(contact)
        }
    }

    fun deleteContact(contact: ContactEntity) {
        viewModelScope.launch {
            manageContactsUseCase.deleteContact(contact)
        }
    }
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager,
    private val contactRepository: ContactRepository
) : ViewModel() {

    private val _spamDetectionEnabled = MutableStateFlow(true)
    val spamDetectionEnabled = _spamDetectionEnabled.asStateFlow()

    private val _drivingModeEnabled = MutableStateFlow(false)
    val drivingModeEnabled = _drivingModeEnabled.asStateFlow()

    private val _drivingModeAutoReply = MutableStateFlow("")
    val drivingModeAutoReply = _drivingModeAutoReply.asStateFlow()

    private val _spamConfidenceThreshold = MutableStateFlow(0.7f)
    val spamConfidenceThreshold = _spamConfidenceThreshold.asStateFlow()

    private val _autoRejectSpam = MutableStateFlow(true)
    val autoRejectSpam = _autoRejectSpam.asStateFlow()

    private val _hasDrivingContacts = MutableStateFlow(false)
    val hasDrivingContacts = _hasDrivingContacts.asStateFlow()

    init {
        viewModelScope.launch {
            contactRepository.getContactsByCategory("DRIVING").collect { contacts ->
                _hasDrivingContacts.value = contacts.isNotEmpty()
                // Only allow enabling driving mode if there are driving contacts
                if (!contacts.isNotEmpty()) {
                    _drivingModeEnabled.value = false
                    preferencesManager.setDrivingModeEnabled(false)
                }
            }
        }
        viewModelScope.launch {
            preferencesManager.drivingModeEnabled.collect { enabled ->
                // Only allow enabling if there are driving contacts
                _drivingModeEnabled.value = enabled && _hasDrivingContacts.value
            }
        }
        viewModelScope.launch {
            preferencesManager.spamDetectionEnabled.collect {
                _spamDetectionEnabled.value = it
            }
        }

       /* viewModelScope.launch {
            preferencesManager.drivingModeEnabled.collect {
                _drivingModeEnabled.value = it
            }
        }*/

        viewModelScope.launch {
            preferencesManager.drivingModeAutoReply.collect {
                _drivingModeAutoReply.value = it
            }
        }

        viewModelScope.launch {
            preferencesManager.spamConfidenceThreshold.collect {
                _spamConfidenceThreshold.value = it
            }
        }

        viewModelScope.launch {
            preferencesManager.autoRejectSpam.collect {
                _autoRejectSpam.value = it
            }
        }
    }

    fun setSpamDetectionEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setSpamDetectionEnabled(enabled)
            _spamDetectionEnabled.value = enabled
        }
    }

    fun setDrivingModeEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setDrivingModeEnabled(enabled)
            _drivingModeEnabled.value = enabled
        }
    }

    fun setDrivingModeAutoReply(message: String) {
        viewModelScope.launch {
            preferencesManager.setDrivingModeAutoReply(message)
            _drivingModeAutoReply.value = message
        }
    }

    fun setSpamConfidenceThreshold(threshold: Float) {
        viewModelScope.launch {
            preferencesManager.setSpamConfidenceThreshold(threshold)
            _spamConfidenceThreshold.value = threshold
        }
    }

    fun setAutoRejectSpam(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setAutoRejectSpam(enabled)
            _autoRejectSpam.value = enabled
        }
    }
}

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val getAnalyticsUseCase: GetAnalyticsUseCase,
    private val getCallHistoryUseCase: GetCallHistoryUseCase
) : ViewModel() {

    private val _blockedCalls = MutableStateFlow(0L)
    val blockedCalls = _blockedCalls.asStateFlow()

    private val _spamCallsPrevented = MutableStateFlow(0L)
    val spamCallsPrevented = _spamCallsPrevented.asStateFlow()

    private val _drivingRepliesSent = MutableStateFlow(0L)
    val drivingRepliesSent = _drivingRepliesSent.asStateFlow()

    // --- Call Trends State ---
    private val _callTrends = MutableStateFlow<List<Pair<String, Int>>>(emptyList())
    val callTrends = _callTrends.asStateFlow()

    // --- New: Filter state and statistics ---
    private val _trendFilter = MutableStateFlow(TrendFilter.TODAY)
    val trendFilter = _trendFilter.asStateFlow()

    private val _totalArrivals = MutableStateFlow(0)
    val totalArrivals = _totalArrivals.asStateFlow()
    private val _answeredCalls = MutableStateFlow(0)
    val answeredCalls = _answeredCalls.asStateFlow()
    private val _blocked = MutableStateFlow(0)
    val blocked = _blocked.asStateFlow()
    private val _autoReply = MutableStateFlow(0)
    val autoReply = _autoReply.asStateFlow()

    fun setTrendFilter(filter: TrendFilter) {
        _trendFilter.value = filter
        updateTrends()
    }

    init {
        viewModelScope.launch {
            getAnalyticsUseCase.getBlockedCallsCount().collect {
                _blockedCalls.value = it
            }
        }
        viewModelScope.launch {
            getAnalyticsUseCase.getSpamCallsCount().collect {
                _spamCallsPrevented.value = it
            }
        }
        viewModelScope.launch {
            getAnalyticsUseCase.getDrivingModeRepliesCount().collect {
                _drivingRepliesSent.value = it
            }
        }
        // Observe filter and update trends
        viewModelScope.launch {
            _trendFilter.collect {
                updateTrends()
            }
        }
    }

    private fun updateTrends() {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val oneHour = 60 * 60 * 1000L
            val oneDay = 24 * 60 * 60 * 1000L
            val filter = _trendFilter.value
            when (filter) {
                TrendFilter.TODAY -> {
                    val start = now - (now % oneDay)
                    getCallHistoryUseCase.getCallsInTimeRange(start, now).collect { callLogs ->
                        val grouped = callLogs.groupBy {
                            java.text.SimpleDateFormat("HH").format(java.util.Date(it.timestamp))
                        }
                        val trends = (0..23).map { h ->
                            val hour = h.toString().padStart(2, '0')
                            hour to (grouped[hour]?.size ?: 0)
                        }
                        _callTrends.value = trends
                        updateStats(callLogs)
                    }
                }
                TrendFilter.WEEK -> {
                    val start = now - 6 * oneDay
                    getCallHistoryUseCase.getCallsInTimeRange(start, now).collect { callLogs ->
                        val grouped = callLogs.groupBy {
                            java.text.SimpleDateFormat("yyyy-MM-dd").format(java.util.Date(it.timestamp))
                        }
                        val trends = (0..6).map { i ->
                            val day = java.text.SimpleDateFormat("yyyy-MM-dd").format(java.util.Date(start + i * oneDay))
                            day to (grouped[day]?.size ?: 0)
                        }
                        _callTrends.value = trends
                        updateStats(callLogs)
                    }
                }
                TrendFilter.MONTH -> {
                    val calendar = java.util.Calendar.getInstance()
                    val daysInMonth = calendar.getActualMaximum(java.util.Calendar.DAY_OF_MONTH)
                    calendar.set(java.util.Calendar.DAY_OF_MONTH, 1)
                    val start = calendar.timeInMillis
                    getCallHistoryUseCase.getCallsInTimeRange(start, now).collect { callLogs ->
                        val grouped = callLogs.groupBy {
                            java.text.SimpleDateFormat("yyyy-MM-dd").format(java.util.Date(it.timestamp))
                        }
                        val trends = (0 until daysInMonth).map { i ->
                            val day = java.text.SimpleDateFormat("yyyy-MM-dd").format(java.util.Date(start + i * oneDay))
                            day to (grouped[day]?.size ?: 0)
                        }
                        _callTrends.value = trends
                        updateStats(callLogs)
                    }
                }
                TrendFilter.OVERALL -> {
                    getCallHistoryUseCase.getAllCallLogs().collect { callLogs: List<CallLogEntity> ->
                        val grouped = callLogs.groupBy { log: CallLogEntity ->
                            java.text.SimpleDateFormat("yyyy-MM").format(java.util.Date(log.timestamp))
                        }
                        val trends = grouped.entries.sortedBy { entry -> entry.key }
                            .map { entry: Map.Entry<String, List<CallLogEntity>> ->
                                entry.key to entry.value.size
                            }
                        _callTrends.value = trends
                        updateStats(callLogs)
                    }
                }
            }
        }
    }

    private fun updateStats(callLogs: List<CallLogEntity>) {
        _totalArrivals.value = callLogs.size
        _answeredCalls.value = callLogs.count {
            it.callType == CallType.INCOMING.ordinal && it.duration > 0 && !it.wasBlocked
        }
        _blocked.value = callLogs.count { it.wasBlocked }
        // For auto-reply, you may need to join with DrivingModeLogEntity for accuracy.
        // Here, we count incoming calls with duration == 0, not blocked, and in driving mode as a proxy.
        _autoReply.value = callLogs.count {
            it.callType == CallType.INCOMING.ordinal && it.duration == 0L && !it.wasBlocked && isDrivingModeActive()
        }
    }

    private fun isDrivingModeActive(): Boolean {
        // TODO: Implement actual check for driving mode if needed, or pass as parameter
        return true // Placeholder, replace with actual logic if available
    }
}
