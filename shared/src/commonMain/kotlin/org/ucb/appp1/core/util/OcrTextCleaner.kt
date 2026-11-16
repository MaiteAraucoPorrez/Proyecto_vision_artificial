package org.ucb.appp1.core.util

/**
 * Limpia el texto devuelto por el OCR para quedarse solo con líneas legibles.
 * Descarta ruido típico: números sueltos, unidades ("12 px"), símbolos y fragmentos de 1-2 letras.
 */
object OcrTextCleaner {

    private const val MIN_LETTERS = 3
    private const val MIN_LETTER_PERCENT = 50
    private const val PERCENT = 100

    /** Una línea es útil si tiene al menos 3 letras y al menos la mitad de sus caracteres son letras. */
    fun isMeaningful(line: String): Boolean {
        val visible = line.count { !it.isWhitespace() }
        if (visible == 0) return false
        val letters = line.count { it.isLetter() }
        return letters >= MIN_LETTERS && letters * PERCENT / visible >= MIN_LETTER_PERCENT
    }

    /** Normaliza espacios, descarta el ruido y elimina duplicados (sin distinguir mayúsculas). */
    fun clean(lines: List<String>): List<String> =
        lines
            .map { normalize(it) }
            .filter { isMeaningful(it) }
            .distinctBy { it.lowercase() }

    fun normalize(line: String): String = line.trim().replace(Regex("\\s+"), " ")
}
