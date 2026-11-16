package org.ucb.appp1.feature.vision.presentation.screen

import androidx.compose.runtime.Composable
import org.ucb.appp1.feature.vision.presentation.viewmodel.VisionViewModel

/**
 * Pantalla de análisis de imagen de galería.
 * Android: selector de galería + Coil + ML Kit. iOS: ver actual en iosMain.
 */
@Composable
expect fun VisionScreen(viewModel: VisionViewModel, onBack: () -> Unit)
