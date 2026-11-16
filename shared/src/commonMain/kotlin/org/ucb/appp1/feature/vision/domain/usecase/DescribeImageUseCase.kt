package org.ucb.appp1.feature.vision.domain.usecase

import org.ucb.appp1.feature.vision.domain.repository.ImageDescriptionRepository

class DescribeImageUseCase(
    private val repository: ImageDescriptionRepository
) {
    suspend operator fun invoke(imageUri: String): Result<String> = runCatching {
        repository.describe(imageUri)
    }
}
