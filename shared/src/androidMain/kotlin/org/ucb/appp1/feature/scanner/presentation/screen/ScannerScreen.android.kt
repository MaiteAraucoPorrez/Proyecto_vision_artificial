package org.ucb.appp1.feature.scanner.presentation.screen

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import edu.ucb.ds.components.buttons.PrimaryButton
import edu.ucb.ds.components.buttons.SecondaryButton
import edu.ucb.ds.components.label.AppBody
import edu.ucb.ds.components.label.AppHeading
import edu.ucb.ds.theme.MyProjectTheme
import kotlinx.coroutines.launch
import org.ucb.appp1.core.ui.AppScaffold
import org.ucb.appp1.core.ui.GlyphBadge
import org.ucb.appp1.feature.camera.presentation.screen.CameraPermissionGate
import org.ucb.appp1.feature.camera.presentation.screen.CameraPreviewView
import org.ucb.appp1.feature.camera.presentation.screen.DetectionOverlay
import org.ucb.appp1.feature.camera.presentation.screen.SCANNER_ANALYSIS_HEIGHT
import org.ucb.appp1.feature.camera.presentation.screen.SCANNER_ANALYSIS_WIDTH
import org.ucb.appp1.feature.camera.presentation.viewmodel.FrameResult
import org.ucb.appp1.feature.scanner.data.BarcodeFrameAnalyzer
import org.ucb.appp1.feature.scanner.data.BarcodeImageScanner
import org.ucb.appp1.feature.scanner.presentation.viewmodel.ScanKind
import org.ucb.appp1.feature.scanner.presentation.viewmodel.ScannedCode
import org.ucb.appp1.feature.scanner.presentation.viewmodel.ScannerEffect
import org.ucb.appp1.feature.scanner.presentation.viewmodel.ScannerEvent
import org.ucb.appp1.feature.scanner.presentation.viewmodel.ScannerViewModel

@Composable
actual fun ScannerScreen(viewModel: ScannerViewModel, onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val state by viewModel.state.collectAsState()
    val snackbar = remember { SnackbarHostState() }
    val imageScanner = remember { BarcodeImageScanner(context) }
    val onCodes by rememberUpdatedState<(List<ScannedCode>, FrameResult) -> Unit> { codes, frame ->
        viewModel.emitEvent(ScannerEvent.OnCodesDetected(codes, frame))
    }

    // Escaneo desde una foto de la galería (no necesita permiso de cámara).
    val pickImage = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                imageScanner.scan(uri)
                    .onSuccess { codes ->
                        if (codes.isEmpty()) {
                            snackbar.showSnackbar("No se encontró ningún código en la imagen")
                        } else {
                            viewModel.emitEvent(ScannerEvent.OnCodesDetected(codes, FrameResult()))
                        }
                    }
                    .onFailure { snackbar.showSnackbar("No se pudo leer la imagen") }
            }
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is ScannerEffect.ShowMessage -> snackbar.showSnackbar(effect.message)
            }
        }
    }

    AppScaffold(title = "Escáner QR y códigos", onBack = onBack, snackbarHostState = snackbar) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            Box(Modifier.fillMaxWidth().weight(1f)) {
                CameraPermissionGate(
                    granted = state.permissionGranted,
                    onResult = { viewModel.emitEvent(ScannerEvent.OnPermissionResult(it)) }
                ) {
                    CameraPreviewView(
                        createAnalyzer = { BarcodeFrameAnalyzer { codes, frame -> onCodes(codes, frame) } },
                        modifier = Modifier.fillMaxSize(),
                        analysisWidth = SCANNER_ANALYSIS_WIDTH,
                        analysisHeight = SCANNER_ANALYSIS_HEIGHT
                    )
                    DetectionOverlay(state.frame)
                }
            }
            CodePanel(
                code = state.code,
                onClear = { viewModel.emitEvent(ScannerEvent.OnClear) },
                onGallery = { pickImage.launch("image/*") }
            )
        }
    }
}

@Composable
private fun CodePanel(code: ScannedCode?, onClear: () -> Unit, onGallery: () -> Unit) {
    val colors = MyProjectTheme.colors
    val context = LocalContext.current
    Column(
        Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(topStart = PANEL_RADIUS.dp, topEnd = PANEL_RADIUS.dp))
            .background(colors.surface)
            .padding(MyProjectTheme.spacing.large),
        verticalArrangement = Arrangement.spacedBy(MyProjectTheme.spacing.medium)
    ) {
        if (code == null) {
            AppBody("Apunta la cámara a un código o escanéalo desde una foto.", color = colors.onSurface)
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(MyProjectTheme.spacing.large)
            ) {
                GlyphBadge(if (code.kind == ScanKind.Payment) Icons.Outlined.Payments else Icons.Outlined.QrCodeScanner)
                Column {
                    AppHeading(code.kind.label, color = colors.onSurface)
                    AppBody(code.format, color = colors.onSurface)
                }
            }
            PaymentDetails(code, colors.onSurface)
            Row(horizontalArrangement = Arrangement.spacedBy(MyProjectTheme.spacing.medium)) {
                if (code.isOpenableUrl) {
                    PrimaryButton("Abrir enlace", { openUrl(context, code.value) }, Modifier.weight(1f))
                }
                SecondaryButton("Copiar", { copy(context, code.value) }, Modifier.weight(1f))
                SecondaryButton("Nuevo", onClear, Modifier.weight(1f))
            }
        }
        SecondaryButton("Escanear desde galería", onGallery, Modifier.fillMaxWidth())
    }
}

private fun openUrl(context: Context, url: String) {
    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
}

private fun copy(context: Context, value: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText("Código", value))
}

/** Para pagos EMV muestra comercio, ciudad y monto; para el resto, el contenido tal cual. */
@Composable
private fun PaymentDetails(code: ScannedCode, color: Color) {
    val payment = code.payment
    if (payment == null) {
        AppBody(code.value, color = color)
        return
    }
    payment.merchant?.let { AppBody("Comercio: $it", color = color) }
    payment.city?.let { AppBody("Ciudad: $it", color = color) }
    payment.amount?.let { AppBody("Monto: $it ${payment.currency.orEmpty()}", color = color) }
    AppBody("Esta app solo lee el código; no realiza pagos.", color = color)
}

private const val PANEL_RADIUS = 24
