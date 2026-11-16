package org.ucb.appp1.feature.home.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import edu.ucb.ds.components.label.AppBody
import edu.ucb.ds.components.label.AppHeading
import edu.ucb.ds.theme.MyProjectTheme
import org.ucb.appp1.core.ui.AppScaffold
import org.ucb.appp1.core.ui.FeatureCard
import org.ucb.appp1.core.ui.GlyphBadge
import org.ucb.appp1.core.ui.gradientHeaderBrush

@Composable
fun HomeScreen(
    onGallery: () -> Unit,
    onCamera: () -> Unit,
    onScanner: () -> Unit,
    onHistory: () -> Unit,
    onSettings: () -> Unit
) {
    val spacing = MyProjectTheme.spacing
    AppScaffold(
        title = "Inicio",
        actions = {
            IconButton(onClick = onSettings) {
                Icon(Icons.Outlined.Settings, contentDescription = "Ajustes")
            }
        }
    ) { padding ->
        Box(Modifier.padding(padding).background(gradientHeaderBrush())) {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()).padding(spacing.large),
                verticalArrangement = Arrangement.spacedBy(spacing.large)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(spacing.large)
                ) {
                    GlyphBadge(Icons.Outlined.Visibility, size = 56.dp)
                    Column {
                        AppHeading("Visión Artificial")
                        AppBody("Procesamiento de imágenes con machine learning, directo en tu celular.")
                    }
                }
                AppHeading("Funciones")
                FeatureCard(
                    title = "Imagen de galería",
                    description = "Identifica objetos y lee el texto de una foto.",
                    icon = Icons.Outlined.PhotoLibrary,
                    onClick = onGallery
                )
                FeatureCard(
                    title = "Cámara en tiempo real",
                    description = "Recuadros sobre objetos y texto mientras apuntas.",
                    icon = Icons.Outlined.CameraAlt,
                    onClick = onCamera
                )
                FeatureCard(
                    title = "Escáner QR y códigos",
                    description = "Lee QR y códigos de barras al instante.",
                    icon = Icons.Outlined.QrCodeScanner,
                    onClick = onScanner
                )
                FeatureCard(
                    title = "Historial",
                    description = "Revisa tus análisis y códigos guardados.",
                    icon = Icons.Outlined.History,
                    onClick = onHistory
                )
            }
        }
    }
}
