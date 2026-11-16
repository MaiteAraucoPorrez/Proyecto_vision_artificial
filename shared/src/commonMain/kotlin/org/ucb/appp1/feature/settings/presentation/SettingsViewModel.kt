package org.ucb.appp1.feature.settings.presentation

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.ucb.appp1.core.ui.theme.ThemeMode
import org.ucb.appp1.core.ui.theme.ThemeState

sealed interface SettingsEvent {
    data class OnModeSelected(val mode: ThemeMode) : SettingsEvent
    data class OnHighContrastChanged(val enabled: Boolean) : SettingsEvent
}

/** Estado de tema compartido por toda la app (singleton: lo leen App() y la pantalla de ajustes). */
class SettingsViewModel : ViewModel() {
    private val _state = MutableStateFlow(ThemeState())
    val state = _state.asStateFlow()

    fun emitEvent(event: SettingsEvent) {
        when (event) {
            is SettingsEvent.OnModeSelected -> _state.update { it.copy(mode = event.mode) }
            is SettingsEvent.OnHighContrastChanged -> _state.update { it.copy(highContrast = event.enabled) }
        }
    }
}
