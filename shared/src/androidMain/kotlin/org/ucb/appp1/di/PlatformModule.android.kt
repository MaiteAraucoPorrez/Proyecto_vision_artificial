package org.ucb.appp1.di

import org.koin.core.module.Module
import org.koin.dsl.module

actual fun platformModule(): Module = module {
    // No platform-specific dependencies needed for vision feature
}
