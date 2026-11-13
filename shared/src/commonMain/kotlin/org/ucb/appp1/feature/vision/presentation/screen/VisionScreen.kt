package org.ucb.appp1.feature.vision.presentation.screen

import android.content.ContentResolver
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.blendMode
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.transform.RoundedCornersTransformation
import org.koin.compose.viewmodel.koinViewModel
import org.ucb.appp1.feature.vision.presentation.viewmodel.VisionEffect
import org.ucb.appp1.feature.vision.presentation.viewmodel.VisionEvent
import org.ucb.appp1.feature.vision.presentation.viewmodel.VisionViewModel

@Composable
fun VisionScreen(
    viewModel: VisionViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()

    // Gallery picker launcher
    val pickImage = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { viewModel.emitEvent(VisionEvent.OnImageSelected(it.toString())) }
    }

    // Collect one-shot effects (navigation, toasts, etc.)
    LaunchedEffect(viewModel.effect) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is VisionEffect.OpenImagePicker -> pickImage.launch("image/*")
                is VisionEffect.ShowError -> {
                    // TODO: Show toast/snackbar - using effect for now
                    // In a real app, you'd use a SnackbarHost or Toast
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        // Header
        Text(
            text = "Visión Artificial",
            style = MaterialTheme.typography.headlineMedium
        )
        Text(
            text = "Procesamiento de Imágenes con ML Kit",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Image preview section
        state.selectedImageUri?.let { imageUri ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .clip(RoundedCornerShape(16.dp))
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(imageUri)
                        .crossfade(true)
                        .transformations(RoundedCornersTransformation(16.dp))
                        .build(),
                    contentDescription = "Imagen seleccionada",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                    placeholder = {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    },
                    error = {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Image,
                                contentDescription = "Error cargando imagen",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                )

                // Close button to clear selection
                if (!state.isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp),
                        contentAlignment = Alignment.TopEnd
                    ) {
                        IconButton(
                            onClick = { viewModel.emitEvent(VisionEvent.OnClearResult) }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Limpiar selección",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Analyze button
            Button(
                onClick = { viewModel.emitEvent(VisionEvent.OnAnalyzeClick) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isLoading
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Analizando...")
                } else {
                    Text("Analizar Imagen", fontSize = 16.sp)
                }
            }
        }

        // No image selected - show pick button
        if (state.selectedImageUri == null && !state.isLoading) {
            Button(
                onClick = { viewModel.emitEvent(VisionEvent.OnSelectImageClick) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Seleccionar Imagen de Galería", fontSize = 16.sp)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Loading indicator (when analyzing without preview visible)
        if (state.isLoading && state.selectedImageUri == null) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text("Analizando imagen...", style = MaterialTheme.typography.bodyMedium)
        }

        // Error message
        state.error?.let { error ->
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Results
        state.result?.let { result ->
            ResultSection(result = result)
        }
    }
}

@Composable
private fun ResultSection(result: org.ucb.appp1.feature.vision.domain.model.VisionResult) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Labels
        if (result.labels.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = 4.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Objetos detectados (${result.labels.size})",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    result.labels.forEach { label ->
                        Text("• $label", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Texts (OCR)
        if (result.texts.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = 4.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Texto detectado (${result.texts.size} bloques)",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    result.texts.forEach { text ->
                        Text("• $text", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}