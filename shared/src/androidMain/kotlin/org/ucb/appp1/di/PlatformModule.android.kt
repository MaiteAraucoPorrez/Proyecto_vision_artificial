package org.ucb.appp1.di

import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module
import org.ucb.appp1.feature.history.data.datasource.HistoryDataSource
import org.ucb.appp1.feature.history.data.datasource.SqliteHistoryDataSource
import org.ucb.appp1.feature.vision.data.datasource.ImageAnalyzerDataSource
import org.ucb.appp1.feature.vision.data.datasource.MlKitImageAnalyzer

actual fun platformModule(): Module = module {
    single<ImageAnalyzerDataSource> { MlKitImageAnalyzer(androidContext()) }
    single<HistoryDataSource> { SqliteHistoryDataSource(androidContext()) }
}
