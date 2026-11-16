package org.ucb.appp1.di

import android.content.Context
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

fun initKoinAndroid(context: Context, geminiApiKey: String = "") {
    startKoin {
        androidContext(context)
        androidLogger()
        properties(mapOf(GEMINI_API_KEY_PROPERTY to geminiApiKey))
        modules(sharedModules())
    }
}
