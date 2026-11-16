package org.ucb.appp1.feature.camera.data

import android.os.SystemClock
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageProxy
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import org.ucb.appp1.core.util.OcrTextCleaner
import org.ucb.appp1.core.util.safeCall
import org.ucb.appp1.feature.camera.presentation.viewmodel.BoxKind
import org.ucb.appp1.feature.camera.presentation.viewmodel.DetectionBox
import org.ucb.appp1.feature.camera.presentation.viewmodel.FrameResult

/**
 * Analiza frames de CameraX con ML Kit: etiquetas, OCR por líneas y detección de objetos (con recuadros).
 * Procesa como máximo un frame cada [INTERVAL_MS]. [onResult] se invoca en el hilo principal.
 */
class CameraFrameAnalyzer(
    private val onResult: (FrameResult) -> Unit
) : ClosableAnalyzer {

    private val labeler = ImageLabeling.getClient(
        ImageLabelerOptions.Builder().setConfidenceThreshold(CONFIDENCE).build()
    )
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    private val objectDetector = ObjectDetection.getClient(
        ObjectDetectorOptions.Builder()
            .setDetectorMode(ObjectDetectorOptions.STREAM_MODE)
            .enableMultipleObjects()
            .enableClassification()
            .build()
    )
    private var lastRun = 0L

    // Evita usar los detectores ya cerrados (provocaba un crash al salir de la pantalla).
    @Volatile
    private var closed = false

    @ExperimentalGetImage
    override fun analyze(imageProxy: ImageProxy) {
        val media = imageProxy.image
        val now = SystemClock.elapsedRealtime()
        if (closed || media == null || now - lastRun < INTERVAL_MS) {
            imageProxy.close()
            return
        }
        lastRun = now
        // Cualquier fallo de ML Kit con este frame se ignora: nunca debe cerrar la app.
        safeCall { process(imageProxy, media) }.onFailure { imageProxy.close() }
    }

    @ExperimentalGetImage
    private fun process(imageProxy: ImageProxy, media: android.media.Image) {

        val input = InputImage.fromMediaImage(media, imageProxy.imageInfo.rotationDegrees)
        val labelsTask = labeler.process(input)
        val textTask = recognizer.process(input)
        val objectsTask = objectDetector.process(input)

        Tasks.whenAllComplete(labelsTask, textTask, objectsTask).addOnCompleteListener {
            if (!closed) {
                onResult(buildResult(input, labelsTask, textTask, objectsTask))
            }
            imageProxy.close()
        }
    }

    private fun buildResult(
        input: InputImage,
        labelsTask: com.google.android.gms.tasks.Task<List<com.google.mlkit.vision.label.ImageLabel>>,
        textTask: com.google.android.gms.tasks.Task<com.google.mlkit.vision.text.Text>,
        objectsTask: com.google.android.gms.tasks.Task<List<com.google.mlkit.vision.objects.DetectedObject>>
    ): FrameResult {
        val labels = if (labelsTask.isSuccessful) {
            labelsTask.result.map { "${it.text} (${(it.confidence * PERCENT).toInt()}%)" }
        } else {
            emptyList()
        }
        val lines = if (textTask.isSuccessful) {
            textTask.result.textBlocks.flatMap { it.lines }
                .filter { it.confidence >= MIN_TEXT_CONFIDENCE && OcrTextCleaner.isMeaningful(it.text) }
        } else {
            emptyList()
        }
        val objects = if (objectsTask.isSuccessful) objectsTask.result else emptyList()

        val boxes = objects.map { obj ->
            val name = obj.labels.firstOrNull()?.text ?: "Objeto"
            DetectionBox(
                name, BoxKind.Object,
                obj.boundingBox.left, obj.boundingBox.top, obj.boundingBox.right, obj.boundingBox.bottom
            )
        } + lines.mapNotNull { line ->
            line.boundingBox?.let {
                DetectionBox(OcrTextCleaner.normalize(line.text).take(MAX_TEXT_LABEL), BoxKind.Text,
                    it.left, it.top, it.right, it.bottom)
            }
        }

        return FrameResult(
            labels = labels,
            texts = OcrTextCleaner.clean(lines.map { it.text }),
            boxes = boxes,
            frameWidth = input.width,
            frameHeight = input.height
        )
    }

    override fun close() {
        closed = true
        labeler.close()
        recognizer.close()
        objectDetector.close()
    }

    private companion object {
        const val INTERVAL_MS = 700L
        const val CONFIDENCE = 0.6f
        const val MIN_TEXT_CONFIDENCE = 0.5f
        const val PERCENT = 100
        const val MAX_TEXT_LABEL = 20
    }
}
