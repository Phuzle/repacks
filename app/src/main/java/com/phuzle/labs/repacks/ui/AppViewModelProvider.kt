package com.phuzle.labs.repacks.ui

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.phuzle.labs.repacks.core.AppContainer
import com.phuzle.labs.repacks.ui.configure.ConfigureViewModel
import com.phuzle.labs.repacks.ui.detail.DetailViewModel
import com.phuzle.labs.repacks.ui.feed.FeedViewModel

object AppViewModelProvider {

    fun feedFactory(container: AppContainer): ViewModelProvider.Factory = viewModelFactory {
        initializer { FeedViewModel(container.repackRepository, container.userPreferencesRepository) }
    }

    fun configureFactory(container: AppContainer): ViewModelProvider.Factory = viewModelFactory {
        initializer {
            ConfigureViewModel(
                container.userPreferencesRepository,
                container.repackRepository,
                container.updateChecker,
                container.updateInstaller,
                container.appContext,
            )
        }
    }

    fun detailFactory(container: AppContainer, provider: String, slug: String): ViewModelProvider.Factory = viewModelFactory {
        initializer { DetailViewModel(container.repackRepository, provider, slug) }
    }
}
