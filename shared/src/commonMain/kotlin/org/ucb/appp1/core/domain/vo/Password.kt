package org.ucb.appp1.core.domain.vo

import kotlin.jvm.JvmInline

@JvmInline
value class Password(val value: String) {
    fun isValid(): Boolean = value.length >= MIN_LENGTH

    companion object {
        const val MIN_LENGTH = 6
    }
}
