package org.ucb.appp1.feature.home.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import edu.ucb.ds.components.buttons.PrimaryButton
import edu.ucb.ds.components.buttons.SecondaryButton
import edu.ucb.ds.components.label.AppBody
import edu.ucb.ds.theme.MyProjectTheme
import org.ucb.appp1.core.ui.AppScaffold

@Composable
fun HomeScreen(
    onGallery: () -> Unit,
    onCamera: () -> Unit,
    onHistory: () -> Unit,
    onSettings: () -> Unit
) {
    val spacing = MyProjectTheme.spacing
    AppScaffold(title = "Visión Artificial") { padding ->
        Column(
            modifier = Modifier.padding(padding).padding(spacing.large),
            verticalArrangement = Arrangement.spacedBy(spacing.large)
        ) {
            AppBody("Reconoce objetos y texto con ML Kit, sin internet.")
            PrimaryButton("Analizar imagen de galería", onGallery, Modifier.fillMaxWidth())
            PrimaryButton("Cámara en tiempo real", onCamera, Modifier.fillMaxWidth())
            SecondaryButton("Historial", onHistory, Modifier.fillMaxWidth())
            SecondaryButton("Ajustes y accesibilidad", onSettings, Modifier.fillMaxWidth())
        }
    }
}
