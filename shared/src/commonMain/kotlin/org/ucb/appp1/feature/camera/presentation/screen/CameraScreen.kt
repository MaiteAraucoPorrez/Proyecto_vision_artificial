package org.ucb.appp1.feature.camera.presentation.screen

import androidx.compose.runtime.Composable
import org.ucb.appp1.feature.camera.presentation.viewmodel.CameraViewModel

/** Android: CameraX + ML Kit en tiempo real. iOS: pantalla informativa. */
@Composable
expect fun CameraScreen(viewModel: CameraViewModel, onBack: () -> Unit)
