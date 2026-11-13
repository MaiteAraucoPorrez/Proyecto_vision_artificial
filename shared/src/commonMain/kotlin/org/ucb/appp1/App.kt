package org.ucb.appp1

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import org.koin.compose.viewmodel.koinViewModel
import org.ucb.appp1.feature.vision.presentation.screen.VisionScreen
import org.ucb.appp1.feature.vision.presentation.viewmodel.VisionViewModel

@Composable
fun App() {
    MaterialTheme {
        VisionScreen(viewModel = koinViewModel())
    }
}
