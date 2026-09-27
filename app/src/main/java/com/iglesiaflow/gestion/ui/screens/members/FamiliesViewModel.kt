package com.iglesiaflow.gestion.ui.screens.members

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iglesiaflow.gestion.core.security.SessionManager
import com.iglesiaflow.gestion.data.local.entity.FamilyEntity
import com.iglesiaflow.gestion.data.local.entity.MemberEntity
import com.iglesiaflow.gestion.data.repository.MemberRepository
import com.iglesiaflow.gestion.domain.model.Permission
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FamilyWithMembers(val family: FamilyEntity, val members: List<MemberEntity>)

data class FamiliesUiState(
    val families: List<FamilyWithMembers> = emptyList(),
    val canEdit: Boolean = false
)

@HiltViewModel
class FamiliesViewModel @Inject constructor(
    private val repository: MemberRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    val uiState: StateFlow<FamiliesUiState> = combine(
        repository.families(),
        repository.members()
    ) { families, members ->
        FamiliesUiState(
            families = families.map { family ->
                FamilyWithMembers(family, members.filter { it.familyId == family.id })
            },
            canEdit = sessionManager.has(Permission.MEMBERS_EDIT)
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), FamiliesUiState())

    fun save(family: FamilyEntity) {
        viewModelScope.launch { repository.saveFamily(family) }
    }

    fun delete(id: Long) {
        viewModelScope.launch { repository.deleteFamily(id) }
    }
}
