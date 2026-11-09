package org.ucb.appp1.core.domain.model

import org.ucb.appp1.core.domain.vo.Email

/**
 * Usuario autenticado. Es "core" (no pertenece a un solo feature)
 * porque tanto Login, SignUp como Perfil lo necesitan.
 */
data class UserModel(
    val id: String,
    val fullName: String,
    val email: Email
)
