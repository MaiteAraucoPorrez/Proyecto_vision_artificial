package org.ucb.appp1.feature.scanner.data

import com.google.mlkit.vision.barcode.common.Barcode
import org.ucb.appp1.feature.scanner.domain.EmvQrParser
import org.ucb.appp1.feature.scanner.presentation.viewmodel.ScanKind
import org.ucb.appp1.feature.scanner.presentation.viewmodel.ScannedCode

/** Convierte un [Barcode] de ML Kit al modelo de la app. */
object BarcodeMapper {

    fun formatName(format: Int): String = FORMATS[format] ?: "Código"

    fun toScannedCode(barcode: Barcode): ScannedCode? {
        val value = barcode.rawValue
        if (value.isNullOrBlank()) return null
        val payment = EmvQrParser.parse(value)
        val kind = if (payment != null) ScanKind.Payment else KINDS[barcode.valueType] ?: ScanKind.Other
        return ScannedCode(value, formatName(barcode.format), kind, payment)
    }

    private val FORMATS = mapOf(
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

    private val KINDS = mapOf(
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
