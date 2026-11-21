package org.ucb.appp1.core.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import edu.ucb.ds.components.buttons.PrimaryButton
import edu.ucb.ds.components.cards.AppCard
import edu.ucb.ds.components.label.AppBody
import edu.ucb.ds.components.label.AppHeading
import edu.ucb.ds.theme.MyProjectTheme
import kotlin.time.Instant

/** Scaffold común con barra superior, botón volver y host de snackbars. */
@Composable
fun AppScaffold(
    title: String,
    onBack: (() -> Unit)? = null,
    snackbarHostState: SnackbarHostState = SnackbarHostState(),
    actions: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    if (onBack != null) {
                        TextButton(
                            onClick = onBack,
                            modifier = Modifier.semantics { contentDescription = "Volver" }
                        ) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null) }
                    }
                },
                actions = { actions() }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        content = content
    )
}

@Composable
fun EmptyState(title: String, message: String, modifier: Modifier = Modifier, badge: ImageVector? = null) {
    Column(
        modifier = modifier.fillMaxSize().padding(MyProjectTheme.spacing.large),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(MyProjectTheme.spacing.medium, Alignment.CenterVertically)
    ) {
        if (badge != null) GlyphBadge(badge, size = BIG_BADGE.dp)
        AppHeading(title, textAlign = TextAlign.Center)
        AppBody(message, textAlign = TextAlign.Center)
    }
}

@Composable
fun ErrorState(message: String, onRetry: (() -> Unit)?, modifier: Modifier = Modifier) {
    val colors = MyProjectTheme.colors
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = colors.error, contentColor = colors.onError)
    ) {
        Column(
            Modifier.padding(MyProjectTheme.spacing.large),
            verticalArrangement = Arrangement.spacedBy(MyProjectTheme.spacing.medium)
        ) {
            AppBody(message, color = colors.onError)
            if (onRetry != null) PrimaryButton(text = "Reintentar", onClick = onRetry)
        }
    }
}

/** Tarjetas con objetos (labels) y texto (OCR) detectados. */
@Composable
fun DetectionCards(labels: List<String>, texts: List<String>, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(MyProjectTheme.spacing.large)
    ) {
        if (labels.isNotEmpty()) DetectionCard("Objetos detectados (${labels.size})", labels)
        if (texts.isNotEmpty()) DetectionCard("Texto detectado (${texts.size} bloques)", texts)
    }
}

@Composable
private fun DetectionCard(title: String, lines: List<String>) {
    AppCard {
        AppHeading(title, color = MyProjectTheme.colors.onSurface)
        lines.forEach { AppBody("• $it", color = MyProjectTheme.colors.onSurface) }
    }
}

fun formatTimestamp(millis: Long): String =
    Instant.fromEpochMilliseconds(millis).toString().take(ISO_DATE_TIME_LENGTH).replace('T', ' ')

private const val ISO_DATE_TIME_LENGTH = 19
private const val BIG_BADGE = 72
