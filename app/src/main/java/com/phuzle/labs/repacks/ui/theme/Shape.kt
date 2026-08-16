package com.phuzle.labs.repacks.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// Plain rounded corners — the earlier cut-corner "HUD" shapes read as an aggressive neon-cyberpunk
// affectation rather than a real design, so every surface in the app now uses this shared scale.
val RepacksShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp),
)
