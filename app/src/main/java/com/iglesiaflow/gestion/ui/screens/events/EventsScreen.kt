@file:OptIn(ExperimentalMaterial3Api::class)

package com.iglesiaflow.gestion.ui.screens.events

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
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
import com.iglesiaflow.gestion.data.local.entity.EventEntity
import com.iglesiaflow.gestion.domain.model.EventType
import com.iglesiaflow.gestion.ui.components.ConfirmDialog
import com.iglesiaflow.gestion.ui.components.InfoBanner

@Composable
fun EventsScreen(
    onOpenEvent: (Long) -> Unit,
    onOpenCheckIn: () -> Unit,
    viewModel: EventsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showForm by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<EventEntity?>(null) }
    var pendingDelete by remember { mutableStateOf<EventEntity?>(null) }

    LaunchedEffect(state.message) {
        state.message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.consumeMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            if (state.canEdit) {
                ExtendedFloatingActionButton(
                    onClick = { editing = null; showForm = true },
                    icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                    text = { Text("Evento") }
                )
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp, 8.dp, 16.dp, 96.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Row(
                    Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = state.typeFilter == null,
                        onClick = { viewModel.onTypeFilter(null) },
                        label = { Text("Todos") }
                    )
                    EventType.entries.forEach { type ->
                        FilterChip(
                            selected = state.typeFilter == type,
                            onClick = { viewModel.onTypeFilter(type) },
                            label = { Text(type.label) }
                        )
                    }
                }
            }

            item {
                Card(Modifier.fillMaxWidth().clickable { onOpenCheckIn() }) {
                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.ChildCare, contentDescription = null)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Modo kiosco de check-in", style = MaterialTheme.typography.titleSmall)
                            Text(
                                "${state.activeCheckIns} niños actualmente en sala",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        AssistChip(onClick = onOpenCheckIn, label = { Text("Abrir") })
                    }
                }
            }

            item { Text("Próximos", style = MaterialTheme.typography.titleMedium) }
            if (state.upcoming.isEmpty()) {
                item { InfoBanner("No hay eventos próximos programados.") }
            }
            items(state.upcoming, key = { it.id }) { event ->
                EventCard(
                    event = event,
                    canEdit = state.canEdit,
                    onClick = { onOpenEvent(event.id) },
                    onEdit = { editing = event; showForm = true },
                    onDelete = { pendingDelete = event }
                )
            }

            item {
                Text("Historial", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 8.dp))
            }
            items(state.past, key = { it.id }) { event ->
                EventCard(
                    event = event,
                    canEdit = state.canEdit,
                    onClick = { onOpenEvent(event.id) },
                    onEdit = { editing = event; showForm = true },
                    onDelete = { pendingDelete = event }
                )
            }
        }
    }

    if (showForm) {
        EventFormDialog(
            event = editing,
            groups = state.groups,
            onDismiss = { showForm = false },
            onSave = { event, recurrences ->
                viewModel.save(event, recurrences)
                showForm = false
            }
        )
    }

    pendingDelete?.let { event ->
        ConfirmDialog(
            title = "Eliminar evento",
            message = "¿Eliminar ${event.title}?",
            confirmLabel = "Eliminar",
            onConfirm = { viewModel.delete(event.id) },
            onDismiss = { pendingDelete = null }
        )
    }
}

@Composable
private fun EventCard(
    event: EventEntity,
    canEdit: Boolean,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(Modifier.fillMaxWidth().clickable { onClick() }) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(event.title, style = MaterialTheme.typography.titleSmall)
                Text(
                    "${DateTimeUtils.formatDateTime(event.startAt)} · ${event.location}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(event.type.label, style = MaterialTheme.typography.labelSmall)
                    if (!event.recurrence.equals("NONE", true)) {
                        Text("· recurrente", style = MaterialTheme.typography.labelSmall)
                    }
                    if (event.requiresCheckIn) {
                        Text("· check-in", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
            if (canEdit) {
                AssistChip(onClick = onEdit, label = { Text("Editar") })
                IconButton(onClick = onDelete) {
                    Icon(Icons.Filled.Delete, contentDescription = "Eliminar")
                }
            }
        }
    }
}
