import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.akshaglobal.smartcallshield.data.preferences.PreferencesManager
import com.akshaglobal.smartcallshield.data.repository.ContactRepository
import com.akshaglobal.smartcallshield.data.model.ContactEntity

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager,
    private val contactRepository: ContactRepository
) : ViewModel() {
    private val _spamDetectionEnabled = MutableStateFlow(true)
    val spamDetectionEnabled = _spamDetectionEnabled.asStateFlow()

    private val _drivingModeEnabled = MutableStateFlow(false)
    val drivingModeEnabled = _drivingModeEnabled.asStateFlow()

    private val _hasDrivingContacts = MutableStateFlow(false)
    val hasDrivingContacts = _hasDrivingContacts.asStateFlow()

    init {
        viewModelScope.launch {
            contactRepository.getContactsByCategory("DRIVING").collect { contacts: List<ContactEntity> ->
                _hasDrivingContacts.value = contacts.isNotEmpty()
                // Only allow enabling driving mode if there are driving contacts
                if (contacts.isEmpty()) {
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
    }

    fun setDrivingModeEnabled(enabled: Boolean) {
        if (_hasDrivingContacts.value) {
            viewModelScope.launch {
                preferencesManager.setDrivingModeEnabled(enabled)
                _drivingModeEnabled.value = enabled
            }
        }
    }
}
