@file:OptIn(ExperimentalMaterial3Api::class)

package com.iglesiaflow.gestion.ui.screens.attendance

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.iglesiaflow.gestion.core.util.DateTimeUtils
import com.iglesiaflow.gestion.data.local.dao.MemberAttendanceRow
import com.iglesiaflow.gestion.ui.components.InfoBanner
import com.iglesiaflow.gestion.ui.components.SectionCard
import com.iglesiaflow.gestion.ui.theme.StatusNegative
import com.iglesiaflow.gestion.ui.theme.StatusPositive
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun MemberAttendanceScreen(
    onBack: () -> Unit,
    viewModel: MemberAttendanceViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val monthName = state.month.month
        .getDisplayName(TextStyle.FULL, Locale.getDefault())
        .replaceFirstChar { it.uppercase() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.member?.fullName ?: "Asistencia") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
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
            if (state.consecutiveAbsences >= 2) {
                item {
                    InfoBanner(
                        "Atención: ha faltado ${state.consecutiveAbsences} veces seguidas. " +
                            "Conviene contactar con el miembro."
                    )
                }
            }

            item {
                SectionCard(title = "$monthName ${state.month.year}") {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = viewModel::previousMonth) {
                                Icon(Icons.Filled.ChevronLeft, contentDescription = "Mes anterior")
                            }
                            TextButton(onClick = viewModel::today) { Text("Hoy") }
                            IconButton(onClick = viewModel::nextMonth) {
                                Icon(Icons.Filled.ChevronRight, contentDescription = "Mes siguiente")
                            }
                        }
                        MonthCalendar(
                            year = state.month.year,
                            month = state.month.monthValue,
                            rows = state.rows
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            LegendDot(StatusPositive, "Presente")
                            LegendDot(StatusNegative, "Faltó")
                        }
                        Text(
                            "Este mes: ${state.attendedMonth} de ${state.totalMonth} eventos " +
                                "(${state.monthPercentage} %)",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            "Año ${state.month.year}: ${state.attendedYear} de ${state.totalYear} " +
                                "(${state.yearPercentage} %)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            item {
                SectionCard(title = "Detalle del mes") {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (state.rows.isEmpty()) {
                            Text("No hubo eventos este mes", style = MaterialTheme.typography.bodyMedium)
                        }
                        state.rows.sortedByDescending { it.startAt }.forEach { row ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(row.title, style = MaterialTheme.typography.bodyLarge)
                                    Text(
                                        DateTimeUtils.formatDateTime(row.startAt),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(
                                    if (row.present) "Presente" else "Faltó",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = if (row.present) StatusPositive else StatusNegative
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color, RoundedCornerShape(50))
        )
        Text(label, style = MaterialTheme.typography.bodySmall)
    }
}

/** Rejilla mensual: cada día con evento se pinta verde (presente) o rojo (ausente). */
@Composable
private fun MonthCalendar(year: Int, month: Int, rows: List<MemberAttendanceRow>) {
    val zone = ZoneId.systemDefault()
    val byDay = rows.groupBy { row ->
        Instant.ofEpochMilli(row.startAt).atZone(zone).toLocalDate().dayOfMonth
    }
    val firstDay = LocalDate.of(year, month, 1)
    val blanks = (firstDay.dayOfWeek.value - DayOfWeek.MONDAY.value + 7) % 7
    val daysInMonth = firstDay.lengthOfMonth()
    val cells = List(blanks) { 0 } + (1..daysInMonth).toList()
    val weeks = cells.chunked(7)
    val today = LocalDate.now()

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(modifier = Modifier.fillMaxWidth()) {
            listOf("L", "M", "X", "J", "V", "S", "D").forEach { label ->
                Text(
                    label,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        weeks.forEach { week ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                week.forEach { day ->
                    val events = byDay[day].orEmpty()
                    val present = events.any { it.present }
                    val background = when {
                        day == 0 || events.isEmpty() -> Color.Transparent
                        present -> StatusPositive
                        else -> StatusNegative
                    }
                    val isToday = day != 0 && today == LocalDate.of(year, month, day)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .background(background, RoundedCornerShape(8.dp))
                            .then(
                                if (isToday) {
                                    Modifier.border(
                                        1.dp,
                                        MaterialTheme.colorScheme.primary,
                                        RoundedCornerShape(8.dp)
                                    )
                                } else {
                                    Modifier
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (day != 0) {
                            Text(
                                day.toString(),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = if (events.isEmpty()) FontWeight.Normal else FontWeight.Bold,
                                color = if (events.isEmpty()) {
                                    MaterialTheme.colorScheme.onSurface
                                } else {
                                    Color.White
                                }
                            )
                        }
                    }
                }
                repeat(7 - week.size) { Box(modifier = Modifier.weight(1f).aspectRatio(1f)) }
            }
        }
    }
}
