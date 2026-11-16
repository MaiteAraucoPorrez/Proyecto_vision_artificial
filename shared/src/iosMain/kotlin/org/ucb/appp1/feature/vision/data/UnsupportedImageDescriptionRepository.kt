package org.ucb.appp1.feature.vision.data

import org.ucb.appp1.feature.vision.domain.repository.ImageDescriptionRepository

/** La descripción con IA generativa solo está implementada en Android. */
class UnsupportedImageDescriptionRepository : ImageDescriptionRepository {
    override suspend fun describe(imageUri: String): String =
        error("La descripción con IA todavía no está disponible en iOS.")
}
