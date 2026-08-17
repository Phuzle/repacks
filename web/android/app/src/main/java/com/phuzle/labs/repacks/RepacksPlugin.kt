package com.phuzle.labs.repacks

import com.getcapacitor.JSArray
import com.getcapacitor.JSObject
import com.getcapacitor.Plugin
import com.getcapacitor.PluginCall
import com.getcapacitor.PluginMethod
import com.getcapacitor.annotation.CapacitorPlugin
import com.phuzle.labs.repacks.core.AppContainer
import com.phuzle.labs.repacks.data.local.RepackEntity
import com.phuzle.labs.repacks.data.local.WatchlistEntity
import com.phuzle.labs.repacks.data.prefs.ThemeMode
import com.phuzle.labs.repacks.data.prefs.UserPreferences
import com.phuzle.labs.repacks.data.remote.github.GitHubRelease
import com.phuzle.labs.repacks.work.WorkScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/** The entire bridge between the (unchanged) native data/work/notification/updater layer and the
 * React UI. Every method here is a thin wrapper around an existing repository/repo call — no new
 * business logic lives in this file, only JS<->Kotlin marshalling. Keeping this file as the ONLY
 * thing that changes when the native side needs a new capability is the whole point of the
 * "native stays native, only the UI moved to React" migration. */
@CapacitorPlugin(name = "Repacks")
class RepacksPlugin : Plugin() {

    private val container: AppContainer
        get() = (activity.application as RepacksApplication).container

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // Set by the most recent checkForUpdate() call so installUpdate() doesn't need the caller to
    // round-trip the full GitHubRelease payload back through the bridge.
    private var pendingRelease: GitHubRelease? = null

    // ---- Feed -----------------------------------------------------------------------------

    @PluginMethod
    fun getFeed(call: PluginCall) = scope.launch {
        runCatching { container.repackRepository.observeFeed().first() }
            .onSuccess { items ->
                val ret = JSObject()
                ret.put("items", items.toJSArray())
                call.resolve(ret)
            }
            .onFailure { call.reject(it.message ?: "getFeed failed") }
    }

    @PluginMethod
    fun getItem(call: PluginCall) = scope.launch {
        val provider = call.getString("provider") ?: return@launch call.reject("provider is required")
        val slug = call.getString("slug") ?: return@launch call.reject("slug is required")
        runCatching { container.repackRepository.observeItem(provider, slug).first() }
            .onSuccess { item -> call.resolve(item?.toJSObject() ?: JSObject()) }
            .onFailure { call.reject(it.message ?: "getItem failed") }
    }

    @PluginMethod
    fun sync(call: PluginCall) = scope.launch {
        runCatching { container.repackRepository.sync() }
            .onSuccess { result ->
                val ret = JSObject()
                ret.put("watchlistMatches", result.watchlistMatches.toJSArray())
                ret.put("otherNewItems", result.otherNewItems.toJSArray())
                ret.put("shouldBackoff", result.shouldBackoff)
                call.resolve(ret)
            }
            .onFailure { call.reject(it.message ?: "sync failed") }
    }

    @PluginMethod
    fun setFavorited(call: PluginCall) = scope.launch {
        val id = call.getLong("id") ?: return@launch call.reject("id is required")
        val favorited = call.getBoolean("favorited") ?: return@launch call.reject("favorited is required")
        runCatching { container.repackRepository.setFavorited(id, favorited) }
            .onSuccess { call.resolve() }
            .onFailure { call.reject(it.message ?: "setFavorited failed") }
    }

    // ---- Watchlist --------------------------------------------------------------------------

    @PluginMethod
    fun getWatchlist(call: PluginCall) = scope.launch {
        runCatching { container.repackRepository.observeWatchlist().first() }
            .onSuccess { keywords ->
                val ret = JSObject()
                val arr = JSArray()
                keywords.forEach { arr.put(it.toJSObject()) }
                ret.put("keywords", arr)
                call.resolve(ret)
            }
            .onFailure { call.reject(it.message ?: "getWatchlist failed") }
    }

    @PluginMethod
    fun addWatchlistKeyword(call: PluginCall) = scope.launch {
        val keyword = call.getString("keyword") ?: return@launch call.reject("keyword is required")
        runCatching { container.repackRepository.addWatchlistKeyword(keyword) }
            .onSuccess { call.resolve() }
            .onFailure { call.reject(it.message ?: "addWatchlistKeyword failed") }
    }

