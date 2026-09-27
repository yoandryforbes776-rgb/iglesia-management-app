@file:OptIn(ExperimentalMaterial3Api::class)

package com.iglesiaflow.gestion.ui.screens.members

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.Diversity3
import androidx.compose.material.icons.filled.Edit
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
import com.iglesiaflow.gestion.data.local.entity.FamilyEntity
import com.iglesiaflow.gestion.ui.components.Avatar
import com.iglesiaflow.gestion.ui.components.ConfirmDialog
import com.iglesiaflow.gestion.ui.components.EmptyState
import com.iglesiaflow.gestion.ui.components.FormDialog
import com.iglesiaflow.gestion.ui.components.FormTextField

@Composable
fun FamiliesScreen(
    onOpenMember: (Long) -> Unit,
    viewModel: FamiliesViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showForm by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<FamilyEntity?>(null) }
    var pendingDelete by remember { mutableStateOf<FamilyEntity?>(null) }
    var expanded by remember { mutableStateOf<Long?>(null) }

    Scaffold(
        floatingActionButton = {
            if (state.canEdit) {
                FloatingActionButton(onClick = { editing = null; showForm = true }) {
                    Icon(Icons.Filled.Add, contentDescription = "Nueva familia")
                }
            }
        }
    ) { padding ->
        if (state.families.isEmpty()) {
            Column(Modifier.fillMaxSize().padding(padding)) {
                EmptyState(
                    title = "Sin familias",
                    subtitle = "Agrupa a los miembros por núcleo familiar.",
                    icon = Icons.Filled.Diversity3
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 96.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.families, key = { it.family.id }) { entry ->
                    Card(
                        Modifier
                            .fillMaxWidth()
                            .clickable { expanded = if (expanded == entry.family.id) null else entry.family.id }
                    ) {
                        Column(Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Avatar(name = entry.family.name)
                                Spacer(Modifier.width(12.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(entry.family.name, style = MaterialTheme.typography.titleSmall)
                                    Text(
                                        "${entry.members.size} miembros · ${entry.family.city}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                if (state.canEdit) {
                                    IconButton(onClick = { editing = entry.family; showForm = true }) {
                                        Icon(Icons.Filled.Edit, contentDescription = "Editar")
                                    }
                                    IconButton(onClick = { pendingDelete = entry.family }) {
                                        Icon(Icons.Filled.Delete, contentDescription = "Eliminar")
                                    }
                                }
                            }
                            AnimatedVisibility(visible = expanded == entry.family.id) {
                                Column(Modifier.padding(top = 8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    entry.members.forEach { member ->
                                        Row(
                                            Modifier
                                                .fillMaxWidth()
                                                .clickable { onOpenMember(member.id) },
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(member.fullName, style = MaterialTheme.typography.bodyMedium)
                                            Text(
                                                member.familyRole.label,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                    if (entry.family.phone.isNotBlank()) {
                                        Text(
                                            "Contacto: ${entry.family.phone}",
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showForm) {
        val family = editing
        var name by remember { mutableStateOf(family?.name.orEmpty()) }
        var address by remember { mutableStateOf(family?.address.orEmpty()) }
        var city by remember { mutableStateOf(family?.city.orEmpty()) }
        var phone by remember { mutableStateOf(family?.phone.orEmpty()) }
        var email by remember { mutableStateOf(family?.email.orEmpty()) }
        FormDialog(
            title = if (family == null) "Nueva familia" else "Editar familia",
            onDismiss = { showForm = false },
            confirmEnabled = name.isNotBlank(),
            onConfirm = {
                viewModel.save(
                    (family ?: FamilyEntity(name = "")).copy(
                        name = name.trim(), address = address.trim(), city = city.trim(),
                        phone = phone.trim(), email = email.trim()
                    )
                )
                showForm = false
            }
        ) {
            FormTextField("Nombre de la familia", name, { name = it })
            FormTextField("Dirección", address, { address = it })
            FormTextField("Ciudad", city, { city = it })
            FormTextField("Teléfono", phone, { phone = it })
            FormTextField("Correo", email, { email = it })
        }
    }

    pendingDelete?.let { family ->
        ConfirmDialog(
            title = "Eliminar familia",
            message = "Se eliminará ${family.name}. Los miembros quedarán sin familia asignada.",
            confirmLabel = "Eliminar",
            onConfirm = { viewModel.delete(family.id) },
            onDismiss = { pendingDelete = null }
        )
    }
}
