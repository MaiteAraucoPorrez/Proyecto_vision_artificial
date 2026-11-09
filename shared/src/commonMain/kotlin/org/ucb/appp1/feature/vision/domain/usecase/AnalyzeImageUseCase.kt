package org.ucb.appp1.feature.vision.domain.usecase

import org.ucb.appp1.feature.vision.domain.model.VisionResult
import org.ucb.appp1.feature.vision.domain.repository.VisionRepository

class AnalyzeImageUseCase(
    private val repository: VisionRepository
) {
    suspend operator fun invoke(imageUri: String): Result<VisionResult> = runCatching {
        repository.analyzeImage(imageUri)
    }
}
