package com.phuzle.labs.repacks.data.remote.providers

enum class FeedKind { RSS, ATOM }

/** The four sources PRD §6.1 lists, each with its feed endpoint and the parser it needs. */
enum class FeedProvider(val id: String, val displayName: String, val feedUrl: String, val kind: FeedKind) {
    FITGIRL("fitgirl", "FitGirl Repacks", "https://fitgirl-repacks.site/feed/", FeedKind.RSS),
    DODI("dodi", "DODI Repacks", "https://dodi-repacks.site/feed/", FeedKind.RSS),
    STEAMRIP("steamrip", "SteamRIP", "https://steamrip.com/feed/", FeedKind.RSS),

    // KaOsKrew publishes release threads on a forum rather than a fixed top-level RSS endpoint;
    // this URL is a best-effort default and may need adjusting to the forum's actual release-
    // subforum feed URL once that's confirmed — see Configure → Providers to disable it meanwhile.
    KAOSKREW("kaoskrew", "KaOsKrew", "https://kaoskrew.org/feed/", FeedKind.ATOM),
    ;

    companion object {
        fun fromId(id: String): FeedProvider? = entries.find { it.id == id }
    }
}
