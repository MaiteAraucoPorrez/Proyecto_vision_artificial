package org.ucb.appp1.feature.vision.data.datasource

import android.content.Context
import android.net.Uri
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeler
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.ucb.appp1.core.util.currentTimeMillis
import org.ucb.appp1.feature.vision.domain.model.VisionResult

/**
 * Implementación real de [ImageAnalyzerDataSource] usando ML Kit (on-device).
 * Ejecuta Image Labeling y Text Recognition en paralelo.
 */
class MlKitImageAnalyzer(
    private val context: Context
) : ImageAnalyzerDataSource {

    private val imageLabeler: ImageLabeler =
        ImageLabeling.getClient(ImageLabelerOptions.Builder().setConfidenceThreshold(CONFIDENCE).build())
    private val textRecognizer: TextRecognizer =
        TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    override suspend fun analyze(imageUri: String): VisionResult = withContext(Dispatchers.IO) {
        val inputImage = InputImage.fromFilePath(context, Uri.parse(imageUri))

        val labelsTask = imageLabeler.process(inputImage)
        val textsTask = textRecognizer.process(inputImage)

        val labels = Tasks.await(labelsTask).map { "${it.text} (${(it.confidence * PERCENT).toInt()}%)" }
        val texts = Tasks.await(textsTask).textBlocks.map { it.text }

        VisionResult(
            imageUri = imageUri,
            labels = labels,
            texts = texts,
            timestamp = currentTimeMillis()
        )
    }

    private companion object {
        const val CONFIDENCE = 0.6f
        const val PERCENT = 100
    }
}
