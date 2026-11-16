package org.ucb.appp1.di

import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module
import org.ucb.appp1.feature.history.domain.usecase.ClearHistoryUseCase
import org.ucb.appp1.feature.history.domain.usecase.DeleteAnalysisUseCase
import org.ucb.appp1.feature.history.domain.usecase.GetHistoryUseCase
import org.ucb.appp1.feature.history.domain.usecase.SaveAnalysisUseCase
import org.ucb.appp1.feature.vision.domain.usecase.AnalyzeImageUseCase

val domainModule = module {
    factoryOf(::AnalyzeImageUseCase)
    factoryOf(::GetHistoryUseCase)
    factoryOf(::SaveAnalysisUseCase)
    factoryOf(::DeleteAnalysisUseCase)
    factoryOf(::ClearHistoryUseCase)
}
