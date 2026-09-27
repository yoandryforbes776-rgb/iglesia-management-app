@file:OptIn(ExperimentalMaterial3Api::class)

package com.iglesiaflow.gestion.ui.screens.groups

import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import com.iglesiaflow.gestion.data.local.entity.GroupEntity
import com.iglesiaflow.gestion.domain.model.GroupType
import com.iglesiaflow.gestion.ui.components.Avatar
import com.iglesiaflow.gestion.ui.components.ConfirmDialog
import com.iglesiaflow.gestion.ui.components.DropdownField
import com.iglesiaflow.gestion.ui.components.EmptyState
import com.iglesiaflow.gestion.ui.components.FormDialog
import com.iglesiaflow.gestion.ui.components.FormTextField
import com.iglesiaflow.gestion.ui.components.SwitchRow

@Composable
fun GroupsScreen(
    onOpenGroup: (Long) -> Unit,
    viewModel: GroupsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showForm by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<GroupEntity?>(null) }
    var pendingDelete by remember { mutableStateOf<GroupEntity?>(null) }

    Scaffold(
        floatingActionButton = {
            if (state.canEdit) {
                FloatingActionButton(onClick = { editing = null; showForm = true }) {
                    Icon(Icons.Filled.Add, contentDescription = "Nuevo grupo")
                }
            }
        }
    ) { padding ->
        if (state.groups.isEmpty()) {
            Column(Modifier.fillMaxSize().padding(padding)) {
                EmptyState(
                    title = "Sin grupos",
                    subtitle = "Crea células, comités, estudios bíblicos o equipos de alabanza.",
                    icon = Icons.Filled.Groups
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 96.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.groups, key = { it.id }) { group ->
                    Card(Modifier.fillMaxWidth().clickable { onOpenGroup(group.id) }) {
                        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Avatar(name = group.name)
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(group.name, style = MaterialTheme.typography.titleSmall)
                                Text(
                                    "${group.type.label} · ${state.sizes[group.id] ?: 0} miembros",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    "${state.leaderName(group)} · ${group.meetingDay} ${group.meetingTime}",
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                            if (state.canEdit) {
                                IconButton(onClick = { editing = group; showForm = true }) {
                                    Icon(Icons.Filled.Edit, contentDescription = "Editar")
                                }
                                IconButton(onClick = { pendingDelete = group }) {
                                    Icon(Icons.Filled.Delete, contentDescription = "Eliminar")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showForm) {
        val group = editing
        var name by remember { mutableStateOf(group?.name.orEmpty()) }
        var type by remember { mutableStateOf(group?.type ?: GroupType.CELULA) }
        var description by remember { mutableStateOf(group?.description.orEmpty()) }
        var leader by remember { mutableStateOf(state.members.firstOrNull { it.id == group?.leaderId }) }
        var day by remember { mutableStateOf(group?.meetingDay.orEmpty()) }
        var time by remember { mutableStateOf(group?.meetingTime.orEmpty()) }
        var location by remember { mutableStateOf(group?.location.orEmpty()) }
        var active by remember { mutableStateOf(group?.active ?: true) }
        FormDialog(
            title = if (group == null) "Nuevo grupo" else "Editar grupo",
            onDismiss = { showForm = false },
            confirmEnabled = name.isNotBlank(),
            onConfirm = {
                viewModel.save(
                    (group ?: GroupEntity(name = "")).copy(
                        name = name.trim(),
                        type = type,
                        description = description.trim(),
                        leaderId = leader?.id,
                        meetingDay = day.trim(),
                        meetingTime = time.trim(),
                        location = location.trim(),
                        active = active
                    )
                )
                showForm = false
            }
        ) {
            FormTextField("Nombre", name, { name = it })
            DropdownField("Tipo", GroupType.entries, type, { it.label }, { type = it })
            FormTextField("Descripción", description, { description = it }, singleLine = false)
            DropdownField("Líder", state.members, leader, { it.fullName }, { leader = it })
            FormTextField("Día de reunión", day, { day = it })
            FormTextField("Hora", time, { time = it })
            FormTextField("Lugar", location, { location = it })
            SwitchRow("Grupo activo", null, active) { active = it }
        }
    }

    pendingDelete?.let { group ->
        ConfirmDialog(
            title = "Eliminar grupo",
            message = "Se eliminará ${group.name} y sus asignaciones de miembros.",
            confirmLabel = "Eliminar",
            onConfirm = { viewModel.delete(group.id) },
            onDismiss = { pendingDelete = null }
        )
    }
}
