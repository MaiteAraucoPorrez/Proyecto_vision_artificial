package org.ucb.appp1.core.data

import org.ucb.appp1.core.domain.model.UserModel
import org.ucb.appp1.core.domain.vo.Email

/**
 * Almacén en memoria TEMPORAL mientras no haya backend real.
 * Es un `object` (singleton de Kotlin), por eso Login, SignUp y ahora
 * la edición de Perfil comparten el mismo usuario sin depender de Koin.
 */
object InMemoryAuthStore {
    private data class Credentials(val user: UserModel, val password: String)

    private val usersByEmail = mutableMapOf(
        "test@ucb.edu.bo" to Credentials(
            user = UserModel(id = "0", fullName = "Usuario de Prueba", email = Email("test@ucb.edu.bo")),
            password = "123456"
        )
    )

    fun register(fullName: String, email: Email, password: String): Result<UserModel> {
        if (usersByEmail.containsKey(email.value)) {
            return Result.failure(IllegalStateException("Ya existe una cuenta con ese correo"))
        }
        val user = UserModel(id = (usersByEmail.size + 1).toString(), fullName = fullName, email = email)
        usersByEmail[email.value] = Credentials(user, password)
        return Result.success(user)
    }

    fun login(email: Email, password: String): Result<UserModel> {
        val credentials = usersByEmail[email.value]
            ?: return Result.failure(IllegalArgumentException("Usuario o contraseña incorrectos"))
        return if (credentials.password == password) {
            Result.success(credentials.user)
        } else {
            Result.failure(IllegalArgumentException("Usuario o contraseña incorrectos"))
        }
    }

    fun updateFullName(email: Email, fullName: String): Result<UserModel> {
        val credentials = usersByEmail[email.value]
            ?: return Result.failure(IllegalStateException("No existe una cuenta con ese correo"))
        val updatedUser = credentials.user.copy(fullName = fullName)
        usersByEmail[email.value] = credentials.copy(user = updatedUser)
        return Result.success(updatedUser)
    }
}
