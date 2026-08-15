package com.phuzle.labs.repacks.ui.configure

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.phuzle.labs.repacks.BuildConfig
import com.phuzle.labs.repacks.ui.components.HudBackdrop
import com.phuzle.labs.repacks.ui.components.HudTopBar
import com.phuzle.labs.repacks.ui.components.NeonPanel
import com.phuzle.labs.repacks.ui.theme.NeonCyan

@Composable
fun UpdatesScreen(viewModel: ConfigureViewModel, onBack: () -> Unit) {
    val updateState by viewModel.updateState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { padding ->
        HudBackdrop(modifier = Modifier.padding(padding)) {
            Column(Modifier.fillMaxSize()) {
                HudTopBar(title = "Updates", onBack = onBack, accent = NeonCyan)
                NeonPanel(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), accent = NeonCyan, glow = false) {
                    Text("Current version", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(BuildConfig.VERSION_NAME, style = MaterialTheme.typography.titleMedium)

                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 16.dp)) {
                        TextButton(onClick = viewModel::checkForUpdate, enabled = updateState !is UpdateCheckState.Checking) {
                            Text("Check for updates")
                        }
                        if (updateState is UpdateCheckState.Checking) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = NeonCyan)
                        }
                    }

                    when (val state = updateState) {
                        is UpdateCheckState.UpToDate -> Text(
                            "You're on the latest version.",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        is UpdateCheckState.Available -> {
                            Text(
                                "Update ${state.info.versionName} is available.",
                                style = MaterialTheme.typography.labelMedium,
                                color = NeonCyan,
                            )
                            if (viewModel.updateInstaller.canRequestInstallPackages()) {
                                TextButton(onClick = { viewModel.installUpdate(state.info.release) }) { Text("Download & install") }
                            } else {
                                TextButton(onClick = { context.startActivity(viewModel.updateInstaller.requestInstallPermissionIntent()) }) {
                                    Text("Grant install permission")
                                }
                            }
                        }
                        is UpdateCheckState.Error -> Text(
                            state.message,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.error,
                        )
                        else -> Unit
                    }
                }
            }
        }
    }
}
