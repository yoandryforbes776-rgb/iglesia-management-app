package com.iglesiaflow.gestion.ui.screens.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iglesiaflow.gestion.core.config.AppSettings
import com.iglesiaflow.gestion.core.config.SettingsRepository
import com.iglesiaflow.gestion.core.security.SessionManager
import com.iglesiaflow.gestion.core.util.DateTimeUtils
import com.iglesiaflow.gestion.core.util.ExportManager
import com.iglesiaflow.gestion.core.util.Formatters
import com.iglesiaflow.gestion.data.local.dao.StatusCount
import com.iglesiaflow.gestion.data.local.entity.MemberEntity
import com.iglesiaflow.gestion.data.repository.EventRepository
import com.iglesiaflow.gestion.data.repository.AbsenceAlert
import com.iglesiaflow.gestion.data.repository.AttendanceRepository
import com.iglesiaflow.gestion.data.repository.MemberRepository
import com.iglesiaflow.gestion.domain.model.Permission
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Entidades disponibles en el constructor de consultas ad-hoc. */
enum class QueryEntity(val label: String) { MIEMBROS("Miembros"), EVENTOS("Eventos") }
enum class QueryOperator(val label: String) {
    CONTIENE("contiene"), IGUAL("es igual a"), MAYOR("es mayor que"), MENOR("es menor que")
}

data class QueryResult(val headers: List<String> = emptyList(), val rows: List<List<String>> = emptyList())

data class ReportsUiState(
    val members: List<MemberEntity> = emptyList(),
    val byStatus: List<StatusCount> = emptyList(),
    val attendanceTrend: List<Pair<String, Double>> = emptyList(),
    val attendanceByEvent: List<Pair<String, Double>> = emptyList(),
    val absenceAlerts: List<AbsenceAlert> = emptyList(),
    val settings: AppSettings = AppSettings(),
    val queryResult: QueryResult = QueryResult(),
    val canExport: Boolean = false,
    val message: String? = null
)

@HiltViewModel
class ReportsViewModel @Inject constructor(
    private val memberRepository: MemberRepository,
    private val attendanceRepository: AttendanceRepository,
    private val eventRepository: EventRepository,
    private val exportManager: ExportManager,
    private val sessionManager: SessionManager,
    settingsRepository: SettingsRepository
) : ViewModel() {

    private val now = System.currentTimeMillis()
    private val queryResult = MutableStateFlow(QueryResult())
    private val message = MutableStateFlow<String?>(null)

    private val attendance = combine(
        eventRepository.attendanceByMonth(DateTimeUtils.monthsAgo(11), now),
        eventRepository.attendanceByEvent(8)
    ) { trend, byEvent ->
        trend.map { DateTimeUtils.formatPeriod(it.period) to it.total } to byEvent.map { it.label to it.total }
    }

    val uiState: StateFlow<ReportsUiState> = combine(
        combine(memberRepository.members(), memberRepository.membersByStatus()) { members, status -> members to status },
        attendanceRepository.absenceAlerts(),
        attendance,
        combine(queryResult, message) { result, msg -> result to msg },
        settingsRepository.settings
    ) { membersAndStatus, alerts, attendanceData, queryAndMessage, settings ->
        ReportsUiState(
            members = membersAndStatus.first,
            byStatus = membersAndStatus.second,
            attendanceTrend = attendanceData.first,
            attendanceByEvent = attendanceData.second,
            absenceAlerts = alerts,
            settings = settings,
            queryResult = queryAndMessage.first,
            canExport = sessionManager.has(Permission.REPORTS_EXPORT),
            message = queryAndMessage.second
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ReportsUiState())

    fun consumeMessage() { message.value = null }

    /** Constructor de consultas personalizadas, evaluado en memoria. */
    fun runQuery(entity: QueryEntity, field: String, operator: QueryOperator, value: String) {
        viewModelScope.launch {
            val result = when (entity) {
                QueryEntity.MIEMBROS -> {
                    val members = memberRepository.allMembers().filter { member ->
                        val candidate = when (field) {
                            "Nombre" -> member.fullName
                            "Ciudad" -> member.city
                            "Estado" -> member.status.label
                            "Rol" -> member.churchRole
                            "Edad" -> (DateTimeUtils.age(member.birthDate) ?: 0).toString()
                            else -> member.email
                        }
                        matches(candidate, operator, value)
                    }
                    QueryResult(
                        headers = listOf("Nombre", "Estado", "Teléfono", "Email", "Ciudad"),
                        rows = members.map { listOf(it.fullName, it.status.label, it.phone, it.email, it.city) }
                    )
                }
                QueryEntity.EVENTOS -> {
                    val events = eventRepository.allEvents().filter { event ->
                        val candidate = when (field) {
                            "Título" -> event.title
                            "Tipo" -> event.type.label
                            else -> event.location
                        }
                        matches(candidate, operator, value)
                    }
                    QueryResult(
                        headers = listOf("Evento", "Fecha", "Tipo", "Lugar"),
                        rows = events.map {
                            listOf(it.title, DateTimeUtils.formatDateTime(it.startAt), it.type.label, it.location)
                        }
                    )
                }
            }
            queryResult.value = result
            message.value = "${result.rows.size} resultados"
        }
    }

    private fun matches(candidate: String, operator: QueryOperator, value: String): Boolean = when (operator) {
        QueryOperator.CONTIENE -> candidate.contains(value, ignoreCase = true)
        QueryOperator.IGUAL -> candidate.equals(value, ignoreCase = true)
        QueryOperator.MAYOR -> (candidate.toDoubleOrNull() ?: 0.0) > (value.toDoubleOrNull() ?: 0.0)
        QueryOperator.MENOR -> (candidate.toDoubleOrNull() ?: 0.0) < (value.toDoubleOrNull() ?: 0.0)
    }

    fun exportQueryCsv() {
        val result = uiState.value.queryResult
        if (result.rows.isEmpty()) {
            message.value = "No hay resultados que exportar"
            return
        }
        viewModelScope.launch {
            val file = exportManager.writeCsv("consulta_personalizada", result.headers, result.rows)
            exportManager.share(file)
            message.value = "Consulta exportada"
        }
    }

    fun exportAbsencesCsv() {
        viewModelScope.launch {
            val alerts = uiState.value.absenceAlerts
            if (alerts.isEmpty()) {
                message.value = "No hay ausencias reiteradas que exportar"
                return@launch
            }
            val file = exportManager.writeCsv(
                baseName = "ausencias_reiteradas",
                headers = listOf("Miembro", "Faltas seguidas", "Teléfono", "Última asistencia"),
                rows = alerts.map {
                    listOf(
                        it.name,
                        it.missedCount.toString(),
                        it.phone,
                        it.lastAttendedAt?.let { date -> DateTimeUtils.formatDate(date) } ?: "sin registro"
                    )
                }
            )
            exportManager.share(file)
            message.value = "Listado de ausencias exportado"
        }
    }

    fun exportAttendanceCsv() {
        viewModelScope.launch {
            val state = uiState.value
            val file = exportManager.writeCsv(
                baseName = "analitica_asistencia",
                headers = listOf("Periodo", "Asistentes"),
                rows = state.attendanceTrend.map { listOf(it.first, it.second.toInt().toString()) }
            )
            exportManager.share(file)
            message.value = "Analítica exportada"
        }
    }
}
