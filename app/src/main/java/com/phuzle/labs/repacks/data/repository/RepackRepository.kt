package com.phuzle.labs.repacks.data.repository

import android.content.Context
import com.phuzle.labs.repacks.data.local.RepackDao
import com.phuzle.labs.repacks.data.local.RepackEntity
import com.phuzle.labs.repacks.data.local.WatchlistDao
import com.phuzle.labs.repacks.data.local.WatchlistEntity
import com.phuzle.labs.repacks.data.prefs.UserPreferencesRepository
import com.phuzle.labs.repacks.data.remote.network.ProxyPool
import com.phuzle.labs.repacks.data.remote.network.RotatingCallFactory
import com.phuzle.labs.repacks.data.remote.providers.AtomFeedParser
import com.phuzle.labs.repacks.data.remote.providers.FeedKind
import com.phuzle.labs.repacks.data.remote.providers.FeedParser
import com.phuzle.labs.repacks.data.remote.providers.FeedProvider
import com.phuzle.labs.repacks.data.remote.providers.ParsedRepackItem
import com.phuzle.labs.repacks.data.remote.providers.RssFeedParser
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.flow.Flow
import okhttp3.Request
import org.json.JSONArray

/** Result of one sync pass (see [RepackRepository.sync]) — split so the caller (the WorkManager
 * worker) knows which items need an immediate high-priority notification (PRD §7.2). */
data class SyncResult(
    val watchlistMatches: List<RepackEntity>,
    val otherNewItems: List<RepackEntity>,
    val shouldBackoff: Boolean,
)

class RepackRepository(
    private val context: Context,
    private val repackDao: RepackDao,
    private val watchlistDao: WatchlistDao,
    private val prefsRepository: UserPreferencesRepository,
) {
    private val parsers: Map<FeedKind, FeedParser> = mapOf(
        FeedKind.RSS to RssFeedParser(),
        FeedKind.ATOM to AtomFeedParser(),
    )

    fun observeFeed(): Flow<List<RepackEntity>> = repackDao.observeAll()

    fun observeItem(provider: String, slug: String): Flow<RepackEntity?> = repackDao.observeOne(provider, slug)

    fun observeWatchlist(): Flow<List<WatchlistEntity>> = watchlistDao.observeAll()

    suspend fun addWatchlistKeyword(keyword: String) {
        val trimmed = keyword.trim()
        if (trimmed.isNotEmpty()) watchlistDao.insert(WatchlistEntity(keyword = trimmed))
    }

    suspend fun removeWatchlistKeyword(id: Long) = watchlistDao.delete(id)

    suspend fun setFavorited(id: Long, favorited: Boolean) = repackDao.setFavorited(id, favorited)

    /** Fetches every enabled provider, inserts genuinely-new items (diffed by guid), matches them
     * against the watchlist, and runs the 30-day retention cleanup (PRD §7.3) — all in one pass. */
    suspend fun sync(): SyncResult {
        val prefs = prefsRepository.current()
        val proxyConfigs = ProxyPool.parse(prefs.proxyList)
        val callFactory = RotatingCallFactory(context.cacheDir, proxyConfigs, prefs.autoRotateOnBlock)

        var rotationNote: String? = null
        var anyRateLimited = false
        val newlyInserted = mutableListOf<RepackEntity>()

        val enabledProviders = FeedProvider.entries.filter { it.id in prefs.enabledProviderIds }
        for (provider in enabledProviders) {
            try {
                val requestBuilder = Request.Builder().url(provider.feedUrl)
                val result = callFactory.execute(requestBuilder)
                result.rotationNote?.let { rotationNote = it }
                result.response.use { response ->
                    if (response.code == 429 || response.code == 503) {
                        anyRateLimited = true
                        return@use
                    }
                    if (!response.isSuccessful) return@use
                    // A conditional GET (ETag/If-Modified-Since) that OkHttp's shared Cache
                    // resolved to "unchanged" surfaces here as a normal 200 with the same body
                    // as last time — filterExistingGuids below naturally drops all of it, so
                    // there's nothing further to special-case for the 304 case.
                    val body = response.body?.string() ?: return@use
                    val parsedItems = parsers.getValue(provider.kind).parse(body)
                    if (parsedItems.isEmpty()) return@use

                    val guids = parsedItems.map { it.guid }
                    val existingGuids = repackDao.filterExistingGuids(guids).toSet()
                    val freshItems = parsedItems.filter { it.guid !in existingGuids }
                    if (freshItems.isEmpty()) return@use

                    val entities = freshItems.map { it.toEntity(provider.id) }
                    val rowIds = repackDao.insertAll(entities)
                    entities.zip(rowIds).forEach { (entity, rowId) ->
                        if (rowId != -1L) newlyInserted += entity.copy(id = rowId)
                    }
                }
            } catch (e: IOException) {
                // This provider failed this cycle (network/still-blocked) — existing data stays
                // put, next scheduled sync tries again.
            }
        }

        rotationNote?.let { prefsRepository.setLastRotationStatus(it) }

        val cutoff = System.currentTimeMillis() - RETENTION_MILLIS
        repackDao.deleteOlderThan(cutoff)

        val watchlistKeywords = watchlistDao.snapshotKeywords()
        val watchlistMatches = newlyInserted.filter { matchesWatchlist(it, watchlistKeywords) }
        val watchlistMatchIds = watchlistMatches.mapTo(mutableSetOf()) { it.id }
        val otherNew = newlyInserted.filter { it.id !in watchlistMatchIds }

        return SyncResult(watchlistMatches, otherNew, anyRateLimited)
    }

    private fun matchesWatchlist(entity: RepackEntity, keywords: List<String>): Boolean {
        if (keywords.isEmpty()) return false
        val haystack = entity.title.lowercase()
        return keywords.any { keyword -> keyword.isNotBlank() && haystack.contains(keyword.trim().lowercase()) }
    }

    private fun ParsedRepackItem.toEntity(providerId: String) = RepackEntity(
        guid = guid,
        provider = providerId,
        slug = slug,
        title = title,
        bannerUrl = bannerUrl,
        originalUrl = originalUrl,
        originalSize = originalSize,
        repackSize = repackSize,
        genres = genres.takeIf { it.isNotEmpty() }?.let { JSONArray(it).toString() },
        description = description,
        timestamp = timestamp,
        isNsfw = isNsfw,
    )

    companion object {
        private val RETENTION_MILLIS = TimeUnit.DAYS.toMillis(30)
    }
}
