package org.ucb.appp1.feature.scanner.data

import android.content.Context
import android.net.Uri
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.ucb.appp1.feature.scanner.presentation.viewmodel.ScannedCode

/** Busca códigos QR / de barras en una imagen de la galería. */
class BarcodeImageScanner(private val context: Context) {

    suspend fun scan(uri: Uri): Result<List<ScannedCode>> = withContext(Dispatchers.IO) {
        runCatching {
            val scanner = BarcodeScanning.getClient(
                BarcodeScannerOptions.Builder().setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS).build()
            )
            try {
                val image = InputImage.fromFilePath(context, uri)
                Tasks.await(scanner.process(image)).mapNotNull { BarcodeMapper.toScannedCode(it) }
            } finally {
                scanner.close()
            }
        }
    }
}
