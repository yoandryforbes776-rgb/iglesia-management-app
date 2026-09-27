@file:OptIn(ExperimentalMaterial3Api::class)

package com.iglesiaflow.gestion.ui.screens.volunteers

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.iglesiaflow.gestion.core.util.DateTimeUtils
import com.iglesiaflow.gestion.core.util.Formatters
import com.iglesiaflow.gestion.data.local.entity.MemberEntity
import com.iglesiaflow.gestion.data.local.entity.MinistryNeedEntity
import com.iglesiaflow.gestion.data.local.entity.ServiceRecordEntity
import com.iglesiaflow.gestion.data.local.entity.VolunteerSkillEntity
import com.iglesiaflow.gestion.domain.model.SkillLevel
import com.iglesiaflow.gestion.ui.components.DateField
import com.iglesiaflow.gestion.ui.components.DropdownField
import com.iglesiaflow.gestion.ui.components.FormDialog
import com.iglesiaflow.gestion.ui.components.FormTextField
import com.iglesiaflow.gestion.ui.components.SectionCard
import com.iglesiaflow.gestion.ui.components.StatCard

private val tabs = listOf("Voluntarios", "Necesidades", "Matching", "Horas")

@Composable
fun VolunteersScreen(viewModel: VolunteersViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var tabIndex by remember { mutableStateOf(0) }
    var showSkill by remember { mutableStateOf(false) }
    var showNeed by remember { mutableStateOf(false) }
    var showRecord by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            if (state.canEdit) {
                FloatingActionButton(onClick = {
                    when (tabIndex) {
                        1 -> showNeed = true
                        3 -> showRecord = true
                        else -> showSkill = true
                    }
                }) { Icon(Icons.Filled.Add, contentDescription = "Añadir") }
            }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            TabRow(selectedTabIndex = tabIndex) {
                tabs.forEachIndexed { index, title ->
                    Tab(selected = tabIndex == index, onClick = { tabIndex = index }, text = { Text(title) })
                }
            }
            when (tabIndex) {
                0 -> LazyColumn(
                    contentPadding = PaddingValues(16.dp, 12.dp, 16.dp, 96.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.skills, key = { it.id }) { skill ->
                        Card(Modifier.fillMaxWidth()) {
                            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) {
                                    Text(state.memberName(skill.memberId), style = MaterialTheme.typography.titleSmall)
                                    Text(
                                        "${skill.skill} · ${skill.level.label}",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                    Text(
                                        "Disponibilidad: ${skill.availability.ifBlank { "—" }}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                if (state.canEdit) {
                                    IconButton(onClick = { viewModel.deleteSkill(skill.id) }) {
                                        Icon(Icons.Filled.Delete, contentDescription = "Eliminar")
                                    }
                                }
                            }
                        }
                    }
                    if (state.skills.isEmpty()) item { Text("Registra dones y talentos de los miembros.") }
                }

                1 -> LazyColumn(
                    contentPadding = PaddingValues(16.dp, 12.dp, 16.dp, 96.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.needs, key = { it.id }) { need ->
                        Card(Modifier.fillMaxWidth()) {
                            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) {
                                    Text(need.ministry, style = MaterialTheme.typography.titleSmall)
                                    Text(
                                        "Necesita: ${need.skillRequired} · ${need.slots} plaza(s) · ${need.dayOfWeek}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                if (state.canEdit) {
                                    IconButton(onClick = { viewModel.deleteNeed(need.id) }) {
                                        Icon(Icons.Filled.Delete, contentDescription = "Eliminar")
                                    }
                                }
                            }
                        }
                    }
                    if (state.needs.isEmpty()) item { Text("Define las necesidades de cada ministerio.") }
                }

                2 -> LazyColumn(
                    contentPadding = PaddingValues(16.dp, 12.dp, 16.dp, 96.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(state.matches) { match ->
                        SectionCard(title = "${match.need.ministry} · ${match.need.skillRequired}") {
                            if (match.candidates.isEmpty()) {
                                Text("Sin voluntarios compatibles todavía.")
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    match.candidates.forEach { candidate ->
                                        Row(
                                            Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(state.memberName(candidate.memberId))
                                            AssistChip(onClick = {}, label = { Text(candidate.level.label) })
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if (state.matches.isEmpty()) item { Text("Añade necesidades para ver el matching automático.") }
                }

                else -> LazyColumn(
                    contentPadding = PaddingValues(16.dp, 12.dp, 16.dp, 96.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            StatCard(
                                title = "Horas de servicio",
                                value = Formatters.decimal(state.totalHours),
                                subtitle = "este año",
                                modifier = Modifier.weight(1f)
                            )
                            StatCard(
                                title = "Voluntarios",
                                value = state.skills.map { it.memberId }.distinct().size.toString(),
                                subtitle = "con habilidades registradas",
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    item {
                        SectionCard(title = "Ranking de servicio") {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                state.ranking.forEach { entry ->
                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("${entry.firstName} ${entry.lastName}")
                                        Text("${Formatters.decimal(entry.hours)} h")
                                    }
                                }
                                if (state.ranking.isEmpty()) Text("Sin horas registradas.")
                            }
                        }
                    }
                    items(state.records, key = { it.id }) { record ->
                        Card(Modifier.fillMaxWidth()) {
                            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) {
                                    Text(state.memberName(record.memberId), style = MaterialTheme.typography.titleSmall)
                                    Text(
                                        "${record.ministry} · ${DateTimeUtils.formatDate(record.date)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text("${Formatters.decimal(record.hours)} h")
                                if (state.canEdit) {
                                    IconButton(onClick = { viewModel.deleteRecord(record.id) }) {
                                        Icon(Icons.Filled.Delete, contentDescription = "Eliminar")
                                    }
                                }
                            }
                        }
                    }
                    item { Spacer(Modifier.height(40.dp)) }
                }
            }
        }
    }

    if (showSkill) {
        var member by remember { mutableStateOf<MemberEntity?>(state.members.firstOrNull()) }
        var skill by remember { mutableStateOf("") }
        var level by remember { mutableStateOf(SkillLevel.BASICO) }
        var availability by remember { mutableStateOf("") }
        FormDialog(
            title = "Registrar habilidad",
            onDismiss = { showSkill = false },
            confirmEnabled = member != null && skill.isNotBlank(),
            onConfirm = {
                viewModel.saveSkill(
                    VolunteerSkillEntity(
                        memberId = member?.id ?: 0L,
                        skill = skill.trim(),
                        level = level,
                        availability = availability.trim()
                    )
                )
                showSkill = false
            }
        ) {
            DropdownField("Miembro", state.members, member, { it.fullName }, { member = it })
            FormTextField("Habilidad / don", skill, { skill = it })
            DropdownField("Nivel", SkillLevel.entries, level, { it.label }, { level = it })
            FormTextField("Disponibilidad", availability, { availability = it }, supportingText = "Ej.: Domingo, Sábado")
        }
    }

    if (showNeed) {
        var ministry by remember { mutableStateOf("") }
        var required by remember { mutableStateOf("") }
        var slots by remember { mutableStateOf("1") }
        var day by remember { mutableStateOf("") }
        FormDialog(
            title = "Nueva necesidad",
            onDismiss = { showNeed = false },
            confirmEnabled = ministry.isNotBlank() && required.isNotBlank(),
            onConfirm = {
                viewModel.saveNeed(
                    MinistryNeedEntity(
                        ministry = ministry.trim(),
                        skillRequired = required.trim(),
                        slots = slots.toIntOrNull() ?: 1,
                        dayOfWeek = day.trim()
                    )
                )
                showNeed = false
            }
        ) {
            FormTextField("Ministerio", ministry, { ministry = it })
            FormTextField("Habilidad requerida", required, { required = it })
            FormTextField("Plazas", slots, { slots = it }, keyboardType = KeyboardType.Number)
            FormTextField("Día", day, { day = it })
        }
    }

    if (showRecord) {
        var member by remember { mutableStateOf<MemberEntity?>(state.members.firstOrNull()) }
        var ministry by remember { mutableStateOf("") }
        var hours by remember { mutableStateOf("2") }
        var date by remember { mutableStateOf(System.currentTimeMillis()) }
        FormDialog(
            title = "Registrar horas de servicio",
            onDismiss = { showRecord = false },
            confirmEnabled = member != null && ministry.isNotBlank(),
            onConfirm = {
                viewModel.saveRecord(
                    ServiceRecordEntity(
                        memberId = member?.id ?: 0L,
                        ministry = ministry.trim(),
                        hours = hours.replace(',', '.').toDoubleOrNull() ?: 1.0,
                        date = date
                    )
                )
                showRecord = false
            }
        ) {
            DropdownField("Voluntario", state.members, member, { it.fullName }, { member = it })
            FormTextField("Ministerio", ministry, { ministry = it })
            FormTextField("Horas", hours, { hours = it }, keyboardType = KeyboardType.Decimal)
            DateField("Fecha", date, { date = it })
        }
    }
}
