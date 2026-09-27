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
import com.iglesiaflow.gestion.data.repository.FinanceRepository
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
enum class QueryEntity(val label: String) { MIEMBROS("Miembros"), DONACIONES("Donaciones"), EVENTOS("Eventos") }
enum class QueryOperator(val label: String) {
    CONTIENE("contiene"), IGUAL("es igual a"), MAYOR("es mayor que"), MENOR("es menor que")
}

data class QueryResult(val headers: List<String> = emptyList(), val rows: List<List<String>> = emptyList())

data class ReportsUiState(
    val members: List<MemberEntity> = emptyList(),
    val byStatus: List<StatusCount> = emptyList(),
    val donationTrend: List<Pair<String, Double>> = emptyList(),
    val donationsByFund: List<Pair<String, Double>> = emptyList(),
    val attendanceTrend: List<Pair<String, Double>> = emptyList(),
    val attendanceByEvent: List<Pair<String, Double>> = emptyList(),
    val topDonors: List<Pair<String, Double>> = emptyList(),
    val incomeYear: Double = 0.0,
    val expensesYear: Double = 0.0,
    val settings: AppSettings = AppSettings(),
    val queryResult: QueryResult = QueryResult(),
    val canExport: Boolean = false,
    val message: String? = null
)

@HiltViewModel
class ReportsViewModel @Inject constructor(
    private val memberRepository: MemberRepository,
    private val financeRepository: FinanceRepository,
    private val eventRepository: EventRepository,
    private val exportManager: ExportManager,
    private val sessionManager: SessionManager,
    settingsRepository: SettingsRepository
) : ViewModel() {

    private val yearStart = DateTimeUtils.startOfYear()
    private val now = System.currentTimeMillis()
    private val queryResult = MutableStateFlow(QueryResult())
    private val message = MutableStateFlow<String?>(null)

    private val financials = combine(
        financeRepository.byMonth(DateTimeUtils.monthsAgo(11), now),
        financeRepository.byFund(),
        financeRepository.topDonors(yearStart, now, 10),
        financeRepository.totalBetween(yearStart, now),
        financeRepository.expensesBetween(yearStart, now)
    ) { trend, byFund, donors, income, expenses ->
        Financials(
            trend.map { DateTimeUtils.formatPeriod(it.period) to it.total },
            byFund.map { it.label to it.total },
            donors.map { it.label to it.total },
            income,
            expenses
        )
    }

    private data class Financials(
        val trend: List<Pair<String, Double>>,
        val byFund: List<Pair<String, Double>>,
        val donors: List<Pair<String, Double>>,
        val income: Double,
        val expenses: Double
    )

    private val attendance = combine(
        eventRepository.attendanceByMonth(DateTimeUtils.monthsAgo(11), now),
        eventRepository.attendanceByEvent(8)
    ) { trend, byEvent ->
        trend.map { DateTimeUtils.formatPeriod(it.period) to it.total } to byEvent.map { it.label to it.total }
    }

    val uiState: StateFlow<ReportsUiState> = combine(
        combine(memberRepository.members(), memberRepository.membersByStatus()) { members, status -> members to status },
        financials,
        attendance,
        combine(queryResult, message) { result, msg -> result to msg },
        settingsRepository.settings
    ) { membersAndStatus, finance, attendanceData, queryAndMessage, settings ->
        ReportsUiState(
            members = membersAndStatus.first,
            byStatus = membersAndStatus.second,
            donationTrend = finance.trend,
            donationsByFund = finance.byFund,
            attendanceTrend = attendanceData.first,
            attendanceByEvent = attendanceData.second,
            topDonors = finance.donors,
            incomeYear = finance.income,
            expensesYear = finance.expenses,
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
                QueryEntity.DONACIONES -> {
                    val members = memberRepository.allMembers().associateBy { it.id }
                    val donations = financeRepository.allDonations().filter { donation ->
                        val candidate = when (field) {
                            "Importe" -> donation.amount.toString()
                            "Tipo" -> donation.type.label
                            "Método" -> donation.method.label
                            else -> members[donation.memberId]?.fullName.orEmpty()
                        }
                        matches(candidate, operator, value)
                    }
                    QueryResult(
                        headers = listOf("Fecha", "Miembro", "Importe", "Tipo"),
                        rows = donations.map {
                            listOf(
                                DateTimeUtils.formatDate(it.date),
                                members[it.memberId]?.fullName ?: "Anónimo",
                                Formatters.decimal(it.amount),
                                it.type.label
                            )
                        }
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

    fun exportFinancialPdf() {
        viewModelScope.launch {
            val state = uiState.value
            val file = exportManager.writePdf(
                baseName = "reporte_financiero",
                title = "Reporte financiero anual",
                subtitle = "${state.settings.churchName} · ingresos " +
                    "${Formatters.money(state.incomeYear, state.settings.currencyCode)} · gastos " +
                    Formatters.money(state.expensesYear, state.settings.currencyCode),
                headers = listOf("Concepto", "Importe"),
                rows = state.donationsByFund.map {
                    listOf(it.first, Formatters.money(it.second, state.settings.currencyCode))
                }
            )
            exportManager.share(file)
            message.value = "Reporte financiero generado"
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
