package com.phuzle.labs.repacks.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.phuzle.labs.repacks.core.AppContainer
import com.phuzle.labs.repacks.data.prefs.UserPreferences
import com.phuzle.labs.repacks.ui.AppViewModelProvider
import com.phuzle.labs.repacks.ui.about.AboutScreen
import com.phuzle.labs.repacks.ui.configure.ConfigureScreen
import com.phuzle.labs.repacks.ui.configure.ConfigureViewModel
import com.phuzle.labs.repacks.ui.detail.DetailScreen
import com.phuzle.labs.repacks.ui.detail.DetailViewModel
import com.phuzle.labs.repacks.ui.feed.FeedScreen
import com.phuzle.labs.repacks.ui.feed.FeedViewModel
import com.phuzle.labs.repacks.ui.theme.RepacksTheme

@Composable
fun RepacksApp(container: AppContainer, startDestination: String = Routes.FEED) {
    val prefs by container.userPreferencesRepository.preferencesFlow
        .collectAsStateWithLifecycle(initialValue = UserPreferences())
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = currentRoute == Routes.FEED || currentRoute == Routes.CONFIGURE || currentRoute == Routes.ABOUT

    RepacksTheme(themeMode = prefs.themeMode) {
        Scaffold(
            bottomBar = { if (showBottomBar) RepacksBottomBar(navController, currentRoute) },
        ) { padding ->
            NavHost(
                navController = navController,
                startDestination = startDestination,
                modifier = Modifier.padding(padding),
            ) {
                composable(Routes.FEED) {
                    val viewModel: FeedViewModel = viewModel(factory = AppViewModelProvider.feedFactory(container))
                    FeedScreen(
                        viewModel = viewModel,
                        onItemClick = { provider, slug -> navController.navigate(Routes.detail(provider, slug)) },
                    )
                }
                composable(Routes.CONFIGURE) {
                    val viewModel: ConfigureViewModel = viewModel(factory = AppViewModelProvider.configureFactory(container))
                    ConfigureScreen(
                        viewModel = viewModel,
                        onNavigateAbout = { navController.navigate(Routes.ABOUT) },
                    )
                }
                composable(Routes.ABOUT) { AboutScreen() }
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
    }
}

@Composable
private fun RepacksBottomBar(navController: NavHostController, currentRoute: String?) {
    NavigationBar {
        NavigationBarItem(
            selected = currentRoute == Routes.FEED,
            onClick = {
                navController.navigate(Routes.FEED) {
                    launchSingleTop = true
                    popUpTo(Routes.FEED) { inclusive = true }
                }
            },
            icon = { Icon(Icons.Filled.Home, contentDescription = "Feed") },
            label = { Text("Feed") },
        )
        NavigationBarItem(
            selected = currentRoute == Routes.CONFIGURE || currentRoute == Routes.ABOUT,
            onClick = {
                navController.navigate(Routes.CONFIGURE) {
                    launchSingleTop = true
                    popUpTo(Routes.FEED)
                }
            },
            icon = { Icon(Icons.Filled.Settings, contentDescription = "Configure") },
            label = { Text("Configure") },
        )
    }
}
