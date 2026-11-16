package org.ucb.appp1.feature.camera.presentation.screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.sp
import edu.ucb.ds.theme.MyProjectTheme
import kotlin.math.max
import org.ucb.appp1.feature.camera.presentation.viewmodel.BoxKind
import org.ucb.appp1.feature.camera.presentation.viewmodel.FrameResult

/**
 * Dibuja los recuadros detectados sobre la vista previa.
 * La vista previa usa FILL_CENTER: el frame se escala para cubrir la vista y se recorta al centro,
 * por eso se calcula la misma escala y desplazamiento aquí.
 *
 * Los recuadros que quedan parcial o totalmente fuera de la vista se recortan u omiten: antes
 * se pedía dibujar texto con un ancho negativo, lo que cerraba la app.
 */
@Composable
fun DetectionOverlay(frame: FrameResult, modifier: Modifier = Modifier) {
    val measurer = rememberTextMeasurer()
    val objectColor = MyProjectTheme.colors.secondary
    val textColor = MyProjectTheme.colors.primary
    val onColor = MyProjectTheme.colors.onPrimary

    Canvas(modifier.fillMaxSize()) {
        if (frame.frameWidth == 0 || frame.frameHeight == 0) return@Canvas
        val scale = max(size.width / frame.frameWidth, size.height / frame.frameHeight)
        val dx = (size.width - frame.frameWidth * scale) / 2f
        val dy = (size.height - frame.frameHeight * scale) / 2f

        frame.boxes.forEach { box ->
            val left = (box.left * scale + dx).coerceIn(0f, size.width)
            val top = (box.top * scale + dy).coerceIn(0f, size.height)
            val right = (box.right * scale + dx).coerceIn(0f, size.width)
            val bottom = (box.bottom * scale + dy).coerceIn(0f, size.height)
            if (right - left < MIN_SIDE || bottom - top < MIN_SIDE) return@forEach

            val color = if (box.kind == BoxKind.Object) objectColor else textColor
            drawRect(color, Offset(left, top), Size(right - left, bottom - top), style = Stroke(width = STROKE))

            val label = measurer.measure(
                text = box.label,
                style = TextStyle(color = onColor, fontSize = LABEL_SP.sp, background = color),
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
                constraints = Constraints(maxWidth = (right - left).toInt().coerceAtLeast(1))
            )
            drawText(label, topLeft = Offset(left, top))
        }
    }
}

private const val STROKE = 4f
private const val MIN_SIDE = 2f
private const val LABEL_SP = 12
