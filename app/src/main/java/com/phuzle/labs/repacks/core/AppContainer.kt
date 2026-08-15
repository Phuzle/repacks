package com.phuzle.labs.repacks.core

import android.content.Context
import com.phuzle.labs.repacks.BuildConfig
import com.phuzle.labs.repacks.data.local.AppDatabase
import com.phuzle.labs.repacks.data.prefs.UserPreferencesRepository
import com.phuzle.labs.repacks.data.remote.github.GitHubReleasesApi
import com.phuzle.labs.repacks.data.repository.RepackRepository
import com.phuzle.labs.repacks.notification.NotificationHelper
import com.phuzle.labs.repacks.updater.UpdateChecker
import com.phuzle.labs.repacks.updater.UpdateInstaller
import java.util.concurrent.TimeUnit
import okhttp3.OkHttpClient

/** Hand-rolled DI container (no Hilt — see build plan) held by [com.phuzle.labs.repacks.RepacksApplication]. */
class AppContainer(context: Context) {
    val appContext: Context = context.applicationContext

    private val database by lazy { AppDatabase.getInstance(appContext) }

    val userPreferencesRepository by lazy { UserPreferencesRepository(appContext) }

    val repackRepository by lazy {
        RepackRepository(appContext, database.repackDao(), database.watchlistDao(), userPreferencesRepository)
    }

    val notificationHelper by lazy { NotificationHelper(appContext) }

    // Separate, non-rotating client for GitHub's Releases API — not a repack provider, so it
    // never needs the anti-block identity rotation.
    private val plainHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .build()
    }

    val gitHubReleasesApi by lazy { GitHubReleasesApi(plainHttpClient) }

    val updateChecker by lazy { UpdateChecker(gitHubReleasesApi, userPreferencesRepository, BuildConfig.VERSION_NAME) }

    val updateInstaller by lazy { UpdateInstaller(appContext) }
}
