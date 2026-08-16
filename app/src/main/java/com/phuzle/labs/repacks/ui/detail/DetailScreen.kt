package com.phuzle.labs.repacks.ui.detail

import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.phuzle.labs.repacks.data.local.RepackEntity
import com.phuzle.labs.repacks.data.remote.providers.FeedProvider
import com.phuzle.labs.repacks.data.remote.providers.SizeUnits
import com.phuzle.labs.repacks.ui.components.HudBackdrop
import com.phuzle.labs.repacks.ui.components.NeonChipShape
import com.phuzle.labs.repacks.ui.components.NeonPanel
import com.phuzle.labs.repacks.ui.components.relativeTime
import com.phuzle.labs.repacks.ui.theme.RepacksShapes
import com.phuzle.labs.repacks.ui.theme.accentForProvider
import org.json.JSONArray

@Composable
fun DetailScreen(viewModel: DetailViewModel, onBack: () -> Unit) {
    val item by viewModel.item.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val current = item
    val accent = current?.let { accentForProvider(it.provider) } ?: MaterialTheme.colorScheme.primary

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (current != null) {
                val providerName = FeedProvider.fromId(current.provider)?.displayName ?: current.provider
                Surface(color = MaterialTheme.colorScheme.background, shadowElevation = 8.dp) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                            .clip(RepacksShapes.small)
                            .background(accent)
                            .clickable {
                                CustomTabsIntent.Builder().build().launchUrl(context, current.originalUrl.toUri())
                            }
                            .padding(vertical = 11.dp),
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        Text(
                            text = "OPEN ON ${providerName.uppercase()}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.background,
                        )
                    }
                }
            }
        },
    ) { padding ->
        if (current == null) return@Scaffold
        val backgroundColor = MaterialTheme.colorScheme.background
        HudBackdrop(modifier = Modifier.padding(padding)) {
            Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                Box(modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f)) {
                    AsyncImage(
                        model = current.bannerUrl,
                        contentDescription = current.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                    )
                    // Clear at the top so the image itself reads as a hero shot, then bleeds
                    // smoothly into the page background over the bottom third instead of a hard
                    // edge — the title Column below is pulled up on top of that faded region.
                    Box(
                        modifier = Modifier.fillMaxSize().background(
                            Brush.verticalGradient(
                                colorStops = arrayOf(0f to Color.Transparent, 0.55f to Color.Transparent, 1f to backgroundColor),
                            ),
                        ),
                    )
                }

                Column(modifier = Modifier.fillMaxWidth().offset(y = (-28).dp).padding(horizontal = 16.dp)) {
                    Text(
                        text = current.title,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                }

                MetadataGrid(current, accent)

                val genres = parseGenres(current.genres)
                if (genres.isNotEmpty()) {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(genres) { genre ->
                            Box(
                                modifier = Modifier
                                    .clip(NeonChipShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                            ) {
                                Text(
                                    text = genre.uppercase(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }

                current.description?.let { description ->
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(16.dp),
                    )
                }

                // Room for the fixed bottom CTA button.
                Box(modifier = Modifier.fillMaxWidth().size(72.dp))
            }

            // Pinned above the scrolling Column (not inside it) so it stays put instead of
            // scrolling away with the hero image — this was the "back button" complaint.
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(12.dp)
                    .clip(NeonChipShape)
                    .background(Color.Black.copy(alpha = 0.5f)),
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = accent)
            }
        }
    }
}

@Composable
private fun MetadataGrid(item: RepackEntity, accent: Color) {
    val reduction = reductionPercent(item.originalSize, item.repackSize)
    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            MetadataStat("REPACK SIZE", item.repackSize ?: "—", accent, Modifier.weight(1f))
            MetadataStat("ORIGINAL SIZE", item.originalSize ?: "—", accent, Modifier.weight(1f))
        }
        Row(modifier = Modifier.padding(top = 10.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            MetadataStat("REDUCTION", reduction?.let { "$it%" } ?: "—", accent, Modifier.weight(1f))
            MetadataStat("RELEASED", relativeTime(item.timestamp).uppercase(), accent, Modifier.weight(1f))
        }
    }
}

@Composable
private fun MetadataStat(label: String, value: String, accent: Color, modifier: Modifier = Modifier) {
    NeonPanel(
        modifier = modifier.height(74.dp),
        accent = accent,
        shape = MaterialTheme.shapes.small,
        contentPadding = PaddingValues(12.dp),
    ) {
        Text(text = value, style = MaterialTheme.typography.titleMedium)
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 2.dp),
        )
    }
}

private fun reductionPercent(originalSize: String?, repackSize: String?): Int? {
    val original = SizeUnits.parseToGb(originalSize) ?: return null
    val repack = SizeUnits.parseToGb(repackSize) ?: return null
    if (original <= 0f) return null
    return (((original - repack) / original) * 100f).toInt().coerceIn(0, 100)
}

private fun parseGenres(genresJson: String?): List<String> {
    if (genresJson.isNullOrBlank()) return emptyList()
    return runCatching {
        val array = JSONArray(genresJson)
        (0 until array.length()).map { array.getString(it) }
    }.getOrDefault(emptyList())
}
