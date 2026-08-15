package com.phuzle.labs.repacks.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.phuzle.labs.repacks.data.local.RepackEntity
import com.phuzle.labs.repacks.data.repository.RepackRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class DetailViewModel(
    repackRepository: RepackRepository,
    provider: String,
    slug: String,
) : ViewModel() {

    val item: StateFlow<RepackEntity?> = repackRepository.observeItem(provider, slug)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
}
