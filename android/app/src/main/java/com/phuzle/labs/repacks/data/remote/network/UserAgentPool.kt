package com.phuzle.labs.repacks.data.remote.network

/**
 * Small pool of realistic mobile-browser User-Agent / Accept-Language pairs. [RotatingCallFactory]
 * cycles through these per identity so a blocked request retries looking like a different device
 * rather than repeating the exact fingerprint that got blocked.
 */
object UserAgentPool {

    private val entries = listOf(
        "Mozilla/5.0 (Linux; Android 14; Pixel 8) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Mobile Safari/537.36" to "en-US,en;q=0.9",
        "Mozilla/5.0 (Linux; Android 13; SM-G991B) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0.0.0 Mobile Safari/537.36" to "en-GB,en;q=0.9",
        "Mozilla/5.0 (Linux; Android 14; Pixel 7 Pro) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/127.0.0.0 Mobile Safari/537.36" to "en-US,en;q=0.8",
        "Mozilla/5.0 (Android 13; Mobile; rv:128.0) Gecko/128.0 Firefox/128.0" to "en-US,en;q=0.9",
        "Mozilla/5.0 (Linux; Android 15; SM-S928B) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/129.0.0.0 Mobile Safari/537.36" to "en-CA,en;q=0.9",
        "Mozilla/5.0 (Linux; Android 12; moto g power (2022)) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/125.0.0.0 Mobile Safari/537.36" to "en-AU,en;q=0.9",
    )

    fun forIndex(index: Int): Pair<String, String> = entries[((index % entries.size) + entries.size) % entries.size]
}
