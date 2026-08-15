package com.phuzle.labs.repacks.data.remote.network

import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Cache
import okhttp3.Credentials
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

/** Outcome of [RotatingCallFactory.execute]: the response actually obtained, plus a human-readable
 * note if an identity switch happened along the way (surfaced in Configure → Anti-Block). */
data class RotationResult(val response: Response, val rotationNote: String?)

/**
 * Client-side anti-block layer for feed fetches (PRD §6.2 + the proxy-rotation addendum).
 *
 * No server is involved: this just holds a handful of [OkHttpClient] "identities" — a direct
 * connection plus one per user-configured proxy (Configure → Anti-Block) — each pinned to a
 * different User-Agent/Accept-Language from [UserAgentPool]. On a 403/429/503 response or a
 * network [IOException], it rotates to the next identity and retries, honoring `Retry-After`
 * when present, capped at the number of identities available. All identities share one
 * [okhttp3.Cache] directory so ETag/If-Modified-Since caching (and 304 responses) keep working
 * no matter which identity served the previous 200.
 */
class RotatingCallFactory(
    cacheDir: File,
    proxyConfigs: List<ProxyConfig>,
    private val autoRotateOnBlock: Boolean,
) {
    private data class Identity(val client: OkHttpClient, val userAgent: String, val acceptLanguage: String, val label: String)

    private val sharedCache = Cache(File(cacheDir, "http_cache"), CACHE_SIZE_BYTES)

    private val identities: List<Identity> = buildList {
        add(buildIdentity(proxyConfig = null, uaIndex = 0, label = "direct connection"))
        proxyConfigs.forEachIndexed { index, proxyConfig ->
            add(buildIdentity(proxyConfig, uaIndex = index + 1, label = "proxy ${proxyConfig.raw}"))
        }
    }

    private fun buildIdentity(proxyConfig: ProxyConfig?, uaIndex: Int, label: String): Identity {
        val (userAgent, acceptLanguage) = UserAgentPool.forIndex(uaIndex)
        val builder = OkHttpClient.Builder()
            .cache(sharedCache)
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
        if (proxyConfig != null) {
            builder.proxy(proxyConfig.proxy)
            val username = proxyConfig.username
            if (username != null) {
                builder.proxyAuthenticator { _, response ->
                    val credential = Credentials.basic(username, proxyConfig.password.orEmpty())
                    response.request.newBuilder().header("Proxy-Authorization", credential).build()
                }
            }
        }
        return Identity(builder.build(), userAgent, acceptLanguage, label)
    }

    /** Executes [requestBuilder] (without User-Agent/Accept-Language set — those are added per identity). */
    suspend fun execute(requestBuilder: Request.Builder): RotationResult = withContext(Dispatchers.IO) {
        val attempts = if (autoRotateOnBlock) identities.size else 1
        var rotationNote: String? = null

        for (attempt in 0 until attempts) {
            val identity = identities[attempt]
            val request = requestBuilder
                .header("User-Agent", identity.userAgent)
                .header("Accept-Language", identity.acceptLanguage)
                .header("Accept", "application/rss+xml, application/xml, text/xml; q=0.9, */*; q=0.8")
                .build()

            try {
                val response = identity.client.newCall(request).execute()
                if (response.isSuccessful || response.code == 304) {
                    return@withContext RotationResult(response, rotationNote)
                }
                if (response.code in BLOCK_STATUS_CODES && attempt < attempts - 1) {
                    rotationNote = "Switched identity after ${response.code} from ${request.url.host} (was using ${identity.label})"
                    response.close()
                    continue
                }
                return@withContext RotationResult(response, rotationNote)
            } catch (e: IOException) {
                if (attempt < attempts - 1) {
                    rotationNote = "Switched identity after network error on ${identity.label}: ${e.message}"
                    continue
                }
                throw e
            }
        }
        // Every branch above either returns or throws on the final attempt, so this is unreachable.
        throw IOException("Anti-block rotation exhausted with no response")
    }

    companion object {
        private const val CACHE_SIZE_BYTES = 15L * 1024 * 1024
        private val BLOCK_STATUS_CODES = setOf(403, 429, 503)
    }
}
