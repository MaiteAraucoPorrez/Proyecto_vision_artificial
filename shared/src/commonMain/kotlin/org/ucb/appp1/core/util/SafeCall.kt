package org.ucb.appp1.core.util

import kotlin.coroutines.cancellation.CancellationException

/**
 * Ejecuta [block] y convierte cualquier error en Result.failure.
 * CancellationException se relanza para no romper la cancelación de corrutinas.
 *
 * Se atrapa Exception a propósito: Firebase, Ktor y gRPC lanzan excepciones de muchos tipos
 * y aquí es el límite donde se convierten en Result (por eso el @Suppress, solo en este lugar).
 * Es inline, así que dentro de [block] se pueden llamar funciones suspend.
 */
@Suppress("TooGenericExceptionCaught")
inline fun <T> safeCall(block: () -> T): Result<T> =
    try {
        Result.success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.failure(e)
    }
