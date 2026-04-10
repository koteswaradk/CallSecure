package com.akshaglobal.smartcallshield.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akshaglobal.smartcallshield.data.model.SpamReportEntity
import com.akshaglobal.smartcallshield.domain.usecase.AddSpamReportUseCase
import com.akshaglobal.smartcallshield.domain.usecase.UpdateSpamReportUseCase
import com.akshaglobal.smartcallshield.domain.usecase.GetSpamReportsCountUseCase
import com.akshaglobal.smartcallshield.domain.usecase.IncrementSpamReportCountUseCase
import com.akshaglobal.smartcallshield.domain.usecase.DeleteOldSpamReportsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SpamReportViewModel @Inject constructor(
    private val addSpamReportUseCase: AddSpamReportUseCase,
    private val updateSpamReportUseCase: UpdateSpamReportUseCase,
    private val getSpamReportsCountUseCase: GetSpamReportsCountUseCase,
    private val incrementSpamReportCountUseCase: IncrementSpamReportCountUseCase,
    private val deleteOldSpamReportsUseCase: DeleteOldSpamReportsUseCase
) : ViewModel() {

    private val _spamReportsCount = MutableStateFlow(0L)
    val spamReportsCount: StateFlow<Long> = _spamReportsCount.asStateFlow()

    fun addSpamReport(report: SpamReportEntity) {
        viewModelScope.launch { addSpamReportUseCase(report) }
    }

    fun updateSpamReport(report: SpamReportEntity) {
        viewModelScope.launch { updateSpamReportUseCase(report) }
    }

    fun incrementReportCount(phoneNumber: String, timestamp: Long) {
        viewModelScope.launch { incrementSpamReportCountUseCase(phoneNumber, timestamp) }
    }

    fun deleteOldReports(olderThan: Long) {
        viewModelScope.launch { deleteOldSpamReportsUseCase(olderThan) }
    }

    fun observeSpamReportsCount() {
        getSpamReportsCountUseCase().onEach { _spamReportsCount.value = it }
            .launchIn(viewModelScope)
    }
}

