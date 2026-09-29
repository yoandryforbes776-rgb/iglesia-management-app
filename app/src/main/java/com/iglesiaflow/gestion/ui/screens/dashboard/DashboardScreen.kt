package com.iglesiaflow.gestion.ui.screens.dashboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.iglesiaflow.gestion.core.util.DateTimeUtils
import com.iglesiaflow.gestion.core.util.Formatters
import com.iglesiaflow.gestion.ui.components.Avatar
import com.iglesiaflow.gestion.ui.components.LineChart
import com.iglesiaflow.gestion.ui.components.SectionCard
import com.iglesiaflow.gestion.ui.components.StatCard
import com.iglesiaflow.gestion.ui.theme.StatusInfo
import com.iglesiaflow.gestion.ui.theme.StatusPositive
import com.iglesiaflow.gestion.ui.theme.StatusNegative
import com.iglesiaflow.gestion.ui.theme.StatusWarning

@Composable
fun DashboardScreen(
    onOpenMembers: () -> Unit,
    onOpenAttendance: () -> Unit,
    onOpenEvents: () -> Unit,
    onOpenEvent: (Long) -> Unit,
    onOpenMember: (Long) -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val currency = state.settings.currencyCode

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Column {
                Text("Hola, ${state.userName}", style = MaterialTheme.typography.headlineSmall)
                Text(
                    "Resumen de ${state.settings.churchName}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(
                    title = "Miembros activos",
                    value = state.activeMembers.toString(),
                    subtitle = "${state.totalMembers} en total",
                    icon = Icons.Filled.People,
                    modifier = Modifier.weight(1f),
                    onClick = onOpenMembers
                )
                StatCard(
                    title = "Seguimiento",
                    value = state.absenceAlerts.size.toString(),
                    subtitle = "faltan 2+ veces seguidas",
                    icon = Icons.Filled.NotificationsActive,
                    accent = if (state.absenceAlerts.isEmpty()) StatusPositive else StatusNegative,
                    modifier = Modifier.weight(1f),
                    onClick = onOpenAttendance
                )
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(
                    title = "Asistencia del mes",
                    value = state.attendanceThisMonth.toString(),
                    subtitle = "registros de presencia",
                    icon = Icons.Filled.TrendingUp,
                    accent = StatusInfo,
                    modifier = Modifier.weight(1f),
                    onClick = onOpenAttendance
                )
                StatCard(
                    title = "Nuevos este mes",
                    value = state.newMembers.toString(),
                    subtitle = "${state.families} familias · ${state.groups} grupos",
                    icon = Icons.Filled.Groups,
                    accent = StatusWarning,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        if (state.activeCheckIns > 0) {
            item {
                StatCard(
                    title = "Check-in infantil activo",
                    value = "${state.activeCheckIns} niños",
                    subtitle = "Pendientes de retirada",
                    icon = Icons.Filled.ChildCare,
                    accent = StatusWarning,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onOpenEvents
                )
            }
        }

        if (state.absenceAlerts.isNotEmpty()) {
            item {
                SectionCard(title = "Miembros que están faltando") {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        state.absenceAlerts.take(5).forEach { alert ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable(onClick = onOpenAttendance)
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(alert.name, style = MaterialTheme.typography.bodyMedium)
                                Text(
                                    "${alert.missedCount} faltas seguidas",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = StatusNegative
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            SectionCard(title = "Próximos eventos") {
                if (state.upcomingEvents.isEmpty()) {
                    Text("No hay eventos programados", style = MaterialTheme.typography.bodyMedium)
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        state.upcomingEvents.forEach { event ->
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .clickable { onOpenEvent(event.id) },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Avatar(name = event.title, size = 38)
                                Spacer(Modifier.padding(horizontal = 6.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(event.title, style = MaterialTheme.typography.bodyLarge)
                                    Text(
                                        "${DateTimeUtils.formatDateTime(event.startAt)} · ${event.location}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                if (event.requiresCheckIn) {
                                    androidx.compose.material3.AssistChip(
                                        onClick = { onOpenEvent(event.id) },
                                        label = { Text("Check-in") }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            SectionCard(title = "Cumpleaños de ${DateTimeUtils.formatMonth(System.currentTimeMillis())}") {
                if (state.birthdays.isEmpty()) {
                    Text("Sin cumpleaños este mes", style = MaterialTheme.typography.bodyMedium)
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        state.birthdays.forEach { member ->
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .clickable { onOpenMember(member.id) },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                androidx.compose.material3.Icon(
                                    Icons.Filled.Cake,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(Modifier.padding(horizontal = 6.dp))
                                Text(member.fullName, Modifier.weight(1f))
                                Text(
                                    DateTimeUtils.formatDate(member.birthDate),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(
                    title = "Peticiones de oración",
                    value = state.openPrayers.toString(),
                    subtitle = "abiertas",
                    icon = Icons.Filled.VolunteerActivism,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Check-in activos",
                    value = state.activeCheckIns.toString(),
                    subtitle = "niños sin retirar",
                    icon = Icons.Filled.CalendarMonth,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item { Spacer(Modifier.height(24.dp)) }
    }
}
