package org.ucb.appp1.feature.vision.domain.quality

import kotlin.math.sqrt

/**
 * Evalúa la calidad de una imagen en escala de grises con algoritmos clásicos (sin ML):
 *
 * - **Nitidez** — varianza del Laplaciano. El Laplaciano resalta los bordes (cambios bruscos de
 *   brillo); una foto nítida tiene bordes marcados y por eso mucha varianza, mientras que una
 *   borrosa los suaviza y la varianza baja.
 * - **Brillo** — promedio de la luminancia: muy bajo = oscura, muy alto = sobreexpuesta.
 * - **Contraste** — desviación estándar de la luminancia: baja = imagen "plana".
 *
 * Los umbrales se calibraron para imágenes reducidas a ~512 px de lado mayor.
 */
object ImageQualityEvaluator {

    const val BLUR_THRESHOLD = 80.0
    const val DARK_THRESHOLD = 60.0
    const val BRIGHT_THRESHOLD = 215.0
    const val LOW_CONTRAST_THRESHOLD = 25.0

    private const val MIN_SIDE = 3

    /** [gray] contiene [width]*[height] valores de luminancia 0..255, fila por fila. */
    fun evaluate(gray: IntArray, width: Int, height: Int): ImageQuality {
        require(gray.size == width * height) { "Tamaño de la imagen inconsistente" }
        if (width < MIN_SIDE || height < MIN_SIDE) {
            return ImageQuality(0.0, 0.0, 0.0, emptyList())
        }

        val brightness = gray.average()
        val contrast = sqrt(gray.sumOf { (it - brightness) * (it - brightness) } / gray.size)
        val sharpness = laplacianVariance(gray, width, height)

        val issues = buildList {
            if (sharpness < BLUR_THRESHOLD) add(QualityIssue.Blurry)
            if (brightness < DARK_THRESHOLD) add(QualityIssue.Dark)
            if (brightness > BRIGHT_THRESHOLD) add(QualityIssue.Bright)
            if (contrast < LOW_CONTRAST_THRESHOLD && none { it == QualityIssue.Dark || it == QualityIssue.Bright }) {
                add(QualityIssue.LowContrast)
            }
        }
        return ImageQuality(sharpness, brightness, contrast, issues)
    }

    /** Convolución con el kernel [0 1 0; 1 -4 1; 0 1 0] y varianza de la respuesta. */
    private fun laplacianVariance(gray: IntArray, width: Int, height: Int): Double {
        var sum = 0.0
        var sumSq = 0.0
        var count = 0
        for (y in 1 until height - 1) {
            for (x in 1 until width - 1) {
                val i = y * width + x
                val v = (gray[i - 1] + gray[i + 1] + gray[i - width] + gray[i + width] - 4 * gray[i]).toDouble()
                sum += v
                sumSq += v * v
                count++
            }
        }
        val mean = sum / count
        return sumSq / count - mean * mean
    }
}
