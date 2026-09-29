package com.iglesiaflow.gestion.ui.screens.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.iglesiaflow.gestion.core.config.AppSettings
import com.iglesiaflow.gestion.ui.components.Avatar
import com.iglesiaflow.gestion.ui.components.InfoBanner

@Composable
fun LoginScreen(
    settings: AppSettings,
    notice: String? = null,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (notice != null) {
            InfoBanner(notice)
            Spacer(Modifier.height(16.dp))
        }
        Avatar(name = settings.churchName, photoUri = settings.logoUri, size = 84)
        Spacer(Modifier.height(16.dp))
        Text(settings.churchName, style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
        Text(
            settings.churchMotto,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(24.dp))

        Card(modifier = Modifier.widthIn(max = 420.dp)) {
            Column(Modifier.padding(20.dp)) {
                Text(
                    if (state.requiresTwoFactor) "Verificación en dos pasos" else "Iniciar sesión",
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(Modifier.height(16.dp))

                if (state.requiresTwoFactor) {
                    OutlinedTextField(
                        value = state.twoFactorCode,
                        onValueChange = viewModel::onCodeChange,
                        label = { Text("Código de verificación (2FA)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    OutlinedTextField(
                        value = state.email,
                        onValueChange = viewModel::onEmailChange,
                        label = { Text("Correo electrónico") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = state.password,
                        onValueChange = viewModel::onPasswordChange,
                        label = { Text("Contraseña") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (state.error != null) {
                    Spacer(Modifier.height(10.dp))
                    Text(state.error!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }

                Spacer(Modifier.height(20.dp))
                Button(
                    onClick = viewModel::submit,
                    enabled = !state.loading,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (state.loading) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    } else {
                        Text(if (state.requiresTwoFactor) "Verificar" else "Entrar")
                    }
                }
            }
        }

        Spacer(Modifier.height(20.dp))
        InfoBanner(
            "Acceso de demostración: admin@iglesia.org / admin123 · pastor@iglesia.org / pastor123 · " +
                "tesorero@iglesia.org / tesoro123",
            modifier = Modifier.widthIn(max = 420.dp)
        )
    }
}
