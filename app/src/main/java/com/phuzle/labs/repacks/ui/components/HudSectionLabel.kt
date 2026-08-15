package com.phuzle.labs.repacks.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/** Small-caps HUD-style label with a leading accent tick — used for section headers throughout
 * (Configure's category groups, Detail's metadata grid, etc). */
@Composable
fun HudSectionLabel(
    text: String,
    modifier: Modifier = Modifier,
    accent: Color = MaterialTheme.colorScheme.primary,
) {
    Row(modifier = modifier) {
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(14.dp)
                .background(accent, RoundedCornerShape(1.dp)),
        )
        Text(
            text = text.uppercase(),
            style = MaterialTheme.typography.titleSmall,
            color = accent,
            modifier = Modifier.padding(start = 8.dp),
        )
    }
}
