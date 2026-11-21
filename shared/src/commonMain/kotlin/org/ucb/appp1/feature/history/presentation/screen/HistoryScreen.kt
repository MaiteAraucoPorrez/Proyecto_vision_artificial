package org.ucb.appp1.feature.history.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import edu.ucb.ds.theme.MyProjectTheme
import org.ucb.appp1.core.ui.AppScaffold
import org.ucb.appp1.core.ui.EmptyState
import org.ucb.appp1.core.ui.ErrorState
import org.ucb.appp1.core.ui.GlyphBadge
import org.ucb.appp1.core.ui.cardOutline
import org.ucb.appp1.core.ui.formatTimestamp
import org.ucb.appp1.feature.history.domain.model.AnalysisHistory
import org.ucb.appp1.feature.history.presentation.viewmodel.HistoryEffect
import org.ucb.appp1.feature.history.presentation.viewmodel.HistoryEvent
import org.ucb.appp1.feature.history.presentation.viewmodel.HistoryViewModel

@Composable
fun HistoryScreen(viewModel: HistoryViewModel, onBack: () -> Unit) {
    val state by viewModel.state.collectAsState()
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is HistoryEffect.ShowMessage -> snackbar.showSnackbar(effect.message)
            }
        }
    }

    AppScaffold(
        title = "Historial",
        onBack = onBack,
        snackbarHostState = snackbar,
        actions = {
            if (state.items.isNotEmpty()) {
                TextButton(onClick = { viewModel.emitEvent(HistoryEvent.OnClearAll) }) { Text("Borrar todo") }
            }
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when {
                state.isLoading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                state.error != null -> ErrorState(
                    state.error.orEmpty(),
                    onRetry = null,
                    modifier = Modifier.padding(MyProjectTheme.spacing.large)
                )
                state.items.isEmpty() -> EmptyState(
                    badge = Icons.Outlined.History,
                    title = "Sin análisis todavía",
                    message = "Analiza una imagen o usa la cámara para ver tu historial aquí."
                )
                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(MyProjectTheme.spacing.large),
                    verticalArrangement = Arrangement.spacedBy(MyProjectTheme.spacing.medium)
                ) {
                    items(state.items, key = { it.id }) { item ->
                        HistoryRow(item) { viewModel.emitEvent(HistoryEvent.OnDelete(item.id)) }
                    }
                }
            }
        }
    }
}

private data class SourceInfo(val label: String, val icon: ImageVector)

private fun sourceInfo(source: String): SourceInfo = when (source) {
    "camera" -> SourceInfo("Cámara", Icons.Outlined.CameraAlt)
    "qr" -> SourceInfo("Escáner", Icons.Outlined.QrCodeScanner)
    else -> SourceInfo("Galería", Icons.Outlined.PhotoLibrary)
}

@Composable
private fun HistoryRow(item: AnalysisHistory, onDelete: () -> Unit) {
    val colors = MyProjectTheme.colors
    val dismissState = rememberSwipeToDismissBoxState()
    LaunchedEffect(dismissState.currentValue) {
        if (dismissState.currentValue == SwipeToDismissBoxValue.EndToStart) onDelete()
    }
    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            Box(
                Modifier.fillMaxSize().clip(RoundedCornerShape(ROW_RADIUS.dp)).background(colors.error)
                    .padding(MyProjectTheme.spacing.large),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(Icons.Outlined.Delete, contentDescription = "Eliminar", tint = colors.onError)
            }
        }
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(ROW_RADIUS.dp),
            colors = CardDefaults.cardColors(containerColor = colors.surface, contentColor = colors.onSurface),
            border = cardOutline()
        ) {
            HistoryRowContent(item)
        }
    }
}

@Composable
private fun HistoryRowContent(item: AnalysisHistory) {
    val colors = MyProjectTheme.colors
    val info = sourceInfo(item.source)
    Column(
        Modifier.padding(MyProjectTheme.spacing.large),
        verticalArrangement = Arrangement.spacedBy(MyProjectTheme.spacing.medium)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(MyProjectTheme.spacing.medium)
        ) {
            GlyphBadge(info.icon, size = ROW_BADGE.dp)
            Column(Modifier.weight(1f)) {
                Text(info.label, style = MaterialTheme.typography.titleMedium, color = colors.onSurface)
                Text(
                    formatTimestamp(item.timestamp),
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.onSurface.copy(alpha = ROW_SUBTEXT_ALPHA)
                )
            }
        }
        if (item.labels.isNotEmpty()) {
            Text("Objetos: " + item.labels.joinToString(), style = MaterialTheme.typography.bodyMedium)
        }
        if (item.texts.isNotEmpty()) {
            Text("Texto: " + item.texts.joinToString(" | "), style = MaterialTheme.typography.bodyMedium)
        }
        if (item.labels.isEmpty() && item.texts.isEmpty()) {
            Text("Sin detecciones", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

private const val ROW_RADIUS = 20
private const val ROW_BADGE = 40
private const val ROW_SUBTEXT_ALPHA = 0.7f
