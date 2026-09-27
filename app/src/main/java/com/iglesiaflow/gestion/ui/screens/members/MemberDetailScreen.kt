@file:OptIn(ExperimentalMaterial3Api::class)

package com.iglesiaflow.gestion.ui.screens.members

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.item
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
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
import com.iglesiaflow.gestion.core.util.Formatters
import com.iglesiaflow.gestion.ui.components.Avatar
import com.iglesiaflow.gestion.ui.components.FormDialog
import com.iglesiaflow.gestion.ui.components.FormTextField
import com.iglesiaflow.gestion.ui.components.SectionCard
import com.iglesiaflow.gestion.ui.components.SwitchRow

@Composable
fun MemberDetailScreen(
    onBack: () -> Unit,
    viewModel: MemberDetailViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val totalDonated by viewModel.totalDonated.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showEdit by remember { mutableStateOf(false) }
    var showNote by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.member?.fullName ?: "Miembro") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                },
                actions = {
                    if (state.canEdit) {
                        IconButton(onClick = { showEdit = true }) {
                            Icon(Icons.Filled.Edit, contentDescription = "Editar")
                        }
                    }
                }
            )
        }
    ) { padding ->
        val member = state.member
        if (member == null) {
            Column(Modifier.fillMaxSize().padding(padding), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Miembro no encontrado", Modifier.padding(32.dp))
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Avatar(name = member.fullName, photoUri = member.photoUri, size = 64)
                            Spacer(Modifier.width(16.dp))
                            Column(Modifier.weight(1f)) {
                                Text(member.fullName, style = MaterialTheme.typography.titleLarge)
                                Text(
                                    listOfNotNull(
                                        member.churchRole.takeIf { it.isNotBlank() },
                                        member.status.label,
                                        DateTimeUtils.age(member.birthDate)?.let { "$it años" }
                                    ).joinToString(" · "),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Spacer(Modifier.padding(top = 12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (member.phone.isNotBlank()) {
                                AssistChip(
                                    onClick = {
                                        context.startActivity(
                                            Intent(Intent.ACTION_DIAL, Uri.parse("tel:${member.phone}"))
                                        )
                                    },
                                    label = { Text(member.phone) },
                                    leadingIcon = { Icon(Icons.Filled.Call, contentDescription = null) }
                                )
                            }
                            if (member.email.isNotBlank()) {
                                AssistChip(
                                    onClick = {
                                        context.startActivity(
                                            Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:${member.email}"))
                                        )
                                    },
                                    label = { Text("Email") },
                                    leadingIcon = { Icon(Icons.Filled.Email, contentDescription = null) }
                                )
                            }
                        }
                    }
                }
            }

            item {
                SectionCard(title = "Datos personales") {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        DetailRow("Nacimiento", DateTimeUtils.formatDate(member.birthDate))
                        DetailRow("Género", member.gender.label)
                        DetailRow("Estado civil", member.maritalStatus.label)
                        DetailRow("Familia", state.family?.name ?: "Sin familia")
                        DetailRow("Rol familiar", member.familyRole.label)
                        DetailRow("Dirección", listOf(member.address, member.city).filter { it.isNotBlank() }.joinToString(", "))
                        DetailRow("Bautizado", if (member.baptized) "Sí" else "No")
                        DetailRow("Miembro desde", DateTimeUtils.formatDate(member.joinedAt))
                        if (member.properties.isNotEmpty()) {
                            DetailRow("Propiedades", member.properties.joinToString(", "))
                        }
                    }
                }
            }

            if (state.customFields.isNotEmpty()) {
                item {
                    SectionCard(title = "Campos personalizados") {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            state.customFields.forEach { field ->
                                DetailRow(field.label, state.customValues[field.id].orEmpty().ifBlank { "—" })
                            }
                        }
                    }
                }
            }

            if (state.skills.isNotEmpty()) {
                item {
                    SectionCard(title = "Voluntariado") {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            state.skills.forEach { skill ->
                                DetailRow(skill.skill, "${skill.level.label} · ${skill.availability}")
                            }
                        }
                    }
                }
            }

            if (state.canSeeFinance) {
                item {
                    SectionCard(title = "Aportaciones") {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            DetailRow("Total aportado", Formatters.money(totalDonated, state.settings.currencyCode))
                            state.donations.take(8).forEach { donation ->
                                DetailRow(
                                    DateTimeUtils.formatDate(donation.date),
                                    "${Formatters.money(donation.amount, state.settings.currencyCode)} · ${donation.type.label}"
                                )
                            }
                            if (state.donations.isEmpty()) Text("Sin aportaciones registradas")
                        }
                    }
                }
            }

            item {
                SectionCard(
                    title = "Historial y notas",
                    action = { TextButton(onClick = { showNote = true }) { Text("Añadir") } }
                ) {
                    if (state.notes.isEmpty()) {
                        Text("Sin notas todavía", style = MaterialTheme.typography.bodyMedium)
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            state.notes.forEach { note ->
                                Column {
                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(
                                            note.title.ifBlank { "Nota" },
                                            style = MaterialTheme.typography.titleSmall
                                        )
                                        Text(
                                            DateTimeUtils.formatDate(note.createdAt),
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    }
                                    Text(note.content, style = MaterialTheme.typography.bodyMedium)
                                    Text(
                                        "${note.authorName}${if (note.isPrivate) " · privada" else ""}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    HorizontalDivider(Modifier.padding(top = 6.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showEdit) {
        state.member?.let { member ->
            MemberFormDialog(
                member = member,
                families = state.families,
                customFields = state.customFields,
                initialCustomValues = state.customValues,
                onDismiss = { showEdit = false },
                onSave = { updated, values ->
                    viewModel.save(updated, values)
                    showEdit = false
                }
            )
        }
    }

    if (showNote) {
        var title by remember { mutableStateOf("") }
        var content by remember { mutableStateOf("") }
        var isPrivate by remember { mutableStateOf(false) }
        FormDialog(
            title = "Nueva nota",
            onDismiss = { showNote = false },
            onConfirm = {
                viewModel.addNote(title, content, isPrivate)
                showNote = false
            },
            confirmEnabled = content.isNotBlank()
        ) {
            FormTextField("Título", title, { title = it })
            FormTextField("Contenido", content, { content = it }, singleLine = false)
            SwitchRow("Nota privada", "Solo visible para el equipo pastoral", isPrivate) { isPrivate = it }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value.ifBlank { "—" }, style = MaterialTheme.typography.bodyMedium)
    }
}
