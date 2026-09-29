package com.iglesiaflow.gestion.ui.sync

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iglesiaflow.gestion.core.config.AppSettings
import com.iglesiaflow.gestion.core.config.SettingsRepository
import com.iglesiaflow.gestion.data.remote.RealtimeStatus
import com.iglesiaflow.gestion.data.remote.SyncManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SyncUiState(
    val status: RealtimeStatus = RealtimeStatus(),
    val pending: Int = 0,
    val settings: AppSettings = AppSettings(),
    val diagnostics: List<String> = emptyList(),
    val diagnosing: Boolean = false
) {
    val label: String
        get() = when {
            !status.enabled -> "Sincronización desactivada"
            !status.available -> "Falta google-services.json"
            status.error != null -> friendlyError(status.error)
            status.syncing -> "Sincronizando…"
            pending > 0 -> "$pending cambios pendientes"
            status.connected -> "Al día"
            else -> "Sin conexión"
        }
}

/** Traduce los errores técnicos de Firebase a instrucciones concretas. */
internal fun friendlyError(raw: String?): String {
    val text = raw.orEmpty()
    return when {
        text.contains("Permission denied", ignoreCase = true) ->
            "Firebase rechaza los datos: publica las reglas y activa Authentication → Anónimo"
        text.contains("CONFIGURATION_NOT_FOUND", ignoreCase = true) ->
            "Falta activar Authentication en la consola de Firebase (botón Comenzar)"
        text.contains("Can't determine Firebase Database URL", ignoreCase = true) ->
            "Falta la dirección de la base de datos: pégala más abajo"
        text.isBlank() -> "Error desconocido de sincronización"
        else -> "Error: $text"
    }
}

@HiltViewModel
class SyncStatusViewModel @Inject constructor(
    private val syncManager: SyncManager,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val diagnostics = MutableStateFlow<List<String>>(emptyList())
    private val diagnosing = MutableStateFlow(false)

    val uiState: StateFlow<SyncUiState> = combine(
        syncManager.status,
        syncManager.pendingCount,
        settingsRepository.settings,
        diagnostics,
        diagnosing
    ) { status, pending, settings, lines, running ->
        SyncUiState(
            status = status,
            pending = pending,
            settings = settings,
            diagnostics = lines,
            diagnosing = running
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SyncUiState())

    /** Prueba paso a paso la conexión con Firebase y explica qué falla. */
    fun runDiagnostics() {
        if (diagnosing.value) return
        viewModelScope.launch {
            diagnosing.value = true
            diagnostics.value = listOf("Probando la conexión…")
            diagnostics.value = runCatching { syncManager.diagnose() }
                .getOrElse { listOf("❌ Error inesperado: ${it.message}") }
            diagnosing.value = false
        }
    }

    fun syncNow() = syncManager.requestSync()

    fun setEnabled(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.update { it.copy(cloudSyncEnabled = enabled) } }
    }

    fun setChurchId(code: String) {
        val normalized = code.trim().lowercase().replace(Regex("[^a-z0-9_-]"), "-")
        viewModelScope.launch {
            settingsRepository.update { it.copy(cloudChurchId = normalized.ifBlank { "principal" }) }
        }
    }

    /** URL manual de la Realtime Database (por si google-services.json no la trae). */
    fun setDatabaseUrl(url: String) {
        viewModelScope.launch {
            settingsRepository.update { it.copy(cloudDatabaseUrl = url.trim()) }
        }
    }
}
