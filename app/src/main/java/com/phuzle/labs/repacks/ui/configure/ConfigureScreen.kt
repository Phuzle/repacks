package com.phuzle.labs.repacks.ui.configure

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.phuzle.labs.repacks.data.local.WatchlistEntity
import com.phuzle.labs.repacks.data.prefs.ThemeMode
import com.phuzle.labs.repacks.data.prefs.UserPreferences
import com.phuzle.labs.repacks.data.remote.github.GitHubRelease
import com.phuzle.labs.repacks.data.remote.providers.FeedProvider
import com.phuzle.labs.repacks.ui.components.NotificationRationaleDialog

private val SYNC_INTERVALS = listOf(1, 2, 6, 12)

@Composable
fun ConfigureScreen(viewModel: ConfigureViewModel, onNavigateAbout: () -> Unit) {
    val prefs by viewModel.prefs.collectAsStateWithLifecycle()
    val watchlist by viewModel.watchlist.collectAsStateWithLifecycle()
    val updateState by viewModel.updateState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var showPermissionRationale by remember { mutableStateOf(false) }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        viewModel.markNotificationPermissionRequested()
    }

    fun maybeRequestNotificationPermission() {
        if (prefs.notificationPermissionRequested) return
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            viewModel.markNotificationPermissionRequested()
            return
        }
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
        ) {
            viewModel.markNotificationPermissionRequested()
            return
        }
        showPermissionRationale = true
    }

    if (showPermissionRationale) {
        NotificationRationaleDialog(
            onAllow = {
                showPermissionRationale = false
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            },
            onDismiss = {
                showPermissionRationale = false
                viewModel.markNotificationPermissionRequested()
            },
        )
    }

    LazyColumn(contentPadding = PaddingValues(vertical = 16.dp)) {
        item {
            SectionTitle("Providers")
            FeedProvider.entries.forEach { provider ->
                ToggleRow(
                    label = provider.displayName,
                    checked = provider.id in prefs.enabledProviderIds,
                    onCheckedChange = { enabled ->
                        viewModel.setProviderEnabled(provider.id, enabled)
                        if (enabled) maybeRequestNotificationPermission()
                    },
                )
            }
            HorizontalDivider(Modifier.padding(vertical = 12.dp))
        }

        item {
            SectionTitle("Filter Engine")
            ToggleRow(
                label = "Filter adult / NSFW content",
                checked = prefs.nsfwFilterEnabled,
                onCheckedChange = viewModel::setNsfwFilterEnabled,
            )
            MaxSizeSlider(prefs.maxSizeGb, onChange = viewModel::setMaxSizeGb)
            HorizontalDivider(Modifier.padding(vertical = 12.dp))
        }

        item {
            SectionTitle("Watchlist")
            WatchlistSection(
                keywords = watchlist,
                onAdd = { keyword ->
                    val wasEmpty = watchlist.isEmpty()
                    viewModel.addWatchlistKeyword(keyword)
                    if (wasEmpty) maybeRequestNotificationPermission()
                },
                onRemove = viewModel::removeWatchlistKeyword,
            )
            HorizontalDivider(Modifier.padding(vertical = 12.dp))
        }

        item {
            SectionTitle("Sync & Schedule")
            SyncIntervalRow(prefs.syncIntervalHours, onSelect = viewModel::setSyncIntervalHours)
            ToggleRow(label = "Wi-Fi only", checked = prefs.wifiOnly, onCheckedChange = viewModel::setWifiOnly)
            ToggleRow(
                label = "Silent notifications only",
                checked = prefs.silentNotificationsOnly,
                onCheckedChange = viewModel::setSilentNotificationsOnly,
            )
            QuietHoursRow(prefs, onChange = viewModel::setQuietHours)
            HorizontalDivider(Modifier.padding(vertical = 12.dp))
        }

        item {
            SectionTitle("Anti-Block")
            Text(
                text = "If a provider starts blocking requests, Repacks retries with a different " +
                    "device fingerprint and, if you've added any below, a different proxy. No " +
                    "proxies are bundled with the app — this only uses ones you supply.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            )
            ToggleRow(
                label = "Auto-rotate identity on block",
                checked = prefs.autoRotateOnBlock,
                onCheckedChange = viewModel::setAutoRotateOnBlock,
            )
            var proxyText by remember(prefs.proxyListRaw) { mutableStateOf(prefs.proxyListRaw) }
            OutlinedTextField(
                value = proxyText,
                onValueChange = { proxyText = it },
                label = { Text("Proxy list (one per line, host:port)") },
                placeholder = { Text("http://user:pass@1.2.3.4:8080") },
                minLines = 3,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            )
            TextButton(
                onClick = { viewModel.setProxyListRaw(proxyText) },
                modifier = Modifier.padding(horizontal = 12.dp),
            ) { Text("Save proxy list") }
            prefs.lastRotationStatus?.let { status ->
                Text(
                    text = status,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }
            HorizontalDivider(Modifier.padding(vertical = 12.dp))
        }

        item {
            SectionTitle("App Settings")
            ThemeSelectorRow(prefs.themeMode, onSelect = viewModel::setThemeMode)
            UpdateRow(
                state = updateState,
                canInstallPackages = viewModel.updateInstaller.canRequestInstallPackages(),
                onCheck = viewModel::checkForUpdate,
                onGrantInstallPermission = { context.startActivity(viewModel.updateInstaller.requestInstallPermissionIntent()) },
                onInstall = viewModel::installUpdate,
            )
            TextButton(onClick = onNavigateAbout, modifier = Modifier.padding(horizontal = 12.dp)) {
                Text("About & Disclaimers")
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
    )
}

@Composable
private fun ToggleRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun MaxSizeSlider(maxSizeGb: Float?, onChange: (Float?) -> Unit) {
    val enabled = maxSizeGb != null
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
        ToggleRow(
            label = "Cap max repack size",
            checked = enabled,
            onCheckedChange = { on -> onChange(if (on) 80f else null) },
        )
        if (enabled) {
            Text("Skip repacks larger than ${maxSizeGb.toInt()} GB", style = MaterialTheme.typography.labelMedium)
            Slider(value = maxSizeGb, onValueChange = onChange, valueRange = 5f..150f, steps = 28)
        }
    }
}

@Composable
private fun WatchlistSection(keywords: List<WatchlistEntity>, onAdd: (String) -> Unit, onRemove: (Long) -> Unit) {
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
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
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
    }
}

@Composable
private fun SyncIntervalRow(selectedHours: Int, onSelect: (Int) -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text("Check for new drops every", style = MaterialTheme.typography.bodyLarge)
        Row(verticalAlignment = Alignment.CenterVertically) {
            SYNC_INTERVALS.forEach { hours ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = hours == selectedHours, onClick = { onSelect(hours) })
                    Text("${hours}h", modifier = Modifier.padding(end = 12.dp))
                }
            }
        }
    }
}

