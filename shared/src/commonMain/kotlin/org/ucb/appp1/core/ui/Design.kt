package org.ucb.appp1.core.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import edu.ucb.ds.components.label.AppBody
import edu.ucb.ds.components.label.AppHeading
import edu.ucb.ds.theme.MyProjectTheme

/** Círculo con un símbolo, usado como icono de las tarjetas y estados vacíos. */
@Composable
fun GlyphBadge(icon: ImageVector, size: Dp = 44.dp, modifier: Modifier = Modifier) {
    val accent = MyProjectTheme.colors.secondary
    Box(
        modifier = modifier.size(size).clip(CircleShape).background(accent.copy(alpha = BADGE_ALPHA)),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(size / 2))
    }
}

/** Tarjeta de función: icono, título, descripción y flecha. Borde con el color de acento del tema. */
@Composable
fun FeatureCard(
    title: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MyProjectTheme.colors
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(CARD_RADIUS.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface, contentColor = colors.onSurface),
        border = BorderStroke(1.dp, colors.secondary.copy(alpha = BORDER_ALPHA))
    ) {
        Row(
            Modifier.padding(MyProjectTheme.spacing.large),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(MyProjectTheme.spacing.large)
        ) {
            GlyphBadge(icon)
            Column(Modifier.weight(1f)) {
                AppHeading(title, color = colors.onSurface)
                AppBody(description, color = colors.onSurface.copy(alpha = SUBTEXT_ALPHA))
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = colors.secondary)
        }
    }
}

/** Fondo con un degradado suave del color primario, para encabezados de pantalla. */
@Composable
fun gradientHeaderBrush(): Brush {
    val primary = MyProjectTheme.colors.primary
    return Brush.verticalGradient(listOf(primary.copy(alpha = HEADER_ALPHA), Color.Transparent))
}

private const val BADGE_ALPHA = 0.16f
private const val BORDER_ALPHA = 0.55f
private const val SUBTEXT_ALPHA = 0.75f
private const val HEADER_ALPHA = 0.22f
private const val CARD_RADIUS = 16
