package org.ucb.appp1.feature.vision.presentation.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Modifier
import edu.ucb.ds.components.label.AppBody
import edu.ucb.ds.components.label.AppHeading
import edu.ucb.ds.theme.MyProjectTheme
import org.ucb.appp1.feature.vision.domain.quality.ImageQuality

/** Avisa si la foto es borrosa, oscura, sobreexpuesta o de poco contraste, y muestra las métricas. */
@Composable
fun QualityCard(quality: ImageQuality, modifier: Modifier = Modifier) {
    val colors = MyProjectTheme.colors
    val accent = if (quality.isGood) colors.secondary else colors.error
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = colors.surface, contentColor = colors.onSurface),
        border = BorderStroke(1.dp, accent)
    ) {
        Column(Modifier.padding(MyProjectTheme.spacing.large)) {
            AppHeading(
                if (quality.isGood) "Calidad de la foto: buena" else "Calidad de la foto: mejorable",
                color = accent
            )
            quality.issues.forEach { AppBody("• ${it.message}", color = colors.onSurface) }
            AppBody(
                "Nitidez ${quality.sharpness.toInt()} · Brillo ${quality.brightness.toInt()}/255 · " +
                    "Contraste ${quality.contrast.toInt()}",
                color = colors.onSurface
            )
        }
    }
}
