package com.iglesiaflow.gestion.ui.screens.attendance

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iglesiaflow.gestion.core.util.DateTimeUtils
import com.iglesiaflow.gestion.data.local.dao.MemberAttendanceRow
import com.iglesiaflow.gestion.data.local.entity.MemberEntity
import com.iglesiaflow.gestion.data.repository.AttendanceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

data class MemberAttendanceUiState(
    val member: MemberEntity? = null,
    val month: YearMonth = YearMonth.now(),
    val rows: List<MemberAttendanceRow> = emptyList(),
    val attendedMonth: Int = 0,
    val totalMonth: Int = 0,
    val attendedYear: Int = 0,
    val totalYear: Int = 0,
    val consecutiveAbsences: Int = 0
) {
    val monthPercentage: Int get() = if (totalMonth == 0) 0 else attendedMonth * 100 / totalMonth
    val yearPercentage: Int get() = if (totalYear == 0) 0 else attendedYear * 100 / totalYear
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class MemberAttendanceViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: AttendanceRepository
) : ViewModel() {

    private val memberId: Long = savedStateHandle.get<Long>("memberId") ?: 0L
    private val month = MutableStateFlow(YearMonth.now())

    private val monthRows = month.flatMapLatest { ym ->
        repository.memberAttendance(memberId, ym.startMillis(), ym.endMillis())
    }

    private val yearRows = month.flatMapLatest { ym ->
        repository.memberAttendance(
            memberId,
            DateTimeUtils.toMillis(LocalDate.of(ym.year, 1, 1)),
            DateTimeUtils.toMillis(LocalDate.of(ym.year, 12, 31).plusDays(1)) - 1
        )
    }

    val uiState: StateFlow<MemberAttendanceUiState> = combine(
        repository.member(memberId),
        month,
        monthRows,
        yearRows
    ) { member, ym, rows, yearly ->
        val past = yearly.filter { it.startAt <= System.currentTimeMillis() }.sortedByDescending { it.startAt }
        var streak = 0
        for (row in past) {
            if (row.present) break
            streak++
        }
        MemberAttendanceUiState(
            member = member,
            month = ym,
            rows = rows,
            attendedMonth = rows.count { it.present },
            totalMonth = rows.size,
            attendedYear = yearly.count { it.present },
            totalYear = yearly.size,
            consecutiveAbsences = streak
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MemberAttendanceUiState())

    fun previousMonth() { month.value = month.value.minusMonths(1) }

    fun nextMonth() { month.value = month.value.plusMonths(1) }

    fun today() { month.value = YearMonth.now() }
}

private fun YearMonth.startMillis(): Long = DateTimeUtils.toMillis(atDay(1))

private fun YearMonth.endMillis(): Long = DateTimeUtils.toMillis(atEndOfMonth().plusDays(1)) - 1
