package org.ucb.appp1.core.domain.repository

import org.ucb.appp1.core.domain.model.UserModel
import org.ucb.appp1.core.domain.vo.Email

/**
 * Nueva: permite editar los datos del usuario ya registrado.
 * La usa el nuevo feature de edición de Perfil.
 */
interface ProfileRepository {
    suspend fun updateFullName(email: Email, fullName: String): Result<UserModel>
}
