package org.ucb.appp1.feature.scanner.domain

/** Datos legibles de un QR de pago. */
data class PaymentInfo(
    val merchant: String?,
    val city: String?,
    val amount: String?,
    val currency: String?
)

/**
 * Lee QR de pago del estándar **EMVCo (Merchant-Presented Mode)**, usado por muchos bancos y
 * billeteras. Es una cadena TLV: cada campo = id (2 dígitos) + largo (2 dígitos) + valor.
 * Campos útiles: 00 versión, 53 moneda, 54 monto, 59 comercio, 60 ciudad.
 *
 * Solo LEE el código para mostrarlo; la app no realiza pagos. Si el QR no sigue este estándar
 * (formatos propietarios), devuelve null y la app muestra el contenido tal cual.
 */
object EmvQrParser {

    private const val ID_LENGTH = 2
    private const val LEN_LENGTH = 2
    private const val HEADER = ID_LENGTH + LEN_LENGTH
    private const val PAYLOAD_FORMAT = "00"
    private const val SUPPORTED_VERSION = "01"

    private val CURRENCIES = mapOf(
        "068" to "BOB", "840" to "USD", "032" to "ARS", "986" to "BRL",
        "604" to "PEN", "152" to "CLP", "170" to "COP", "484" to "MXN", "978" to "EUR"
    )

    fun parse(value: String): PaymentInfo? {
        val fields = tlv(value)
        val isPayment = fields != null &&
            fields[PAYLOAD_FORMAT] == SUPPORTED_VERSION &&
            (fields["59"] != null || fields["54"] != null)
        return if (isPayment && fields != null) {
            PaymentInfo(
                merchant = fields["59"],
                city = fields["60"],
                amount = fields["54"],
                currency = fields["53"]?.let { CURRENCIES[it] ?: it }
            )
        } else {
            null
        }
    }

    private fun tlv(text: String): Map<String, String>? {
        val result = mutableMapOf<String, String>()
        var i = 0
        var valid = true
        while (valid && i < text.length) {
            val length = if (i + HEADER <= text.length) {
                text.substring(i + ID_LENGTH, i + HEADER).toIntOrNull()?.takeIf { it >= 0 }
            } else {
                null
            }
            val end = if (length != null) i + HEADER + length else -1
            if (length == null || end > text.length) {
                valid = false
            } else {
                result[text.substring(i, i + ID_LENGTH)] = text.substring(i + HEADER, end)
                i = end
            }
        }
        return if (valid && result.isNotEmpty()) result else null
    }
}
