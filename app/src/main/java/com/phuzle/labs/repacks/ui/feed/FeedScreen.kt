package com.phuzle.labs.repacks.ui.feed

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.phuzle.labs.repacks.ui.components.FilterChipsRow
import com.phuzle.labs.repacks.ui.components.RepackCard
import com.phuzle.labs.repacks.ui.components.relativeTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(
    viewModel: FeedViewModel,
    onItemClick: (provider: String, slug: String) -> Unit,
) {
    val items by viewModel.visibleItems.collectAsStateWithLifecycle()
    val filter by viewModel.filter.collectAsStateWithLifecycle()
    val isSyncing by viewModel.isSyncing.collectAsStateWithLifecycle()
    val lastSyncedAt by viewModel.lastSyncedAt.collectAsStateWithLifecycle()

    PullToRefreshBox(isRefreshing = isSyncing, onRefresh = viewModel::refresh, modifier = Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            FeedHeader(lastSyncedAt = lastSyncedAt, isSyncing = isSyncing, onRefresh = viewModel::refresh)
            FilterChipsRow(
                selected = filter,
                onSelect = viewModel::setFilter,
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            )
            if (items.isEmpty()) {
                EmptyFeedState()
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(items, key = { it.id }) { item ->
                        RepackCard(item = item, onClick = { onItemClick(item.provider, item.slug) })
                    }
                }
            }
        }
    }
}

@Composable
private fun FeedHeader(lastSyncedAt: Long?, isSyncing: Boolean, onRefresh: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column {
            Text("Repacks", style = MaterialTheme.typography.titleLarge)
            Text(
                text = when {
                    isSyncing -> "Syncing…"
                    lastSyncedAt != null -> "Last synced ${relativeTime(lastSyncedAt)}"
                    else -> "Pull to refresh"
                },
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        IconButton(onClick = onRefresh, enabled = !isSyncing) {
            Icon(Icons.Filled.Refresh, contentDescription = "Refresh")
        }
    }
}

@Composable
private fun EmptyFeedState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = "No repacks yet — pull to refresh",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
