package com.phuzle.labs.repacks.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.NewReleases
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.phuzle.labs.repacks.core.AppContainer
import com.phuzle.labs.repacks.data.prefs.UserPreferences
import com.phuzle.labs.repacks.ui.AppViewModelProvider
import com.phuzle.labs.repacks.ui.about.AboutScreen
import com.phuzle.labs.repacks.ui.components.NeonPanel
import com.phuzle.labs.repacks.ui.configure.AppearanceScreen
import com.phuzle.labs.repacks.ui.configure.ConfigureScreen
import com.phuzle.labs.repacks.ui.configure.ConfigureViewModel
import com.phuzle.labs.repacks.ui.configure.FiltersWatchlistScreen
import com.phuzle.labs.repacks.ui.configure.ProvidersScreen
import com.phuzle.labs.repacks.ui.configure.SyncAntiBlockScreen
import com.phuzle.labs.repacks.ui.configure.UpdatesScreen
import com.phuzle.labs.repacks.ui.detail.DetailScreen
import com.phuzle.labs.repacks.ui.detail.DetailViewModel
import com.phuzle.labs.repacks.ui.feed.FeedScreen
import com.phuzle.labs.repacks.ui.feed.FeedViewModel
import com.phuzle.labs.repacks.ui.theme.NeonCyan
import com.phuzle.labs.repacks.ui.theme.NeonCyanOnLight
import com.phuzle.labs.repacks.ui.theme.RepacksTheme
import com.phuzle.labs.repacks.ui.theme.themedAccent
import com.phuzle.labs.repacks.updater.UpdateInfo

/** No bottom nav — Feed is the app's home screen, and Settings (Configure) is reached via the
 * gear icon in its header and returned from with the system/HUD back button, like every other
 * pushed screen. */
@Composable
fun RepacksApp(container: AppContainer, startDestination: String = Routes.FEED) {
    val prefs by container.userPreferencesRepository.preferencesFlow
        .collectAsStateWithLifecycle(initialValue = UserPreferences())
    val navController = rememberNavController()

    var updateAvailable by remember { mutableStateOf<UpdateInfo?>(null) }
    LaunchedEffect(prefs.autoUpdateCheckEnabled) {
        if (prefs.autoUpdateCheckEnabled) {
            updateAvailable = runCatching { container.updateChecker.checkForUpdate(force = false) }.getOrNull()
        }
    }

    RepacksTheme(themeMode = prefs.themeMode) {
        Box(Modifier.fillMaxSize()) {
            Scaffold { padding ->
                NavHost(
                    navController = navController,
                    startDestination = startDestination,
                    modifier = Modifier.padding(padding),
                    enterTransition = { fadeIn(tween(220)) },
                    exitTransition = { fadeOut(tween(160)) },
                    popEnterTransition = { fadeIn(tween(220)) },
                    popExitTransition = { fadeOut(tween(160)) },
                ) {
                    composable(Routes.FEED) {
                        val viewModel: FeedViewModel = viewModel(factory = AppViewModelProvider.feedFactory(container))
                        FeedScreen(
                            viewModel = viewModel,
                            onItemClick = { provider, slug -> navController.navigate(Routes.detail(provider, slug)) },
                            onSettingsClick = { navController.navigate(Routes.CONFIGURE) },
                        )
                    }
                    composable(Routes.CONFIGURE) {
                        ConfigureScreen(
                            onNavigate = { route -> navController.navigate(route) },
                            onBack = { navController.popBackStack() },
                        )
                    }
                    composable(Routes.CONFIGURE_PROVIDERS) {
                        val viewModel: ConfigureViewModel = viewModel(factory = AppViewModelProvider.configureFactory(container))
                        ProvidersScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
                    }
                    composable(Routes.CONFIGURE_FILTERS_WATCHLIST) {
                        val viewModel: ConfigureViewModel = viewModel(factory = AppViewModelProvider.configureFactory(container))
                        FiltersWatchlistScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
                    }
                    composable(Routes.CONFIGURE_SYNC_ANTIBLOCK) {
                        val viewModel: ConfigureViewModel = viewModel(factory = AppViewModelProvider.configureFactory(container))
                        SyncAntiBlockScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
                    }
                    composable(Routes.CONFIGURE_APPEARANCE) {
                        val viewModel: ConfigureViewModel = viewModel(factory = AppViewModelProvider.configureFactory(container))
                        AppearanceScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
                    }
                    composable(Routes.CONFIGURE_UPDATES) {
                        val viewModel: ConfigureViewModel = viewModel(factory = AppViewModelProvider.configureFactory(container))
                        UpdatesScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
                    }
                    composable(Routes.ABOUT) {
                        AboutScreen(onBack = { navController.popBackStack() })
                    }
                    composable(
                        route = Routes.DETAIL_PATTERN,
                        arguments = listOf(
                            navArgument("provider") { type = NavType.StringType },
                            navArgument("slug") { type = NavType.StringType },
                        ),
                    ) { entry ->
                        val provider = entry.arguments?.getString("provider").orEmpty()
                        val slug = entry.arguments?.getString("slug").orEmpty()
                        val viewModel: DetailViewModel = viewModel(factory = AppViewModelProvider.detailFactory(container, provider, slug))
                        DetailScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
                    }
                }
            }

            AnimatedVisibility(
                visible = updateAvailable != null,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(top = 8.dp, start = 16.dp, end = 16.dp),
                enter = fadeIn(tween(220)) + slideInVertically(tween(220)) { -it },
                exit = fadeOut(tween(160)) + slideOutVertically(tween(160)) { -it },
            ) {
                updateAvailable?.let { info ->
                    UpdateAvailableBanner(
                        versionName = info.versionName,
                        onDismiss = { updateAvailable = null },
                        onOpen = {
                            updateAvailable = null
                            navController.navigate(Routes.CONFIGURE_UPDATES)
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun UpdateAvailableBanner(
    versionName: String,
    onDismiss: () -> Unit,
    onOpen: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val accent = themedAccent(NeonCyan, NeonCyanOnLight)
    NeonPanel(
        modifier = modifier.fillMaxWidth().clickable(onClick = onOpen),
        accent = accent,
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Filled.NewReleases, contentDescription = null, tint = accent, modifier = Modifier.height(20.dp))
            Column(modifier = Modifier.weight(1f).padding(start = 10.dp)) {
                Text("UPDATE AVAILABLE", style = MaterialTheme.typography.labelSmall, color = accent)
                Text(
                    "v$versionName is ready — tap to update",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            IconButton(onClick = onDismiss, modifier = Modifier.height(32.dp)) {
                Icon(Icons.Filled.Close, contentDescription = "Dismiss", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
