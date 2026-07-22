package com.akshaglobal.smartcallshield.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "smartcallshield_prefs")

class PreferencesManager(private val context: Context) {

    companion object {
        // General Settings
        private val CURRENT_MODE = stringPreferencesKey("current_mode")
        private val IS_APP_ENABLED = booleanPreferencesKey("is_app_enabled")
        private val IS_PREMIUM = booleanPreferencesKey("is_premium")

        // Spam Detection
        private val SPAM_DETECTION_ENABLED = booleanPreferencesKey("spam_detection_enabled")
        private val SPAM_CONFIDENCE_THRESHOLD = floatPreferencesKey("spam_confidence_threshold")

        // Driving Mode
        private val DRIVING_MODE_ENABLED = booleanPreferencesKey("driving_mode_enabled")
        private val DRIVING_MODE_AUTO_REPLY = stringPreferencesKey("driving_mode_auto_reply")
        private val DRIVING_MODE_GPS_DETECTION = booleanPreferencesKey("driving_mode_gps_detection")

        // Family Mode
        private val FAMILY_MODE_ENABLED = booleanPreferencesKey("family_mode_enabled")

        // Call Handling
        private val AUTO_REJECT_UNKNOWN = booleanPreferencesKey("auto_reject_unknown")
        private val AUTO_REJECT_SPAM = booleanPreferencesKey("auto_reject_spam")
        private val RING_COUNT_THRESHOLD = intPreferencesKey("ring_count_threshold")

        // Analytics
        private val ANALYTICS_ENABLED = booleanPreferencesKey("analytics_enabled")
        private val LAST_ANALYTICS_SYNC = longPreferencesKey("last_analytics_sync")

        // Cloud Sync
        private val CLOUD_SYNC_ENABLED = booleanPreferencesKey("cloud_sync_enabled")
        private val LAST_CLOUD_SYNC = longPreferencesKey("last_cloud_sync")
        private val CLOUD_SYNC_INTERVAL = longPreferencesKey("cloud_sync_interval")

        // Privacy
        private val PRIVACY_MODE = booleanPreferencesKey("privacy_mode")
        private val DATA_COLLECTION_CONSENT = booleanPreferencesKey("data_collection_consent")

        // Theme
        private val APP_THEME = stringPreferencesKey("app_theme")
    }

