package org.ucb.appp1.di

import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module
import org.ucb.appp1.feature.history.data.datasource.HistoryDataSource
import org.ucb.appp1.feature.history.data.datasource.SqliteHistoryDataSource
import org.ucb.appp1.feature.vision.data.ai.GeminiImageDescriptionRepository
import org.ucb.appp1.feature.vision.data.datasource.ImageAnalyzerDataSource
import org.ucb.appp1.feature.vision.data.datasource.MlKitImageAnalyzer
import org.ucb.appp1.feature.vision.domain.repository.ImageDescriptionRepository

/** Clave con la que MainApplication pasa la API key de Gemini (viene de local.properties). */
const val GEMINI_API_KEY_PROPERTY = "gemini.apiKey"

actual fun platformModule(): Module = module {
    single<ImageAnalyzerDataSource> { MlKitImageAnalyzer(androidContext()) }
    single<HistoryDataSource> { SqliteHistoryDataSource(androidContext()) }
    single<ImageDescriptionRepository> {
        GeminiImageDescriptionRepository(androidContext(), getProperty(GEMINI_API_KEY_PROPERTY, ""))
    }
}
