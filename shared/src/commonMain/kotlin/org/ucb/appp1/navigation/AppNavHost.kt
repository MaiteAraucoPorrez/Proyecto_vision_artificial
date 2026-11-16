package org.ucb.appp1.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.ucb.appp1.feature.camera.presentation.screen.CameraScreen
import org.ucb.appp1.feature.history.presentation.screen.HistoryScreen
import org.ucb.appp1.feature.home.presentation.HomeScreen
import org.ucb.appp1.feature.scanner.presentation.screen.ScannerScreen
import org.ucb.appp1.feature.settings.presentation.SettingsScreen
import org.ucb.appp1.feature.settings.presentation.SettingsViewModel
import org.ucb.appp1.feature.vision.presentation.screen.VisionScreen

@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    val back: () -> Unit = { navController.popBackStack() }

    NavHost(navController = navController, startDestination = NavRoute.Home) {
        composable<NavRoute.Home> {
            HomeScreen(
                onGallery = { navController.navigate(NavRoute.Vision) },
                onCamera = { navController.navigate(NavRoute.Camera) },
                onScanner = { navController.navigate(NavRoute.Scanner) },
                onHistory = { navController.navigate(NavRoute.History) },
                onSettings = { navController.navigate(NavRoute.Settings) }
            )
        }
        composable<NavRoute.Vision> { VisionScreen(viewModel = koinViewModel(), onBack = back) }
        composable<NavRoute.Camera> { CameraScreen(viewModel = koinViewModel(), onBack = back) }
        composable<NavRoute.Scanner> { ScannerScreen(viewModel = koinViewModel(), onBack = back) }
        composable<NavRoute.History> { HistoryScreen(viewModel = koinViewModel(), onBack = back) }
        composable<NavRoute.Settings> { SettingsScreen(viewModel = koinInject<SettingsViewModel>(), onBack = back) }
    }
}
