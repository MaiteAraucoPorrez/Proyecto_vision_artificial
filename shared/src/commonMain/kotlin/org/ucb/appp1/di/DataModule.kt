package org.ucb.appp1.di

import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import org.ucb.appp1.core.data.ProfileRepositoryImpl
import org.ucb.appp1.core.data.SessionRepositoryImpl
import org.ucb.appp1.core.domain.repository.ProfileRepository
import org.ucb.appp1.core.domain.repository.SessionRepository
import org.ucb.appp1.feature.vision.data.datasource.ImageAnalyzerDataSource
import org.ucb.appp1.feature.vision.data.datasource.StubImageAnalyzerDataSource
import org.ucb.appp1.feature.vision.data.repository.VisionRepositoryImpl
import org.ucb.appp1.feature.vision.domain.repository.VisionRepository

val dataModule = module {
    single<SessionRepository> { SessionRepositoryImpl() }
    single<ProfileRepository> { ProfileRepositoryImpl() }

    // --- Vision / ML Kit ---
    single<ImageAnalyzerDataSource> { StubImageAnalyzerDataSource() }
    singleOf(::VisionRepositoryImpl)
    single<VisionRepository> { VisionRepositoryImpl(get()) }
}
