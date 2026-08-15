package com.phuzle.labs.repacks.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.phuzle.labs.repacks.data.remote.providers.FeedProvider
import com.phuzle.labs.repacks.ui.feed.FeedFilter

/** Horizontally scrollable quick filters (PRD §4.2.1): All / per-provider / Watchlist Only. */
@Composable
fun FilterChipsRow(
    selected: FeedFilter,
    onSelect: (FeedFilter) -> Unit,
    modifier: Modifier = Modifier,
) {
    val options: List<Pair<String, FeedFilter>> = buildList {
        add("All" to FeedFilter.All)
        FeedProvider.entries.forEach { add(it.displayName to FeedFilter.Provider(it.id)) }
        add("Watchlist Only" to FeedFilter.WatchlistOnly)
    }

    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
    ) {
        items(options) { (label, filter) ->
            FilterChip(
                selected = filter == selected,
                onClick = { onSelect(filter) },
                label = { Text(label) },
            )
        }
    }
}
