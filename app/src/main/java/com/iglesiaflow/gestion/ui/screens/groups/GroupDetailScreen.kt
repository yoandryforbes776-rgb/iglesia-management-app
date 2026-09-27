@file:OptIn(ExperimentalMaterial3Api::class)

package com.iglesiaflow.gestion.ui.screens.groups

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
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PersonRemove
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import com.iglesiaflow.gestion.data.local.entity.MemberEntity
import com.iglesiaflow.gestion.domain.model.GroupRole
import com.iglesiaflow.gestion.ui.components.Avatar
import com.iglesiaflow.gestion.ui.components.DropdownField
import com.iglesiaflow.gestion.ui.components.FormDialog
import com.iglesiaflow.gestion.ui.components.SectionCard

@Composable
fun GroupDetailScreen(
    onBack: () -> Unit,
    viewModel: GroupDetailViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showAdd by remember { mutableStateOf(false) }
    var messageText by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.group?.name ?: "Grupo") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                },
                actions = {
                    if (state.canEdit) {
                        IconButton(onClick = { showAdd = true }) {
                            Icon(Icons.Filled.PersonAdd, contentDescription = "Añadir miembro")
                        }
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
            state.group?.let { group ->
                item {
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp)) {
                            Text(group.name, style = MaterialTheme.typography.titleLarge)
                            Text(
                                "${group.type.label} · ${group.meetingDay} ${group.meetingTime} · ${group.location}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (group.description.isNotBlank()) {
                                Spacer(Modifier.padding(top = 8.dp))
                                Text(group.description)
                            }
                        }
                    }
                }
            }

            item {
                SectionCard(title = "Integrantes (${state.members.size})") {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        state.members.forEach { row ->
                            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Avatar(name = "${row.firstName} ${row.lastName}", size = 36)
                                Spacer(Modifier.width(10.dp))
                                Column(Modifier.weight(1f)) {
                                    Text("${row.firstName} ${row.lastName}", style = MaterialTheme.typography.bodyLarge)
                                    Text(
                                        row.role.label,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                if (state.canEdit) {
                                    IconButton(onClick = { viewModel.removeMember(row.membershipId) }) {
                                        Icon(Icons.Filled.PersonRemove, contentDescription = "Quitar")
                                    }
                                }
                            }
                        }
                        if (state.members.isEmpty()) Text("Aún no hay integrantes.")
                    }
                }
            }

            item { Text("Muro del grupo", style = MaterialTheme.typography.titleMedium) }

            items(state.messages, key = { it.id }) { message ->
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp)) {
                        Text(message.authorName, style = MaterialTheme.typography.labelLarge)
                        Text(message.content, style = MaterialTheme.typography.bodyMedium)
                        Text(
                            DateTimeUtils.formatDateTime(message.createdAt),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            item {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = messageText,
                        onValueChange = { messageText = it },
                        placeholder = { Text("Escribe un mensaje al grupo") },
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = {
                        viewModel.postMessage(messageText)
                        messageText = ""
                    }) { Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Enviar") }
                }
            }
        }
    }

    if (showAdd) {
        var member by remember { mutableStateOf<MemberEntity?>(state.candidates.firstOrNull()) }
        var role by remember { mutableStateOf(GroupRole.MIEMBRO) }
        FormDialog(
            title = "Añadir integrante",
            onDismiss = { showAdd = false },
            confirmEnabled = member != null,
            onConfirm = {
                member?.let { viewModel.addMember(it.id, role) }
                showAdd = false
            }
        ) {
            DropdownField("Miembro", state.candidates, member, { it.fullName }, { member = it })
            DropdownField("Rol", GroupRole.entries, role, { it.label }, { role = it })
        }
    }
}
