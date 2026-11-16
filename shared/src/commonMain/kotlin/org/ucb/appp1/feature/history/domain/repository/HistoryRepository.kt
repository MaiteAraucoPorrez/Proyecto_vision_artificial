package org.ucb.appp1.feature.history.domain.repository

import kotlinx.coroutines.flow.Flow
import org.ucb.appp1.feature.history.domain.model.AnalysisHistory

interface HistoryRepository {
    fun observeAll(): Flow<List<AnalysisHistory>>
    suspend fun save(item: AnalysisHistory)
    suspend fun delete(id: Long)
    suspend fun clear()
}
