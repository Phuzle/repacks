package com.phuzle.labs.repacks.data.remote.providers

enum class FeedKind { RSS, ATOM }

/** The sources PRD §6.1 lists, each with its feed endpoint and the parser it needs. */
enum class FeedProvider(val id: String, val displayName: String, val feedUrl: String, val kind: FeedKind) {
    FITGIRL("fitgirl", "FitGirl Repacks", "https://fitgirl-repacks.site/feed/", FeedKind.RSS),
    DODI("dodi", "DODI Repacks", "https://dodi-repacks.site/feed/", FeedKind.RSS),
    STEAMRIP("steamrip", "SteamRIP", "https://steamrip.com/feed/", FeedKind.RSS),

    // Disabled for now: KaOsKrew publishes release threads on a forum rather than a fixed
    // top-level RSS endpoint, and the placeholder URL below hasn't been confirmed against a real
    // feed, so AtomFeedParser has nothing reliable to poll. Re-enable once a real release-subforum
    // feed URL is confirmed. AtomFeedParser itself is unaffected and still used if this returns.
    // KAOSKREW("kaoskrew", "KaOsKrew", "https://kaoskrew.org/feed/", FeedKind.ATOM),
    ;

    companion object {
        fun fromId(id: String): FeedProvider? = entries.find { it.id == id }
    }
}