    @PluginMethod
    fun removeWatchlistKeyword(call: PluginCall) = scope.launch {
        val id = call.getLong("id") ?: return@launch call.reject("id is required")
        runCatching { container.repackRepository.removeWatchlistKeyword(id) }
            .onSuccess { call.resolve() }
            .onFailure { call.reject(it.message ?: "removeWatchlistKeyword failed") }
    }

    // ---- Preferences ------------------------------------------------------------------------

    @PluginMethod
    fun getPreferences(call: PluginCall) = scope.launch {
        runCatching { container.userPreferencesRepository.current() }
            .onSuccess { call.resolve(it.toJSObject()) }
            .onFailure { call.reject(it.message ?: "getPreferences failed") }
    }

    @PluginMethod
    fun setThemeMode(call: PluginCall) = scope.launch {
        val raw = call.getString("mode") ?: return@launch call.reject("mode is required")
        val mode = runCatching { ThemeMode.valueOf(raw) }.getOrNull() ?: return@launch call.reject("unknown theme mode: $raw")
        runCatching { container.userPreferencesRepository.setThemeMode(mode) }
            .onSuccess { call.resolve() }
            .onFailure { call.reject(it.message ?: "setThemeMode failed") }
    }

    @PluginMethod
    fun setProviderEnabled(call: PluginCall) = scope.launch {
        val id = call.getString("id") ?: return@launch call.reject("id is required")
        val enabled = call.getBoolean("enabled") ?: return@launch call.reject("enabled is required")
        runCatching { container.userPreferencesRepository.setProviderEnabled(id, enabled) }
            .onSuccess { call.resolve() }
            .onFailure { call.reject(it.message ?: "setProviderEnabled failed") }
    }

    @PluginMethod
    fun setNsfwFilterEnabled(call: PluginCall) = scope.launch {
        val enabled = call.getBoolean("enabled") ?: return@launch call.reject("enabled is required")
        runCatching { container.userPreferencesRepository.setNsfwFilterEnabled(enabled) }
            .onSuccess { call.resolve() }
            .onFailure { call.reject(it.message ?: "setNsfwFilterEnabled failed") }
    }

    @PluginMethod
    fun setMaxSizeGb(call: PluginCall) = scope.launch {
        // 0 (or absent) means "no cap" — mirrors the nullable Float in UserPreferences.
        val value = call.getDouble("value")?.toFloat()
        runCatching { container.userPreferencesRepository.setMaxSizeGb(value) }
            .onSuccess { call.resolve() }
            .onFailure { call.reject(it.message ?: "setMaxSizeGb failed") }
    }

    @PluginMethod
    fun setSyncIntervalHours(call: PluginCall) = scope.launch {
        val hours = call.getInt("hours") ?: return@launch call.reject("hours is required")
        runCatching {
            container.userPreferencesRepository.setSyncIntervalHours(hours)
            WorkScheduler.reschedule(context, container.userPreferencesRepository.current())
        }
            .onSuccess { call.resolve() }
            .onFailure { call.reject(it.message ?: "setSyncIntervalHours failed") }
    }

    @PluginMethod
    fun setWifiOnly(call: PluginCall) = scope.launch {
        val enabled = call.getBoolean("enabled") ?: return@launch call.reject("enabled is required")
        runCatching {
            container.userPreferencesRepository.setWifiOnly(enabled)
            WorkScheduler.reschedule(context, container.userPreferencesRepository.current())
        }
            .onSuccess { call.resolve() }
            .onFailure { call.reject(it.message ?: "setWifiOnly failed") }
    }

    @PluginMethod
    fun setQuietHours(call: PluginCall) = scope.launch {
        val enabled = call.getBoolean("enabled") ?: return@launch call.reject("enabled is required")
        val start = call.getInt("startHour") ?: return@launch call.reject("startHour is required")
        val end = call.getInt("endHour") ?: return@launch call.reject("endHour is required")
        runCatching { container.userPreferencesRepository.setQuietHours(enabled, start, end) }
            .onSuccess { call.resolve() }
            .onFailure { call.reject(it.message ?: "setQuietHours failed") }
    }

    @PluginMethod
    fun setSilentNotificationsOnly(call: PluginCall) = scope.launch {
        val silent = call.getBoolean("silent") ?: return@launch call.reject("silent is required")
        runCatching { container.userPreferencesRepository.setSilentNotificationsOnly(silent) }
            .onSuccess { call.resolve() }
            .onFailure { call.reject(it.message ?: "setSilentNotificationsOnly failed") }
    }

