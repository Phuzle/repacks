package com.phuzle.labs.repacks.ui.configure

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.phuzle.labs.repacks.data.local.WatchlistEntity
import com.phuzle.labs.repacks.data.prefs.ThemeMode
import com.phuzle.labs.repacks.data.prefs.UserPreferences
import com.phuzle.labs.repacks.data.prefs.UserPreferencesRepository
import com.phuzle.labs.repacks.data.remote.github.GitHubRelease
import com.phuzle.labs.repacks.data.repository.RepackRepository
import com.phuzle.labs.repacks.updater.UpdateChecker
import com.phuzle.labs.repacks.updater.UpdateInfo
import com.phuzle.labs.repacks.updater.UpdateInstaller
import com.phuzle.labs.repacks.work.WorkScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface UpdateCheckState {
    data object Idle : UpdateCheckState
    data object Checking : UpdateCheckState
    data object UpToDate : UpdateCheckState
    data class Available(val info: UpdateInfo) : UpdateCheckState
    data class Error(val message: String) : UpdateCheckState
}

class ConfigureViewModel(
    private val prefsRepository: UserPreferencesRepository,
    private val repackRepository: RepackRepository,
    private val updateChecker: UpdateChecker,
    val updateInstaller: UpdateInstaller,
    private val appContext: Context,
) : ViewModel() {

    val prefs: StateFlow<UserPreferences> = prefsRepository.preferencesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserPreferences())

    val watchlist: StateFlow<List<WatchlistEntity>> = repackRepository.observeWatchlist()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _updateState = MutableStateFlow<UpdateCheckState>(UpdateCheckState.Idle)
    val updateState: StateFlow<UpdateCheckState> = _updateState

    fun setProviderEnabled(id: String, enabled: Boolean) = launchAndPersist { prefsRepository.setProviderEnabled(id, enabled) }

    fun setThemeMode(mode: ThemeMode) = launchAndPersist { prefsRepository.setThemeMode(mode) }

    fun setNsfwFilterEnabled(enabled: Boolean) = launchAndPersist { prefsRepository.setNsfwFilterEnabled(enabled) }

    fun setMaxSizeGb(value: Float?) = launchAndPersist { prefsRepository.setMaxSizeGb(value) }

    fun setSyncIntervalHours(hours: Int) = viewModelScope.launch {
        prefsRepository.setSyncIntervalHours(hours)
        WorkScheduler.reschedule(appContext, prefsRepository.current())
    }

    fun setWifiOnly(enabled: Boolean) = viewModelScope.launch {
        prefsRepository.setWifiOnly(enabled)
        WorkScheduler.reschedule(appContext, prefsRepository.current())
    }

    fun setQuietHours(enabled: Boolean, startHour: Int, endHour: Int) =
        launchAndPersist { prefsRepository.setQuietHours(enabled, startHour, endHour) }

    fun setSilentNotificationsOnly(silent: Boolean) = launchAndPersist { prefsRepository.setSilentNotificationsOnly(silent) }

    fun setProxyListRaw(raw: String) = launchAndPersist { prefsRepository.setProxyListRaw(raw) }

    fun setAutoRotateOnBlock(enabled: Boolean) = launchAndPersist { prefsRepository.setAutoRotateOnBlock(enabled) }

    fun addWatchlistKeyword(keyword: String) = launchAndPersist { repackRepository.addWatchlistKeyword(keyword) }

    fun removeWatchlistKeyword(id: Long) = launchAndPersist { repackRepository.removeWatchlistKeyword(id) }

    fun markNotificationPermissionRequested() = launchAndPersist { prefsRepository.setNotificationPermissionRequested(true) }

    fun checkForUpdate() {
        viewModelScope.launch {
            _updateState.value = UpdateCheckState.Checking
            _updateState.value = try {
                val info = updateChecker.checkForUpdate(force = true)
                if (info != null) UpdateCheckState.Available(info) else UpdateCheckState.UpToDate
            } catch (e: Exception) {
                UpdateCheckState.Error(e.message ?: "Update check failed")
            }
        }
    }

    fun dismissUpdateState() {
        _updateState.value = UpdateCheckState.Idle
    }

    fun installUpdate(release: GitHubRelease) {
        updateInstaller.downloadAndInstall(release)
    }

    private fun launchAndPersist(block: suspend () -> Unit) = viewModelScope.launch { block() }
}
