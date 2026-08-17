package com.phuzle.labs.repacks.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.phuzle.labs.repacks.RepacksApplication
import com.phuzle.labs.repacks.data.prefs.UserPreferences
import java.time.LocalTime

/** Periodic/on-demand feed sync (PRD §2.2, §6). Fetches every enabled provider, fires drop
 * notifications for anything new, and asks WorkManager for an exponential-backoff retry when a
 * provider came back rate-limited (PRD §6.2). */
class RepackSyncWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val container = (applicationContext as RepacksApplication).container
        return try {
            val syncResult = container.repackRepository.sync()
            val prefs = container.userPreferencesRepository.current()
            val quiet = isInQuietHours(prefs)

            for (entity in syncResult.watchlistMatches) {
                container.notificationHelper.notifyNewRepack(
                    entity,
                    highPriority = true,
                    silent = quiet || prefs.silentNotificationsOnly,
                )
            }
            if (!prefs.silentNotificationsOnly) {
                for (entity in syncResult.otherNewItems) {
                    container.notificationHelper.notifyNewRepack(entity, highPriority = false, silent = quiet)
                }
            }

            if (syncResult.shouldBackoff) Result.retry() else Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    private fun isInQuietHours(prefs: UserPreferences): Boolean {
        if (!prefs.quietHoursEnabled) return false
        val hour = LocalTime.now().hour
        val start = prefs.quietHoursStartHour
        val end = prefs.quietHoursEndHour
        return if (start <= end) hour in start until end else hour >= start || hour < end
    }
}
