package org.ucb.appp1.feature.vision.data.datasource

import org.ucb.appp1.feature.vision.domain.model.VisionResult

interface ImageAnalyzerDataSource {
    suspend fun analyze(imageUri: String): VisionResult
}
