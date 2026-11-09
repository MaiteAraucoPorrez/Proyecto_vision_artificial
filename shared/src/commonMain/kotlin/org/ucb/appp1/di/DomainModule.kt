package org.ucb.appp1.di

import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import org.ucb.appp1.feature.vision.domain.usecase.AnalyzeImageUseCase

val domainModule = module {
    singleOf(::AnalyzeImageUseCase)
}
