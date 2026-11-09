package org.ucb.appp1.feature.vision.data.datasource

import org.ucb.appp1.feature.vision.domain.model.VisionResult

/**
 * Implementación temporal (stub) de ImageAnalyzerDataSource.
 * Se reemplazará por MlKitImageAnalyzer (en androidMain) cuando
 * integremos ML Kit en el 40%.
 */
class StubImageAnalyzerDataSource : ImageAnalyzerDataSource {
    override suspend fun analyze(imageUri: String): VisionResult {
        return VisionResult(
            imageUri = imageUri,
            labels = listOf("Objeto de prueba", "Elemento detectado"),
            texts = emptyList(),
            timestamp = currentTimestamp()
        )
    }
}

expect fun currentTimestamp(): Long