@Composable
private fun QuietHoursRow(prefs: UserPreferences, onChange: (Boolean, Int, Int) -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
        ToggleRow(
            label = "Quiet hours",
            checked = prefs.quietHoursEnabled,
            onCheckedChange = { enabled -> onChange(enabled, prefs.quietHoursStartHour, prefs.quietHoursEndHour) },
        )
        if (prefs.quietHoursEnabled) {
            Text(
                text = "Silent from ${prefs.quietHoursStartHour}:00 to ${prefs.quietHoursEndHour}:00",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ThemeSelectorRow(selected: ThemeMode, onSelect: (ThemeMode) -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
        Text("Theme", style = MaterialTheme.typography.bodyLarge)
        listOf(
            ThemeMode.SYSTEM to "System",
            ThemeMode.LIGHT to "Light",
            ThemeMode.DARK_AMOLED to "Dark / AMOLED",
        ).forEach { (mode, label) ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = mode == selected, onClick = { onSelect(mode) })
                Text(label)
            }
        }
    }
}

@Composable
private fun UpdateRow(
    state: UpdateCheckState,
    canInstallPackages: Boolean,
    onCheck: () -> Unit,
    onGrantInstallPermission: () -> Unit,
    onInstall: (GitHubRelease) -> Unit,
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onCheck, enabled = state !is UpdateCheckState.Checking) {
                Text("Check for updates")
            }
            if (state is UpdateCheckState.Checking) {
                CircularProgressIndicator(modifier = Modifier.size(16.dp))
            }
        }
        when (state) {
            is UpdateCheckState.UpToDate -> Text("You're on the latest version.", style = MaterialTheme.typography.labelMedium)
            is UpdateCheckState.Available -> {
                Text(
                    "Update ${state.info.versionName} is available.",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
                if (canInstallPackages) {
                    TextButton(onClick = { onInstall(state.info.release) }) { Text("Download & install") }
                } else {
                    TextButton(onClick = onGrantInstallPermission) { Text("Grant install permission") }
                }
            }
            is UpdateCheckState.Error -> Text(state.message, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.error)
            else -> Unit
        }
    }
}
