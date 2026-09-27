package com.iglesiaflow.gestion.ui.screens.events

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iglesiaflow.gestion.core.security.SessionManager
import com.iglesiaflow.gestion.core.util.NotificationHelper
import com.iglesiaflow.gestion.data.local.entity.CheckInEntity
import com.iglesiaflow.gestion.data.local.entity.EventEntity
import com.iglesiaflow.gestion.data.local.entity.MemberEntity
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

data class CheckInUiState(
    val events: List<EventEntity> = emptyList(),
    val selectedEvent: EventEntity? = null,
    val children: List<MemberEntity> = emptyList(),
    val activeCheckIns: List<CheckInEntity> = emptyList(),
    val canManage: Boolean = false,
    val lastCode: String? = null,
    val message: String? = null
)

@HiltViewModel
class CheckInViewModel @Inject constructor(
    private val repository: EventRepository,
    memberRepository: MemberRepository,
    private val notificationHelper: NotificationHelper,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val selectedEventId = MutableStateFlow<Long?>(null)
    private val message = MutableStateFlow<String?>(null)
    private val lastCode = MutableStateFlow<String?>(null)

    val uiState: StateFlow<CheckInUiState> = combine(
        repository.events(),
        memberRepository.children(),
        repository.activeCheckIns(),
        combine(selectedEventId, message) { id, msg -> id to msg },
        lastCode
    ) { events, children, active, selection, code ->
        val checkInEvents = events.filter { it.requiresCheckIn }.sortedBy { it.startAt }
        val selected = checkInEvents.firstOrNull { it.id == selection.first } ?: checkInEvents.firstOrNull()
        CheckInUiState(
            events = checkInEvents,
            selectedEvent = selected,
            children = children,
            activeCheckIns = active.filter { selected == null || it.eventId == selected.id },
            canManage = sessionManager.has(Permission.ATTENDANCE_MANAGE),
            lastCode = code,
            message = selection.second
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CheckInUiState())

    fun selectEvent(id: Long) { selectedEventId.value = id }
    fun consumeMessage() { message.value = null }
    fun consumeCode() { lastCode.value = null }

    fun checkIn(child: MemberEntity, guardianName: String, guardianPhone: String, room: String, allergies: String) {
        val event = uiState.value.selectedEvent ?: return
        if (!sessionManager.has(Permission.ATTENDANCE_MANAGE)) {
            message.value = "No tienes permisos para el check-in"
            return
        }
        viewModelScope.launch {
            val entity = repository.checkIn(
                eventId = event.id,
                childMemberId = child.id,
                childName = child.fullName,
                guardianName = guardianName,
                guardianPhone = guardianPhone.ifBlank { child.guardianPhone },
                room = room,
                allergies = allergies
            )
            lastCode.value = entity.securityCode
            notificationHelper.notify(
                NotificationHelper.CHANNEL_CHECKIN,
                "Check-in registrado",
                "${child.fullName} · código ${entity.securityCode}"
            )
            message.value = "Check-in de ${child.fullName} realizado"
        }
    }

    fun checkOut(checkInId: Long, code: String) {
        viewModelScope.launch {
            val ok = repository.checkOut(checkInId, code)
            message.value = if (ok) "Salida validada" else "Código incorrecto"
        }
    }
}
