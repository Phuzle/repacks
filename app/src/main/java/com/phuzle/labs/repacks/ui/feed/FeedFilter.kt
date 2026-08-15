package com.phuzle.labs.repacks.ui.feed

sealed interface FeedFilter {
    data object All : FeedFilter
    data class Provider(val id: String) : FeedFilter
    data object WatchlistOnly : FeedFilter
}
