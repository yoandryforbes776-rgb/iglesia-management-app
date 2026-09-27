package com.iglesiaflow.gestion.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iglesiaflow.gestion.core.security.LoginResult
import com.iglesiaflow.gestion.core.security.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val twoFactorCode: String = "",
    val requiresTwoFactor: Boolean = false,
    val loading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onEmailChange(value: String) = _uiState.update { it.copy(email = value, error = null) }
    fun onPasswordChange(value: String) = _uiState.update { it.copy(password = value, error = null) }
    fun onCodeChange(value: String) = _uiState.update { it.copy(twoFactorCode = value, error = null) }

    fun submit() {
        val state = _uiState.value
        if (state.loading) return
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, error = null) }
            val result = if (state.requiresTwoFactor) {
                sessionManager.verifyTwoFactor(state.twoFactorCode)
            } else {
                sessionManager.login(state.email, state.password)
            }
            when (result) {
                is LoginResult.Success -> _uiState.update { LoginUiState() }
                is LoginResult.RequiresTwoFactor ->
                    _uiState.update { it.copy(loading = false, requiresTwoFactor = true) }
                is LoginResult.Failure ->
                    _uiState.update { it.copy(loading = false, error = result.message) }
            }
        }
    }
}
