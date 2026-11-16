package org.ucb.appp1.feature.history.data.datasource

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import org.ucb.appp1.feature.history.domain.model.AnalysisHistory

class InMemoryHistoryDataSource : HistoryDataSource {
    private val items = MutableStateFlow<List<AnalysisHistory>>(emptyList())
    private var nextId = 1L

    override fun observeAll(): Flow<List<AnalysisHistory>> = items

    override suspend fun insert(item: AnalysisHistory) {
        val saved = item.copy(id = nextId++)
        items.update { listOf(saved) + it }
    }

    override suspend fun delete(id: Long) {
        items.update { list -> list.filterNot { it.id == id } }
    }

    override suspend fun clear() {
        items.value = emptyList()
    }
}
