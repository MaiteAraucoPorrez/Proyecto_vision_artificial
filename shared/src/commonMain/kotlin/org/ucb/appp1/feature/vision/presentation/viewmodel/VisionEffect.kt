package org.ucb.appp1.feature.vision.presentation.viewmodel

sealed interface VisionEffect {
    object OpenImagePicker : VisionEffect
    data class ShowError(val message: String) : VisionEffect
}
