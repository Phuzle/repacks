package com.phuzle.labs.repacks.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

/** The app's shared card chrome: a flat surface-colored panel with a thin neutral outline and a
 * slim accent bar down the leading edge for color-coding — deliberately not a full glowing
 * accent-colored border (that read as "too much neon" once every card had one). */
@Composable
fun NeonPanel(
    modifier: Modifier = Modifier,
    accent: Color = MaterialTheme.colorScheme.primary,
    shape: Shape = MaterialTheme.shapes.medium,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    showAccentBar: Boolean = true,
    content: @Composable ColumnScope.() -> Unit,
) {
    // IntrinsicSize.Min goes first (outermost) so it only sets the *default* height — if the
    // caller's own `modifier` fixes an explicit height (e.g. DetailScreen's stat tiles), that
    // later modifier wins instead of being clobbered by this one.
    Row(
        modifier = Modifier
            .height(IntrinsicSize.Min)
            .then(modifier)
            .clip(shape)
            .background(MaterialTheme.colorScheme.surface, shape)
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f), shape),
    ) {
        if (showAccentBar) {
            Box(modifier = Modifier.fillMaxHeight().width(3.dp).background(accent))
        }
        Column(modifier = Modifier.weight(1f).padding(contentPadding), content = content)
    }
}

/** A single rounded-pill accent chip shape, used for small badges/filter chips. */
val NeonChipShape: Shape = RoundedCornerShape(50)
