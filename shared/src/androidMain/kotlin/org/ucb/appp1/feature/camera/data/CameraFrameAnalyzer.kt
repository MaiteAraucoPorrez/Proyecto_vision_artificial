package org.ucb.appp1.feature.camera.data

import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageProxy
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import org.ucb.appp1.core.util.OcrTextCleaner
import org.ucb.appp1.core.util.safeCall
import org.ucb.appp1.feature.camera.presentation.viewmodel.BoxKind
import org.ucb.appp1.feature.camera.presentation.viewmodel.DetectionBox
import org.ucb.appp1.feature.camera.presentation.viewmodel.FrameResult

/**
 * Analiza frames de CameraX con ML Kit: etiquetas, OCR por líneas y detección de objetos (con recuadros).
 *
 * Los tres detectores se ejecutan **uno tras otro** en el hilo del analizador (no en paralelo) y
 * se liberan mediante [AnalysisGuard], porque cerrarlos o usarlos a la vez provocaba un
 * SIGSEGV nativo. Procesa como máximo un frame por segundo; [onResult] se invoca en el hilo principal.
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
    private val guard = AnalysisGuard {
        labeler.close()
        recognizer.close()
        objectDetector.close()
    }
    private val mainHandler = Handler(Looper.getMainLooper())
    private var lastRun = 0L

    @ExperimentalGetImage
    override fun analyze(imageProxy: ImageProxy) {
        val media = imageProxy.image
        val now = SystemClock.elapsedRealtime()
        if (media == null || now - lastRun < INTERVAL_MS || !guard.tryStart()) {
            imageProxy.close()
            return
        }
        lastRun = now
        try {
            // Cualquier fallo de ML Kit con este frame se ignora: nunca debe cerrar la app.
            safeCall {
                val input = InputImage.fromMediaImage(media, imageProxy.imageInfo.rotationDegrees)
                val result = buildResult(input)
                mainHandler.post { onResult(result) }
            }
        } finally {
            guard.finish()
            imageProxy.close()
        }
    }

    private fun buildResult(input: InputImage): FrameResult {
        val labels = Tasks.await(labeler.process(input))
            .map { "${it.text} (${(it.confidence * PERCENT).toInt()}%)" }
        val text: Text = Tasks.await(recognizer.process(input))
        val objects = Tasks.await(objectDetector.process(input))

        val lines = text.textBlocks.flatMap { it.lines }
            .filter { it.confidence >= MIN_TEXT_CONFIDENCE && OcrTextCleaner.isMeaningful(it.text) }

        val boxes = objects.map { obj ->
            val name = obj.labels.firstOrNull()?.text ?: "Objeto"
            val r = obj.boundingBox
            DetectionBox(name, BoxKind.Object, r.left, r.top, r.right, r.bottom)
        } + lines.mapNotNull { line ->
            line.boundingBox?.let {
                val label = OcrTextCleaner.normalize(line.text).take(MAX_TEXT_LABEL)
                DetectionBox(label, BoxKind.Text, it.left, it.top, it.right, it.bottom)
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

    override fun close() = guard.close()

    private companion object {
        const val INTERVAL_MS = 1000L
        const val CONFIDENCE = 0.6f
        const val MIN_TEXT_CONFIDENCE = 0.5f
        const val PERCENT = 100
        const val MAX_TEXT_LABEL = 20
    }
}
