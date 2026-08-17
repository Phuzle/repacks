package com.phuzle.labs.repacks.data.remote.github

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

/** Self-updater's GitHub Releases lookup (PRD §7.1) — no auth needed, this is a public repo. */
class GitHubReleasesApi(private val client: OkHttpClient) {

    suspend fun fetchLatest(): GitHubRelease? = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(LATEST_RELEASE_URL)
            .header("Accept", "application/vnd.github+json")
            .header("User-Agent", "Repacks-Android-App")
            .build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) return@withContext null
            val bodyJson = response.body?.string() ?: return@withContext null
            parse(bodyJson)
        }
    }

    private fun parse(json: String): GitHubRelease {
        val obj = JSONObject(json)
        val assets = obj.optJSONArray("assets")
        var apkUrl: String? = null
        var apkName: String? = null
        if (assets != null) {
            // Prefer the universal (no ABI suffix) APK the release workflow publishes.
            for (i in 0 until assets.length()) {
                val asset = assets.getJSONObject(i)
                val name = asset.optString("name")
                if (name.endsWith(".apk", ignoreCase = true) &&
                    ABI_HINTS.none { name.contains(it, ignoreCase = true) }
                ) {
                    apkUrl = asset.optString("browser_download_url")
                    apkName = name
                    break
                }
            }
            if (apkUrl == null) {
                for (i in 0 until assets.length()) {
                    val asset = assets.getJSONObject(i)
                    val name = asset.optString("name")
                    if (name.endsWith(".apk", ignoreCase = true)) {
                        apkUrl = asset.optString("browser_download_url")
                        apkName = name
                        break
                    }
                }
            }
        }
        return GitHubRelease(
            tagName = obj.optString("tag_name"),
            name = obj.optString("name").takeIf { it.isNotBlank() },
            body = obj.optString("body").takeIf { it.isNotBlank() },
            htmlUrl = obj.optString("html_url"),
            apkAssetUrl = apkUrl,
            apkAssetName = apkName,
        )
    }

    companion object {
        private const val LATEST_RELEASE_URL = "https://api.github.com/repos/Phuzle/repacks/releases/latest"
        private val ABI_HINTS = listOf("arm64", "armeabi", "x86_64", "x86")
    }
}
