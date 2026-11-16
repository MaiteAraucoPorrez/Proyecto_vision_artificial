package org.ucb.appp1.feature.camera.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import edu.ucb.ds.components.label.AppBody
import edu.ucb.ds.components.label.AppHeading
import edu.ucb.ds.theme.MyProjectTheme
import org.ucb.appp1.core.ui.AppScaffold
import org.ucb.appp1.feature.camera.data.CameraFrameAnalyzer
import org.ucb.appp1.feature.camera.presentation.viewmodel.CameraEffect
import org.ucb.appp1.feature.camera.presentation.viewmodel.CameraEvent
import org.ucb.appp1.feature.camera.presentation.viewmodel.CameraViewModel

@Composable
actual fun CameraScreen(viewModel: CameraViewModel, onBack: () -> Unit) {
    val state by viewModel.state.collectAsState()
    val snackbar = remember { SnackbarHostState() }
    val onFrame by rememberUpdatedState<(org.ucb.appp1.feature.camera.presentation.viewmodel.FrameResult) -> Unit> {
        viewModel.emitEvent(CameraEvent.OnFrameAnalyzed(it))
    }

    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is CameraEffect.ShowMessage -> snackbar.showSnackbar(effect.message)
            }
        }
    }

    AppScaffold(title = "Cámara en tiempo real", onBack = onBack, snackbarHostState = snackbar) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            CameraPermissionGate(
                granted = state.permissionGranted,
                onResult = { viewModel.emitEvent(CameraEvent.OnPermissionResult(it)) }
            ) {
                Column(Modifier.fillMaxSize()) {
                    Box(Modifier.fillMaxWidth().weight(1f)) {
                        CameraPreviewView(
                            createAnalyzer = { CameraFrameAnalyzer { onFrame(it) } },
                            modifier = Modifier.fillMaxSize()
                        )
                        DetectionOverlay(state.frame)
                    }
                    ResultsPanel(
                        labels = state.labels,
                        texts = state.texts,
                        enabled = !state.isSaving,
                        onCapture = { viewModel.emitEvent(CameraEvent.OnSaveClick) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ResultsPanel(labels: List<String>, texts: List<String>, enabled: Boolean, onCapture: () -> Unit) {
    val colors = MyProjectTheme.colors
    Column(
        Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(topStart = PANEL_RADIUS.dp, topEnd = PANEL_RADIUS.dp))
            .background(colors.surface)
            .padding(MyProjectTheme.spacing.large),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(MyProjectTheme.spacing.medium)
    ) {
        Column(
            Modifier.fillMaxWidth().heightIn(max = RESULTS_MAX_HEIGHT.dp).verticalScroll(rememberScrollState())
        ) {
            if (labels.isEmpty() && texts.isEmpty()) {
                AppBody("Apunta la cámara a un objeto o a un texto…", color = colors.onSurface)
            }
            if (labels.isNotEmpty()) {
                AppHeading("Objetos", color = colors.onSurface)
                AppBody(labels.joinToString(), color = colors.onSurface)
            }
            if (texts.isNotEmpty()) {
                AppHeading("Texto", color = colors.onSurface)
                texts.forEach { AppBody("• $it", color = colors.onSurface) }
            }
        }
        ShutterButton(enabled, onCapture)
        AppBody("Toca para guardar en el historial", color = colors.onSurface)
    }
}

/** Botón circular de captura, como el de una cámara. */
@Composable
private fun ShutterButton(enabled: Boolean, onClick: () -> Unit) {
    val colors = MyProjectTheme.colors
    Box(
        Modifier.size(SHUTTER_SIZE.dp)
            .border(SHUTTER_RING.dp, colors.primary, CircleShape)
            .padding(SHUTTER_GAP.dp)
            .clip(CircleShape)
            .background(if (enabled) colors.primary else colors.primary.copy(alpha = DISABLED_ALPHA))
            .clickable(enabled = enabled, role = Role.Button, onClickLabel = "Capturar y guardar", onClick = onClick)
    )
}

private const val PANEL_RADIUS = 24
private const val RESULTS_MAX_HEIGHT = 160
private const val SHUTTER_SIZE = 72
private const val SHUTTER_RING = 3
private const val SHUTTER_GAP = 6
private const val DISABLED_ALPHA = 0.4f
