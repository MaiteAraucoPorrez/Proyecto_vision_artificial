package org.ucb.appp1.feature.scanner.data

import android.os.SystemClock
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import org.ucb.appp1.feature.camera.data.ClosableAnalyzer
import org.ucb.appp1.feature.camera.presentation.viewmodel.BoxKind
import org.ucb.appp1.feature.camera.presentation.viewmodel.DetectionBox
import org.ucb.appp1.feature.camera.presentation.viewmodel.FrameResult
import org.ucb.appp1.feature.scanner.presentation.viewmodel.ScannedCode

/** Lee QR y códigos de barras de los frames de CameraX con ML Kit Barcode Scanning. */
class BarcodeFrameAnalyzer(
    private val onResult: (List<ScannedCode>, FrameResult) -> Unit
) : ClosableAnalyzer {

    private val scanner = BarcodeScanning.getClient(
        BarcodeScannerOptions.Builder().setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS).build()
    )
    private var lastRun = 0L

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

        val input = InputImage.fromMediaImage(media, imageProxy.imageInfo.rotationDegrees)
        scanner.process(input).addOnCompleteListener { task ->
            if (!closed) {
                val barcodes = if (task.isSuccessful) task.result else emptyList()
                val boxes = barcodes.mapNotNull { b ->
                    b.boundingBox?.let {
                        DetectionBox(
                            BarcodeMapper.formatName(b.format), BoxKind.Object,
                            it.left, it.top, it.right, it.bottom
                        )
                    }
                }
                onResult(
                    barcodes.mapNotNull { BarcodeMapper.toScannedCode(it) },
                    FrameResult(boxes = boxes, frameWidth = input.width, frameHeight = input.height)
                )
            }
            imageProxy.close()
        }
    }

    override fun close() {
        closed = true
        scanner.close()
    }

    private companion object {
        const val INTERVAL_MS = 300L
    }
}
