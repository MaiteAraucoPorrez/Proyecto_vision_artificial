package org.ucb.appp1.feature.vision.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel
import org.ucb.appp1.feature.vision.presentation.viewmodel.VisionEvent
import org.ucb.appp1.feature.vision.presentation.viewmodel.VisionViewModel

@Composable
fun VisionScreen(
    viewModel: VisionViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = "Visión Artificial",
            style = MaterialTheme.typography.headlineMedium
        )
        Text(
            text = "Procesamiento de Imágenes con ML Kit",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { viewModel.emitEvent(VisionEvent.OnSelectImageClick) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Seleccionar Imagen de Galería")
        }

        state.selectedImageUri?.let {
            Spacer(modifier = Modifier.height(16.dp))
            Text("Imagen seleccionada ✓", color = MaterialTheme.colorScheme.primary)

            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = { viewModel.emitEvent(VisionEvent.OnAnalyzeClick) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Analizar Imagen")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (state.isLoading) {
            CircularProgressIndicator()
        }

        state.error?.let { error ->
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        state.result?.let { result ->
            Text(
                text = "Objetos detectados:",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            result.labels.forEach { label ->
                Text("• $label")
            }

            if (result.texts.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Texto detectado:",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                result.texts.forEach { text ->
                    Text("• $text")
                }
            }
        }
    }
}
