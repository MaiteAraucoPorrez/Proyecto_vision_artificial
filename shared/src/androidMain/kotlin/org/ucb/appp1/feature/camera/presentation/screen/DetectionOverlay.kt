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
import androidx.compose.ui.unit.sp
import edu.ucb.ds.theme.MyProjectTheme
import kotlin.math.max
import org.ucb.appp1.feature.camera.presentation.viewmodel.BoxKind
import org.ucb.appp1.feature.camera.presentation.viewmodel.FrameResult

/**
 * Dibuja los recuadros detectados sobre la vista previa.
 * La vista previa usa FILL_CENTER: el frame se escala para cubrir la vista y se recorta al centro,
 * por eso se calcula la misma escala y desplazamiento aquí.
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
            val color = if (box.kind == BoxKind.Object) objectColor else textColor
            val topLeft = Offset(box.left * scale + dx, box.top * scale + dy)
            val boxSize = Size((box.right - box.left) * scale, (box.bottom - box.top) * scale)
            drawRect(color, topLeft, boxSize, style = Stroke(width = STROKE))
            drawText(
                textMeasurer = measurer,
                text = box.label,
                topLeft = topLeft,
                style = TextStyle(color = onColor, fontSize = LABEL_SP.sp, background = color)
            )
        }
    }
}

private const val STROKE = 4f
private const val LABEL_SP = 12
