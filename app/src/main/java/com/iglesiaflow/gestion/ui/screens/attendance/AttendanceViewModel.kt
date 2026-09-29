package com.iglesiaflow.gestion.ui.screens.attendance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iglesiaflow.gestion.data.local.entity.MemberEntity
import com.iglesiaflow.gestion.data.repository.AbsenceAlert
import com.iglesiaflow.gestion.data.repository.AttendanceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AttendanceUiState(
    val alerts: List<AbsenceAlert> = emptyList(),
    val members: List<Pair<MemberEntity, Int>> = emptyList(),
    val query: String = "",
    val pastEvents: Int = 0
)

@HiltViewModel
class AttendanceViewModel @Inject constructor(
    private val repository: AttendanceRepository
) : ViewModel() {

    private val query = MutableStateFlow("")

    val uiState: StateFlow<AttendanceUiState> = combine(
        repository.absenceAlerts(),
        repository.attendanceOverview(),
        repository.pastEventCount(),
        query
    ) { alerts, overview, pastEvents, text ->
        val filtered = if (text.isBlank()) overview else overview.filter {
            it.first.fullName.contains(text, ignoreCase = true)
        }
        AttendanceUiState(
            alerts = alerts,
            members = filtered.sortedBy { it.first.lastName.lowercase() },
            query = text,
            pastEvents = pastEvents
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AttendanceUiState())

    init {
        viewModelScope.launch {
            repository.absenceAlerts().collect { alerts -> repository.notifyLeaders(alerts) }
        }
    }

    fun search(text: String) { query.value = text }
}
