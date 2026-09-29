@file:OptIn(ExperimentalMaterial3Api::class)

package com.iglesiaflow.gestion.ui.screens.reports

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
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.iglesiaflow.gestion.core.util.Formatters
import com.iglesiaflow.gestion.ui.components.BarChart
import com.iglesiaflow.gestion.ui.components.DataTable
import com.iglesiaflow.gestion.ui.components.DropdownField
import com.iglesiaflow.gestion.ui.components.FormTextField
import com.iglesiaflow.gestion.ui.components.HorizontalBreakdown
import com.iglesiaflow.gestion.ui.components.LineChart
import com.iglesiaflow.gestion.ui.components.SectionCard
import com.iglesiaflow.gestion.ui.components.StatCard

private val tabs = listOf("Resumen", "Directorio", "Asistencia", "Seguimiento", "Consultas")

@Composable
fun ReportsScreen(viewModel: ReportsViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var tabIndex by remember { mutableStateOf(0) }
    val currency = state.settings.currencyCode

    LaunchedEffect(state.message) {
        state.message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.consumeMessage()
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            TabRow(selectedTabIndex = tabIndex) {
                tabs.forEachIndexed { index, title ->
                    Tab(selected = tabIndex == index, onClick = { tabIndex = index }, text = { Text(title) })
                }
            }
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                when (tabIndex) {
                    0 -> {
                        item {
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                StatCard(
                                    title = "Miembros",
                                    value = state.members.size.toString(),
                                    subtitle = "registrados",
                                    modifier = Modifier.weight(1f)
                                )
                                StatCard(
                                    title = "Seguimiento",
                                    value = state.absenceAlerts.size.toString(),
                                    subtitle = "faltan 2+ veces seguidas",
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                        item {
                            SectionCard(title = "Distribución por estado") {
                                HorizontalBreakdown(
                                    state.byStatus.map { it.status to it.total.toDouble() },
                                    { it.toInt().toString() }
                                )
                            }
                        }
                        item {
                            SectionCard(title = "Tendencia de asistencia (12 meses)") {
                                BarChart(state.attendanceTrend)
                            }
                        }
                    }

                    1 -> item {
                        SectionCard(title = "Directorio de miembros") {
                            DataTable(
                                headers = listOf("Nombre", "Teléfono", "Email", "Estado"),
                                rows = state.members.map {
                                    listOf(it.fullName, it.phone, it.email, it.status.label)
                                }
                            )
                        }
                    }

                    2 -> {
                        item {
                            SectionCard(title = "Asistencia por evento") {
                                HorizontalBreakdown(state.attendanceByEvent, { it.toInt().toString() })
                            }
                        }
                        item {
                            SectionCard(title = "Evolución mensual") { LineChart(state.attendanceTrend) }
                        }
                        if (state.canExport) {
                            item { OutlinedButton(onClick = viewModel::exportAttendanceCsv) { Text("Exportar CSV") } }
                        }
                    }

                    3 -> {
                        item {
                            SectionCard(title = "Miembros que están faltando") {
                                DataTable(
                                    headers = listOf("Miembro", "Faltas seguidas", "Última asistencia"),
                                    rows = state.absenceAlerts.map {
                                        listOf(
                                            it.name,
                                            it.missedCount.toString(),
                                            it.lastAttendedAt?.let { date ->
                                                com.iglesiaflow.gestion.core.util.DateTimeUtils.formatDate(date)
                                            } ?: "sin registro"
                                        )
                                    }
                                )
                            }
                        }
                        if (state.canExport) {
                            item {
                                Button(onClick = viewModel::exportAbsencesCsv) { Text("Exportar listado de ausencias") }
                            }
                        }
                    }

                    else -> item { QueryBuilder(state, viewModel) }
                }
                item { Spacer(Modifier.height(40.dp)) }
            }
        }
    }
}

@Composable
private fun QueryBuilder(state: ReportsUiState, viewModel: ReportsViewModel) {
    var entity by remember { mutableStateOf(QueryEntity.MIEMBROS) }
    var operator by remember { mutableStateOf(QueryOperator.CONTIENE) }
    var value by remember { mutableStateOf("") }
    val fields = when (entity) {
        QueryEntity.MIEMBROS -> listOf("Nombre", "Ciudad", "Estado", "Rol", "Edad", "Email")
        QueryEntity.EVENTOS -> listOf("Título", "Tipo", "Lugar")
    }
    var field by remember(entity) { mutableStateOf(fields.first()) }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SectionCard(title = "Constructor de consultas") {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                DropdownField("Entidad", QueryEntity.entries, entity, { it.label }, { entity = it })
                DropdownField("Campo", fields, field, { it }, { field = it })
                DropdownField("Condición", QueryOperator.entries, operator, { it.label }, { operator = it })
                FormTextField("Valor", value, { value = it })
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { viewModel.runQuery(entity, field, operator, value) }) { Text("Ejecutar") }
                    if (state.canExport) {
                        OutlinedButton(onClick = viewModel::exportQueryCsv) { Text("Exportar") }
                    }
                }
            }
        }
        if (state.queryResult.headers.isNotEmpty()) {
            SectionCard(title = "Resultados (${state.queryResult.rows.size})") {
                DataTable(headers = state.queryResult.headers, rows = state.queryResult.rows)
            }
        } else {
            Text(
                "Define una condición y pulsa Ejecutar para generar un reporte ad-hoc.",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
