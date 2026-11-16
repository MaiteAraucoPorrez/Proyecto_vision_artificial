package org.ucb.appp1.feature.camera.presentation.screen

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import edu.ucb.ds.components.buttons.PrimaryButton
import edu.ucb.ds.theme.MyProjectTheme
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import java.util.concurrent.Executors
import org.ucb.appp1.core.ui.AppScaffold
import org.ucb.appp1.core.ui.EmptyState
import org.ucb.appp1.feature.camera.data.CameraFrameAnalyzer
import org.ucb.appp1.feature.camera.presentation.viewmodel.CameraEffect
import org.ucb.appp1.feature.camera.presentation.viewmodel.CameraEvent
import org.ucb.appp1.feature.camera.presentation.viewmodel.CameraViewModel
import org.ucb.appp1.feature.camera.presentation.viewmodel.FrameResult

@Composable
actual fun CameraScreen(viewModel: CameraViewModel, onBack: () -> Unit) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()
    val snackbar = remember { SnackbarHostState() }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        viewModel.emitEvent(CameraEvent.OnPermissionResult(it))
    }

    LaunchedEffect(Unit) {
        val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
            PackageManager.PERMISSION_GRANTED
        if (granted) {
            viewModel.emitEvent(CameraEvent.OnPermissionResult(true))
        } else {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is CameraEffect.ShowMessage -> snackbar.showSnackbar(effect.message)
            }
        }
    }

    AppScaffold(title = "Cámara en tiempo real", onBack = onBack, snackbarHostState = snackbar) { padding ->
        when (state.permissionGranted) {
            null -> Box(Modifier.fillMaxSize().padding(padding))
            false -> Box(Modifier.fillMaxSize().padding(padding)) {
                EmptyState(
                    title = "Permiso de cámara denegado",
                    message = "Concede el permiso de cámara para analizar en tiempo real."
                )
                PrimaryButton(
                    text = "Conceder permiso",
                    onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                    modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(16.dp)
                )
            }
            true -> Column(Modifier.fillMaxSize().padding(padding)) {
                Box(Modifier.fillMaxWidth().weight(1f)) {
                    CameraPreview(
                        onResult = { viewModel.emitEvent(CameraEvent.OnFrameAnalyzed(it)) },
                        modifier = Modifier.fillMaxSize()
                    )
                    DetectionOverlay(state.frame)
                }
                Column(
                    Modifier.fillMaxWidth().heightIn(max = 220.dp).verticalScroll(rememberScrollState()).padding(12.dp)
                ) {
                    Card(
                        Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MyProjectTheme.colors.surface,
                            contentColor = MyProjectTheme.colors.onSurface
                        )
                    ) {
                        Column(Modifier.padding(12.dp)) {
                            Text(
                                if (state.labels.isEmpty()) "Apunta la cámara a un objeto…"
                                else "Objetos: " + state.labels.joinToString(),
                                style = MaterialTheme.typography.bodyMedium
                            )
                            if (state.texts.isNotEmpty()) {
                                Text(
                                    "Texto: " + state.texts.joinToString(" | "),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                    PrimaryButton(
                        text = "Capturar y guardar en historial",
                        onClick = { viewModel.emitEvent(CameraEvent.OnSaveClick) },
                        enabled = !state.isSaving,
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun CameraPreview(
    onResult: (FrameResult) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val currentOnResult by rememberUpdatedState(onResult)
    val previewView = remember { PreviewView(context) }

    DisposableEffect(lifecycleOwner) {
        val executor = Executors.newSingleThreadExecutor()
        val analyzer = CameraFrameAnalyzer { currentOnResult(it) }
        val providerFuture = ProcessCameraProvider.getInstance(context)
        providerFuture.addListener({
            val provider = providerFuture.get()
            val preview = Preview.Builder().build().also { it.surfaceProvider = previewView.surfaceProvider }
            val analysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also { it.setAnalyzer(executor, analyzer) }
            provider.unbindAll()
            provider.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, analysis)
        }, ContextCompat.getMainExecutor(context))

        onDispose {
            if (providerFuture.isDone) providerFuture.get().unbindAll()
            executor.shutdown()
            analyzer.close()
        }
    }

    AndroidView(factory = { previewView }, modifier = modifier)
}
