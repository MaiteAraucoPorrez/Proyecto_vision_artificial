package org.ucb.appp1.feature.history.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.unit.dp
import org.ucb.appp1.core.ui.AppScaffold
import org.ucb.appp1.core.ui.EmptyState
import org.ucb.appp1.core.ui.ErrorState
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
                    modifier = Modifier.padding(16.dp)
                )
                state.items.isEmpty() -> EmptyState(
                    badge = "🕘",
                    title = "Sin análisis todavía",
                    message = "Analiza una imagen o usa la cámara para ver tu historial aquí."
                )
                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.items, key = { it.id }) { item ->
                        HistoryRow(item) { viewModel.emitEvent(HistoryEvent.OnDelete(item.id)) }
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryRow(item: AnalysisHistory, onDelete: () -> Unit) {
    val dismissState = rememberSwipeToDismissBoxState()
    LaunchedEffect(dismissState.currentValue) {
        if (dismissState.currentValue == SwipeToDismissBoxValue.EndToStart) onDelete()
    }
    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            Box(Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.CenterEnd) {
                Text("Eliminar", color = MaterialTheme.colorScheme.error)
            }
        }
    ) {
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        when (item.source) {
                            "camera" -> "Cámara"
                            "qr" -> "Escáner"
                            else -> "Galería"
                        },
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(formatTimestamp(item.timestamp), style = MaterialTheme.typography.labelMedium)
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
    }
}
