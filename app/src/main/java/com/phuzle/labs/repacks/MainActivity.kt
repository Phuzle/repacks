package com.phuzle.labs.repacks

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.phuzle.labs.repacks.ui.navigation.RepacksApp
import com.phuzle.labs.repacks.ui.navigation.Routes

/** Single activity (PRD §3's Navigation Compose). Handles notification deep links
 * (`repacks://{provider}/{slug}`, built in notification/NotificationHelper.kt) into the detail route. */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        val container = (application as RepacksApplication).container
        val startDestination = deepLinkDestination(intent) ?: Routes.FEED

        setContent {
            RepacksApp(container = container, startDestination = startDestination)
        }
    }

    private fun deepLinkDestination(intent: Intent?): String? {
        val uri = intent?.data ?: return null
        if (uri.scheme != "repacks") return null
        val provider = uri.host ?: return null
        val slug = uri.pathSegments.firstOrNull() ?: return null
        return Routes.detail(provider, slug)
    }
}
