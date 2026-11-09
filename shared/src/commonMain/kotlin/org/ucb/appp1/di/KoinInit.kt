package org.ucb.appp1.di

// NO DEBES ELIMINAR EL PAQUETE DE TU APLICACION
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration

fun initKoin(appDeclaration: KoinAppDeclaration = {}) {
    startKoin {
        appDeclaration()
        modules(sharedModules())
    }
}
