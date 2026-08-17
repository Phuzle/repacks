package com.phuzle.labs.repacks.work

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.phuzle.labs.repacks.data.prefs.UserPreferences
import java.util.concurrent.TimeUnit

/** (Re)schedules [RepackSyncWorker] whenever the user changes the sync interval or Wi-Fi-only
 * toggle in Configure (PRD §4.2.3's "Sync & Schedule" section). */
object WorkScheduler {
    private const val UNIQUE_WORK_NAME = "repack_sync"
    private val MIN_BACKOFF = TimeUnit.MINUTES.toMillis(15)

    fun reschedule(context: Context, prefs: UserPreferences) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(if (prefs.wifiOnly) NetworkType.UNMETERED else NetworkType.CONNECTED)
            .build()

        val request = PeriodicWorkRequestBuilder<RepackSyncWorker>(prefs.syncIntervalHours.toLong(), TimeUnit.HOURS)
            .setConstraints(constraints)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, MIN_BACKOFF, TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(UNIQUE_WORK_NAME, ExistingPeriodicWorkPolicy.UPDATE, request)
    }

    fun triggerImmediateSync(context: Context) {
        val request = OneTimeWorkRequestBuilder<RepackSyncWorker>().build()
        WorkManager.getInstance(context).enqueue(request)
    }
}
