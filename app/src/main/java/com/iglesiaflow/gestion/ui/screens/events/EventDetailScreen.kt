@file:OptIn(ExperimentalMaterial3Api::class)

package com.iglesiaflow.gestion.ui.screens.events

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.item
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.iglesiaflow.gestion.core.util.DateTimeUtils
import com.iglesiaflow.gestion.ui.components.FormDialog
import com.iglesiaflow.gestion.ui.components.FormTextField
import com.iglesiaflow.gestion.ui.components.SearchField
import com.iglesiaflow.gestion.ui.components.SectionCard
import com.iglesiaflow.gestion.ui.components.StatCard

@Composable
fun EventDetailScreen(
    onBack: () -> Unit,
    viewModel: EventDetailViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    var checkOutTarget by remember { mutableStateOf<Long?>(null) }

    LaunchedEffect(state.message) {
        state.message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.consumeMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(state.event?.title ?: "Evento") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                },
                actions = {
                    IconButton(onClick = viewModel::exportAttendance) {
                        Icon(Icons.Filled.FileDownload, contentDescription = "Exportar asistencia")
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
            state.event?.let { event ->
                item {
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp)) {
                            Text(event.title, style = MaterialTheme.typography.titleLarge)
                            Text(
                                viewModel.eventSubtitle(event),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (event.description.isNotBlank()) {
                                Spacer(Modifier.padding(top = 8.dp))
                                Text(event.description, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        StatCard(
                            title = "Asistentes",
                            value = state.presentCount.toString(),
                            subtitle = "de ${state.attendance.size} miembros",
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            title = "Check-ins",
                            value = state.checkIns.count { it.checkOutAt == null }.toString(),
                            subtitle = "niños en sala",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            if (state.checkIns.isNotEmpty()) {
                item {
                    SectionCard(title = "Ministerio infantil") {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            state.checkIns.forEach { checkIn ->
                                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                    Column(Modifier.weight(1f)) {
                                        Text(checkIn.childName, style = MaterialTheme.typography.bodyLarge)
                                        Text(
                                            "Código ${checkIn.securityCode} · ${checkIn.guardianName} · " +
                                                DateTimeUtils.formatTime(checkIn.checkInAt),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        if (checkIn.allergies.isNotBlank()) {
                                            Text(
                                                "Alergias: ${checkIn.allergies}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.error
                                            )
                                        }
                                    }
                                    if (checkIn.guardianPhone.isNotBlank()) {
                                        IconButton(onClick = {
                                            context.startActivity(
                                                Intent(
                                                    Intent.ACTION_SENDTO,
                                                    Uri.parse("smsto:${checkIn.guardianPhone}")
                                                ).putExtra(
                                                    "sms_body",
                                                    "Le esperamos para recoger a ${checkIn.childName}. Código: ${checkIn.securityCode}"
                                                )
                                            )
                                        }) { Icon(Icons.Filled.Sms, contentDescription = "Avisar por SMS") }
                                    }
                                    if (checkIn.checkOutAt == null) {
                                        TextButton(onClick = { checkOutTarget = checkIn.id }) { Text("Salida") }
                                    } else {
                                        Text("Retirado", style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                SearchField(
                    value = state.query,
                    onValueChange = viewModel::onQueryChange,
                    placeholder = "Buscar miembro"
                )
            }

            item { Text("Control de asistencia", style = MaterialTheme.typography.titleMedium) }

            items(state.attendance, key = { it.memberId }) { row ->
                Row(
                    Modifier.fillMaxWidth().padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = row.present,
                        enabled = state.canManage,
                        onCheckedChange = { viewModel.toggleAttendance(row.memberId, it) }
                    )
                    Spacer(Modifier.width(6.dp))
                    Text("${row.firstName} ${row.lastName}", style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }

    checkOutTarget?.let { id ->
        var code by remember { mutableStateOf("") }
        FormDialog(
            title = "Registrar salida",
            onDismiss = { checkOutTarget = null },
            confirmEnabled = code.isNotBlank(),
            confirmLabel = "Validar",
            onConfirm = {
                viewModel.checkOut(id, code)
                checkOutTarget = null
            }
        ) {
            FormTextField("Código de seguridad", code, { code = it })
        }
    }
}
