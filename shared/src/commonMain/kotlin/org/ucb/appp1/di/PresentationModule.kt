package org.ucb.appp1.di

import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import org.ucb.appp1.feature.camera.presentation.viewmodel.CameraViewModel
import org.ucb.appp1.feature.history.presentation.viewmodel.HistoryViewModel
import org.ucb.appp1.feature.scanner.presentation.viewmodel.ScannerViewModel
import org.ucb.appp1.feature.settings.presentation.SettingsViewModel
import org.ucb.appp1.feature.vision.presentation.viewmodel.VisionViewModel

val presentationModule = module {
    viewModelOf(::VisionViewModel)
    viewModelOf(::HistoryViewModel)
    viewModelOf(::CameraViewModel)
    viewModelOf(::ScannerViewModel)
    // Singleton: el tema es global y lo leen App() y la pantalla de ajustes.
    singleOf(::SettingsViewModel)
}
