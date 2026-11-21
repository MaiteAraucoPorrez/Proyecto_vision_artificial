package org.ucb.appp1.feature.settings.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Contrast
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import edu.ucb.ds.components.cards.AppCard
import edu.ucb.ds.components.label.AppBody
import edu.ucb.ds.components.label.AppHeading
import edu.ucb.ds.theme.MyProjectTheme
import org.ucb.appp1.core.ui.AppScaffold
import org.ucb.appp1.core.ui.GlyphBadge
import org.ucb.appp1.core.ui.theme.ThemeMode

@Composable
fun SettingsScreen(viewModel: SettingsViewModel, onBack: () -> Unit) {
    val state by viewModel.state.collectAsState()
    val spacing = MyProjectTheme.spacing

    AppScaffold(title = "Ajustes", onBack = onBack) { padding ->
        Column(
            Modifier.padding(padding).padding(spacing.large).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(spacing.large)
        ) {
            AppCard {
                SectionTitle("Tema", Icons.Outlined.Palette)
                ThemeMode.entries.forEach { mode ->
                    Row(
                        Modifier.fillMaxWidth().selectable(
                            selected = state.mode == mode,
                            onClick = { viewModel.emitEvent(SettingsEvent.OnModeSelected(mode)) },
                            role = Role.RadioButton
                        ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = state.mode == mode, onClick = null)
                        AppBody(mode.label, Modifier.padding(spacing.medium))
                    }
                }
            }
            AppCard {
                SectionTitle("Alto contraste", Icons.Outlined.Contrast)
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    AppBody(
                        "Mejora la legibilidad (claro u oscuro según el tema)",
                        Modifier.weight(1f),
                        textAlign = TextAlign.Justify
                    )
                    Switch(
                        checked = state.highContrast,
                        onCheckedChange = { viewModel.emitEvent(SettingsEvent.OnHighContrastChanged(it)) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String, icon: ImageVector) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(MyProjectTheme.spacing.medium)
    ) {
        GlyphBadge(icon, size = SECTION_BADGE.dp)
        AppHeading(text)
    }
}

private const val SECTION_BADGE = 36
