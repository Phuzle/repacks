package com.phuzle.labs.repacks.ui.navigation

object Routes {
    const val FEED = "feed"
    const val CONFIGURE = "configure"
    const val ABOUT = "about"
    const val DETAIL_PATTERN = "detail/{provider}/{slug}"

    fun detail(provider: String, slug: String) = "detail/$provider/$slug"
}
