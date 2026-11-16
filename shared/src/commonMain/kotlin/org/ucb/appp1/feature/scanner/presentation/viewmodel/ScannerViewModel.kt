package org.ucb.appp1.feature.scanner.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.ucb.appp1.feature.camera.presentation.viewmodel.FrameResult
import org.ucb.appp1.feature.history.domain.usecase.SaveAnalysisUseCase

enum class ScanKind(val label: String) {
    Url("Enlace"),
    Wifi("Red WiFi"),
    Text("Texto"),
    Email("Correo"),
    Phone("Teléfono"),
    Sms("SMS"),
    Contact("Contacto"),
    Geo("Ubicación"),
    Product("Producto"),
    Other("Código")
}

/** Código leído: contenido, formato (QR, EAN-13, ...) y tipo de contenido. */
data class ScannedCode(val value: String, val format: String, val kind: ScanKind) {
    /** Solo se ofrece abrir enlaces http/https; otros esquemas (tel:, intent:, ...) no se abren. */
    val isOpenableUrl: Boolean
        get() = kind == ScanKind.Url && (value.startsWith("http://") || value.startsWith("https://"))
}

data class ScannerState(
    val permissionGranted: Boolean? = null,
    val code: ScannedCode? = null,
    val frame: FrameResult = FrameResult()
)

sealed interface ScannerEvent {
    data class OnPermissionResult(val granted: Boolean) : ScannerEvent
    data class OnCodesDetected(val codes: List<ScannedCode>, val frame: FrameResult) : ScannerEvent
    data object OnClear : ScannerEvent
}

sealed interface ScannerEffect {
    data class ShowMessage(val message: String) : ScannerEffect
}

class ScannerViewModel(
    private val saveAnalysis: SaveAnalysisUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(ScannerState())
    val state = _state.asStateFlow()

    private val _effect = MutableSharedFlow<ScannerEffect>()
    val effect = _effect.asSharedFlow()

    private var lastSavedValue: String? = null

    fun emitEvent(event: ScannerEvent) {
        when (event) {
            is ScannerEvent.OnPermissionResult -> _state.update { it.copy(permissionGranted = event.granted) }
            is ScannerEvent.OnCodesDetected -> onCodes(event)
            ScannerEvent.OnClear -> {
                lastSavedValue = null
                _state.update { it.copy(code = null, frame = FrameResult()) }
            }
        }
    }

    private fun onCodes(event: ScannerEvent.OnCodesDetected) {
        val first = event.codes.firstOrNull()
        // Sin código en este frame: se conserva el último leído, solo se limpian los recuadros.
        _state.update { it.copy(code = first ?: it.code, frame = event.frame) }
        if (first != null && first.value != lastSavedValue) {
            lastSavedValue = first.value
            viewModelScope.launch {
                saveAnalysis("qr", listOf("${first.kind.label} · ${first.format}"), listOf(first.value))
                    .onSuccess { _effect.emit(ScannerEffect.ShowMessage("Código guardado en el historial")) }
            }
        }
    }
}
