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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
fun GlyphBadge(
    icon: ImageVector,
    size: Dp = 44.dp,
    modifier: Modifier = Modifier,
    accent: Color = MyProjectTheme.colors.secondary
) {
    Box(
        modifier = modifier.size(size).clip(CircleShape).background(accent.copy(alpha = BADGE_ALPHA)),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(size / 2))
    }
}

/**
 * Tarjeta destacada para la función principal: fondo con el color primario del tema,
 * icono grande, título, descripción y flecha.
 */
@Composable
fun FeaturedCard(
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
        shape = RoundedCornerShape(FEATURED_RADIUS.dp),
        colors = CardDefaults.cardColors(containerColor = colors.primary, contentColor = colors.onPrimary)
    ) {
        Row(
            Modifier.padding(MyProjectTheme.spacing.large),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(MyProjectTheme.spacing.large)
        ) {
            GlyphBadge(icon, size = FEATURED_BADGE.dp, accent = colors.onPrimary)
            Column(Modifier.weight(1f)) {
                AppHeading(title, color = colors.onPrimary)
                AppBody(description, color = colors.onPrimary)
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = colors.onPrimary)
        }
    }
}

/** Mosaico de función: icono arriba, título y descripción corta. Se usan dos por fila. */
@Composable
fun TileCard(
    title: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MyProjectTheme.colors
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(CARD_RADIUS.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface, contentColor = colors.onSurface),
        border = BorderStroke(1.dp, colors.secondary.copy(alpha = BORDER_ALPHA))
    ) {
        Column(
            Modifier.fillMaxWidth().padding(MyProjectTheme.spacing.large),
            verticalArrangement = Arrangement.spacedBy(MyProjectTheme.spacing.medium)
        ) {
            GlyphBadge(icon)
            AppHeading(title, color = colors.onSurface)
            AppBody(description, color = colors.onSurface.copy(alpha = SUBTEXT_ALPHA))
        }
    }
}

/** Etiqueta redondeada con un icono, para resaltar ventajas (sin internet, privado...). */
@Composable
fun InfoPill(text: String, icon: ImageVector, modifier: Modifier = Modifier) {
    val colors = MyProjectTheme.colors
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(PILL_RADIUS.dp))
            .background(colors.surface)
            .padding(horizontal = PILL_PADDING_H.dp, vertical = PILL_PADDING_V.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(PILL_GAP.dp)
    ) {
        Icon(icon, contentDescription = null, tint = colors.secondary, modifier = Modifier.size(PILL_ICON.dp))
        Text(text, color = colors.onSurface, style = MaterialTheme.typography.labelMedium)
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
private const val CARD_RADIUS = 20
private const val FEATURED_RADIUS = 28
private const val FEATURED_BADGE = 56
private const val PILL_RADIUS = 50
private const val PILL_PADDING_H = 12
private const val PILL_PADDING_V = 6
private const val PILL_GAP = 6
private const val PILL_ICON = 16
