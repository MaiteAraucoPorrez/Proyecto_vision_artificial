package org.ucb.appp1.feature.history.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.ucb.appp1.feature.history.domain.usecase.ClearHistoryUseCase
import org.ucb.appp1.feature.history.domain.usecase.DeleteAnalysisUseCase
import org.ucb.appp1.feature.history.domain.usecase.GetHistoryUseCase

class HistoryViewModel(
    getHistory: GetHistoryUseCase,
    private val deleteAnalysis: DeleteAnalysisUseCase,
    private val clearHistory: ClearHistoryUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(HistoryState())
    val state = _state.asStateFlow()

    private val _effect = MutableSharedFlow<HistoryEffect>()
    val effect = _effect.asSharedFlow()

    init {
        getHistory()
            .onEach { items -> _state.update { it.copy(isLoading = false, items = items, error = null) } }
            .catch { e -> _state.update { it.copy(isLoading = false, error = e.message ?: "Error") } }
            .launchIn(viewModelScope)
    }

    fun emitEvent(event: HistoryEvent) = viewModelScope.launch {
        when (event) {
            is HistoryEvent.OnDelete -> deleteAnalysis(event.id)
                .onSuccess { _effect.emit(HistoryEffect.ShowMessage("Análisis eliminado")) }
                .onFailure { _effect.emit(HistoryEffect.ShowMessage("No se pudo eliminar")) }
            HistoryEvent.OnClearAll -> clearHistory()
                .onSuccess { _effect.emit(HistoryEffect.ShowMessage("Historial borrado")) }
                .onFailure { _effect.emit(HistoryEffect.ShowMessage("No se pudo borrar")) }
        }
    }
}
