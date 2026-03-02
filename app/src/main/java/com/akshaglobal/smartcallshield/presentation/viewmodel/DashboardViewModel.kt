import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

class DashboardViewModel @Inject constructor() : ViewModel() {
    private val _isAppEnabled = MutableStateFlow(false)
    val isAppEnabled: StateFlow<Boolean> = _isAppEnabled.asStateFlow()

    fun setAppEnabled(enabled: Boolean) {
        _isAppEnabled.value = enabled
        // Optionally persist this state to DataStore or SharedPreferences if needed
    }
}
