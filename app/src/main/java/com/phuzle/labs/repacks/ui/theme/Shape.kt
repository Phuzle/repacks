package com.phuzle.labs.repacks.ui.theme

import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/** HUD-panel shapes: corners cut diagonally instead of rounded — the single biggest lever for
 * moving this app's shape language away from "stock Material". CutCornerShape is a
 * CornerBasedShape, which is what Material3's Shapes() requires for theme-wide defaults. */
val RepacksShapes = Shapes(
    extraSmall = CutCornerShape(4.dp),
    small = CutCornerShape(8.dp),
    medium = CutCornerShape(14.dp),
    large = CutCornerShape(20.dp),
    extraLarge = CutCornerShape(28.dp),
)
