package org.ucb.appp1.di

import org.koin.core.module.Module
import org.koin.dsl.module
import org.ucb.appp1.feature.history.data.datasource.HistoryDataSource
import org.ucb.appp1.feature.history.data.datasource.InMemoryHistoryDataSource
import org.ucb.appp1.feature.vision.data.datasource.AppleVisionImageAnalyzer
import org.ucb.appp1.feature.vision.data.datasource.ImageAnalyzerDataSource

actual fun platformModule(): Module = module {
    single<ImageAnalyzerDataSource> { AppleVisionImageAnalyzer() }
    single<HistoryDataSource> { InMemoryHistoryDataSource() }
}
