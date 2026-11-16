package org.ucb.appp1.feature.camera.data

import androidx.camera.core.ImageAnalysis

/** Analizador de frames que además libera sus recursos nativos (ML Kit) al cerrarse. */
interface ClosableAnalyzer : ImageAnalysis.Analyzer {
    fun close()
}
