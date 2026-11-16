package org.ucb.appp1.feature.camera.presentation.screen

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.ucb.appp1.core.ui.AppScaffold
import org.ucb.appp1.core.ui.EmptyState
import org.ucb.appp1.feature.camera.presentation.viewmodel.CameraViewModel

@Composable
actual fun CameraScreen(viewModel: CameraViewModel, onBack: () -> Unit) {
    AppScaffold(title = "Cámara", onBack = onBack) { padding ->
        EmptyState(
            title = "Próximamente en iOS",
            message = "El análisis en tiempo real usa CameraX y solo está disponible en Android.",
            modifier = Modifier.padding(padding)
        )
    }
}
