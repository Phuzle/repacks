package com.phuzle.labs.repacks.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

/** Contextual permission-rationale dialog (PRD §5.1 step 3) — shown only after the user has
 * already opted into something that needs alerts (enabling a provider, adding a watchlist term),
 * never on first launch. */
@Composable
fun NotificationRationaleDialog(onAllow: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Stay in the loop") },
        text = {
            Text(
                "Repacks needs notification access to alert you when your monitored games drop. " +
                    "We do not run background ads or spam.",
            )
        },
        confirmButton = { TextButton(onClick = onAllow) { Text("Allow") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Not Now") } },
    )
}
