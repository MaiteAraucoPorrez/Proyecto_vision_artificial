package org.ucb.appp1.core.data

import org.ucb.appp1.core.domain.model.UserModel
import org.ucb.appp1.core.domain.repository.ProfileRepository
import org.ucb.appp1.core.domain.vo.Email

class ProfileRepositoryImpl : ProfileRepository {
    override suspend fun updateFullName(email: Email, fullName: String): Result<UserModel> =
        InMemoryAuthStore.updateFullName(email, fullName)
}
