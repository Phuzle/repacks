package com.phuzle.labs.repacks.ui.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.phuzle.labs.repacks.data.local.RepackEntity
import com.phuzle.labs.repacks.data.prefs.UserPreferences
import com.phuzle.labs.repacks.data.prefs.UserPreferencesRepository
import com.phuzle.labs.repacks.data.remote.providers.SizeUnits
import com.phuzle.labs.repacks.data.repository.RepackRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FeedViewModel(
    private val repackRepository: RepackRepository,
    prefsRepository: UserPreferencesRepository,
) : ViewModel() {

    private val filterFlow = MutableStateFlow<FeedFilter>(FeedFilter.All)
    val filter: StateFlow<FeedFilter> = filterFlow

    val prefs: StateFlow<UserPreferences> = prefsRepository.preferencesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserPreferences())

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing

    private val _syncError = MutableStateFlow<String?>(null)
    val syncError: StateFlow<String?> = _syncError

    val visibleItems: StateFlow<List<RepackEntity>> = combine(
        repackRepository.observeFeed(),
        prefs,
        repackRepository.observeWatchlist(),
        filterFlow,
    ) { items, prefs, watchlist, filter ->
        val keywords = watchlist.map { it.keyword.trim().lowercase() }.filter { it.isNotEmpty() }
        items.asSequence()
            .filter { !prefs.nsfwFilterEnabled || !it.isNsfw }
            .filter { prefs.maxSizeGb == null || sizeFitsUnder(it.repackSize, prefs.maxSizeGb) }
            .filter { entity ->
                when (filter) {
                    FeedFilter.All -> true
                    is FeedFilter.Provider -> entity.provider == filter.id
                    FeedFilter.WatchlistOnly -> keywords.any { entity.title.lowercase().contains(it) }
                }
            }
            .toList()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setFilter(filter: FeedFilter) {
        filterFlow.value = filter
    }

    fun refresh() {
        if (_isSyncing.value) return
        viewModelScope.launch {
            _isSyncing.value = true
            try {
                repackRepository.sync()
                _syncError.value = null
            } catch (e: Exception) {
                // A provider-level failure is already swallowed inside sync() — this only catches
                // something unexpected (e.g. a local DB error), so refresh can never crash the app.
                _syncError.value = "Refresh failed — check your connection and try again."
            } finally {
                _isSyncing.value = false
            }
        }
    }

    fun dismissSyncError() {
        _syncError.value = null
    }
}

private fun sizeFitsUnder(sizeText: String?, maxGb: Float): Boolean {
    val gb = SizeUnits.parseToGb(sizeText) ?: return true // unknown size — don't hide it
    return gb <= maxGb
}
