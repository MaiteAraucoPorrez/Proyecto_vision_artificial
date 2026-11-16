package org.ucb.appp1.feature.vision.domain.model

import org.ucb.appp1.feature.vision.domain.quality.ImageQuality

data class VisionResult(
    val imageUri: String = "",
    val labels: List<String> = emptyList(),
    val texts: List<String> = emptyList(),
    val timestamp: Long = 0L,
    val quality: ImageQuality? = null
)
