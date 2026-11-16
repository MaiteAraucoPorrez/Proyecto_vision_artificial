package org.ucb.appp1

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import edu.ucb.ds.components.surfaces.AppSurface
import edu.ucb.ds.theme.MyProjectTheme
import org.koin.compose.koinInject
import org.ucb.appp1.core.ui.theme.ThemeMode
import org.ucb.appp1.feature.settings.presentation.SettingsViewModel
import org.ucb.appp1.navigation.AppNavHost

@Composable
fun App() {
    val settings = koinInject<SettingsViewModel>()
    val theme by settings.state.collectAsState()
    val dark = when (theme.mode) {
        ThemeMode.System -> isSystemInDarkTheme()
        ThemeMode.Light -> false
        ThemeMode.Dark -> true
    }
    MyProjectTheme(darkTheme = dark, highContrast = theme.highContrast) {
        AppSurface {
            AppNavHost()
        }
    }
}
