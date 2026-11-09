package org.ucb.appp1.feature.vision.domain.repository

import org.ucb.appp1.feature.vision.domain.model.VisionResult

interface VisionRepository {
    suspend fun analyzeImage(imageUri: String): VisionResult
}
