package com.iglesiaflow.gestion.ui.sync

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iglesiaflow.gestion.core.config.AppSettings
import com.iglesiaflow.gestion.core.config.SettingsRepository
import com.iglesiaflow.gestion.data.remote.RealtimeStatus
import com.iglesiaflow.gestion.data.remote.SyncManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SyncUiState(
    val status: RealtimeStatus = RealtimeStatus(),
    val pending: Int = 0,
    val settings: AppSettings = AppSettings()
) {
    val label: String
        get() = when {
            !status.enabled -> "Sincronización desactivada"
            !status.available -> "Falta google-services.json"
            status.error != null -> "Error: ${status.error}"
            status.syncing -> "Sincronizando…"
            pending > 0 -> "$pending cambios pendientes"
            status.connected -> "Al día"
            else -> "Sin conexión"
        }
}

@HiltViewModel
class SyncStatusViewModel @Inject constructor(
    private val syncManager: SyncManager,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val uiState: StateFlow<SyncUiState> = combine(
        syncManager.status,
        syncManager.pendingCount,
        settingsRepository.settings
    ) { status, pending, settings ->
        SyncUiState(status = status, pending = pending, settings = settings)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SyncUiState())

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
}
