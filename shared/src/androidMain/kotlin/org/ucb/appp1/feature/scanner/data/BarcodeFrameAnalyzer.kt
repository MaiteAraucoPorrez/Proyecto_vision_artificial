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
import org.ucb.appp1.feature.scanner.presentation.viewmodel.ScanKind
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
                val barcodes = if (task.isSuccessful) {
                    task.result.filter { !it.rawValue.isNullOrBlank() }
                } else {
                    emptyList()
                }
                val boxes = barcodes.mapNotNull { b ->
                    b.boundingBox?.let {
                        DetectionBox(formatName(b.format), BoxKind.Object, it.left, it.top, it.right, it.bottom)
                    }
                }
                onResult(
                    barcodes.map { ScannedCode(it.rawValue.orEmpty(), formatName(it.format), kindOf(it.valueType)) },
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

    private fun formatName(format: Int): String = FORMATS[format] ?: "Código"

    private fun kindOf(valueType: Int): ScanKind = KINDS[valueType] ?: ScanKind.Other

    private companion object {
        const val INTERVAL_MS = 300L

        val FORMATS = mapOf(
            Barcode.FORMAT_QR_CODE to "QR",
            Barcode.FORMAT_EAN_13 to "EAN-13",
            Barcode.FORMAT_EAN_8 to "EAN-8",
            Barcode.FORMAT_UPC_A to "UPC-A",
            Barcode.FORMAT_UPC_E to "UPC-E",
            Barcode.FORMAT_CODE_128 to "Code 128",
            Barcode.FORMAT_CODE_39 to "Code 39",
            Barcode.FORMAT_CODE_93 to "Code 93",
            Barcode.FORMAT_CODABAR to "Codabar",
            Barcode.FORMAT_ITF to "ITF",
            Barcode.FORMAT_DATA_MATRIX to "Data Matrix",
            Barcode.FORMAT_PDF417 to "PDF417",
            Barcode.FORMAT_AZTEC to "Aztec"
        )

        val KINDS = mapOf(
            Barcode.TYPE_URL to ScanKind.Url,
            Barcode.TYPE_WIFI to ScanKind.Wifi,
            Barcode.TYPE_TEXT to ScanKind.Text,
            Barcode.TYPE_EMAIL to ScanKind.Email,
            Barcode.TYPE_PHONE to ScanKind.Phone,
            Barcode.TYPE_SMS to ScanKind.Sms,
            Barcode.TYPE_CONTACT_INFO to ScanKind.Contact,
            Barcode.TYPE_GEO to ScanKind.Geo,
            Barcode.TYPE_PRODUCT to ScanKind.Product
        )
    }
}
