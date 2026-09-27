package com.iglesiaflow.gestion.ui.screens.finance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iglesiaflow.gestion.core.config.AppSettings
import com.iglesiaflow.gestion.core.config.SettingsRepository
import com.iglesiaflow.gestion.core.security.SessionManager
import com.iglesiaflow.gestion.core.util.DateTimeUtils
import com.iglesiaflow.gestion.core.util.ExportManager
import com.iglesiaflow.gestion.core.util.Formatters
import com.iglesiaflow.gestion.data.local.entity.DepositEntity
import com.iglesiaflow.gestion.data.local.entity.DonationEntity
import com.iglesiaflow.gestion.data.local.entity.EnvelopeEntity
import com.iglesiaflow.gestion.data.local.entity.ExpenseEntity
import com.iglesiaflow.gestion.data.local.entity.FundEntity
import com.iglesiaflow.gestion.data.local.entity.MemberEntity
import com.iglesiaflow.gestion.data.local.entity.PledgeEntity
import com.iglesiaflow.gestion.data.repository.FinanceRepository
import com.iglesiaflow.gestion.data.repository.MemberRepository
import com.iglesiaflow.gestion.data.repository.PledgeProgress
import com.iglesiaflow.gestion.domain.model.Permission
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class FinancePeriod(val label: String) {
    MES("Este mes"), TRIMESTRE("90 días"), ANIO("Este año"), HISTORICO("Histórico");

    fun from(): Long = when (this) {
        MES -> DateTimeUtils.startOfMonth()
        TRIMESTRE -> System.currentTimeMillis() - 90L * 24 * 3600 * 1000
        ANIO -> DateTimeUtils.startOfYear()
        HISTORICO -> 0L
    }
}

data class FinanceUiState(
    val period: FinancePeriod = FinancePeriod.MES,
    val donations: List<DonationEntity> = emptyList(),
    val members: List<MemberEntity> = emptyList(),
    val funds: List<FundEntity> = emptyList(),
    val pledges: List<PledgeProgress> = emptyList(),
    val envelopes: List<EnvelopeEntity> = emptyList(),
    val deposits: List<DepositEntity> = emptyList(),
    val expenses: List<ExpenseEntity> = emptyList(),
    val totalIncome: Double = 0.0,
    val totalExpenses: Double = 0.0,
    val byFund: List<Pair<String, Double>> = emptyList(),
    val byType: List<Pair<String, Double>> = emptyList(),
    val trend: List<Pair<String, Double>> = emptyList(),
    val settings: AppSettings = AppSettings(),
    val canEdit: Boolean = false,
    val canDeposit: Boolean = false,
    val message: String? = null
) {
    fun memberName(id: Long?): String =
        members.firstOrNull { it.id == id }?.fullName ?: "Anónimo"

    fun fundName(id: Long?): String = funds.firstOrNull { it.id == id }?.name ?: "Sin fondo"
}

