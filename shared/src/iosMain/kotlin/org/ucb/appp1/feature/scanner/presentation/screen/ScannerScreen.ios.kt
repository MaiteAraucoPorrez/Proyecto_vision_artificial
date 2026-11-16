package org.ucb.appp1.feature.scanner.presentation.screen

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.ucb.appp1.core.ui.AppScaffold
import org.ucb.appp1.core.ui.EmptyState
import org.ucb.appp1.feature.scanner.presentation.viewmodel.ScannerViewModel

@Composable
actual fun ScannerScreen(viewModel: ScannerViewModel, onBack: () -> Unit) {
    AppScaffold(title = "Escáner QR", onBack = onBack) { padding ->
        EmptyState(
            badge = "▦",
            title = "Próximamente en iOS",
            message = "El escáner de códigos usa CameraX y solo está disponible en Android.",
            modifier = Modifier.padding(padding)
        )
    }
}
