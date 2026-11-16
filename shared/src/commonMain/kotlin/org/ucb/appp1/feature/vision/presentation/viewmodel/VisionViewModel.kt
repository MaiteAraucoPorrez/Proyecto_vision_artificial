package org.ucb.appp1.feature.vision.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.ucb.appp1.feature.history.domain.usecase.SaveAnalysisUseCase
import org.ucb.appp1.feature.vision.domain.usecase.AnalyzeImageUseCase

class VisionViewModel(
    private val analyzeImageUseCase: AnalyzeImageUseCase,
    private val saveAnalysisUseCase: SaveAnalysisUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(VisionState())
    val state = _state.asStateFlow()

    private val _effect = MutableSharedFlow<VisionEffect>()
    val effect = _effect.asSharedFlow()

    fun emitEvent(event: VisionEvent) = viewModelScope.launch {
        when (event) {
            VisionEvent.OnSelectImageClick -> _effect.emit(VisionEffect.OpenImagePicker)
            is VisionEvent.OnImageSelected ->
                _state.update { it.copy(selectedImageUri = event.uri, result = null, error = null) }
            VisionEvent.OnAnalyzeClick -> analyze()
            VisionEvent.OnClearResult -> _state.update { VisionState() }
        }
    }

    private suspend fun analyze() {
        val uri = _state.value.selectedImageUri ?: return
        _state.update { it.copy(isLoading = true, error = null) }
        analyzeImageUseCase(uri)
            .onSuccess { result ->
                _state.update { it.copy(isLoading = false, result = result) }
                saveAnalysisUseCase("gallery", result.labels, result.texts)
            }
            .onFailure { e ->
                _state.update { it.copy(isLoading = false, error = e.message ?: "No se pudo analizar la imagen") }
            }
    }
}
