package com.phuzle.labs.repacks.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.phuzle.labs.repacks.data.local.RepackEntity
import com.phuzle.labs.repacks.data.remote.providers.FeedProvider
import com.phuzle.labs.repacks.ui.theme.accentForProvider
import java.util.concurrent.TimeUnit

/** One feed entry (PRD §4.2.1), reworked as a HUD panel: neon accent border per provider, angular
 * banner clip, uppercase provider badge, monospace-flavored meta line. */
@Composable
fun RepackCard(item: RepackEntity, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val accent = accentForProvider(item.provider)
    NeonPanel(
        modifier = modifier.fillMaxWidth().clickable(onClick = onClick),
        accent = accent,
        contentPadding = PaddingValues(0.dp),
    ) {
        Box(modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f)) {
            var loaded by remember(item.id) { mutableStateOf(false) }
            if (!loaded) ShimmerBox(modifier = Modifier.fillMaxWidth())
            AsyncImage(
                model = item.bannerUrl,
                contentDescription = item.title,
                modifier = Modifier.fillMaxWidth(),
                contentScale = ContentScale.Crop,
                onSuccess = { loaded = true },
                onError = { loaded = true },
            )
            Text(
                text = FeedProvider.fromId(item.provider)?.displayName?.uppercase() ?: item.provider.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.background,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(10.dp)
                    .clip(NeonChipShape)
                    .background(accent)
                    .padding(horizontal = 10.dp, vertical = 5.dp),
            )
        }
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Row(modifier = Modifier.padding(top = 6.dp)) {
                Text(
                    text = relativeTime(item.timestamp).uppercase(),
                    style = MaterialTheme.typography.labelMedium,
                    color = accent,
                )
                item.repackSize?.let { size ->
                    Text(
                        text = "  //  $size",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

fun relativeTime(timestampMillis: Long): String {
    val diffMillis = (System.currentTimeMillis() - timestampMillis).coerceAtLeast(0)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(diffMillis)
    val hours = TimeUnit.MILLISECONDS.toHours(diffMillis)
    val days = TimeUnit.MILLISECONDS.toDays(diffMillis)
    return when {
        minutes < 1 -> "just now"
        minutes < 60 -> "${minutes}m ago"
        hours < 24 -> "${hours}h ago"
        else -> "${days}d ago"
    }
}
