package com.iglesiaflow.gestion.ui.screens.attendance

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.iglesiaflow.gestion.core.util.DateTimeUtils
import com.iglesiaflow.gestion.ui.components.Avatar
import com.iglesiaflow.gestion.ui.components.EmptyState
import com.iglesiaflow.gestion.ui.components.SearchField
import com.iglesiaflow.gestion.ui.components.SectionCard
import com.iglesiaflow.gestion.ui.theme.StatusNegative
import com.iglesiaflow.gestion.ui.theme.StatusPositive
import com.iglesiaflow.gestion.ui.theme.StatusWarning

@Composable
fun AttendanceScreen(
    onOpenMemberAttendance: (Long) -> Unit,
    viewModel: AttendanceViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Column {
                Text("Asistencia", style = MaterialTheme.typography.headlineSmall)
                Text(
                    "Historial por miembro y avisos de ausencias reiteradas " +
                        "(${state.pastEvents} eventos celebrados)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (state.alerts.isEmpty()) {
                        MaterialTheme.colorScheme.surfaceVariant
                    } else {
                        MaterialTheme.colorScheme.errorContainer
                    }
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Filled.NotificationsActive, contentDescription = null)
                        Text(
                            "Seguimiento pastoral",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    if (state.alerts.isEmpty()) {
                        Text(
                            "Nadie acumula 2 faltas seguidas. ¡Buen trabajo!",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    } else {
                        Text(
                            "${state.alerts.size} miembro(s) han faltado 2 o más veces seguidas:",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        state.alerts.forEach { alert ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onOpenMemberAttendance(alert.memberId) }
                                    .padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Avatar(name = alert.name, size = 36)
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(alert.name, style = MaterialTheme.typography.bodyLarge)
                                    Text(
                                        "Faltó ${alert.missedCount} veces seguidas · última vez presente: " +
                                            (alert.lastAttendedAt?.let { DateTimeUtils.formatDate(it) } ?: "sin registro"),
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                                Text(
                                    alert.missedCount.toString(),
                                    style = MaterialTheme.typography.titleLarge,
                                    color = StatusNegative
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            SearchField(
                value = state.query,
                onValueChange = viewModel::search,
                placeholder = "Buscar miembro"
            )
        }

        item {
            Text(
                "Asistencia a los últimos 8 eventos",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (state.members.isEmpty()) {
            item { EmptyState(title = "Sin miembros", subtitle = "Aún no hay miembros registrados") }
        }

        items(state.members, key = { it.first.id }) { (member, percentage) ->
            SectionCard(
                title = member.fullName,
                modifier = Modifier.clickable { onOpenMemberAttendance(member.id) }
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    LinearProgressIndicator(
                        progress = { percentage / 100f },
                        modifier = Modifier.fillMaxWidth(),
                        color = when {
                            percentage >= 70 -> StatusPositive
                            percentage >= 40 -> StatusWarning
                            else -> StatusNegative
                        }
                    )
                    Text("$percentage % de asistencia", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}
