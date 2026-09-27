package com.iglesiaflow.gestion.ui.screens.groups

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iglesiaflow.gestion.core.security.SessionManager
import com.iglesiaflow.gestion.data.local.dao.GroupMemberRow
import com.iglesiaflow.gestion.data.local.entity.GroupEntity
import com.iglesiaflow.gestion.data.local.entity.GroupMessageEntity
import com.iglesiaflow.gestion.data.local.entity.MemberEntity
import com.iglesiaflow.gestion.data.repository.GroupRepository
import com.iglesiaflow.gestion.data.repository.MemberRepository
import com.iglesiaflow.gestion.domain.model.GroupRole
import com.iglesiaflow.gestion.domain.model.Permission
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class GroupDetailUiState(
    val group: GroupEntity? = null,
    val members: List<GroupMemberRow> = emptyList(),
    val messages: List<GroupMessageEntity> = emptyList(),
    val candidates: List<MemberEntity> = emptyList(),
    val canEdit: Boolean = false
)

@HiltViewModel
class GroupDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: GroupRepository,
    memberRepository: MemberRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val groupId: Long = savedStateHandle.get<Long>("groupId") ?: 0L

    val uiState: StateFlow<GroupDetailUiState> = combine(
        repository.group(groupId),
        repository.members(groupId),
        repository.messages(groupId),
        memberRepository.members()
    ) { group, members, messages, allMembers ->
        val memberIds = members.map { it.memberId }.toSet()
        GroupDetailUiState(
            group = group,
            members = members,
            messages = messages,
            candidates = allMembers.filter { it.id !in memberIds },
            canEdit = sessionManager.has(Permission.GROUPS_EDIT)
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), GroupDetailUiState())

    fun addMember(memberId: Long, role: GroupRole) {
        viewModelScope.launch { repository.addMember(groupId, memberId, role) }
    }

    fun removeMember(membershipId: Long) {
        viewModelScope.launch { repository.removeMember(membershipId) }
    }

    fun postMessage(content: String) {
        if (content.isBlank()) return
        viewModelScope.launch { repository.postMessage(groupId, content.trim()) }
    }

    fun deleteMessage(id: Long) {
        viewModelScope.launch { repository.deleteMessage(id) }
    }
}
