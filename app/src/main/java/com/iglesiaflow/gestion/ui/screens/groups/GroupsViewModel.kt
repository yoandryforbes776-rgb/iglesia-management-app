package com.iglesiaflow.gestion.ui.screens.groups

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iglesiaflow.gestion.core.security.SessionManager
import com.iglesiaflow.gestion.data.local.entity.GroupEntity
import com.iglesiaflow.gestion.data.local.entity.MemberEntity
import com.iglesiaflow.gestion.data.repository.GroupRepository
import com.iglesiaflow.gestion.data.repository.MemberRepository
import com.iglesiaflow.gestion.domain.model.Permission
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class GroupsUiState(
    val groups: List<GroupEntity> = emptyList(),
    val sizes: Map<Long, Int> = emptyMap(),
    val members: List<MemberEntity> = emptyList(),
    val canEdit: Boolean = false
) {
    fun leaderName(group: GroupEntity): String =
        members.firstOrNull { it.id == group.leaderId }?.fullName ?: "Sin líder"
}

@HiltViewModel
class GroupsViewModel @Inject constructor(
    private val repository: GroupRepository,
    memberRepository: MemberRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    val uiState: StateFlow<GroupsUiState> = combine(
        repository.groups(),
        repository.sizes(),
        memberRepository.members()
    ) { groups, sizes, members ->
        GroupsUiState(
            groups = groups,
            sizes = sizes.associate { it.groupId to it.total },
            members = members,
            canEdit = sessionManager.has(Permission.GROUPS_EDIT)
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), GroupsUiState())

    fun save(group: GroupEntity) {
        viewModelScope.launch { repository.save(group) }
    }

    fun delete(id: Long) {
        viewModelScope.launch { repository.delete(id) }
    }
}
