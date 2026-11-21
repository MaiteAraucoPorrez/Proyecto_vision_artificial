package org.ucb.appp1.feature.home.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import edu.ucb.ds.components.label.AppBody
import edu.ucb.ds.components.label.AppHeading
import edu.ucb.ds.theme.MyProjectTheme
import org.ucb.appp1.core.ui.AppScaffold
import org.ucb.appp1.core.ui.FeaturedCard
import org.ucb.appp1.core.ui.TileCard

/**
 * Pantalla de inicio: solo las funciones de visión artificial.
 * Historial y Ajustes son accesos secundarios en la barra superior.
 */
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
            IconButton(onClick = onHistory) {
                Icon(Icons.Outlined.History, contentDescription = "Historial")
            }
            IconButton(onClick = onSettings) {
                Icon(Icons.Outlined.Settings, contentDescription = "Ajustes")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).verticalScroll(rememberScrollState()).padding(spacing.large),
            verticalArrangement = Arrangement.spacedBy(spacing.large)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(spacing.medium)) {
                AppHeading("Visión artificial")
                AppBody("Reconoce objetos, texto y códigos con la cámara de tu celular.", textAlign = TextAlign.Justify)
            }
            FeaturedCard(
                title = "Cámara en tiempo real",
                description = "Recuadros sobre objetos y texto mientras apuntas.",
                icon = Icons.Outlined.CameraAlt,
                onClick = onCamera
            )
            Row(
                modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max),
                horizontalArrangement = Arrangement.spacedBy(spacing.large)
            ) {
                TileCard(
                    title = "Imagen de galería",
                    description = "Objetos y texto de una foto.",
                    icon = Icons.Outlined.PhotoLibrary,
                    onClick = onGallery,
                    modifier = Modifier.weight(1f).fillMaxHeight()
                )
                TileCard(
                    title = "Escáner QR y códigos",
                    description = "Lee QR y códigos de barras.",
                    icon = Icons.Outlined.QrCodeScanner,
                    onClick = onScanner,
                    modifier = Modifier.weight(1f).fillMaxHeight()
                )
            }
        }
    }
}
