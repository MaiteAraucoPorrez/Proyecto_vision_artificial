package org.ucb.appp1.feature.hub.presentation.screen

import androidx.compose.runtime.Composable
import org.koin.compose.viewmodel.koinViewModel
import org.ucb.appp1.feature.vision.presentation.screen.VisionScreen
import org.ucb.appp1.feature.vision.presentation.viewmodel.VisionViewModel

@Composable
fun FeaturesHubScreen() {
    VisionScreen(viewModel = koinViewModel())
}
