package org.ucb.appp1.feature.vision.data.datasource

import android.content.Context
import android.net.Uri
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeler
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptionsDEFAULT_OPTIONS
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptionsDEFAULT_OPTIONS
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.ucb.appp1.feature.vision.domain.model.VisionResult

/**
 * Implementación real de [ImageAnalyzerDataSource] usando ML Kit de Google.
 *
 * Ejecuta en el dispositivo (offline) dos análisis en paralelo:
 * - Image Labeling: detecta objetos/etiquetas en la imagen
 * - Text Recognition (OCR): extrae texto en latín
 *
 * Requiere que la imagen sea accesible via [Uri] (content:// o file://).
 */
class MlKitImageAnalyzer(
    private val context: Context
) : ImageAnalyzerDataSource {

    private val imageLabeler: ImageLabeler = ImageLabeling.getClient(ImageLabelerOptionsDEFAULT_OPTIONS)
    private val textRecognizer: TextRecognizer = TextRecognition.getClient(TextRecognizerOptionsDEFAULT_OPTIONS)

    @OptIn(ExperimentalGetImage::class)
    override suspend fun analyze(imageUri: String): VisionResult = withContext(Dispatchers.IO) {
        val uri = Uri.parse(imageUri)
        val inputImage = InputImage.fromFilePath(context, uri)

        // Ejecutar ambos detectores en paralelo
        val labelsDeferred = kotlinx.coroutines.async { imageLabeler.process(inputImage) }
        val textsDeferred = kotlinx.coroutines.async { textRecognizer.process(inputImage) }

        val labelsResult = labelsDeferred.await()
        val textsResult = textsDeferred.await()

        val labels = labelsResult.map { it.text }
        val texts = textsResult.textBlocks.map { it.text }

        VisionResult(
            imageUri = imageUri,
            labels = labels,
            texts = texts,
            timestamp = currentTimestamp()
        )
    }

    /**
     * Liberar recursos de ML Kit cuando ya no se necesite.
     * Llamar desde [VisionRepositoryImpl] o al cerrar la app.
     */
    fun close() {
        imageLabeler.close()
        textRecognizer.close()
    }
}