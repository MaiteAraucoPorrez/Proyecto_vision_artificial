package org.ucb.appp1.core.util

import kotlin.time.Clock

/** Hora actual en milisegundos (Unix epoch). Funciona en Android e iOS. */
fun currentTimeMillis(): Long = Clock.System.now().toEpochMilliseconds()
