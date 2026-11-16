package org.ucb.appp1.feature.vision.domain.repository

/** Descripción de una imagen en lenguaje natural con IA generativa (requiere internet). */
interface ImageDescriptionRepository {
    suspend fun describe(imageUri: String): String
}
