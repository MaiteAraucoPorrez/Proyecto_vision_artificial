package org.ucb.appp1.feature.scanner.presentation.screen

import androidx.compose.runtime.Composable
import org.ucb.appp1.feature.scanner.presentation.viewmodel.ScannerViewModel

/** Android: CameraX + ML Kit Barcode Scanning. iOS: pantalla informativa. */
@Composable
expect fun ScannerScreen(viewModel: ScannerViewModel, onBack: () -> Unit)
