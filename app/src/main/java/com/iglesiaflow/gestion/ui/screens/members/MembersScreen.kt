@file:OptIn(ExperimentalMaterial3Api::class)

package com.iglesiaflow.gestion.ui.screens.members

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
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
import com.iglesiaflow.gestion.data.local.entity.MemberEntity
import com.iglesiaflow.gestion.domain.model.MemberStatus
import com.iglesiaflow.gestion.ui.components.Avatar
import com.iglesiaflow.gestion.ui.components.ConfirmDialog
import com.iglesiaflow.gestion.ui.components.EmptyState
import com.iglesiaflow.gestion.ui.components.SearchField

@Composable
fun MembersScreen(
    onOpenMember: (Long) -> Unit,
    viewModel: MembersViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showForm by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<MemberEntity?>(null) }
    var pendingDelete by remember { mutableStateOf<MemberEntity?>(null) }

    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let(viewModel::importCsv)
    }

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
                FloatingActionButton(onClick = { editing = null; showForm = true }) {
                    Icon(Icons.Filled.Add, contentDescription = "Nuevo miembro")
                }
            }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp)) {
            Spacer(Modifier.padding(top = 8.dp))
            SearchField(
                value = state.query,
                onValueChange = viewModel::onQueryChange,
                placeholder = "Buscar por nombre, email o teléfono"
            )
            Spacer(Modifier.padding(top = 8.dp))
            Row(
                Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterChip(
                    selected = state.statusFilter == null,
                    onClick = { viewModel.onStatusFilter(null) },
                    label = { Text("Todos") }
                )
                MemberStatus.entries.forEach { status ->
                    FilterChip(
                        selected = state.statusFilter == status,
                        onClick = { viewModel.onStatusFilter(status) },
                        label = { Text(status.label) }
                    )
                }
            }
            Row(
                Modifier.fillMaxWidth().padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "${state.members.size} miembros",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = viewModel::exportCsv) {
                    Icon(Icons.Filled.FileDownload, contentDescription = "Exportar CSV")
                }
                IconButton(onClick = viewModel::exportPdf) {
                    Icon(Icons.Filled.PictureAsPdf, contentDescription = "Exportar PDF")
                }
                if (state.canEdit) {
                    IconButton(onClick = { importLauncher.launch("*/*") }) {
                        Icon(Icons.Filled.FileUpload, contentDescription = "Importar CSV")
                    }
                }
            }

            if (state.members.isEmpty()) {
                EmptyState(
                    title = "Sin miembros",
                    subtitle = "Añade el primer miembro con el botón +, o importa un CSV.",
                    icon = Icons.Filled.People
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.members, key = { it.id }) { member ->
                        MemberRow(
                            member = member,
                            canDelete = state.canDelete,
                            onClick = { onOpenMember(member.id) },
                            onEdit = { editing = member; showForm = true },
                            onDelete = { pendingDelete = member }
                        )
                    }
                }
            }
        }
    }

    if (showForm) {
        MemberFormDialog(
            member = editing,
            families = state.families,
            customFields = state.customFields,
            onDismiss = { showForm = false },
            onSave = { member, values ->
                viewModel.save(member, values)
                showForm = false
            }
        )
    }

    pendingDelete?.let { member ->
        ConfirmDialog(
            title = "Eliminar miembro",
            message = "¿Eliminar a ${member.fullName}? Esta acción queda registrada en la auditoría.",
            confirmLabel = "Eliminar",
            onConfirm = { viewModel.delete(member) },
            onDismiss = { pendingDelete = null }
        )
    }
}

@Composable
private fun MemberRow(
    member: MemberEntity,
    canDelete: Boolean,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(Modifier.fillMaxWidth().clickable { onClick() }) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Avatar(name = member.fullName, photoUri = member.photoUri)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(member.fullName, style = MaterialTheme.typography.titleSmall)
                Text(
                    listOfNotNull(
                        member.phone.takeIf { it.isNotBlank() },
                        member.email.takeIf { it.isNotBlank() },
                        DateTimeUtils.age(member.birthDate)?.let { "$it años" }
                    ).joinToString(" · "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                AssistChip(onClick = onEdit, label = { Text(member.status.label) })
                if (canDelete) {
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Filled.Delete, contentDescription = "Eliminar")
                    }
                }
            }
        }
    }
}
