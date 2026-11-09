package org.ucb.appp1.core.data

import org.ucb.appp1.core.domain.model.UserModel

/**
 * Mismo patrón que InMemoryAuthStore: un `object` (singleton de Kotlin)
 * para que toda la app comparta la misma sesión sin depender de Koin.
 */
object SessionStore {
    private var currentUser: UserModel? = null

    fun get(): UserModel? = currentUser
    fun set(user: UserModel) { currentUser = user }
    fun clear() { currentUser = null }
}