@HiltViewModel
class FinanceViewModel @Inject constructor(
    private val repository: FinanceRepository,
    private val memberRepository: MemberRepository,
    private val exportManager: ExportManager,
    private val sessionManager: SessionManager,
    settingsRepository: SettingsRepository
) : ViewModel() {

    private val period = MutableStateFlow(FinancePeriod.MES)
    private val message = MutableStateFlow<String?>(null)
    private val pledgeProgress = MutableStateFlow<List<PledgeProgress>>(emptyList())

    private val donationsFlow = repository.donations()

    private val catalogs = combine(
        memberRepository.members(),
        repository.funds(),
        repository.envelopes(DateTimeUtils.currentYear()),
        repository.deposits(),
        repository.expenses()
    ) { members, funds, envelopes, deposits, expenses ->
        Catalogs(members, funds, envelopes, deposits, expenses)
    }

    private data class Catalogs(
        val members: List<MemberEntity>,
        val funds: List<FundEntity>,
        val envelopes: List<EnvelopeEntity>,
        val deposits: List<DepositEntity>,
        val expenses: List<ExpenseEntity>
    )

    private val analytics = combine(
        repository.byFund(),
        repository.byType(),
        repository.byMonth(DateTimeUtils.monthsAgo(5), System.currentTimeMillis())
    ) { byFund, byType, trend ->
        Analytics(
            byFund.map { it.label to it.total },
            byType.map { it.label to it.total },
            trend.map { DateTimeUtils.formatPeriod(it.period) to it.total }
        )
    }

    private data class Analytics(
        val byFund: List<Pair<String, Double>>,
        val byType: List<Pair<String, Double>>,
        val trend: List<Pair<String, Double>>
    )

    val uiState: StateFlow<FinanceUiState> = combine(
        combine(donationsFlow, period) { donations, selected ->
            val from = selected.from()
            selected to donations.filter { it.date >= from }
        },
        catalogs,
        analytics,
        combine(pledgeProgress, message) { pledges, msg -> pledges to msg },
        settingsRepository.settings
    ) { periodAndDonations, catalogs, analytics, pledgesAndMessage, settings ->
        val (selectedPeriod, donations) = periodAndDonations
        FinanceUiState(
            period = selectedPeriod,
            donations = donations,
            members = catalogs.members,
            funds = catalogs.funds,
            pledges = pledgesAndMessage.first,
            envelopes = catalogs.envelopes,
            deposits = catalogs.deposits,
            expenses = catalogs.expenses.filter { it.date >= selectedPeriod.from() },
            totalIncome = donations.sumOf { it.amount },
            totalExpenses = catalogs.expenses.filter { it.date >= selectedPeriod.from() }.sumOf { it.amount },
            byFund = analytics.byFund,
            byType = analytics.byType,
            trend = analytics.trend,
            settings = settings,
            canEdit = sessionManager.has(Permission.FINANCE_EDIT),
            canDeposit = sessionManager.has(Permission.FINANCE_DEPOSIT),
            message = pledgesAndMessage.second
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), FinanceUiState())

    init {
        viewModelScope.launch {
            repository.pledges().collect { pledges -> refreshPledges(pledges) }
        }
    }

    private suspend fun refreshPledges(pledges: List<PledgeEntity>) {
        val members = memberRepository.allMembers()
        pledgeProgress.value = pledges.map { pledge ->
            PledgeProgress(
                pledge = pledge,
                memberName = members.firstOrNull { it.id == pledge.memberId }?.fullName ?: "—",
                contributed = repository.pledgeProgress(pledge)
            )
        }
    }

    fun onPeriodChange(value: FinancePeriod) { period.value = value }
    fun consumeMessage() { message.value = null }

    fun saveDonation(donation: DonationEntity) = guarded(Permission.FINANCE_EDIT) {
        repository.saveDonation(donation)
        message.value = "Donación registrada"
    }

    fun deleteDonation(id: Long) = guarded(Permission.FINANCE_EDIT) {
        repository.deleteDonation(id)
        message.value = "Donación eliminada"
    }

    fun savePledge(pledge: PledgeEntity) = guarded(Permission.FINANCE_EDIT) {
        repository.savePledge(pledge)
        message.value = "Promesa guardada"
    }

    fun deletePledge(id: Long) = guarded(Permission.FINANCE_EDIT) { repository.deletePledge(id) }

    fun saveEnvelope(envelope: EnvelopeEntity) = guarded(Permission.FINANCE_EDIT) {
        repository.saveEnvelope(envelope)
    }

    fun saveDeposit(deposit: DepositEntity, donationIds: List<Long>) = guarded(Permission.FINANCE_DEPOSIT) {
        val id = repository.saveDeposit(deposit)
        if (donationIds.isNotEmpty()) repository.assignToDeposit(id, donationIds)
        message.value = "Depósito guardado"
    }

    fun saveExpense(expense: ExpenseEntity) = guarded(Permission.FINANCE_EDIT) {
        repository.saveExpense(expense)
        message.value = "Gasto registrado"
    }

    fun deleteExpense(id: Long) = guarded(Permission.FINANCE_EDIT) { repository.deleteExpense(id) }

    fun exportDonationsCsv() {
        viewModelScope.launch {
            val state = uiState.value
            val file = exportManager.writeCsv(
                baseName = "donaciones",
                headers = listOf("Fecha", "Miembro", "Importe", "Tipo", "Método", "Fondo", "Sobre", "Referencia"),
                rows = state.donations.map { donation ->
                    listOf(
                        DateTimeUtils.formatDate(donation.date),
                        if (donation.anonymous) "Anónimo" else state.memberName(donation.memberId),
                        Formatters.decimal(donation.amount),
                        donation.type.label,
                        donation.method.label,
                        state.fundName(donation.fundId),
                        donation.envelopeNumber?.toString().orEmpty(),
                        donation.reference
                    )
                }
            )
            exportManager.share(file)
            message.value = "Exportado ${file.name}"
        }
    }

    fun exportDonationsPdf() {
        viewModelScope.launch {
            val state = uiState.value
            val file = exportManager.writePdf(
                baseName = "reporte_donaciones",
                title = "Reporte de donaciones",
                subtitle = "${state.period.label} · Total ${Formatters.money(state.totalIncome, state.settings.currencyCode)}",
                headers = listOf("Fecha", "Miembro", "Importe", "Tipo"),
                rows = state.donations.map {
                    listOf(
                        DateTimeUtils.formatDate(it.date),
                        if (it.anonymous) "Anónimo" else state.memberName(it.memberId),
                        Formatters.money(it.amount, state.settings.currencyCode),
                        it.type.label
                    )
                }
            )
            exportManager.share(file)
            message.value = "PDF generado"
        }
    }

    private fun guarded(permission: Permission, block: suspend () -> Unit) {
        if (!sessionManager.has(permission)) {
            message.value = "No tienes permisos para esta acción"
            return
        }
        viewModelScope.launch { block() }
    }
}
