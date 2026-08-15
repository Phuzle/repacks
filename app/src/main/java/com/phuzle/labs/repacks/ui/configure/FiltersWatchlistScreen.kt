package com.phuzle.labs.repacks.ui.configure

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.phuzle.labs.repacks.data.local.WatchlistEntity
import com.phuzle.labs.repacks.ui.components.HudBackdrop
import com.phuzle.labs.repacks.ui.components.HudSectionLabel
import com.phuzle.labs.repacks.ui.components.HudTopBar
import com.phuzle.labs.repacks.ui.components.NeonPanel
import com.phuzle.labs.repacks.ui.components.ToggleRow
import com.phuzle.labs.repacks.ui.components.rememberNotificationPermissionRequester
import com.phuzle.labs.repacks.ui.theme.NeonMagenta

@Composable
fun FiltersWatchlistScreen(viewModel: ConfigureViewModel, onBack: () -> Unit) {
    val prefs by viewModel.prefs.collectAsStateWithLifecycle()
    val watchlist by viewModel.watchlist.collectAsStateWithLifecycle()
    val maybeRequestPermission = rememberNotificationPermissionRequester(
        alreadyRequested = prefs.notificationPermissionRequested,
        onMarkRequested = viewModel::markNotificationPermissionRequested,
    )

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { padding ->
        HudBackdrop(modifier = Modifier.padding(padding)) {
            Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                HudTopBar(title = "Filters & Watchlist", onBack = onBack, accent = NeonMagenta)

                HudSectionLabel("Filter Engine", accent = NeonMagenta, modifier = Modifier.padding(start = 20.dp, bottom = 8.dp))
                NeonPanel(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), accent = NeonMagenta, glow = false) {
                    ToggleRow(
                        label = "Filter adult / NSFW content",
                        checked = prefs.nsfwFilterEnabled,
                        onCheckedChange = viewModel::setNsfwFilterEnabled,
                    )
                    MaxSizeSlider(prefs.maxSizeGb, onChange = viewModel::setMaxSizeGb)
                }

                HudSectionLabel(
                    "Watchlist",
                    accent = NeonMagenta,
                    modifier = Modifier.padding(start = 20.dp, top = 24.dp, bottom = 8.dp),
                )
                WatchlistManager(
                    keywords = watchlist,
                    onAdd = { keyword ->
                        val wasEmpty = watchlist.isEmpty()
                        viewModel.addWatchlistKeyword(keyword)
                        if (wasEmpty) maybeRequestPermission()
                    },
                    onRemove = viewModel::removeWatchlistKeyword,
                )

                Spacer(Modifier.size(24.dp))
            }
        }
    }
}

@Composable
private fun MaxSizeSlider(maxSizeGb: Float?, onChange: (Float?) -> Unit) {
    val enabled = maxSizeGb != null
    Column {
        ToggleRow(
            label = "Cap max repack size",
            checked = enabled,
            onCheckedChange = { on -> onChange(if (on) 80f else null) },
        )
        if (enabled) {
            Text(
                "Skip repacks larger than ${maxSizeGb.toInt()} GB",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Slider(value = maxSizeGb, onValueChange = onChange, valueRange = 5f..150f, steps = 28)
        }
    }
}

@Composable
private fun WatchlistManager(keywords: List<WatchlistEntity>, onAdd: (String) -> Unit, onRemove: (Long) -> Unit) {
    var input by remember { mutableStateOf("") }
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
        OutlinedTextField(
            value = input,
            onValueChange = { input = it },
            label = { Text("Add a game title") },
            modifier = Modifier.weight(1f),
        )
        TextButton(onClick = {
            if (input.isNotBlank()) {
                onAdd(input)
                input = ""
            }
        }) { Text("Add") }
    }
    if (keywords.isNotEmpty()) {
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(keywords, key = { it.id }) { entry ->
                InputChip(
                    selected = false,
                    onClick = {},
                    label = { Text(entry.keyword) },
                    trailingIcon = {
                        IconButton(onClick = { onRemove(entry.id) }) {
                            Icon(Icons.Filled.Close, contentDescription = "Remove", modifier = Modifier.size(16.dp))
                        }
                    },
                )
            }
        }
    } else {
        Text(
            text = "No tracked titles yet — matches fire a high-priority alert immediately.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        )
    }
}
