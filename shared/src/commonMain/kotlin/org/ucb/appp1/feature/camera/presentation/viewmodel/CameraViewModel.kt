package org.ucb.appp1.feature.camera.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.ucb.appp1.feature.history.domain.usecase.SaveAnalysisUseCase

enum class BoxKind { Object, Text }

/** Recuadro detectado, en píxeles del frame analizado (ya rotado). */
data class DetectionBox(
    val label: String,
    val kind: BoxKind,
    val left: Int,
    val top: Int,
    val right: Int,
    val bottom: Int
)

data class FrameResult(
    val labels: List<String> = emptyList(),
    val texts: List<String> = emptyList(),
    val boxes: List<DetectionBox> = emptyList(),
    val frameWidth: Int = 0,
    val frameHeight: Int = 0
)

data class CameraState(
    val permissionGranted: Boolean? = null,
    val frame: FrameResult = FrameResult(),
    val isSaving: Boolean = false
) {
    val labels: List<String> get() = frame.labels
    val texts: List<String> get() = frame.texts
}

sealed interface CameraEvent {
    data class OnPermissionResult(val granted: Boolean) : CameraEvent
    data class OnFrameAnalyzed(val frame: FrameResult) : CameraEvent
    data object OnSaveClick : CameraEvent
}

sealed interface CameraEffect {
    data class ShowMessage(val message: String) : CameraEffect
}

class CameraViewModel(
    private val saveAnalysis: SaveAnalysisUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(CameraState())
    val state = _state.asStateFlow()

    private val _effect = MutableSharedFlow<CameraEffect>()
    val effect = _effect.asSharedFlow()

    fun emitEvent(event: CameraEvent) {
        when (event) {
            is CameraEvent.OnPermissionResult -> _state.update { it.copy(permissionGranted = event.granted) }
            is CameraEvent.OnFrameAnalyzed -> _state.update { it.copy(frame = event.frame) }
            CameraEvent.OnSaveClick -> save()
        }
    }

    private fun save() = viewModelScope.launch {
        val current = _state.value
        if (current.labels.isEmpty() && current.texts.isEmpty()) {
            _effect.emit(CameraEffect.ShowMessage("Nada que guardar todavía"))
            return@launch
        }
        _state.update { it.copy(isSaving = true) }
        saveAnalysis("camera", current.labels, current.texts)
            .onSuccess { _effect.emit(CameraEffect.ShowMessage("Guardado en el historial")) }
            .onFailure { _effect.emit(CameraEffect.ShowMessage("No se pudo guardar")) }
        _state.update { it.copy(isSaving = false) }
    }
}
