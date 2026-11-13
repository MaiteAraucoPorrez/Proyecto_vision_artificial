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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.koin.compose.viewmodel.koinViewModel
import org.ucb.appp1.feature.vision.presentation.viewmodel.VisionEvent
import org.ucb.appp1.feature.vision.presentation.viewmodel.VisionViewModel

/**
 * Versión común (stub) de VisionScreen para plataformas sin implementación nativa.
 * La implementación completa con galería, Coil y ML Kit está en androidMain.
 */
@Composable
expect fun VisionScreen(viewModel: VisionViewModel)

@Composable
private fun ResultSection(result: org.ucb.appp1.feature.vision.domain.model.VisionResult) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (result.labels.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Objetos detectados (${result.labels.size})",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    result.labels.forEach { label ->
                        Text("• $label", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        if (result.texts.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Texto detectado (${result.texts.size} bloques)",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    result.texts.forEach { text ->
                        Text("• $text", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}
