package com.iglesiaflow.gestion.ui.screens.events

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iglesiaflow.gestion.core.security.SessionManager
import com.iglesiaflow.gestion.core.util.DateTimeUtils
import com.iglesiaflow.gestion.core.util.ExportManager
import com.iglesiaflow.gestion.data.local.dao.AttendanceRow
import com.iglesiaflow.gestion.data.local.entity.CheckInEntity
import com.iglesiaflow.gestion.data.local.entity.EventEntity
import com.iglesiaflow.gestion.data.repository.EventRepository
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

data class EventDetailUiState(
    val event: EventEntity? = null,
    val attendance: List<AttendanceRow> = emptyList(),
    val checkIns: List<CheckInEntity> = emptyList(),
    val query: String = "",
    val presentCount: Int = 0,
    val canManage: Boolean = false,
    val message: String? = null
)

@HiltViewModel
class EventDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: EventRepository,
    private val memberRepository: MemberRepository,
    private val exportManager: ExportManager,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val eventId: Long = savedStateHandle.get<Long>("eventId") ?: 0L
    private val query = MutableStateFlow("")
    private val message = MutableStateFlow<String?>(null)

    val uiState: StateFlow<EventDetailUiState> = combine(
        repository.event(eventId),
        repository.attendanceRows(eventId),
        repository.checkIns(eventId),
        query,
        message
    ) { event, attendance, checkIns, search, msg ->
        val filtered = attendance.filter {
            search.isBlank() || "${it.firstName} ${it.lastName}".contains(search, ignoreCase = true)
        }
        EventDetailUiState(
            event = event,
            attendance = filtered,
            checkIns = checkIns,
            query = search,
            presentCount = attendance.count { it.present },
            canManage = sessionManager.has(Permission.ATTENDANCE_MANAGE),
            message = msg
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), EventDetailUiState())

    fun onQueryChange(value: String) { query.value = value }
    fun consumeMessage() { message.value = null }

    fun toggleAttendance(memberId: Long, present: Boolean) {
        if (!sessionManager.has(Permission.ATTENDANCE_MANAGE)) {
            message.value = "No tienes permisos para registrar asistencia"
            return
        }
        viewModelScope.launch { repository.toggleAttendance(eventId, memberId, present) }
    }

    fun checkOut(checkInId: Long, code: String) {
        viewModelScope.launch {
            val ok = repository.checkOut(checkInId, code)
            message.value = if (ok) "Salida registrada" else "Código de seguridad incorrecto"
        }
    }

    fun exportAttendance() {
        viewModelScope.launch {
            val state = uiState.value
            val file = exportManager.writeCsv(
                baseName = "asistencia_evento_$eventId",
                headers = listOf("Miembro", "Presente"),
                rows = state.attendance.map { listOf("${it.firstName} ${it.lastName}", if (it.present) "Sí" else "No") }
            )
            exportManager.share(file)
            message.value = "Asistencia exportada"
        }
    }

    fun eventSubtitle(event: EventEntity): String =
        "${DateTimeUtils.formatDateTime(event.startAt)} · ${event.location}"
}
