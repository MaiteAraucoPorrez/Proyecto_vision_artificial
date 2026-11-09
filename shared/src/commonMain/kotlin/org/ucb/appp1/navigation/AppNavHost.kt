package org.ucb.appp1.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import org.ucb.appp1.feature.vision.presentation.screen.VisionScreen
import org.ucb.appp1.feature.localization.presentation.screen.LocalizationScreen
import org.ucb.appp1.feature.designsystem.presentation.screen.DesignSystemDemoScreen

@Composable
fun AppNavHost() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = NavRoute.Vision) {

        composable<NavRoute.Vision> {
            VisionScreen()
        }

        composable<NavRoute.Localization> {
            LocalizationScreen()
        }

        composable<NavRoute.DesignSystem> {
            DesignSystemDemoScreen()
        }
    }
}
