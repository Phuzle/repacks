package com.phuzle.labs.repacks.updater

import com.phuzle.labs.repacks.data.prefs.UserPreferencesRepository
import com.phuzle.labs.repacks.data.remote.github.GitHubRelease
import com.phuzle.labs.repacks.data.remote.github.GitHubReleasesApi
import java.util.concurrent.TimeUnit

data class UpdateInfo(val release: GitHubRelease, val versionName: String)

/** In-app self-updater (PRD §7.1) — the app has no other update channel since it isn't on Play. */
class UpdateChecker(
    private val api: GitHubReleasesApi,
    private val prefsRepository: UserPreferencesRepository,
    private val currentVersionName: String,
) {
    suspend fun checkForUpdate(force: Boolean = false): UpdateInfo? {
        val prefs = prefsRepository.current()
        val now = System.currentTimeMillis()
        if (!force && now - prefs.lastUpdateCheckMillis < THROTTLE_MILLIS) return null
        prefsRepository.setLastUpdateCheckMillis(now)

        val release = api.fetchLatest() ?: return null
        val latestVersion = release.tagName.removePrefix("v")
        return if (isNewer(latestVersion, currentVersionName)) UpdateInfo(release, latestVersion) else null
    }

    private fun isNewer(latest: String, current: String): Boolean {
        val latestParts = latest.substringBefore("-").split(".").mapNotNull { it.toIntOrNull() }
        val currentParts = current.substringBefore("-").split(".").mapNotNull { it.toIntOrNull() }
        for (i in 0 until maxOf(latestParts.size, currentParts.size)) {
            val l = latestParts.getOrElse(i) { 0 }
            val c = currentParts.getOrElse(i) { 0 }
            if (l != c) return l > c
        }
        return false
    }

    companion object {
        private val THROTTLE_MILLIS = TimeUnit.HOURS.toMillis(24)
    }
}
