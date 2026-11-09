package org.ucb.appp1.feature.vision.presentation.viewmodel

sealed interface VisionEvent {
    object OnSelectImageClick : VisionEvent
    data class OnImageSelected(val uri: String) : VisionEvent
    object OnAnalyzeClick : VisionEvent
    object OnClearResult : VisionEvent
}
