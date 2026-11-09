package org.ucb.appp1.feature.vision.data.repository

import org.ucb.appp1.feature.vision.data.datasource.ImageAnalyzerDataSource
import org.ucb.appp1.feature.vision.domain.model.VisionResult
import org.ucb.appp1.feature.vision.domain.repository.VisionRepository

class VisionRepositoryImpl(
    private val dataSource: ImageAnalyzerDataSource
) : VisionRepository {
    override suspend fun analyzeImage(imageUri: String): VisionResult {
        return dataSource.analyze(imageUri)
    }
}
