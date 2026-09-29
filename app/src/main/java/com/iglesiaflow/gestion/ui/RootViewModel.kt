package com.iglesiaflow.gestion.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iglesiaflow.gestion.core.config.AppSettings
import com.iglesiaflow.gestion.core.config.SettingsRepository
import com.iglesiaflow.gestion.core.security.SessionManager
import com.iglesiaflow.gestion.core.security.SessionUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RootViewModel @Inject constructor(
    settingsRepository: SettingsRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    val settings: StateFlow<AppSettings> = settingsRepository.settings
        .stateIn(viewModelScope, SharingStarted.Eagerly, AppSettings())

    val currentUser: StateFlow<SessionUser?> = sessionManager.currentUser

    private val _sessionExpired = MutableStateFlow(false)

    /** true cuando la última sesión se cerró sola por inactividad. */
    val sessionExpired: StateFlow<Boolean> = _sessionExpired.asStateFlow()

    init {
        // Vigilante de inactividad: si no se toca la app durante
        // `sessionTimeoutMinutes` (30 por defecto) la sesión se cierra sola.
        // Cualquier interacción reinicia la cuenta atrás, de modo que usarla
        // dentro de ese margen la prolonga otros 30 minutos.
        viewModelScope.launch {
            while (true) {
                delay(CHECK_INTERVAL_MS)
                val minutes = settings.value.sessionTimeoutMinutes
                if (minutes > 0 && sessionManager.isSessionExpired(minutes)) {
                    _sessionExpired.value = true
                    sessionManager.logout()
                }
            }
        }
    }

    /** Se llama en cada toque de pantalla para renovar la sesión. */
    fun touch() {
        if (currentUser.value != null) sessionManager.touch()
    }

    fun consumeExpiredNotice() { _sessionExpired.value = false }

    fun logout() {
        _sessionExpired.value = false
        sessionManager.logout()
    }

    private companion object {
        const val CHECK_INTERVAL_MS = 30_000L
    }
}
