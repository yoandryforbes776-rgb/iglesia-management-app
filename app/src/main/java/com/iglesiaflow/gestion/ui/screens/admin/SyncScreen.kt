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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.iglesiaflow.gestion.core.util.DateTimeUtils
import com.iglesiaflow.gestion.ui.components.FormTextField
import com.iglesiaflow.gestion.ui.components.InfoBanner
import com.iglesiaflow.gestion.ui.components.SectionCard
import com.iglesiaflow.gestion.ui.components.SwitchRow
import com.iglesiaflow.gestion.ui.sync.SyncStatusViewModel

@Composable
fun SyncScreen(
    onBack: () -> Unit,
    viewModel: SyncStatusViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var churchCode by remember(state.settings.cloudChurchId) {
        mutableStateOf(state.settings.cloudChurchId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sincronización en tiempo real") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                SectionCard(title = "Estado") {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(state.label, style = MaterialTheme.typography.titleSmall)
                        Text(
                            "Firebase: " + if (state.status.available) "configurado" else "sin google-services.json",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            "Conexión: " + if (state.status.connected) "en línea" else "sin conexión",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            "Cambios pendientes de subir: ${state.pending}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            "Última sincronización: " +
                                (state.status.lastSyncAt?.let { DateTimeUtils.formatDateTime(it) } ?: "nunca"),
                            style = MaterialTheme.typography.bodySmall
                        )
                        state.status.error?.let {
                            Text(
                                "Error: $it",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = viewModel::syncNow) { Text("Sincronizar ahora") }
                        }
                    }
                }
            }

            item {
                SectionCard(title = "Configuración") {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        SwitchRow(
                            "Sincronización en la nube",
                            "Replica los datos entre todos los dispositivos de la iglesia",
                            state.settings.cloudSyncEnabled
                        ) { viewModel.setEnabled(it) }
                        FormTextField(
                            "Código de la iglesia",
                            churchCode,
                            { churchCode = it },
                            supportingText = "Todos los móviles deben usar exactamente el mismo código"
                        )
                        OutlinedButton(onClick = { viewModel.setChurchId(churchCode) }) {
                            Text("Guardar código")
                        }
                    }
                }
            }

            item {
                InfoBanner(
                    "Qué se sincroniza: miembros, familias, fondos, donaciones, eventos, asistencia, " +
                        "grupos, integrantes, mensajes de grupo y peticiones de oración. Los datos " +
                        "sensibles de usuarios, contraseñas y auditoría permanecen solo en el dispositivo."
                )
            }

            item {
                SectionCard(title = "Cómo activarlo (gratis)") {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("1. Crea un proyecto en console.firebase.google.com (plan Spark, sin tarjeta).")
                        Text("2. Añade una app Android con el paquete com.iglesiaflow.gestion.")
                        Text("3. Activa Realtime Database y pega las reglas de docs/FIREBASE.md.")
                        Text("4. Coloca google-services.json en la carpeta app/ y recompila la APK.")
                        Text("5. Enciende el interruptor de arriba en todos los dispositivos con el mismo código.")
                    }
                }
            }
        }
    }
}
