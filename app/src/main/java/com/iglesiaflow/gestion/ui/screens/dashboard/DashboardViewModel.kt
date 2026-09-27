package com.iglesiaflow.gestion.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iglesiaflow.gestion.core.config.AppSettings
import com.iglesiaflow.gestion.core.config.SettingsRepository
import com.iglesiaflow.gestion.core.security.SessionManager
import com.iglesiaflow.gestion.core.util.DateTimeUtils
import com.iglesiaflow.gestion.data.local.entity.EventEntity
import com.iglesiaflow.gestion.data.local.entity.MemberEntity
import com.iglesiaflow.gestion.data.repository.CommunicationRepository
import com.iglesiaflow.gestion.data.repository.EventRepository
import com.iglesiaflow.gestion.data.repository.FinanceRepository
import com.iglesiaflow.gestion.data.repository.GroupRepository
import com.iglesiaflow.gestion.data.repository.MemberRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class DashboardUiState(
    val settings: AppSettings = AppSettings(),
    val userName: String = "",
    val totalMembers: Int = 0,
    val activeMembers: Int = 0,
    val newMembers: Int = 0,
    val families: Int = 0,
    val groups: Int = 0,
    val donationsThisMonth: Double = 0.0,
    val donationsThisYear: Double = 0.0,
    val expensesThisMonth: Double = 0.0,
    val donationTrend: List<Pair<String, Double>> = emptyList(),
    val upcomingEvents: List<EventEntity> = emptyList(),
    val birthdays: List<MemberEntity> = emptyList(),
    val openPrayers: Int = 0,
    val activeCheckIns: Int = 0,
    val attendanceThisMonth: Int = 0
)

private data class Counters(val total: Int, val active: Int, val new: Int, val families: Int, val groups: Int)
private data class FinanceSnapshot(
    val month: Double,
    val year: Double,
    val expenses: Double,
    val trend: List<Pair<String, Double>>
)
private data class ActivitySnapshot(
    val events: List<EventEntity>,
    val birthdays: List<MemberEntity>,
    val prayers: Int,
    val checkIns: Int,
    val attendance: Int
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    memberRepository: MemberRepository,
    financeRepository: FinanceRepository,
    eventRepository: EventRepository,
    groupRepository: GroupRepository,
    communicationRepository: CommunicationRepository,
    settingsRepository: SettingsRepository,
    sessionManager: SessionManager
) : ViewModel() {

    private val monthStart = DateTimeUtils.startOfMonth()
    private val yearStart = DateTimeUtils.startOfYear()
    private val now = System.currentTimeMillis()

    private val counters = combine(
        memberRepository.totalMembers(),
        memberRepository.activeMembers(),
        memberRepository.newMembersSince(monthStart),
        memberRepository.totalFamilies(),
        groupRepository.activeGroups()
    ) { total, active, new, families, groups -> Counters(total, active, new, families, groups) }

    private val finance = combine(
        financeRepository.totalBetween(monthStart, now),
        financeRepository.totalBetween(yearStart, now),
        financeRepository.expensesBetween(monthStart, now),
        financeRepository.byMonth(DateTimeUtils.monthsAgo(5), now)
    ) { month, year, expenses, trend ->
        FinanceSnapshot(month, year, expenses, trend.map { DateTimeUtils.formatPeriod(it.period) to it.total })
    }

    private val activity = combine(
        eventRepository.upcoming(5),
        memberRepository.birthdays(DateTimeUtils.currentMonth()),
        communicationRepository.openPrayerCount(),
        eventRepository.activeCheckInCount(),
        eventRepository.attendanceSince(monthStart)
    ) { events, birthdays, prayers, checkIns, attendance ->
        ActivitySnapshot(events, birthdays, prayers, checkIns, attendance)
    }

    val uiState: StateFlow<DashboardUiState> = combine(
        counters,
        finance,
        activity,
        settingsRepository.settings,
        sessionManager.currentUser
    ) { counters, finance, activity, settings, user ->
        DashboardUiState(
            settings = settings,
            userName = user?.name.orEmpty(),
            totalMembers = counters.total,
            activeMembers = counters.active,
            newMembers = counters.new,
            families = counters.families,
            groups = counters.groups,
            donationsThisMonth = finance.month,
            donationsThisYear = finance.year,
            expensesThisMonth = finance.expenses,
            donationTrend = finance.trend,
            upcomingEvents = activity.events,
            birthdays = activity.birthdays,
            openPrayers = activity.prayers,
            activeCheckIns = activity.checkIns,
            attendanceThisMonth = activity.attendance
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DashboardUiState())
}
