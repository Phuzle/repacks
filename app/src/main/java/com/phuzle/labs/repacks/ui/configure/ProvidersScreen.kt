package com.phuzle.labs.repacks.ui.configure

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.phuzle.labs.repacks.data.remote.providers.FeedProvider
import com.phuzle.labs.repacks.ui.components.HudBackdrop
import com.phuzle.labs.repacks.ui.components.HudTopBar
import com.phuzle.labs.repacks.ui.components.NeonPanel
import com.phuzle.labs.repacks.ui.components.ToggleRow
import com.phuzle.labs.repacks.ui.components.rememberNotificationPermissionRequester
import com.phuzle.labs.repacks.ui.theme.accentForProvider

@Composable
fun ProvidersScreen(viewModel: ConfigureViewModel, onBack: () -> Unit) {
    val prefs by viewModel.prefs.collectAsStateWithLifecycle()
    val maybeRequestPermission = rememberNotificationPermissionRequester(
        alreadyRequested = prefs.notificationPermissionRequested,
        onMarkRequested = viewModel::markNotificationPermissionRequested,
    )

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { padding ->
        HudBackdrop(modifier = Modifier.padding(padding)) {
            Column(Modifier.fillMaxSize()) {
                HudTopBar(title = "Providers", onBack = onBack)
                Text(
                    text = "Release sources to monitor for new drops.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 12.dp),
                )
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(FeedProvider.entries) { provider ->
                        val accent = accentForProvider(provider.id)
                        NeonPanel(modifier = Modifier.fillMaxWidth(), accent = accent) {
                            ToggleRow(
                                label = provider.displayName,
                                checked = provider.id in prefs.enabledProviderIds,
                                onCheckedChange = { enabled ->
                                    viewModel.setProviderEnabled(provider.id, enabled)
                                    if (enabled) maybeRequestPermission()
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}
