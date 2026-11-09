package org.ucb.appp1.feature.vision.presentation.viewmodel

import org.ucb.appp1.feature.vision.domain.model.VisionResult

data class VisionState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedImageUri: String? = null,
    val result: VisionResult? = null
)
