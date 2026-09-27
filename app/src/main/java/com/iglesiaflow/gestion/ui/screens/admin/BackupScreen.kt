@file:OptIn(ExperimentalMaterial3Api::class)

package com.iglesiaflow.gestion.ui.screens.admin

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import com.iglesiaflow.gestion.ui.components.ConfirmDialog
import com.iglesiaflow.gestion.ui.components.InfoBanner
import com.iglesiaflow.gestion.ui.components.SectionCard
import java.io.File

@Composable
fun BackupScreen(
    onBack: () -> Unit,
    viewModel: AdminViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var restoring by remember { mutableStateOf<File?>(null) }

    val csvPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { viewModel.importMembersCsv(it) }
    }
    val backupPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { viewModel.importBackup(it) }
    }

    LaunchedEffect(state.message) {
        state.message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.consumeMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Backup e importación") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                InfoBanner(
                    "Las copias incluyen toda la base de datos cifrada. Guárdalas en un lugar seguro: " +
                        "contienen datos personales de la congregación."
                )
            }

            item {
                SectionCard(title = "Copias de seguridad") {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = viewModel::createBackup,
                                enabled = state.canBackup
                            ) { Text("Crear copia") }
                            OutlinedButton(
                                onClick = { backupPicker.launch("*/*") },
                                enabled = state.canBackup
                            ) { Text("Importar copia") }
                        }
                    }
                }
            }

            items(state.backups, key = { it.absolutePath }) { file ->
                Card(Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(file.name, style = MaterialTheme.typography.bodyMedium)
                            Text(
                                "${DateTimeUtils.formatDateTime(file.lastModified())} · " +
                                    "${file.length() / 1024} KB",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(onClick = { viewModel.shareBackup(file) }) {
                            Icon(Icons.Filled.Share, contentDescription = "Compartir")
                        }
                        IconButton(onClick = { restoring = file }) {
                            Icon(Icons.Filled.Restore, contentDescription = "Restaurar")
                        }
                        IconButton(onClick = { viewModel.deleteBackup(file) }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Eliminar")
                        }
                    }
                }
            }

            item {
                SectionCard(title = "Importar / exportar CSV") {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            "El CSV de miembros admite las columnas: nombre, apellidos, email, telefono, " +
                                "nacimiento, estado, rol, direccion, ciudad (separador ; o ,).",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = { csvPicker.launch("*/*") }) { Text("Importar miembros") }
                            OutlinedButton(onClick = viewModel::exportMembersCsv) { Text("Exportar miembros") }
                        }
                    }
                }
            }
        }
    }

    restoring?.let { file ->
        ConfirmDialog(
            title = "Restaurar copia",
            message = "Se sustituirán todos los datos actuales por los de ${file.name}. " +
                "Deberás reiniciar la aplicación. ¿Continuar?",
            confirmLabel = "Restaurar",
            onConfirm = {
                viewModel.restoreBackup(file)
                restoring = null
            },
            onDismiss = { restoring = null }
        )
    }
}
