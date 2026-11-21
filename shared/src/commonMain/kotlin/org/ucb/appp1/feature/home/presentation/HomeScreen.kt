package org.ucb.appp1.feature.home.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.WifiOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import edu.ucb.ds.components.label.AppBody
import edu.ucb.ds.components.label.AppHeading
import edu.ucb.ds.theme.MyProjectTheme
import org.ucb.appp1.core.ui.AppScaffold
import org.ucb.appp1.core.ui.FeaturedCard
import org.ucb.appp1.core.ui.GlyphBadge
import org.ucb.appp1.core.ui.InfoPill
import org.ucb.appp1.core.ui.TileCard
import org.ucb.appp1.core.ui.gradientHeaderBrush

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
        Box(Modifier.padding(padding).background(gradientHeaderBrush())) {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()).padding(spacing.large),
                verticalArrangement = Arrangement.spacedBy(spacing.large)
            ) {
                HeroHeader()
                AppHeading("¿Qué quieres hacer?")
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
}

@Composable
private fun HeroHeader() {
    val colors = MyProjectTheme.colors
    val spacing = MyProjectTheme.spacing
    val brush = Brush.horizontalGradient(
        listOf(colors.primary.copy(alpha = HERO_ALPHA), colors.secondary.copy(alpha = HERO_ALPHA))
    )
    Column(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(HERO_RADIUS.dp)).background(brush)
            .padding(spacing.large),
        verticalArrangement = Arrangement.spacedBy(spacing.medium)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(spacing.large)
        ) {
            GlyphBadge(Icons.Outlined.Visibility, size = HERO_BADGE.dp)
            Column {
                AppHeading("Visión Artificial", color = colors.onSurface)
                AppBody("Reconoce objetos, texto y códigos con tu cámara.", color = colors.onSurface)
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(spacing.medium)) {
            InfoPill("Sin internet", Icons.Outlined.WifiOff)
            InfoPill("Privado", Icons.Outlined.Lock)
        }
    }
}

private const val HERO_ALPHA = 0.28f
private const val HERO_RADIUS = 28
private const val HERO_BADGE = 56
