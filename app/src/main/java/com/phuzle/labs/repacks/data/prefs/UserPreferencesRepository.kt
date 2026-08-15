package com.phuzle.labs.repacks.data.prefs

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "repacks_prefs")

class UserPreferencesRepository(private val context: Context) {

    private object Keys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val ENABLED_PROVIDERS = stringSetPreferencesKey("enabled_providers")
        val NSFW_FILTER = booleanPreferencesKey("nsfw_filter_enabled")
        val MAX_SIZE_GB = floatPreferencesKey("max_size_gb")
        val SYNC_INTERVAL_HOURS = intPreferencesKey("sync_interval_hours")
        val WIFI_ONLY = booleanPreferencesKey("wifi_only")
        val QUIET_HOURS_ENABLED = booleanPreferencesKey("quiet_hours_enabled")
        val QUIET_HOURS_START = intPreferencesKey("quiet_hours_start")
        val QUIET_HOURS_END = intPreferencesKey("quiet_hours_end")
        val NOTIFICATION_PERMISSION_REQUESTED = booleanPreferencesKey("notification_permission_requested")
        val SILENT_NOTIFICATIONS_ONLY = booleanPreferencesKey("silent_notifications_only")
        val PROXY_LIST_RAW = stringPreferencesKey("proxy_list_raw")
        val AUTO_ROTATE_ON_BLOCK = booleanPreferencesKey("auto_rotate_on_block")
        val LAST_ROTATION_STATUS = stringPreferencesKey("last_rotation_status")
        val LAST_UPDATE_CHECK_MILLIS = longPreferencesKey("last_update_check_millis")
    }

    val preferencesFlow: Flow<UserPreferences> = context.dataStore.data.map { prefs ->
        UserPreferences(
            themeMode = prefs[Keys.THEME_MODE]?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() }
                ?: ThemeMode.SYSTEM,
            enabledProviderIds = prefs[Keys.ENABLED_PROVIDERS] ?: UserPreferences.DEFAULT_ENABLED_PROVIDERS,
            nsfwFilterEnabled = prefs[Keys.NSFW_FILTER] ?: true,
            maxSizeGb = prefs[Keys.MAX_SIZE_GB]?.takeIf { it > 0f },
            syncIntervalHours = prefs[Keys.SYNC_INTERVAL_HOURS] ?: 6,
            wifiOnly = prefs[Keys.WIFI_ONLY] ?: false,
            quietHoursEnabled = prefs[Keys.QUIET_HOURS_ENABLED] ?: false,
            quietHoursStartHour = prefs[Keys.QUIET_HOURS_START] ?: 22,
            quietHoursEndHour = prefs[Keys.QUIET_HOURS_END] ?: 8,
            notificationPermissionRequested = prefs[Keys.NOTIFICATION_PERMISSION_REQUESTED] ?: false,
            silentNotificationsOnly = prefs[Keys.SILENT_NOTIFICATIONS_ONLY] ?: false,
            proxyListRaw = prefs[Keys.PROXY_LIST_RAW] ?: "",
            autoRotateOnBlock = prefs[Keys.AUTO_ROTATE_ON_BLOCK] ?: true,
            lastRotationStatus = prefs[Keys.LAST_ROTATION_STATUS],
            lastUpdateCheckMillis = prefs[Keys.LAST_UPDATE_CHECK_MILLIS] ?: 0L,
        )
    }

    suspend fun current(): UserPreferences = preferencesFlow.first()

    suspend fun setThemeMode(mode: ThemeMode) = context.dataStore.edit { it[Keys.THEME_MODE] = mode.name }

    suspend fun setProviderEnabled(providerId: String, enabled: Boolean) = context.dataStore.edit { prefs ->
        val current = prefs[Keys.ENABLED_PROVIDERS] ?: UserPreferences.DEFAULT_ENABLED_PROVIDERS
        prefs[Keys.ENABLED_PROVIDERS] = if (enabled) current + providerId else current - providerId
    }

    suspend fun setNsfwFilterEnabled(enabled: Boolean) = context.dataStore.edit { it[Keys.NSFW_FILTER] = enabled }

    suspend fun setMaxSizeGb(maxSizeGb: Float?) = context.dataStore.edit {
        if (maxSizeGb == null) it.remove(Keys.MAX_SIZE_GB) else it[Keys.MAX_SIZE_GB] = maxSizeGb
    }

    suspend fun setSyncIntervalHours(hours: Int) = context.dataStore.edit { it[Keys.SYNC_INTERVAL_HOURS] = hours }

    suspend fun setWifiOnly(enabled: Boolean) = context.dataStore.edit { it[Keys.WIFI_ONLY] = enabled }

    suspend fun setQuietHours(enabled: Boolean, startHour: Int, endHour: Int) = context.dataStore.edit {
        it[Keys.QUIET_HOURS_ENABLED] = enabled
        it[Keys.QUIET_HOURS_START] = startHour
        it[Keys.QUIET_HOURS_END] = endHour
    }

    suspend fun setNotificationPermissionRequested(requested: Boolean) = context.dataStore.edit {
        it[Keys.NOTIFICATION_PERMISSION_REQUESTED] = requested
    }

    suspend fun setSilentNotificationsOnly(silent: Boolean) = context.dataStore.edit {
        it[Keys.SILENT_NOTIFICATIONS_ONLY] = silent
    }

    suspend fun setProxyListRaw(raw: String) = context.dataStore.edit { it[Keys.PROXY_LIST_RAW] = raw }

    suspend fun setAutoRotateOnBlock(enabled: Boolean) = context.dataStore.edit { it[Keys.AUTO_ROTATE_ON_BLOCK] = enabled }

    suspend fun setLastRotationStatus(status: String?) = context.dataStore.edit {
        if (status == null) it.remove(Keys.LAST_ROTATION_STATUS) else it[Keys.LAST_ROTATION_STATUS] = status
    }

    suspend fun setLastUpdateCheckMillis(millis: Long) = context.dataStore.edit { it[Keys.LAST_UPDATE_CHECK_MILLIS] = millis }
}
