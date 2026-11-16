package org.ucb.appp1.feature.vision.domain.quality

enum class QualityIssue(val message: String) {
    Blurry("La foto parece borrosa. Mantén firme el teléfono y enfoca antes de capturar."),
    Dark("La foto está muy oscura. Prueba con más luz."),
    Bright("La foto está sobreexpuesta (demasiada luz). Evita el reflejo directo."),
    LowContrast("La foto tiene poco contraste; los detalles se pierden.")
}

/**
 * Métricas objetivas de calidad de una imagen.
 * - [sharpness]: varianza del Laplaciano (más alto = más nítida).
 * - [brightness]: luminancia media, de 0 (negro) a 255 (blanco).
 * - [contrast]: desviación estándar de la luminancia.
 */
data class ImageQuality(
    val sharpness: Double,
    val brightness: Double,
    val contrast: Double,
    val issues: List<QualityIssue>
) {
    val isGood: Boolean get() = issues.isEmpty()
}
