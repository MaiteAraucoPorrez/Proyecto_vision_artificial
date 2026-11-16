package org.ucb.appp1

import kotlin.test.Test
import kotlin.test.assertEquals
import org.ucb.appp1.feature.scanner.domain.EmvQrParser

class EmvQrParserTest {
    // 00 versión=01, 01 método=12, 53 moneda=068 (BOB), 54 monto=25.50, 59 comercio, 60 ciudad
    private val sample = "000201010212" + "5303068" + "540525.50" + "5909Tienda SA" + "6006La Paz"

    @Test
    fun parsesMerchantAmountAndCurrency() {
        val info = EmvQrParser.parse(sample)
        assertEquals("Tienda SA", info?.merchant)
        assertEquals("La Paz", info?.city)
        assertEquals("25.50", info?.amount)
        assertEquals("BOB", info?.currency)
    }

    @Test
    fun rejectsNonEmvAndMalformedText() {
        assertEquals(null, EmvQrParser.parse("https://ucb.edu.bo"))
        assertEquals(null, EmvQrParser.parse("0002010101"))
        assertEquals(null, EmvQrParser.parse("00020101120"))
    }
}
