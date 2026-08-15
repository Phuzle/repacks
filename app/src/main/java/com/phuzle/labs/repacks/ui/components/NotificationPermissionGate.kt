package com.phuzle.labs.repacks.ui.components

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

/** Shared contextual notification-permission flow (PRD §5.1): call the returned function from
 * wherever the user opts into something that needs alerts — enabling a provider (Providers
 * screen) or adding the first watchlist keyword (Filters & Watchlist screen). Renders the
 * rationale dialog itself when needed. */
@Composable
fun rememberNotificationPermissionRequester(
    alreadyRequested: Boolean,
    onMarkRequested: () -> Unit,
): () -> Unit {
    val context = LocalContext.current
    var showRationale by remember { mutableStateOf(false) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        onMarkRequested()
    }

    if (showRationale) {
        NotificationRationaleDialog(
            onAllow = {
                showRationale = false
                launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
            },
            onDismiss = {
                showRationale = false
                onMarkRequested()
            },
        )
    }

    return {
        if (!alreadyRequested) {
            when {
                Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU -> onMarkRequested()
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
                    PackageManager.PERMISSION_GRANTED -> onMarkRequested()
                else -> showRationale = true
            }
        }
    }
}
