package org.ucb.appp1.di

import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import org.ucb.appp1.feature.history.data.repository.HistoryRepositoryImpl
import org.ucb.appp1.feature.history.domain.repository.HistoryRepository
import org.ucb.appp1.feature.vision.data.repository.VisionRepositoryImpl
import org.ucb.appp1.feature.vision.domain.repository.VisionRepository

val dataModule = module {
    // ImageAnalyzerDataSource y HistoryDataSource los provee platformModule()
    singleOf(::VisionRepositoryImpl) bind VisionRepository::class
    singleOf(::HistoryRepositoryImpl) bind HistoryRepository::class
}