    // Current Mode
    val currentMode: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[CURRENT_MODE] ?: "NORMAL"
    }

    suspend fun setCurrentMode(mode: String) {
        context.dataStore.edit { preferences ->
            preferences[CURRENT_MODE] = mode
        }
    }

    // App Enabled
    val isAppEnabled: Flow<Boolean> = context.dataStore.data.map { preferences -> preferences[IS_APP_ENABLED] ?: false // Default to false on first launch
    }

    suspend fun setAppEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[IS_APP_ENABLED] = enabled
        }
    }

    // Premium Status
    val isPremium: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[IS_PREMIUM] ?: false
    }

    suspend fun setPremium(premium: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[IS_PREMIUM] = premium
        }
    }

    // Spam Detection
    val spamDetectionEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[SPAM_DETECTION_ENABLED] ?: true
    }

    suspend fun setSpamDetectionEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[SPAM_DETECTION_ENABLED] = enabled
        }
    }

    val spamConfidenceThreshold: Flow<Float> = context.dataStore.data.map { preferences ->
        preferences[SPAM_CONFIDENCE_THRESHOLD] ?: 0.7f
    }

    suspend fun setSpamConfidenceThreshold(threshold: Float) {
        context.dataStore.edit { preferences ->
            preferences[SPAM_CONFIDENCE_THRESHOLD] = threshold
        }
    }

    // Driving Mode
    val drivingModeEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[DRIVING_MODE_ENABLED] ?: false
    }

    suspend fun setDrivingModeEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[DRIVING_MODE_ENABLED] = enabled
        }
    }

    val drivingModeAutoReply: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[DRIVING_MODE_AUTO_REPLY] ?: "I'm currently driving. I will call you back shortly."
    }

    suspend fun setDrivingModeAutoReply(message: String) {
        context.dataStore.edit { preferences ->
            preferences[DRIVING_MODE_AUTO_REPLY] = message
        }
    }

    val drivingModeGpsDetection: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[DRIVING_MODE_GPS_DETECTION] ?: false
    }

    suspend fun setDrivingModeGpsDetection(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[DRIVING_MODE_GPS_DETECTION] = enabled
        }
    }

    // Family Mode
    val familyModeEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[FAMILY_MODE_ENABLED] ?: false
    }

    suspend fun setFamilyModeEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[FAMILY_MODE_ENABLED] = enabled
        }
    }

    // Call Handling
    val autoRejectUnknown: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[AUTO_REJECT_UNKNOWN] ?: false
    }

    suspend fun setAutoRejectUnknown(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[AUTO_REJECT_UNKNOWN] = enabled
        }
    }

    val autoRejectSpam: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[AUTO_REJECT_SPAM] ?: true
    }

    suspend fun setAutoRejectSpam(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[AUTO_REJECT_SPAM] = enabled
        }
    }

    val ringCountThreshold: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[RING_COUNT_THRESHOLD] ?: 5
    }

    suspend fun setRingCountThreshold(count: Int) {
        context.dataStore.edit { preferences ->
            preferences[RING_COUNT_THRESHOLD] = count
        }
    }

    // Analytics
    val analyticsEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[ANALYTICS_ENABLED] ?: true
    }

    suspend fun setAnalyticsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[ANALYTICS_ENABLED] = enabled
        }
    }

    val lastAnalyticsSync: Flow<Long> = context.dataStore.data.map { preferences ->
        preferences[LAST_ANALYTICS_SYNC] ?: 0L
    }

    suspend fun setLastAnalyticsSync(timestamp: Long) {
        context.dataStore.edit { preferences ->
            preferences[LAST_ANALYTICS_SYNC] = timestamp
        }
    }

    // Cloud Sync
    val cloudSyncEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[CLOUD_SYNC_ENABLED] ?: false
    }

    suspend fun setCloudSyncEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[CLOUD_SYNC_ENABLED] = enabled
        }
    }

    val lastCloudSync: Flow<Long> = context.dataStore.data.map { preferences ->
        preferences[LAST_CLOUD_SYNC] ?: 0L
    }

    suspend fun setLastCloudSync(timestamp: Long) {
        context.dataStore.edit { preferences ->
            preferences[LAST_CLOUD_SYNC] = timestamp
        }
    }

    val cloudSyncInterval: Flow<Long> = context.dataStore.data.map { preferences ->
        preferences[CLOUD_SYNC_INTERVAL] ?: (24 * 60 * 60 * 1000) // 24 hours default
    }

    suspend fun setCloudSyncInterval(interval: Long) {
        context.dataStore.edit { preferences ->
            preferences[CLOUD_SYNC_INTERVAL] = interval
        }
    }

    // Privacy
    val privacyMode: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PRIVACY_MODE] ?: false
    }

    suspend fun setPrivacyMode(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PRIVACY_MODE] = enabled
        }
    }

    val dataCollectionConsent: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[DATA_COLLECTION_CONSENT] ?: false
    }

    suspend fun setDataCollectionConsent(consent: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[DATA_COLLECTION_CONSENT] = consent
        }
    }

    // Theme
    val appTheme: Flow<com.akshaglobal.smartcallshield.data.model.AppTheme> = context.dataStore.data.map { preferences ->
        val themeName = preferences[APP_THEME] ?: com.akshaglobal.smartcallshield.data.model.AppTheme.SYSTEM.name
        try {
            com.akshaglobal.smartcallshield.data.model.AppTheme.valueOf(themeName)
        } catch (e: Exception) {
            com.akshaglobal.smartcallshield.data.model.AppTheme.SYSTEM
        }
    }

    suspend fun setAppTheme(theme: com.akshaglobal.smartcallshield.data.model.AppTheme) {
        context.dataStore.edit { preferences ->
            preferences[APP_THEME] = theme.name
        }
    }
}