    @PluginMethod
    fun setProxyListRaw(call: PluginCall) = scope.launch {
        val raw = call.getString("raw") ?: ""
        runCatching { container.userPreferencesRepository.setProxyListRaw(raw) }
            .onSuccess { call.resolve() }
            .onFailure { call.reject(it.message ?: "setProxyListRaw failed") }
    }

    @PluginMethod
    fun setAutoRotateOnBlock(call: PluginCall) = scope.launch {
        val enabled = call.getBoolean("enabled") ?: return@launch call.reject("enabled is required")
        runCatching { container.userPreferencesRepository.setAutoRotateOnBlock(enabled) }
            .onSuccess { call.resolve() }
            .onFailure { call.reject(it.message ?: "setAutoRotateOnBlock failed") }
    }

    @PluginMethod
    fun setAutoUpdateCheckEnabled(call: PluginCall) = scope.launch {
        val enabled = call.getBoolean("enabled") ?: return@launch call.reject("enabled is required")
        runCatching { container.userPreferencesRepository.setAutoUpdateCheckEnabled(enabled) }
            .onSuccess { call.resolve() }
            .onFailure { call.reject(it.message ?: "setAutoUpdateCheckEnabled failed") }
    }

    // ---- Self-updater -----------------------------------------------------------------------

    @PluginMethod
    fun checkForUpdate(call: PluginCall) = scope.launch {
        val force = call.getBoolean("force") ?: false
        runCatching { container.updateChecker.checkForUpdate(force) }
            .onSuccess { info ->
                pendingRelease = info?.release
                val ret = JSObject()
                ret.put("available", info != null)
                if (info != null) ret.put("versionName", info.versionName)
                call.resolve(ret)
            }
            .onFailure { call.reject(it.message ?: "checkForUpdate failed") }
    }

    @PluginMethod
    fun canRequestInstallPackages(call: PluginCall) {
        val ret = JSObject()
        ret.put("canInstall", container.updateInstaller.canRequestInstallPackages())
        call.resolve(ret)
    }

    @PluginMethod
    fun requestInstallPermission(call: PluginCall) {
        activity.startActivity(container.updateInstaller.requestInstallPermissionIntent())
        call.resolve()
    }

    @PluginMethod
    fun installUpdate(call: PluginCall) {
        val release = pendingRelease ?: return call.reject("No update was found by the last checkForUpdate() call")
        container.updateInstaller.downloadAndInstall(release)
        call.resolve()
    }
}

private fun List<RepackEntity>.toJSArray(): JSArray {
    val arr = JSArray()
    forEach { arr.put(it.toJSObject()) }
    return arr
}

private fun RepackEntity.toJSObject(): JSObject = JSObject().apply {
    put("id", id)
    put("guid", guid)
    put("provider", provider)
    put("slug", slug)
    put("title", title)
    put("bannerUrl", bannerUrl)
    put("originalUrl", originalUrl)
    put("originalSize", originalSize)
    put("repackSize", repackSize)
    put("genres", genres) // JSON-encoded array string — JSON.parse on the JS side
    put("description", description)
    put("timestamp", timestamp)
    put("isNsfw", isNsfw)
    put("isFavorited", isFavorited)
}

private fun WatchlistEntity.toJSObject(): JSObject = JSObject().apply {
    put("id", id)
    put("keyword", keyword)
}

private fun UserPreferences.toJSObject(): JSObject = JSObject().apply {
    put("themeMode", themeMode.name)
    put("enabledProviderIds", JSArray(enabledProviderIds.toTypedArray()))
    put("nsfwFilterEnabled", nsfwFilterEnabled)
    maxSizeGb?.let { put("maxSizeGb", it) }
    put("syncIntervalHours", syncIntervalHours)
    put("wifiOnly", wifiOnly)
    put("quietHoursEnabled", quietHoursEnabled)
    put("quietHoursStartHour", quietHoursStartHour)
    put("quietHoursEndHour", quietHoursEndHour)
    put("silentNotificationsOnly", silentNotificationsOnly)
    put("proxyListRaw", proxyListRaw)
    put("autoRotateOnBlock", autoRotateOnBlock)
    lastRotationStatus?.let { put("lastRotationStatus", it) }
    put("autoUpdateCheckEnabled", autoUpdateCheckEnabled)
}
