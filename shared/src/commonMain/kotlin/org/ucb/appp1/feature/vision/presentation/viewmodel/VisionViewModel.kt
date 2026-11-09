package org.ucb.appp1.feature.vision.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.ucb.appp1.feature.vision.domain.usecase.AnalyzeImageUseCase

class VisionViewModel(
    private val analyzeImageUseCase: AnalyzeImageUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(VisionState())
    val state = _state.asStateFlow()

    private val _effect = MutableSharedFlow<VisionEffect>()
    val effect = _effect.asSharedFlow()

    fun emitEvent(event: VisionEvent) = viewModelScope.launch {
        when (event) {
            VisionEvent.OnSelectImageClick -> {
                _effect.emit(VisionEffect.OpenImagePicker)
            }
            is VisionEvent.OnImageSelected -> {
                _state.update { it.copy(selectedImageUri = event.uri, result = null, error = null) }
            }
            VisionEvent.OnAnalyzeClick -> {
                val uri = _state.value.selectedImageUri ?: return@launch
                _state.update { it.copy(isLoading = true, error = null) }
                analyzeImageUseCase(uri)
                    .onSuccess { result ->
                        _state.update { it.copy(isLoading = false, result = result) }
                    }
                    .onFailure { e ->
                        _state.update { it.copy(isLoading = false) }
                        _effect.emit(VisionEffect.ShowError(e.message ?: "Error desconocido"))
                    }
            }
            VisionEvent.OnClearResult -> {
                _state.update { VisionState() }
            }
        }
    }
}
