package com.phuzle.labs.repacks.data.prefs

/** Snapshot of every user-configurable setting from the Configure tab (PRD §4.2.3). */
data class UserPreferences(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val enabledProviderIds: Set<String> = DEFAULT_ENABLED_PROVIDERS,
    val nsfwFilterEnabled: Boolean = true,
    val maxSizeGb: Float? = null,
    val syncIntervalHours: Int = 6,
    val wifiOnly: Boolean = false,
    val quietHoursEnabled: Boolean = false,
    val quietHoursStartHour: Int = 22,
    val quietHoursEndHour: Int = 8,
    val notificationPermissionRequested: Boolean = false,
    val silentNotificationsOnly: Boolean = false,
    val proxyListRaw: String = "",
    val autoRotateOnBlock: Boolean = true,
    val lastRotationStatus: String? = null,
    val lastUpdateCheckMillis: Long = 0L,
) {
    /** Parsed, blank-filtered proxy entries — one per line of [proxyListRaw]. */
    val proxyList: List<String>
        get() = proxyListRaw.lineSequence().map { it.trim() }.filter { it.isNotEmpty() }.toList()

    companion object {
        val DEFAULT_ENABLED_PROVIDERS = setOf("fitgirl", "dodi", "steamrip", "kaoskrew")
    }
}
