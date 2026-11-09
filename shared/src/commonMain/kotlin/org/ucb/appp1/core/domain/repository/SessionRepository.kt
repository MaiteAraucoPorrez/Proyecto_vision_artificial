package org.ucb.appp1.core.domain.repository

import org.ucb.appp1.core.domain.model.UserModel

/**
 * Sabe quién es el usuario que inició sesión en este momento.
 * La usan Login/SignUp (para guardarlo) y UserState (para leerlo).
 */
interface SessionRepository {
    fun getCurrentUser(): UserModel?
    fun setCurrentUser(user: UserModel)
    fun clearSession()
}
