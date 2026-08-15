package com.phuzle.labs.repacks.ui.feed

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.phuzle.labs.repacks.ui.components.FilterChipsRow
import com.phuzle.labs.repacks.ui.components.HudBackdrop
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
    val syncError by viewModel.syncError.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(syncError) {
        syncError?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.dismissSyncError()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.error,
                ) { Text(data.visuals.message) }
            }
        },
    ) { scaffoldPadding ->
        HudBackdrop(modifier = Modifier.padding(scaffoldPadding)) {
            PullToRefreshBox(isRefreshing = isSyncing, onRefresh = viewModel::refresh, modifier = Modifier.fillMaxSize()) {
                Column(Modifier.fillMaxSize()) {
                    FeedHeader(lastSyncedAt = lastSyncedAt, isSyncing = isSyncing, onRefresh = viewModel::refresh)
                    FilterChipsRow(
                        selected = filter,
                        onSelect = viewModel::setFilter,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    )
                    if (items.isEmpty()) {
                        EmptyFeedState()
                    } else {
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp),
                        ) {
                            items(items, key = { it.id }) { item ->
                                RepackCard(item = item, onClick = { onItemClick(item.provider, item.slug) })
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FeedHeader(lastSyncedAt: Long?, isSyncing: Boolean, onRefresh: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 16.dp, top = 20.dp, bottom = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .width(8.dp)
                        .height(8.dp)
                        .background(
                            if (isSyncing) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary,
                            RoundedCornerShape(2.dp),
                        ),
                )
                Text(
                    text = "REPACKS",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
            Text(
                text = when {
                    isSyncing -> "SYNCING…"
                    lastSyncedAt != null -> "LAST SYNC ${relativeTime(lastSyncedAt).uppercase()}"
                    else -> "PULL TO REFRESH"
                },
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 16.dp, top = 2.dp),
            )
        }
        if (isSyncing) {
            CircularProgressIndicator(
                modifier = Modifier.padding(8.dp).width(20.dp).height(20.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.primary,
            )
        } else {
            IconButton(onClick = onRefresh) {
                Icon(Icons.Filled.Refresh, contentDescription = "Refresh", tint = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
private fun EmptyFeedState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "NO SIGNAL",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = "Pull to refresh, or check Configure → Providers",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
    }
}
