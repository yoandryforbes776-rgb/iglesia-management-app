@file:OptIn(ExperimentalMaterial3Api::class)

package com.iglesiaflow.gestion.ui.screens.communication

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
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
import com.iglesiaflow.gestion.data.local.entity.PrayerRequestEntity
import com.iglesiaflow.gestion.ui.components.FormDialog
import com.iglesiaflow.gestion.ui.components.FormTextField
import com.iglesiaflow.gestion.ui.components.SwitchRow

@Composable
fun PrayerScreen(
    onBack: () -> Unit,
    viewModel: PrayerViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showForm by remember { mutableStateOf(false) }
    var answering by remember { mutableStateOf<PrayerRequestEntity?>(null) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
                title = { Text("Peticiones de oración") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showForm = true }) {
                Icon(Icons.Filled.Add, contentDescription = "Nueva petición")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp, 12.dp, 16.dp, 96.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(state.requests, key = { it.id }) { request ->
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(request.title, style = MaterialTheme.typography.titleSmall)
                                Text(
                                    "${request.requesterName.ifBlank { "Anónimo" }} · " +
                                        "${request.status.label} · ${DateTimeUtils.formatDate(request.createdAt)}" +
                                        if (request.isPrivate) " · privada" else "",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(onClick = { viewModel.pray(request.id) }) {
                                Icon(Icons.Filled.Favorite, contentDescription = "Orar")
                            }
                            if (state.canModerate) {
                                IconButton(onClick = { answering = request }) {
                                    Icon(Icons.Filled.Check, contentDescription = "Marcar respondida")
                                }
                                IconButton(onClick = { viewModel.delete(request.id) }) {
                                    Icon(Icons.Filled.Delete, contentDescription = "Eliminar")
                                }
                            }
                        }
                        if (request.detail.isNotBlank()) {
                            Spacer(Modifier.padding(top = 6.dp))
                            Text(request.detail, style = MaterialTheme.typography.bodyMedium)
                        }
                        Text(
                            "${request.prayerCount} personas han orado",
                            style = MaterialTheme.typography.labelSmall
                        )
                        if (request.answerNote.isNotBlank()) {
                            Text(
                                "Respuesta: ${request.answerNote}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
            if (state.requests.isEmpty()) {
                items(listOf(0)) { Text("No hay peticiones registradas.") }
            }
        }
    }

    if (showForm) {
        var title by remember { mutableStateOf("") }
        var detail by remember { mutableStateOf("") }
        var name by remember { mutableStateOf("") }
        var isPrivate by remember { mutableStateOf(false) }
        FormDialog(
            title = "Nueva petición",
            onDismiss = { showForm = false },
            confirmEnabled = title.isNotBlank(),
            onConfirm = {
                viewModel.save(
                    PrayerRequestEntity(
                        title = title.trim(),
                        detail = detail.trim(),
                        requesterName = name.trim(),
                        isPrivate = isPrivate
                    )
                )
                showForm = false
            }
        ) {
            FormTextField("Título", title, { title = it })
            FormTextField("Detalle", detail, { detail = it }, singleLine = false)
            FormTextField("Solicitante", name, { name = it })
            SwitchRow("Petición privada", "Solo visible para el equipo pastoral", isPrivate) { isPrivate = it }
        }
    }

    answering?.let { request ->
        var note by remember { mutableStateOf("") }
        FormDialog(
            title = "Marcar como respondida",
            onDismiss = { answering = null },
            onConfirm = {
                viewModel.answer(request.id, note.trim())
                answering = null
            }
        ) {
            FormTextField("Testimonio / respuesta", note, { note = it }, singleLine = false)
        }
    }
}
