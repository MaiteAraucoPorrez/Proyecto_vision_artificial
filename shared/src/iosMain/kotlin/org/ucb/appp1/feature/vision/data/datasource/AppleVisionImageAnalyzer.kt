package org.ucb.appp1.feature.vision.data.datasource

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.ucb.appp1.core.util.currentTimeMillis
import org.ucb.appp1.feature.vision.domain.model.VisionResult
import platform.Foundation.NSData
import platform.Foundation.NSURL
import platform.Foundation.dataWithContentsOfURL
import platform.Vision.VNClassificationObservation
import platform.Vision.VNClassifyImageRequest
import platform.Vision.VNImageRequestHandler
import platform.Vision.VNRecognizeTextRequest
import platform.Vision.VNRecognizedTextObservation

/**
 * Implementación iOS con el framework Apple Vision (on-device):
 * clasificación de imagen + reconocimiento de texto.
 *
 * NOTA: solo compila en macOS/Xcode; no se pudo verificar en Windows.
 */
@OptIn(ExperimentalForeignApi::class)
class AppleVisionImageAnalyzer : ImageAnalyzerDataSource {

    override suspend fun analyze(imageUri: String): VisionResult = withContext(Dispatchers.Default) {
        val url = NSURL.URLWithString(imageUri) ?: NSURL.fileURLWithPath(imageUri)
        val data = NSData.dataWithContentsOfURL(url)
            ?: error("No se pudo leer la imagen: $imageUri")

        val classify = VNClassifyImageRequest()
        val recognizeText = VNRecognizeTextRequest()
        val handler = VNImageRequestHandler(data = data, options = emptyMap<Any?, Any>())
        handler.performRequests(listOf(classify, recognizeText), null)

        val labels = (classify.results ?: emptyList<Any>())
            .filterIsInstance<VNClassificationObservation>()
            .filter { it.confidence >= MIN_CONFIDENCE }
            .take(MAX_LABELS)
            .map { "${it.identifier} (${(it.confidence * PERCENT).toInt()}%)" }

        val texts = (recognizeText.results ?: emptyList<Any>())
            .filterIsInstance<VNRecognizedTextObservation>()
            .mapNotNull { obs -> obs.topCandidates(1u).firstOrNull() as? platform.Vision.VNRecognizedText }
            .map { it.string }

        VisionResult(
            imageUri = imageUri,
            labels = labels,
            texts = texts,
            timestamp = currentTimeMillis()
        )
    }

    private companion object {
        const val MIN_CONFIDENCE = 0.3f
        const val MAX_LABELS = 8
        const val PERCENT = 100
    }
}
