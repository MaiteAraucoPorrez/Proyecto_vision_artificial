package org.ucb.appp1.feature.camera.presentation.screen

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.util.Size
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.LocalLifecycleOwner
import edu.ucb.ds.components.buttons.PrimaryButton
import java.util.concurrent.Executors
import org.ucb.appp1.core.ui.EmptyState
import org.ucb.appp1.core.util.safeCall
import org.ucb.appp1.feature.camera.data.ClosableAnalyzer

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

/**
 * Gestiona el permiso de cámara y muestra [content] solo cuando está concedido.
 *
 * - Ya concedido: no pregunta nada.
 * - Primera vez en la sesión: muestra el diálogo del sistema.
 * - Denegado antes: NO vuelve a abrir el diálogo solo; muestra una pantalla con un botón
 *   "Conceder permiso" y, si el usuario marcó "no volver a preguntar", un botón "Abrir ajustes".
 * - Al volver de Ajustes se vuelve a comprobar solo.
 *
 * [granted] es null mientras se consulta, true/false después.
 */
@Composable
fun CameraPermissionGate(
    granted: Boolean?,
    onResult: (Boolean) -> Unit,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }
    var blockedForever by remember { mutableStateOf(false) }

    fun hasPermission() =
        ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED

    fun isBlockedForever() =
        activity != null && !ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.CAMERA)

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { ok ->
        onResult(ok)
        if (!ok) blockedForever = isBlockedForever()
    }

    LaunchedEffect(Unit) {
        when {
            hasPermission() -> onResult(true)
            !CameraPermissionSession.asked -> {
                CameraPermissionSession.asked = true
                launcher.launch(Manifest.permission.CAMERA)
            }
            else -> {
                blockedForever = isBlockedForever()
                onResult(false)
            }
        }
    }

    // Al volver de Ajustes (o del diálogo) se comprueba de nuevo.
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        if (hasPermission()) onResult(true)
    }

    when (granted) {
        null -> Box(Modifier.fillMaxSize())
        true -> content()
        false -> PermissionDenied(blockedForever) {
            if (blockedForever) openAppSettings(context) else launcher.launch(Manifest.permission.CAMERA)
        }
    }
}

@Composable
private fun PermissionDenied(blockedForever: Boolean, onAction: () -> Unit) {
    Box(Modifier.fillMaxSize()) {
        EmptyState(
            badge = "📷",
            title = "Permiso de cámara necesario",
            message = if (blockedForever) {
                "Activa el permiso de cámara en los ajustes de la app para usar esta función."
            } else {
                "Necesitamos la cámara para analizar lo que apuntas."
            }
        )
        PrimaryButton(
            text = if (blockedForever) "Abrir ajustes" else "Conceder permiso",
            onClick = onAction,
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(16.dp)
        )
    }
}

private fun openAppSettings(context: Context) {
    val uri = Uri.fromParts("package", context.packageName, null)
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, uri).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(intent)
}

private const val ANALYSIS_WIDTH = 640
private const val ANALYSIS_HEIGHT = 480

/**
 * Vista previa de CameraX con un analizador de frames. El analizador se crea con [createAnalyzer]
 * y se cierra (junto con el hilo) al salir de la pantalla.
 *
 * Medidas de estabilidad: resolución de análisis reducida, el analizador se desconecta antes de
 * cerrarse, y los errores de la cámara (p. ej. sin cámara disponible) no cierran la app.
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
        var disposed = false
        var analysisUseCase: ImageAnalysis? = null
        val executor = Executors.newSingleThreadExecutor()
        val analyzer = createAnalyzer()
        val providerFuture = ProcessCameraProvider.getInstance(context)

        providerFuture.addListener({
            if (disposed) return@addListener
            safeCall {
                val provider = providerFuture.get()
                val preview = Preview.Builder().build().also { it.surfaceProvider = previewView.surfaceProvider }
                val resolution = ResolutionSelector.Builder()
                    .setResolutionStrategy(
                        ResolutionStrategy(
                            Size(ANALYSIS_WIDTH, ANALYSIS_HEIGHT),
                            ResolutionStrategy.FALLBACK_RULE_CLOSEST_HIGHER_THEN_LOWER
                        )
                    )
                    .build()
                val analysis = ImageAnalysis.Builder()
                    .setResolutionSelector(resolution)
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also { it.setAnalyzer(executor, analyzer) }
                analysisUseCase = analysis
                provider.unbindAll()
                provider.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, analysis)
            }
        }, ContextCompat.getMainExecutor(context))

        onDispose {
            disposed = true
            // Orden: dejar de enviar frames, soltar la cámara, cerrar los detectores y el hilo.
            analysisUseCase?.clearAnalyzer()
            safeCall { if (providerFuture.isDone) providerFuture.get().unbindAll() }
            analyzer.close()
            executor.shutdown()
        }
    }

    AndroidView(factory = { previewView }, modifier = modifier)
}
