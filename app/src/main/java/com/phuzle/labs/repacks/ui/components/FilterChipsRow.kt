package com.phuzle.labs.repacks.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.phuzle.labs.repacks.data.remote.providers.FeedProvider
import com.phuzle.labs.repacks.ui.feed.FeedFilter
import com.phuzle.labs.repacks.ui.theme.accentForProvider

/** Horizontally scrollable quick filters (PRD §4.2.1): All / per-provider / Watchlist Only, styled
 * as angular HUD toggle pills, with a fade-out edge so the row visibly hints it scrolls. */
@Composable
fun FilterChipsRow(
    selected: FeedFilter,
    onSelect: (FeedFilter) -> Unit,
    modifier: Modifier = Modifier,
) {
    val options: List<Triple<String, FeedFilter, androidx.compose.ui.graphics.Color>> = buildList {
        add(Triple("All", FeedFilter.All, MaterialTheme.colorScheme.primary))
        FeedProvider.entries.forEach {
            add(Triple(it.displayName, FeedFilter.Provider(it.id), accentForProvider(it.id)))
        }
        add(Triple("Watchlist", FeedFilter.WatchlistOnly, MaterialTheme.colorScheme.tertiary))
    }

    // Fixed height rather than wrap-content: this Box sits in a Column alongside a weight(1f)
    // sibling (the feed list), and Column measures non-weighted children with an unbounded max
    // height — leaving fillMaxHeight() below to resolve to "infinite" and starve the weighted
    // sibling down to zero. A definite height here breaks that chain.
    Box(modifier = modifier.height(44.dp)) {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(start = 16.dp, end = 32.dp),
        ) {
            items(options) { (label, filter, accent) ->
                val isSelected = filter == selected
                Box(
                    modifier = Modifier
                        .clip(NeonChipShape)
                        .background(if (isSelected) accent else MaterialTheme.colorScheme.surfaceVariant)
                        .border(1.dp, accent.copy(alpha = if (isSelected) 1f else 0.4f), NeonChipShape)
                        .clickable { onSelect(filter) }
                        .padding(horizontal = 14.dp, vertical = 9.dp),
                ) {
                    Text(
                        text = label.uppercase(),
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isSelected) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .height(44.dp)
                .width(28.dp)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.background.copy(alpha = 0f),
                            MaterialTheme.colorScheme.background,
                        ),
                    ),
                ),
        )
    }
}
