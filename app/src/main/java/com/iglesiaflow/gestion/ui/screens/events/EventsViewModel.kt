package com.iglesiaflow.gestion.ui.screens.events

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iglesiaflow.gestion.core.security.SessionManager
import com.iglesiaflow.gestion.core.util.DateTimeUtils
import com.iglesiaflow.gestion.data.local.entity.EventEntity
import com.iglesiaflow.gestion.data.local.entity.GroupEntity
import com.iglesiaflow.gestion.data.repository.EventRepository
import com.iglesiaflow.gestion.data.repository.GroupRepository
import com.iglesiaflow.gestion.domain.model.EventType
import com.iglesiaflow.gestion.domain.model.Permission
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EventsUiState(
    val upcoming: List<EventEntity> = emptyList(),
    val past: List<EventEntity> = emptyList(),
    val groups: List<GroupEntity> = emptyList(),
    val typeFilter: EventType? = null,
    val activeCheckIns: Int = 0,
    val canEdit: Boolean = false,
    val canManageAttendance: Boolean = false,
    val message: String? = null
)

@HiltViewModel
class EventsViewModel @Inject constructor(
    private val repository: EventRepository,
    groupRepository: GroupRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val typeFilter = MutableStateFlow<EventType?>(null)
    private val message = MutableStateFlow<String?>(null)

    val uiState: StateFlow<EventsUiState> = combine(
        repository.events(),
        groupRepository.groups(),
        repository.activeCheckInCount(),
        typeFilter,
        message
    ) { events, groups, checkIns, filter, msg ->
        val now = System.currentTimeMillis()
        val filtered = events.filter { filter == null || it.type == filter }
        EventsUiState(
            upcoming = filtered.filter { it.endAt >= now }.sortedBy { it.startAt },
            past = filtered.filter { it.endAt < now }.sortedByDescending { it.startAt },
            groups = groups,
            typeFilter = filter,
            activeCheckIns = checkIns,
            canEdit = sessionManager.has(Permission.EVENTS_EDIT),
            canManageAttendance = sessionManager.has(Permission.ATTENDANCE_MANAGE),
            message = msg
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), EventsUiState())

    fun onTypeFilter(type: EventType?) { typeFilter.value = type }
    fun consumeMessage() { message.value = null }

    fun save(event: EventEntity, generateRecurrences: Boolean) {
        if (!sessionManager.has(Permission.EVENTS_EDIT)) {
            message.value = "No tienes permisos para editar eventos"
            return
        }
        viewModelScope.launch {
            val id = repository.save(event)
            if (generateRecurrences && !event.recurrence.equals("NONE", true)) {
                repository.generateRecurrences(event.copy(id = id))
            }
            message.value = "Evento guardado"
        }
    }

    fun delete(id: Long) {
        viewModelScope.launch {
            repository.delete(id)
            message.value = "Evento eliminado"
        }
    }

    fun nextOccurrenceLabel(event: EventEntity): String =
        DateTimeUtils.formatDateTime(event.startAt)
}
