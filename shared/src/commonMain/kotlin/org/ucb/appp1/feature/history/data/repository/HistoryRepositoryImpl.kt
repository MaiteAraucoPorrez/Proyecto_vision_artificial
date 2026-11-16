package org.ucb.appp1.feature.history.data.repository

import kotlinx.coroutines.flow.Flow
import org.ucb.appp1.feature.history.data.datasource.HistoryDataSource
import org.ucb.appp1.feature.history.domain.model.AnalysisHistory
import org.ucb.appp1.feature.history.domain.repository.HistoryRepository

class HistoryRepositoryImpl(
    private val dataSource: HistoryDataSource
) : HistoryRepository {
    override fun observeAll(): Flow<List<AnalysisHistory>> = dataSource.observeAll()
    override suspend fun save(item: AnalysisHistory) = dataSource.insert(item)
    override suspend fun delete(id: Long) = dataSource.delete(id)
    override suspend fun clear() = dataSource.clear()
}
