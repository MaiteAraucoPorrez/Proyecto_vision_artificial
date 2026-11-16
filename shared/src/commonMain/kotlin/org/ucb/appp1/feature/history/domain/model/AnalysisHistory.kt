package org.ucb.appp1.feature.history.domain.model

data class AnalysisHistory(
    val id: Long = 0L,
    val source: String = "",
    val labels: List<String> = emptyList(),
    val texts: List<String> = emptyList(),
    val timestamp: Long = 0L
)
