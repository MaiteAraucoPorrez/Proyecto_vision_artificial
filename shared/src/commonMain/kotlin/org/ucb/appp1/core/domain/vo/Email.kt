package org.ucb.appp1.core.domain.vo

import kotlin.jvm.JvmInline

/**
 * VO (Value Object): un correo no es un String cualquiera, así que lo
 * envolvemos para que valide sus propias reglas y no se pueda pasar
 * "cualquier texto" donde se espera un correo.
 */
@JvmInline
value class Email(val value: String) {
    fun isValid(): Boolean = value.contains("@") && value.substringAfter("@").contains(".")
}
