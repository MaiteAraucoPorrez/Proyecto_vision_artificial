package org.ucb.appp1.feature.vision.presentation.viewmodel

sealed interface VisionEffect {
    data object OpenImagePicker : VisionEffect
}
