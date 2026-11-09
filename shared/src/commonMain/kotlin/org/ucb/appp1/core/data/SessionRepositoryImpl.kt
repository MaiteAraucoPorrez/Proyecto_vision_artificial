package org.ucb.appp1.core.data

import org.ucb.appp1.core.domain.model.UserModel
import org.ucb.appp1.core.domain.repository.SessionRepository

class SessionRepositoryImpl : SessionRepository {
    override fun getCurrentUser(): UserModel? = SessionStore.get()
    override fun setCurrentUser(user: UserModel) = SessionStore.set(user)
    override fun clearSession() = SessionStore.clear()
}
