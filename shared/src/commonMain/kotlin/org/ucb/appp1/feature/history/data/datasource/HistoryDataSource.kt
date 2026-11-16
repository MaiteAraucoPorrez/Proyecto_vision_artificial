package org.ucb.appp1.feature.history.data.datasource

import kotlinx.coroutines.flow.Flow
import org.ucb.appp1.feature.history.domain.model.AnalysisHistory

/** Almacenamiento del historial. Android: SQLite; iOS: memoria. */
interface HistoryDataSource {
    fun observeAll(): Flow<List<AnalysisHistory>>
    suspend fun insert(item: AnalysisHistory)
    suspend fun delete(id: Long)
    suspend fun clear()
}
