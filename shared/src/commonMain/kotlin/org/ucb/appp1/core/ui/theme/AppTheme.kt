package org.ucb.appp1.core.ui.theme

enum class ThemeMode(val label: String) {
    System("Sistema"),
    Light("Claro"),
    Dark("Oscuro")
}

/**
 * Preferencias de apariencia. Se pasan a `MyProjectTheme` del Design System, que ofrece
 * 4 paletas: claro, oscuro, alto contraste claro y alto contraste oscuro.
 */
data class ThemeState(
    val mode: ThemeMode = ThemeMode.System,
    val highContrast: Boolean = false
)
