package com.phuzle.labs.repacks.ui.configure

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.phuzle.labs.repacks.ui.components.HudBackdrop
import com.phuzle.labs.repacks.ui.components.NeonPanel
import com.phuzle.labs.repacks.ui.theme.NeonAcid
import com.phuzle.labs.repacks.ui.theme.NeonCyan
import com.phuzle.labs.repacks.ui.theme.NeonMagenta
import com.phuzle.labs.repacks.ui.theme.NeonViolet

private data class ConfigureCategory(
    val title: String,
    val subtitle: String,
    val accent: Color,
    val route: String,
)

/** Configure hub (PRD §4.2.3, restructured into nested categories instead of one long scrolling
 * list): each tile opens its own dedicated screen. */
@Composable
fun ConfigureScreen(onNavigate: (route: String) -> Unit) {
    val categories = listOf(
        ConfigureCategory("Providers", "Enable or disable release sources", NeonCyan, "configure/providers"),
        ConfigureCategory("Filters & Watchlist", "NSFW filter, size cap, tracked titles", NeonMagenta, "configure/filters"),
        ConfigureCategory("Sync & Anti-Block", "Schedule, Wi-Fi only, identity rotation", NeonViolet, "configure/sync"),
        ConfigureCategory("Appearance", "Theme", NeonAcid, "configure/appearance"),
        ConfigureCategory("Updates", "Check for and install new versions", NeonCyan, "configure/updates"),
        ConfigureCategory("About & Disclaimers", "Attribution, license, source", NeonMagenta, "about"),
    )

    HudBackdrop {
        Column(Modifier.fillMaxSize()) {
            Text(
                text = "CONFIGURE",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 20.dp, top = 20.dp, bottom = 12.dp),
            )
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(categories) { category ->
                    NeonPanel(
                        modifier = Modifier.fillMaxWidth().clickable { onNavigate(category.route) },
                        accent = category.accent,
                        glow = false,
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column {
                                Text(
                                    text = category.title.uppercase(),
                                    style = MaterialTheme.typography.titleSmall,
                                    color = category.accent,
                                )
                                Text(
                                    text = category.subtitle,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(top = 4.dp),
                                )
                            }
                            Icon(
                                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = null,
                                tint = category.accent,
                            )
                        }
                    }
                }
            }
        }
    }
}
