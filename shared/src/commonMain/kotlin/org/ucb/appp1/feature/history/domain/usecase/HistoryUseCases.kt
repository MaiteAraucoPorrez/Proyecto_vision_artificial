package org.ucb.appp1.feature.history.domain.usecase

import kotlinx.coroutines.flow.Flow
import org.ucb.appp1.core.util.currentTimeMillis
import org.ucb.appp1.feature.history.domain.model.AnalysisHistory
import org.ucb.appp1.feature.history.domain.repository.HistoryRepository

class GetHistoryUseCase(private val repository: HistoryRepository) {
    operator fun invoke(): Flow<List<AnalysisHistory>> = repository.observeAll()
}

class SaveAnalysisUseCase(private val repository: HistoryRepository) {
    suspend operator fun invoke(source: String, labels: List<String>, texts: List<String>): Result<Unit> =
        runCatching {
            repository.save(
                AnalysisHistory(
                    source = source,
                    labels = labels,
                    texts = texts,
                    timestamp = currentTimeMillis()
                )
            )
        }
}

class DeleteAnalysisUseCase(private val repository: HistoryRepository) {
    suspend operator fun invoke(id: Long): Result<Unit> = runCatching { repository.delete(id) }
}

class ClearHistoryUseCase(private val repository: HistoryRepository) {
    suspend operator fun invoke(): Result<Unit> = runCatching { repository.clear() }
}
