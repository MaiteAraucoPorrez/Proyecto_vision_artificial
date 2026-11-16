package org.ucb.appp1.feature.history.presentation.viewmodel

import org.ucb.appp1.feature.history.domain.model.AnalysisHistory

data class HistoryState(
    val isLoading: Boolean = true,
    val items: List<AnalysisHistory> = emptyList(),
    val error: String? = null
)

sealed interface HistoryEvent {
    data class OnDelete(val id: Long) : HistoryEvent
    data object OnClearAll : HistoryEvent
}

sealed interface HistoryEffect {
    data class ShowMessage(val message: String) : HistoryEffect
}
