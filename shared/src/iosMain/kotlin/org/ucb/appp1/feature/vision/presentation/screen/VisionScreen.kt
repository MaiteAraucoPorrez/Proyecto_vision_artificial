package org.ucb.appp1.feature.vision.presentation.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.ucb.appp1.core.ui.AppScaffold
import org.ucb.appp1.core.ui.DetectionCards
import org.ucb.appp1.core.ui.ErrorState
import org.ucb.appp1.feature.vision.presentation.viewmodel.VisionEvent
import org.ucb.appp1.feature.vision.presentation.viewmodel.VisionViewModel

/** iOS: selector de galería nativo pendiente de integrar; el análisis usa Apple Vision. */
@Composable
actual fun VisionScreen(viewModel: VisionViewModel, onBack: () -> Unit) {
    val state by viewModel.state.collectAsState()

    AppScaffold(title = "Imagen de galería", onBack = onBack) { padding ->
        Column(Modifier.padding(padding).padding(16.dp)) {
            Button(
                onClick = { viewModel.emitEvent(VisionEvent.OnSelectImageClick) },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Seleccionar imagen de galería") }

            if (state.selectedImageUri != null) {
                Button(
                    onClick = { viewModel.emitEvent(VisionEvent.OnAnalyzeClick) },
                    enabled = !state.isLoading,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                ) { Text("Analizar imagen") }
            }
            if (state.isLoading) CircularProgressIndicator(Modifier.padding(top = 16.dp))
            state.error?.let {
                ErrorState(it, onRetry = { viewModel.emitEvent(VisionEvent.OnAnalyzeClick) })
            }
            state.result?.let { DetectionCards(it.labels, it.texts, Modifier.padding(top = 16.dp)) }
        }
    }
}
