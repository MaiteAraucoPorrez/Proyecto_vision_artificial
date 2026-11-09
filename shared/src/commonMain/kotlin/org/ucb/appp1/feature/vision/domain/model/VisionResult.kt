package org.ucb.appp1.feature.vision.domain.model

data class VisionResult(
    val imageUri: String = "",
    val labels: List<String> = emptyList(),
    val texts: List<String> = emptyList(),
    val timestamp: Long = 0L
)
