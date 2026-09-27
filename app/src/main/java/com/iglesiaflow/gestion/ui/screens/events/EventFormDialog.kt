package com.iglesiaflow.gestion.ui.screens.events

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.KeyboardType
import com.iglesiaflow.gestion.data.local.entity.EventEntity
import com.iglesiaflow.gestion.data.local.entity.GroupEntity
import com.iglesiaflow.gestion.domain.model.EventType
import com.iglesiaflow.gestion.ui.components.DateField
import com.iglesiaflow.gestion.ui.components.DropdownField
import com.iglesiaflow.gestion.ui.components.FormDialog
import com.iglesiaflow.gestion.ui.components.FormTextField
import com.iglesiaflow.gestion.ui.components.SwitchRow
import java.time.ZoneId

private val recurrenceOptions = listOf(
    "NONE" to "Sin repetición",
    "DAILY" to "Diario",
    "WEEKLY" to "Semanal",
    "BIWEEKLY" to "Quincenal",
    "MONTHLY" to "Mensual"
)

@Composable
fun EventFormDialog(
    event: EventEntity?,
    groups: List<GroupEntity>,
    onDismiss: () -> Unit,
    onSave: (EventEntity, Boolean) -> Unit
) {
    var title by remember { mutableStateOf(event?.title.orEmpty()) }
    var description by remember { mutableStateOf(event?.description.orEmpty()) }
    var type by remember { mutableStateOf(event?.type ?: EventType.CULTO) }
    var location by remember { mutableStateOf(event?.location.orEmpty()) }
    var startAt by remember { mutableStateOf(event?.startAt ?: System.currentTimeMillis()) }
    var startHour by remember { mutableStateOf(event?.let { com.iglesiaflow.gestion.core.util.DateTimeUtils.formatTime(it.startAt) } ?: "11:00") }
    var durationHours by remember { mutableStateOf(((event?.let { (it.endAt - it.startAt) / 3_600_000.0 } ?: 2.0)).toString()) }
    var recurrence by remember { mutableStateOf(recurrenceOptions.firstOrNull { it.first == (event?.recurrence ?: "NONE") } ?: recurrenceOptions.first()) }
    var requiresCheckIn by remember { mutableStateOf(event?.requiresCheckIn ?: false) }
    var group by remember { mutableStateOf(groups.firstOrNull { it.id == event?.groupId }) }
    var capacity by remember { mutableStateOf(event?.capacity?.toString().orEmpty()) }
    var reminder by remember { mutableStateOf((event?.reminderMinutesBefore ?: 60).toString()) }
    var generateRecurrences by remember { mutableStateOf(false) }

    FormDialog(
        title = if (event == null) "Nuevo evento" else "Editar evento",
        onDismiss = onDismiss,
        confirmEnabled = title.isNotBlank(),
        onConfirm = {
            val parts = startHour.split(":")
            val hour = parts.getOrNull(0)?.toIntOrNull() ?: 11
            val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0
            val startDateTime = com.iglesiaflow.gestion.core.util.DateTimeUtils.localDateTime(startAt)
                .withHour(hour.coerceIn(0, 23)).withMinute(minute.coerceIn(0, 59))
            val start = com.iglesiaflow.gestion.core.util.DateTimeUtils.toMillis(startDateTime)
            val duration = (durationHours.replace(',', '.').toDoubleOrNull() ?: 2.0) * 3_600_000
            onSave(
                (event ?: EventEntity(title = "", startAt = start, endAt = start)).copy(
                    title = title.trim(),
                    description = description.trim(),
                    type = type,
                    location = location.trim(),
                    startAt = start,
                    endAt = start + duration.toLong(),
                    recurrence = recurrence.first,
                    requiresCheckIn = requiresCheckIn,
                    groupId = group?.id,
                    capacity = capacity.toIntOrNull(),
                    reminderMinutesBefore = reminder.toIntOrNull() ?: 60,
                    timezoneId = ZoneId.systemDefault().id
                ),
                generateRecurrences
            )
        }
    ) {
        FormTextField("Título", title, { title = it })
        FormTextField("Descripción", description, { description = it }, singleLine = false)
        DropdownField("Tipo", EventType.entries, type, { it.label }, { type = it })
        FormTextField("Lugar", location, { location = it })
        DateField("Fecha", startAt, { startAt = it })
        FormTextField("Hora de inicio (HH:mm)", startHour, { startHour = it })
        FormTextField("Duración (horas)", durationHours, { durationHours = it }, keyboardType = KeyboardType.Decimal)
        DropdownField("Repetición", recurrenceOptions, recurrence, { it.second }, { recurrence = it })
        if (!recurrence.first.equals("NONE", true)) {
            SwitchRow(
                "Generar próximas 8 repeticiones",
                "Crea las ocurrencias en el calendario",
                generateRecurrences
            ) { generateRecurrences = it }
        }
        DropdownField("Grupo asociado", groups, group, { it.name }, { group = it })
        FormTextField("Aforo", capacity, { capacity = it }, keyboardType = KeyboardType.Number)
        FormTextField("Recordatorio (minutos antes)", reminder, { reminder = it }, keyboardType = KeyboardType.Number)
        SwitchRow("Requiere check-in infantil", "Activa el control de entrada/salida", requiresCheckIn) {
            requiresCheckIn = it
        }
    }
}
