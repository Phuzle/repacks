package com.phuzle.labs.repacks.ui.detail

import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.phuzle.labs.repacks.data.local.RepackEntity
import com.phuzle.labs.repacks.data.remote.providers.FeedProvider
import com.phuzle.labs.repacks.data.remote.providers.SizeUnits
import com.phuzle.labs.repacks.ui.components.relativeTime
import org.json.JSONArray

@Composable
fun DetailScreen(viewModel: DetailViewModel, onBack: () -> Unit) {
    val item by viewModel.item.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val current = item

    Scaffold(
        bottomBar = {
            if (current != null) {
                val providerName = FeedProvider.fromId(current.provider)?.displayName ?: current.provider
                Surface(shadowElevation = 8.dp) {
                    Button(
                        onClick = {
                            CustomTabsIntent.Builder().build().launchUrl(context, current.originalUrl.toUri())
                        },
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                    ) {
                        Icon(Icons.Filled.OpenInBrowser, contentDescription = null)
                        Text(" Open on $providerName", modifier = Modifier.padding(start = 4.dp))
                    }
                }
            }
        },
    ) { padding ->
        if (current == null) return@Scaffold
        Column(modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())) {
            Box(modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f)) {
                AsyncImage(
                    model = current.bannerUrl,
                    contentDescription = current.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
                Box(
                    modifier = Modifier.fillMaxSize().background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.75f)),
                            startY = 0f,
                        ),
                    ),
                )
                IconButton(onClick = onBack, modifier = Modifier.align(Alignment.TopStart).padding(8.dp)) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Text(
                    text = current.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.align(Alignment.BottomStart).padding(16.dp),
                )
            }

            MetadataGrid(current)

            val genres = parseGenres(current.genres)
            if (genres.isNotEmpty()) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(genres) { genre -> SuggestionChip(onClick = {}, label = { Text(genre) }) }
                }
            }

            current.description?.let { description ->
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(16.dp),
                )
            }

            // Room for the fixed bottom CTA button.
            Box(modifier = Modifier.fillMaxWidth().size(72.dp))
        }
    }
}

@Composable
private fun MetadataGrid(item: RepackEntity) {
    val reduction = reductionPercent(item.originalSize, item.repackSize)
    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            MetadataStat(label = "Repack Size", value = item.repackSize ?: "—", modifier = Modifier.weight(1f))
            MetadataStat(label = "Original Size", value = item.originalSize ?: "—", modifier = Modifier.weight(1f))
        }
        Row(modifier = Modifier.fillMaxWidth().padding(top = 12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            MetadataStat(label = "Reduction", value = reduction?.let { "$it%" } ?: "—", modifier = Modifier.weight(1f))
            MetadataStat(label = "Released", value = relativeTime(item.timestamp), modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun MetadataStat(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(text = value, style = MaterialTheme.typography.titleMedium)
        Text(text = label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
