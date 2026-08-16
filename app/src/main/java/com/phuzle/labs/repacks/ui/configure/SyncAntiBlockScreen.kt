package com.phuzle.labs.repacks.ui.configure

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
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
import com.phuzle.labs.repacks.data.prefs.UserPreferences
import com.phuzle.labs.repacks.ui.components.HudBackdrop
import com.phuzle.labs.repacks.ui.components.HudSectionLabel
import com.phuzle.labs.repacks.ui.components.HudTopBar
import com.phuzle.labs.repacks.ui.components.NeonPanel
import com.phuzle.labs.repacks.ui.components.ToggleRow
import com.phuzle.labs.repacks.ui.theme.NeonViolet
import com.phuzle.labs.repacks.ui.theme.NeonVioletOnLight
import com.phuzle.labs.repacks.ui.theme.themedAccent

private val SYNC_INTERVALS = listOf(1, 2, 6, 12)

@Composable
fun SyncAntiBlockScreen(viewModel: ConfigureViewModel, onBack: () -> Unit) {
    val prefs by viewModel.prefs.collectAsStateWithLifecycle()
    val accent = themedAccent(NeonViolet, NeonVioletOnLight)

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { padding ->
        HudBackdrop(modifier = Modifier.padding(padding)) {
            Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                HudTopBar(title = "Sync & Anti-Block", onBack = onBack, accent = accent)

                HudSectionLabel("Sync & Schedule", accent = accent, modifier = Modifier.padding(start = 20.dp, bottom = 8.dp))
                NeonPanel(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), accent = accent, glow = false) {
                    Text("Check for new drops every", style = MaterialTheme.typography.bodyLarge)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        SYNC_INTERVALS.forEach { hours ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(selected = hours == prefs.syncIntervalHours, onClick = { viewModel.setSyncIntervalHours(hours) })
                                Text("${hours}h", modifier = Modifier.padding(end = 10.dp))
                            }
                        }
                    }
                    ToggleRow(label = "Wi-Fi only", checked = prefs.wifiOnly, onCheckedChange = viewModel::setWifiOnly)
                    ToggleRow(
                        label = "Silent notifications only",
                        checked = prefs.silentNotificationsOnly,
                        onCheckedChange = viewModel::setSilentNotificationsOnly,
                    )
                    QuietHoursRow(prefs, onChange = viewModel::setQuietHours)
                }

                HudSectionLabel(
                    "Anti-Block",
                    accent = accent,
                    modifier = Modifier.padding(start = 20.dp, top = 24.dp, bottom = 8.dp),
                )
                NeonPanel(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), accent = accent, glow = false) {
                    Text(
                        text = "If a provider starts blocking requests, Repacks retries with a different " +
                            "device fingerprint and, if you've added any below, a different proxy. No " +
                            "proxies are bundled with the app — this only uses ones you supply.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    )
                    TextButton(onClick = { viewModel.setProxyListRaw(proxyText) }) { Text("Save proxy list") }
                    prefs.lastRotationStatus?.let { status ->
                        Text(status, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Spacer(Modifier.size(24.dp))
            }
        }
    }
}

@Composable
private fun QuietHoursRow(prefs: UserPreferences, onChange: (Boolean, Int, Int) -> Unit) {
    Column {
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
