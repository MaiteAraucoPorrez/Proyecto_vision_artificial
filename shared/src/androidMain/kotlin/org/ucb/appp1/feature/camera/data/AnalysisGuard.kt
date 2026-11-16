package org.ucb.appp1.feature.camera.data

/**
 * Evita cerrar un detector de ML Kit mientras está analizando un frame.
 *
 * Cerrar un detector (liberar su "pipeline" nativo) mientras otro hilo lo usa provoca un
 * SIGSEGV en `libmlkitcommonpipeline.so` que cierra la app sin dejar excepción de Kotlin.
 * Con esta guarda, si se pide cerrar durante un análisis, la liberación se pospone hasta
 * que el análisis termina.
 */
class AnalysisGuard(private val release: () -> Unit) {
    private val lock = Any()
    private var running = false
    private var closeRequested = false
    private var released = false

    /** true si se puede analizar un frame; false si ya se pidió cerrar. */
    fun tryStart(): Boolean = synchronized(lock) {
        if (closeRequested) {
            false
        } else {
            running = true
            true
        }
    }

    fun finish() = synchronized(lock) {
        running = false
        if (closeRequested) releaseOnce()
    }

    fun close() = synchronized(lock) {
        closeRequested = true
        if (!running) releaseOnce()
    }

    private fun releaseOnce() {
        if (!released) {
            released = true
            release()
        }
    }
}
