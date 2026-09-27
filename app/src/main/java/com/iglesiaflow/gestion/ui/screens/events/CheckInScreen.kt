@file:OptIn(ExperimentalMaterial3Api::class)

package com.iglesiaflow.gestion.ui.screens.events

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.iglesiaflow.gestion.core.util.DateTimeUtils
import com.iglesiaflow.gestion.data.local.entity.MemberEntity
import com.iglesiaflow.gestion.ui.components.FormDialog
import com.iglesiaflow.gestion.ui.components.FormTextField
import com.iglesiaflow.gestion.ui.components.InfoBanner

/** Modo kiosco: check-in y check-out rápido del ministerio infantil. */
@Composable
fun CheckInScreen(
    onBack: () -> Unit,
    viewModel: CheckInViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var checkInChild by remember { mutableStateOf<MemberEntity?>(null) }
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
                title = { Text("Kiosco de check-in") },
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
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                if (state.events.isEmpty()) {
                    InfoBanner("No hay eventos con check-in activado. Marca «Requiere check-in» al crear el evento.")
                } else {
                    Column {
                        Text("Evento", style = MaterialTheme.typography.titleMedium)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            state.events.take(4).forEach { event ->
                                FilterChip(
                                    selected = state.selectedEvent?.id == event.id,
                                    onClick = { viewModel.selectEvent(event.id) },
                                    label = { Text(event.title, maxLines = 1) }
                                )
                            }
                        }
                        state.selectedEvent?.let {
                            Text(
                                DateTimeUtils.formatDateTime(it.startAt),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            item { Text("En sala (${state.activeCheckIns.size})", style = MaterialTheme.typography.titleMedium) }
            items(state.activeCheckIns, key = { it.id }) { checkIn ->
                Card(Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(checkIn.childName, style = MaterialTheme.typography.titleSmall)
                            Text(
                                "Entrada ${DateTimeUtils.formatTime(checkIn.checkInAt)} · sala ${checkIn.room.ifBlank { "-" }}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        TextButton(onClick = { checkOutTarget = checkIn.id }) { Text("Salida") }
                    }
                }
            }

            item {
                Text("Niños registrados", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 8.dp))
            }
            items(state.children, key = { it.id }) { child ->
                val alreadyIn = state.activeCheckIns.any { it.childMemberId == child.id }
                Card(Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(child.fullName, style = MaterialTheme.typography.titleSmall)
                            Text(
                                DateTimeUtils.age(child.birthDate)?.let { "$it años" } ?: "",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Button(
                            onClick = { checkInChild = child },
                            enabled = state.canManage && !alreadyIn && state.selectedEvent != null
                        ) { Text(if (alreadyIn) "En sala" else "Check-in") }
                    }
                }
            }
        }
    }

    checkInChild?.let { child ->
        var guardian by remember { mutableStateOf("") }
        var phone by remember { mutableStateOf(child.guardianPhone) }
        var room by remember { mutableStateOf("") }
        var allergies by remember { mutableStateOf("") }
        FormDialog(
            title = "Check-in de ${child.firstName}",
            onDismiss = { checkInChild = null },
            confirmLabel = "Registrar",
            onConfirm = {
                viewModel.checkIn(child, guardian, phone, room, allergies)
                checkInChild = null
            }
        ) {
            FormTextField("Persona que lo trae", guardian, { guardian = it })
            FormTextField("Teléfono de contacto", phone, { phone = it })
            FormTextField("Sala", room, { room = it })
            FormTextField("Alergias / notas", allergies, { allergies = it }, singleLine = false)
        }
    }

    checkOutTarget?.let { id ->
        var code by remember { mutableStateOf("") }
        FormDialog(
            title = "Validar salida",
            onDismiss = { checkOutTarget = null },
            confirmLabel = "Validar",
            confirmEnabled = code.isNotBlank(),
            onConfirm = {
                viewModel.checkOut(id, code)
                checkOutTarget = null
            }
        ) {
            FormTextField("Código de seguridad del tutor", code, { code = it })
        }
    }

    state.lastCode?.let { code ->
        AlertDialog(
            onDismissRequest = viewModel::consumeCode,
            title = { Text("Código de seguridad") },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        code,
                        style = MaterialTheme.typography.displaySmall,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.padding(top = 8.dp))
                    Text("Entrega este código al tutor para la recogida.", textAlign = TextAlign.Center)
                }
            },
            confirmButton = { TextButton(onClick = viewModel::consumeCode) { Text("Entendido") } }
        )
    }
}
