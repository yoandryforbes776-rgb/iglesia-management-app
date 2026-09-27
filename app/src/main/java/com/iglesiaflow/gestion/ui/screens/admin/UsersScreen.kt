@file:OptIn(ExperimentalMaterial3Api::class)

package com.iglesiaflow.gestion.ui.screens.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Key
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.iglesiaflow.gestion.core.util.DateTimeUtils
import com.iglesiaflow.gestion.data.local.entity.UserEntity
import com.iglesiaflow.gestion.domain.model.UserRole
import com.iglesiaflow.gestion.ui.components.ConfirmDialog
import com.iglesiaflow.gestion.ui.components.DropdownField
import com.iglesiaflow.gestion.ui.components.FormDialog
import com.iglesiaflow.gestion.ui.components.FormTextField
import com.iglesiaflow.gestion.ui.components.InfoBanner
import com.iglesiaflow.gestion.ui.components.SwitchRow

@Composable
fun UsersScreen(
    onBack: () -> Unit,
    viewModel: AdminViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showCreate by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<UserEntity?>(null) }
    var passwordFor by remember { mutableStateOf<UserEntity?>(null) }
    var deleting by remember { mutableStateOf<UserEntity?>(null) }

    LaunchedEffect(state.message) {
        state.message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.consumeMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Usuarios") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            if (state.canManageUsers) {
                FloatingActionButton(onClick = { showCreate = true }) {
                    Icon(Icons.Filled.Add, contentDescription = "Nuevo usuario")
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp, 12.dp, 16.dp, 96.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (!state.canManageUsers) {
                item { InfoBanner("Solo puedes consultar la lista: no tienes permiso de gestión de usuarios.") }
            }
            items(state.users, key = { it.id }) { user ->
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(user.displayName, style = MaterialTheme.typography.titleSmall)
                                Text(
                                    "${user.email} · ${user.role.label}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    "Último acceso: " +
                                        (user.lastLoginAt?.let { DateTimeUtils.formatDateTime(it) } ?: "nunca"),
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                            if (state.canManageUsers) {
                                IconButton(onClick = { editing = user }) {
                                    Icon(Icons.Filled.Edit, contentDescription = "Editar")
                                }
                                IconButton(onClick = { passwordFor = user }) {
                                    Icon(Icons.Filled.Key, contentDescription = "Contraseña")
                                }
                                IconButton(onClick = { deleting = user }) {
                                    Icon(Icons.Filled.Delete, contentDescription = "Eliminar")
                                }
                            }
                        }
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AssistChip(
                                onClick = {},
                                label = { Text(if (user.active) "Activo" else "Desactivado") }
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("2FA", style = MaterialTheme.typography.labelMedium)
                                Switch(
                                    checked = user.twoFactorEnabled,
                                    enabled = state.canManageUsers,
                                    onCheckedChange = { viewModel.toggleTwoFactor(user, it) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreate) {
        var name by remember { mutableStateOf("") }
        var email by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var role by remember { mutableStateOf(UserRole.MIEMBRO) }
        var twoFactor by remember { mutableStateOf(false) }
        FormDialog(
            title = "Nuevo usuario",
            onDismiss = { showCreate = false },
            confirmEnabled = name.isNotBlank() && email.contains("@") && password.length >= 6,
            onConfirm = {
                viewModel.createUser(name.trim(), email.trim(), password, role, twoFactor)
                showCreate = false
            }
        ) {
            FormTextField("Nombre", name, { name = it })
            FormTextField("Email", email, { email = it })
            FormTextField("Contraseña", password, { password = it }, supportingText = "Mínimo 6 caracteres")
            DropdownField("Rol", UserRole.entries, role, { it.label }, { role = it })
            SwitchRow("Activar 2FA", "Genera un secreto TOTP", twoFactor) { twoFactor = it }
        }
    }

    editing?.let { user ->
        var name by remember { mutableStateOf(user.displayName) }
        var role by remember { mutableStateOf(user.role) }
        var active by remember { mutableStateOf(user.active) }
        FormDialog(
            title = "Editar usuario",
            onDismiss = { editing = null },
            onConfirm = {
                viewModel.updateUser(user.copy(displayName = name.trim(), role = role, active = active))
                editing = null
            }
        ) {
            FormTextField("Nombre", name, { name = it })
            DropdownField("Rol", UserRole.entries, role, { it.label }, { role = it })
            SwitchRow("Usuario activo", null, active) { active = it }
        }
    }

    passwordFor?.let { user ->
        var password by remember { mutableStateOf("") }
        FormDialog(
            title = "Cambiar contraseña",
            onDismiss = { passwordFor = null },
            confirmEnabled = password.length >= 6,
            onConfirm = {
                viewModel.changePassword(user, password)
                passwordFor = null
            }
        ) {
            FormTextField("Nueva contraseña", password, { password = it })
        }
    }

    deleting?.let { user ->
        ConfirmDialog(
            title = "Eliminar usuario",
            message = "¿Eliminar a ${user.displayName}? Esta acción queda registrada en la auditoría.",
            onConfirm = {
                viewModel.deleteUser(user)
                deleting = null
            },
            onDismiss = { deleting = null }
        )
    }

    state.totpSecret?.let { secret ->
        FormDialog(
            title = "Secreto TOTP generado",
            onDismiss = viewModel::consumeSecret,
            confirmLabel = "Entendido",
            onConfirm = viewModel::consumeSecret
        ) {
            Text("Introduce este código en Google Authenticator o similar:")
            Text(secret, style = MaterialTheme.typography.titleMedium)
        }
    }
}
