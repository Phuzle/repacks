package com.phuzle.labs.repacks.ui.configure

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.phuzle.labs.repacks.ui.components.HudBackdrop
import com.phuzle.labs.repacks.ui.components.HudTopBar
import com.phuzle.labs.repacks.ui.theme.NeonAcid
import com.phuzle.labs.repacks.ui.theme.NeonAcidOnLight
import com.phuzle.labs.repacks.ui.theme.NeonCyan
import com.phuzle.labs.repacks.ui.theme.NeonCyanOnLight
import com.phuzle.labs.repacks.ui.theme.NeonMagenta
import com.phuzle.labs.repacks.ui.theme.NeonMagentaOnLight
import com.phuzle.labs.repacks.ui.theme.NeonViolet
import com.phuzle.labs.repacks.ui.theme.NeonVioletOnLight
import com.phuzle.labs.repacks.ui.theme.themedAccent

private data class ConfigureCategory(
    val title: String,
    val subtitle: String,
    val accent: Color,
    val icon: ImageVector,
    val route: String,
)

/** Settings hub, restyled as one grouped list (icon + title/subtitle + chevron rows, divider
 * separated) instead of six separate bordered tiles — a standard settings-screen layout reads far
 * cleaner than a stack of boxed cards. */
@Composable
fun ConfigureScreen(onNavigate: (route: String) -> Unit, onBack: () -> Unit) {
    val cyan = themedAccent(NeonCyan, NeonCyanOnLight)
    val magenta = themedAccent(NeonMagenta, NeonMagentaOnLight)
    val violet = themedAccent(NeonViolet, NeonVioletOnLight)
    val acid = themedAccent(NeonAcid, NeonAcidOnLight)
    val categories = listOf(
        ConfigureCategory("Providers", "Enable or disable release sources", cyan, Icons.Filled.Dns, "configure/providers"),
        ConfigureCategory("Filters & Watchlist", "NSFW filter, size cap, tracked titles", magenta, Icons.Filled.FilterAlt, "configure/filters"),
        ConfigureCategory("Sync & Anti-Block", "Schedule, Wi-Fi only, identity rotation", violet, Icons.Filled.Sync, "configure/sync"),
        ConfigureCategory("Appearance", "Theme", acid, Icons.Filled.Palette, "configure/appearance"),
        ConfigureCategory("Updates", "Check for and install new versions", cyan, Icons.Filled.SystemUpdate, "configure/updates"),
        ConfigureCategory("About & Disclaimers", "Attribution, license, source", magenta, Icons.Filled.Info, "about"),
    )

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { padding ->
        HudBackdrop(modifier = Modifier.padding(padding)) {
            Column(Modifier.fillMaxSize()) {
                HudTopBar(title = "Settings", onBack = onBack)
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surface),
                ) {
                    categories.forEachIndexed { index, category ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onNavigate(category.route) }
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(category.icon, contentDescription = null, tint = category.accent, modifier = Modifier.size(22.dp))
                            Column(modifier = Modifier.weight(1f).padding(start = 14.dp)) {
                                Text(
                                    text = category.title,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                                Text(
                                    text = category.subtitle,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(top = 2.dp),
                                )
                            }
                            Icon(
                                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        if (index != categories.lastIndex) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                        }
                    }
                }
            }
        }
    }
}
