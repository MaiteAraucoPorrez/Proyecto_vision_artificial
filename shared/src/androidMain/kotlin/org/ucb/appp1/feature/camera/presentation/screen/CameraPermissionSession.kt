package org.ucb.appp1.feature.camera.presentation.screen

/**
 * Recuerda mientras viva el proceso (una "sesión" de la app) si ya se mostró el diálogo del
 * permiso de cámara. Así se pregunta UNA vez, justo cuando el usuario abre una función de cámara
 * (no al iniciar la app), y no se vuelve a insistir cada vez que entra a Cámara o Escáner.
 */
internal object CameraPermissionSession {
    @Volatile
    var asked = false
}
