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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import edu.ucb.ds.components.label.AppBody
import edu.ucb.ds.components.label.AppHeading
import edu.ucb.ds.theme.MyProjectTheme

/** Icono dentro de un cuadrado redondeado de color sólido (sin transparencias ni degradados). */
@Composable
fun GlyphBadge(
    icon: ImageVector,
    size: Dp = 44.dp,
    modifier: Modifier = Modifier,
    container: Color = MyProjectTheme.colors.secondary,
    content: Color = MyProjectTheme.colors.onSecondary
) {
    Box(
        modifier = modifier.size(size).clip(RoundedCornerShape(size / BADGE_CORNER_DIVISOR)).background(container),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = content, modifier = Modifier.size(size / 2))
    }
}

/** Contorno gris suave, igual en claro y oscuro, para tarjetas. */
@Composable
fun cardOutline(): BorderStroke = BorderStroke(1.dp, MyProjectTheme.colors.onSurface.copy(alpha = OUTLINE_ALPHA))

/** Tarjeta de la función principal: fondo sólido del color primario del tema. */
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
        shape = RoundedCornerShape(CARD_RADIUS.dp),
        colors = CardDefaults.cardColors(containerColor = colors.primary, contentColor = colors.onPrimary)
    ) {
        Row(
            Modifier.padding(MyProjectTheme.spacing.large),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(MyProjectTheme.spacing.large)
        ) {
            GlyphBadge(icon, size = FEATURED_BADGE.dp, container = colors.onPrimary, content = colors.primary)
            Column(Modifier.weight(1f)) {
                AppHeading(title, color = colors.onPrimary)
                AppBody(description, color = colors.onPrimary, textAlign = TextAlign.Justify)
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
        border = cardOutline()
    ) {
        Column(
            Modifier.fillMaxWidth().padding(MyProjectTheme.spacing.large),
            verticalArrangement = Arrangement.spacedBy(MyProjectTheme.spacing.medium)
        ) {
            GlyphBadge(icon)
            AppHeading(title, color = colors.onSurface)
            AppBody(
                description,
                color = colors.onSurface.copy(alpha = SUBTEXT_ALPHA),
                textAlign = TextAlign.Justify
            )
        }
    }
}

private const val BADGE_CORNER_DIVISOR = 3.5f
private const val OUTLINE_ALPHA = 0.14f
private const val SUBTEXT_ALPHA = 0.75f
private const val CARD_RADIUS = 20
private const val FEATURED_BADGE = 52
