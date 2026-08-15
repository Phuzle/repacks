package com.phuzle.labs.repacks.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp

/** Faint horizontal scanline field behind every screen — the app's signature backdrop texture. */
@Composable
fun HudBackdrop(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    val lineColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.035f)
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .drawWithCache {
                val step = 28.dp.toPx()
                onDrawBehind {
                    var y = 0f
                    while (y < size.height) {
                        drawLine(lineColor, Offset(0f, y), Offset(size.width, y), strokeWidth = 1f)
                        y += step
                    }
                }
            },
        content = content,
    )
}
