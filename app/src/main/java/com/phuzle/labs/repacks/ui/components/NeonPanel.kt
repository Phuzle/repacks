package com.phuzle.labs.repacks.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

/** The app's signature card chrome: a HUD-panel shape, a neon accent border, and a soft blurred
 * glow underlay in the same accent color — used for repack cards, stat tiles, and Configure's
 * category tiles so the whole app reads as one cohesive "terminal" rather than stock widgets. */
@Composable
fun NeonPanel(
    modifier: Modifier = Modifier,
    accent: Color = MaterialTheme.colorScheme.primary,
    shape: Shape = MaterialTheme.shapes.medium,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    glow: Boolean = true,
    content: @Composable ColumnScope.() -> Unit,
) {
    Box(modifier = modifier) {
        if (glow) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .blur(20.dp)
                    .background(accent.copy(alpha = 0.16f), shape),
            )
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape)
                .background(MaterialTheme.colorScheme.surface, shape)
                .border(1.dp, accent.copy(alpha = 0.75f), shape)
                .padding(contentPadding),
            content = content,
        )
    }
}

/** A single sharp-cornered accent chip shape, used where a full HUD cut would be too busy
 * (small badges, filter chips). */
val NeonChipShape: Shape = CutCornerShape(topStart = 2.dp, bottomEnd = 10.dp)
