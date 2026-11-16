package org.ucb.appp1.feature.camera.data

import android.os.SystemClock
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import org.ucb.appp1.feature.camera.presentation.viewmodel.BoxKind
import org.ucb.appp1.feature.camera.presentation.viewmodel.DetectionBox
import org.ucb.appp1.feature.camera.presentation.viewmodel.FrameResult

/**
 * Analiza frames de CameraX con ML Kit: etiquetas, OCR y detección de objetos (con recuadros).
 * Procesa como máximo un frame cada [INTERVAL_MS]. [onResult] se invoca en el hilo principal.
 */
class CameraFrameAnalyzer(
    private val onResult: (FrameResult) -> Unit
) : ImageAnalysis.Analyzer {

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

    @ExperimentalGetImage
    override fun analyze(imageProxy: ImageProxy) {
        val media = imageProxy.image
        val now = SystemClock.elapsedRealtime()
        if (media == null || now - lastRun < INTERVAL_MS) {
            imageProxy.close()
            return
        }
        lastRun = now

        val input = InputImage.fromMediaImage(media, imageProxy.imageInfo.rotationDegrees)
        val labelsTask = labeler.process(input)
        val textTask = recognizer.process(input)
        val objectsTask = objectDetector.process(input)

        Tasks.whenAllComplete(labelsTask, textTask, objectsTask).addOnCompleteListener {
            val labels = if (labelsTask.isSuccessful) {
                labelsTask.result.map { "${it.text} (${(it.confidence * PERCENT).toInt()}%)" }
            } else {
                emptyList()
            }
            val textBlocks = if (textTask.isSuccessful) textTask.result.textBlocks else emptyList()
            val objects = if (objectsTask.isSuccessful) objectsTask.result else emptyList()

            val boxes = objects.mapNotNull { obj ->
                val name = obj.labels.firstOrNull()?.text ?: "Objeto"
                obj.boundingBox.toBox(name, BoxKind.Object)
            } + textBlocks.mapNotNull { block ->
                block.boundingBox?.toBox(block.text.take(MAX_TEXT_LABEL), BoxKind.Text)
            }

            onResult(
                FrameResult(
                    labels = labels,
                    texts = textBlocks.map { it.text },
                    boxes = boxes,
                    frameWidth = input.width,
                    frameHeight = input.height
                )
            )
            imageProxy.close()
        }
    }

    private fun android.graphics.Rect.toBox(label: String, kind: BoxKind) =
        DetectionBox(label, kind, left, top, right, bottom)

    fun close() {
        labeler.close()
        recognizer.close()
        objectDetector.close()
    }

    private companion object {
        const val INTERVAL_MS = 500L
        const val CONFIDENCE = 0.6f
        const val PERCENT = 100
        const val MAX_TEXT_LABEL = 20
    }
}
