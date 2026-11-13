package org.ucb.appp1.di

import org.koin.core.module.Module
import org.koin.dsl.module
import org.ucb.appp1.feature.vision.data.datasource.ImageAnalyzerDataSource
import org.ucb.appp1.feature.vision.data.datasource.StubImageAnalyzerDataSource

actual fun platformModule(): Module = module {
    // iOS stub: ML Kit no está disponible en iOS
    single<ImageAnalyzerDataSource> { StubImageAnalyzerDataSource() }
}
