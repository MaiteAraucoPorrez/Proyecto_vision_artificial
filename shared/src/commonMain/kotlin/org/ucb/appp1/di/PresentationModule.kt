package org.ucb.appp1.di

import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import org.ucb.appp1.feature.vision.presentation.viewmodel.VisionViewModel

val presentationModule = module {
    viewModelOf(::VisionViewModel)
}
