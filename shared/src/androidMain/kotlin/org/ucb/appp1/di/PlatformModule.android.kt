package org.ucb.appp1.di

import android.content.Context
import org.koin.android.ext.android.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module
import org.ucb.appp1.feature.vision.data.datasource.ImageAnalyzerDataSource
import org.ucb.appp1.feature.vision.data.datasource.MlKitImageAnalyzer

actual fun platformModule(): Module = module {
    // Android-specific: ML Kit ImageAnalyzer implementation
    single<ImageAnalyzerDataSource> { MlKitImageAnalyzer(androidContext()) }
}
