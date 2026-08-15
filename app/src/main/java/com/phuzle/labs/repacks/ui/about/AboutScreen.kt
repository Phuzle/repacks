package com.phuzle.labs.repacks.ui.about

import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.phuzle.labs.repacks.BuildConfig
import com.phuzle.labs.repacks.ui.components.HudBackdrop
import com.phuzle.labs.repacks.ui.components.HudTopBar
import com.phuzle.labs.repacks.ui.components.NeonPanel
import com.phuzle.labs.repacks.ui.theme.NeonMagenta

@Composable
fun AboutScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    fun open(url: String) = CustomTabsIntent.Builder().build().launchUrl(context, url.toUri())

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { padding ->
        HudBackdrop(modifier = Modifier.padding(padding)) {
            Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                HudTopBar(title = "About & Disclaimers", onBack = onBack, accent = NeonMagenta)

                NeonPanel(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    accent = NeonMagenta,
                    glow = false,
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("REPACKS", style = MaterialTheme.typography.titleLarge, color = NeonMagenta)
                        Text("Version ${BuildConfig.VERSION_NAME}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

                        Text(
                            text = "Repacks is an open-source metadata aggregator and RSS reader. This application " +
                                "does not host, index, or distribute any game files, torrents, magnets, or direct " +
                                "download links. All trademarks, titles, and cover art belong to their respective owners.",
                            style = MaterialTheme.typography.bodyMedium,
                        )

                        HorizontalDivider()

                        TextButton(onClick = { open("https://github.com/Phuzle") }) { Text("Phuzle on GitHub") }
                        TextButton(onClick = { open("https://github.com/Phuzle/repacks") }) { Text("Source repository") }
                        TextButton(onClick = { open("https://github.com/Phuzle/repacks/issues") }) { Text("Report an issue") }
                        TextButton(onClick = { open("https://github.com/Phuzle/repacks/blob/main/LICENSE") }) { Text("MIT License") }
                    }
                }
            }
        }
    }
}
