@file:JvmName("VisionScreenAndroid")

package org.ucb.appp1.feature.vision.presentation.screen

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.PhotoLibrary
import edu.ucb.ds.components.buttons.PrimaryButton
import edu.ucb.ds.components.buttons.SecondaryButton
import edu.ucb.ds.components.cards.AppCard
import edu.ucb.ds.components.label.AppBody
import edu.ucb.ds.components.label.AppHeading
import edu.ucb.ds.theme.MyProjectTheme
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import org.ucb.appp1.core.ui.AppScaffold
import org.ucb.appp1.core.ui.DetectionCards
import org.ucb.appp1.core.ui.EmptyState
import org.ucb.appp1.core.ui.ErrorState
import org.ucb.appp1.feature.vision.presentation.viewmodel.VisionEffect
import org.ucb.appp1.feature.vision.presentation.viewmodel.VisionEvent
import org.ucb.appp1.feature.vision.presentation.viewmodel.VisionViewModel

@Composable
actual fun VisionScreen(viewModel: VisionViewModel, onBack: () -> Unit) {
    val state by viewModel.state.collectAsState()

    val pickImage = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { viewModel.emitEvent(VisionEvent.OnImageSelected(it.toString())) }
    }

    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                VisionEffect.OpenImagePicker -> pickImage.launch("image/*")
            }
        }
    }

    AppScaffold(title = "Imagen de galería", onBack = onBack) { padding ->
        val imageUri = state.selectedImageUri
        if (imageUri == null) {
            Box(Modifier.fillMaxSize().padding(padding)) {
                EmptyState(
                    badge = Icons.Outlined.PhotoLibrary,
                    title = "Ninguna imagen seleccionada",
                    message = "Elige una foto de tu galería para detectar objetos y texto."
                )
                PrimaryButton(
                    text = "Seleccionar imagen de galería",
                    onClick = { viewModel.emitEvent(VisionEvent.OnSelectImageClick) },
                    modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(16.dp)
                )
            }
        } else {
            Column(
                modifier = Modifier.padding(padding).padding(16.dp).verticalScroll(rememberScrollState())
            ) {
                ImagePreview(imageUri, canClear = !state.isLoading) {
                    viewModel.emitEvent(VisionEvent.OnClearResult)
                }
                Spacer(Modifier.height(16.dp))
                Row2(
                    isLoading = state.isLoading,
                    onAnalyze = { viewModel.emitEvent(VisionEvent.OnAnalyzeClick) },
                    onPick = { viewModel.emitEvent(VisionEvent.OnSelectImageClick) }
                )
                Spacer(Modifier.height(16.dp))
                state.error?.let { error ->
                    ErrorState(error, onRetry = { viewModel.emitEvent(VisionEvent.OnAnalyzeClick) })
                    Spacer(Modifier.height(16.dp))
                }
                state.result?.let { result ->
                    result.quality?.let {
                        QualityCard(it)
                        Spacer(Modifier.height(16.dp))
                    }
                    if (result.labels.isEmpty() && result.texts.isEmpty()) {
                        Text(
                            if (result.quality?.isGood == false) {
                                "No se detectó nada. Revisa el aviso de calidad: otra foto puede cambiar el resultado."
                            } else {
                                "No se detectó nada en esta imagen."
                            },
                            style = MaterialTheme.typography.bodyMedium
                        )
                    } else {
                        DetectionCards(result.labels, result.texts)
                    }
                }
                Spacer(Modifier.height(16.dp))
                AiSection(
                    isDescribing = state.isDescribing,
                    description = state.aiDescription,
                    error = state.aiError,
                    onDescribe = { viewModel.emitEvent(VisionEvent.OnDescribeClick) }
                )
            }
        }
    }
}

@Composable
private fun ImagePreview(imageUri: String, canClear: Boolean, onClear: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxWidth().height(300.dp).clip(RoundedCornerShape(16.dp))
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current).data(imageUri).build(),
            contentDescription = "Imagen seleccionada para analizar",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
            placeholder = ColorPainter(MyProjectTheme.colors.surface),
            error = ColorPainter(MyProjectTheme.colors.error)
        )
        if (canClear) {
            IconButton(onClick = onClear, modifier = Modifier.align(Alignment.TopEnd)) {
                Icon(Icons.Filled.Close, contentDescription = "Quitar imagen", tint = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
private fun Row2(isLoading: Boolean, onAnalyze: () -> Unit, onPick: () -> Unit) {
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(MyProjectTheme.spacing.medium)) {
        PrimaryButton(
            text = if (isLoading) "Analizando..." else "Analizar imagen",
            onClick = onAnalyze,
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth()
        )
        if (isLoading) CircularProgressIndicator(Modifier.size(24.dp).align(Alignment.CenterHorizontally))
        SecondaryButton("Elegir otra imagen", onPick, Modifier.fillMaxWidth(), enabled = !isLoading)
    }
}

/** Descripción opcional con IA generativa (Gemini). Requiere internet y una API key. */
@Composable
private fun AiSection(isDescribing: Boolean, description: String?, error: String?, onDescribe: () -> Unit) {
    val colors = MyProjectTheme.colors
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(MyProjectTheme.spacing.medium)) {
        SecondaryButton(
            text = if (isDescribing) "Consultando a Gemini..." else "Describir con IA (Gemini)",
            onClick = onDescribe,
            enabled = !isDescribing,
            modifier = Modifier.fillMaxWidth()
        )
        if (isDescribing) CircularProgressIndicator(Modifier.size(24.dp).align(Alignment.CenterHorizontally))
        error?.let { ErrorState(it, onRetry = onDescribe) }
        description?.let {
            AppCard {
                AppHeading("Descripción con IA", color = colors.onSurface)
                AppBody(it, color = colors.onSurface)
                AppBody("Generado por Gemini (IA generativa, en la nube).", color = colors.onSurface)
            }
        }
    }
}
