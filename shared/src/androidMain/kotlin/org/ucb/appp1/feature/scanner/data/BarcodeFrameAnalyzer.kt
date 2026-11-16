package org.ucb.appp1.feature.scanner.data

import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageProxy
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import org.ucb.appp1.core.util.safeCall
import org.ucb.appp1.feature.camera.data.AnalysisGuard
import org.ucb.appp1.feature.camera.data.ClosableAnalyzer
import org.ucb.appp1.feature.camera.presentation.viewmodel.BoxKind
import org.ucb.appp1.feature.camera.presentation.viewmodel.DetectionBox
import org.ucb.appp1.feature.camera.presentation.viewmodel.FrameResult
import org.ucb.appp1.feature.scanner.presentation.viewmodel.ScannedCode

/**
 * Lee QR y códigos de barras de los frames de CameraX con ML Kit Barcode Scanning.
 * Analiza de forma síncrona en el hilo del analizador y se libera con [AnalysisGuard]
 * (cerrar el escáner durante un análisis provocaba un SIGSEGV nativo).
 */
class BarcodeFrameAnalyzer(
    private val onResult: (List<ScannedCode>, FrameResult) -> Unit
) : ClosableAnalyzer {

    private val scanner = BarcodeScanning.getClient(
        BarcodeScannerOptions.Builder().setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS).build()
    )
    private val guard = AnalysisGuard { scanner.close() }
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
            safeCall {
                val input = InputImage.fromMediaImage(media, imageProxy.imageInfo.rotationDegrees)
                val barcodes = Tasks.await(scanner.process(input))
                val boxes = barcodes.mapNotNull { b ->
                    b.boundingBox?.let {
                        DetectionBox(
                            BarcodeMapper.formatName(b.format), BoxKind.Object,
                            it.left, it.top, it.right, it.bottom
                        )
                    }
                }
                val codes = barcodes.mapNotNull { BarcodeMapper.toScannedCode(it) }
                val frame = FrameResult(boxes = boxes, frameWidth = input.width, frameHeight = input.height)
                mainHandler.post { onResult(codes, frame) }
            }
        } finally {
            guard.finish()
            imageProxy.close()
        }
    }

    override fun close() = guard.close()

    private companion object {
        const val INTERVAL_MS = 300L
    }
}
