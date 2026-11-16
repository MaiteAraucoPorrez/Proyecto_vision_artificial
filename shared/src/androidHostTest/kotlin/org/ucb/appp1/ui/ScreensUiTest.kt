package org.ucb.appp1.ui

import androidx.compose.ui.test.isToggleable
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import edu.ucb.ds.theme.MyProjectTheme
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.ucb.appp1.core.ui.ErrorState
import org.ucb.appp1.core.ui.theme.ThemeMode
import org.ucb.appp1.feature.history.data.datasource.InMemoryHistoryDataSource
import org.ucb.appp1.feature.history.data.repository.HistoryRepositoryImpl
import org.ucb.appp1.feature.history.domain.usecase.ClearHistoryUseCase
import org.ucb.appp1.feature.history.domain.usecase.DeleteAnalysisUseCase
import org.ucb.appp1.feature.history.domain.usecase.GetHistoryUseCase
import org.ucb.appp1.feature.history.domain.usecase.SaveAnalysisUseCase
import org.ucb.appp1.feature.history.presentation.screen.HistoryScreen
import org.ucb.appp1.feature.history.presentation.viewmodel.HistoryViewModel
import org.ucb.appp1.feature.home.presentation.HomeScreen
import org.ucb.appp1.feature.settings.presentation.SettingsScreen
import org.ucb.appp1.feature.settings.presentation.SettingsViewModel

/** Pruebas de UI (Compose + Robolectric) que corren en la JVM, sin emulador. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ScreensUiTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun home_showsAllEntryPoints_andNavigates() {
        val clicks = mutableListOf<String>()
        rule.setContent {
            MyProjectTheme {
                HomeScreen(
                    onGallery = { clicks += "gallery" },
                    onCamera = { clicks += "camera" },
                    onHistory = { clicks += "history" },
                    onSettings = { clicks += "settings" }
                )
            }
        }
        rule.onNodeWithText("Analizar imagen de galería").performClick()
        rule.onNodeWithText("Cámara en tiempo real").performClick()
        rule.onNodeWithText("Historial").performClick()
        rule.onNodeWithText("Ajustes y accesibilidad").performClick()
        assertEquals(listOf("gallery", "camera", "history", "settings"), clicks)
    }

    @Test
    fun settings_changesThemeModeAndHighContrast() {
        val vm = SettingsViewModel()
        rule.setContent { MyProjectTheme { SettingsScreen(vm, onBack = {}) } }

        rule.onNodeWithText("Oscuro").performClick()
        rule.waitForIdle()
        assertEquals(ThemeMode.Dark, vm.state.value.mode)

        rule.onNode(isToggleable()).performClick()
        rule.waitForIdle()
        assertTrue(vm.state.value.highContrast)
    }

    @Test
    fun history_showsEmptyState_thenItemsAndClearAll() {
        val repo = HistoryRepositoryImpl(InMemoryHistoryDataSource())
        val vm = HistoryViewModel(GetHistoryUseCase(repo), DeleteAnalysisUseCase(repo), ClearHistoryUseCase(repo))
        rule.setContent { MyProjectTheme { HistoryScreen(vm, onBack = {}) } }

        rule.onNodeWithText("Sin análisis todavía").assertExists()

        runBlocking { SaveAnalysisUseCase(repo)("gallery", listOf("Dog (90%)"), listOf("STOP")) }
        rule.waitUntil(timeoutMillis = 5_000) { vm.state.value.items.size == 1 }
        rule.onNodeWithText("Objetos: Dog (90%)").assertExists()
        rule.onNodeWithText("Texto: STOP").assertExists()

        rule.onNodeWithText("Borrar todo").performClick()
        rule.waitUntil(timeoutMillis = 5_000) { vm.state.value.items.isEmpty() }
        rule.onNodeWithText("Sin análisis todavía").assertExists()
    }

    @Test
    fun errorState_retryButtonInvokesCallback() {
        var retried = false
        rule.setContent {
            MyProjectTheme { ErrorState("Falló el análisis", onRetry = { retried = true }) }
        }
        rule.onNodeWithText("Falló el análisis").assertExists()
        rule.onNodeWithText("Reintentar").performClick()
        assertTrue(retried)
    }
}
