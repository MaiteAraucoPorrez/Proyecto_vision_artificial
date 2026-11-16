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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import edu.ucb.ds.components.buttons.PrimaryButton
import java.util.concurrent.Executors
import org.ucb.appp1.core.ui.EmptyState
import org.ucb.appp1.feature.camera.data.ClosableAnalyzer

/**
 * Pide el permiso de cámara y muestra [content] solo cuando está concedido.
 * [granted] es null mientras se consulta, true/false después.
 */
@Composable
fun CameraPermissionGate(
    granted: Boolean?,
    onResult: (Boolean) -> Unit,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { onResult(it) }

    LaunchedEffect(Unit) {
        val has = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
            PackageManager.PERMISSION_GRANTED
        if (has) onResult(true) else launcher.launch(Manifest.permission.CAMERA)
    }

    when (granted) {
        null -> Box(Modifier.fillMaxSize())
        true -> content()
        false -> Box(Modifier.fillMaxSize()) {
            EmptyState(
                badge = "📷",
                title = "Permiso de cámara denegado",
                message = "Concede el permiso de cámara para usar esta función."
            )
            PrimaryButton(
                text = "Conceder permiso",
                onClick = { launcher.launch(Manifest.permission.CAMERA) },
                modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(16.dp)
            )
        }
    }
}

/**
 * Vista previa de CameraX con un analizador de frames. El analizador se crea con [createAnalyzer]
 * y se cierra (junto con el hilo) al salir de la pantalla.
 */
@Composable
fun CameraPreviewView(
    createAnalyzer: () -> ClosableAnalyzer,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember { PreviewView(context) }

    DisposableEffect(lifecycleOwner) {
        val executor = Executors.newSingleThreadExecutor()
        val analyzer = createAnalyzer()
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
            // Primero se deja de enviar frames y luego se cierran los detectores.
            if (providerFuture.isDone) providerFuture.get().unbindAll()
            executor.shutdown()
            analyzer.close()
        }
    }

    AndroidView(factory = { previewView }, modifier = modifier)
}
