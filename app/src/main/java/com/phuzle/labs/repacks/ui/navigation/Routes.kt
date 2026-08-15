package com.phuzle.labs.repacks.ui.navigation

object Routes {
    const val FEED = "feed"
    const val CONFIGURE = "configure"
    const val CONFIGURE_PROVIDERS = "configure/providers"
    const val CONFIGURE_FILTERS_WATCHLIST = "configure/filters"
    const val CONFIGURE_SYNC_ANTIBLOCK = "configure/sync"
    const val CONFIGURE_APPEARANCE = "configure/appearance"
    const val CONFIGURE_UPDATES = "configure/updates"
    const val ABOUT = "about"
    const val DETAIL_PATTERN = "detail/{provider}/{slug}"

    fun detail(provider: String, slug: String) = "detail/$provider/$slug"
}
