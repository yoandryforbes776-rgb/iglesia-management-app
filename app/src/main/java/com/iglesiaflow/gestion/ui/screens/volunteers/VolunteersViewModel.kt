package com.iglesiaflow.gestion.ui.screens.volunteers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iglesiaflow.gestion.core.security.SessionManager
import com.iglesiaflow.gestion.core.util.DateTimeUtils
import com.iglesiaflow.gestion.data.local.dao.VolunteerHours
import com.iglesiaflow.gestion.data.local.entity.MemberEntity
import com.iglesiaflow.gestion.data.local.entity.MinistryNeedEntity
import com.iglesiaflow.gestion.data.local.entity.ServiceRecordEntity
import com.iglesiaflow.gestion.data.local.entity.VolunteerSkillEntity
import com.iglesiaflow.gestion.data.repository.MemberRepository
import com.iglesiaflow.gestion.data.repository.VolunteerMatch
import com.iglesiaflow.gestion.data.repository.VolunteerRepository
import com.iglesiaflow.gestion.domain.model.Permission
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class VolunteersUiState(
    val skills: List<VolunteerSkillEntity> = emptyList(),
    val needs: List<MinistryNeedEntity> = emptyList(),
    val matches: List<VolunteerMatch> = emptyList(),
    val records: List<ServiceRecordEntity> = emptyList(),
    val ranking: List<VolunteerHours> = emptyList(),
    val totalHours: Double = 0.0,
    val members: List<MemberEntity> = emptyList(),
    val canEdit: Boolean = false
) {
    fun memberName(id: Long): String = members.firstOrNull { it.id == id }?.fullName ?: "—"
}

@HiltViewModel
class VolunteersViewModel @Inject constructor(
    private val repository: VolunteerRepository,
    memberRepository: MemberRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val since = DateTimeUtils.startOfYear()

    val uiState: StateFlow<VolunteersUiState> = combine(
        repository.skills(),
        repository.needs(),
        repository.serviceRecords(),
        combine(repository.topVolunteers(since), repository.totalHours(since)) { ranking, hours -> ranking to hours },
        memberRepository.members()
    ) { skills, needs, records, rankingAndHours, members ->
        VolunteersUiState(
            skills = skills,
            needs = needs,
            matches = repository.match(needs, skills),
            records = records,
            ranking = rankingAndHours.first,
            totalHours = rankingAndHours.second,
            members = members,
            canEdit = sessionManager.has(Permission.VOLUNTEERS_EDIT)
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), VolunteersUiState())

    fun saveSkill(skill: VolunteerSkillEntity) { viewModelScope.launch { repository.saveSkill(skill) } }
    fun deleteSkill(id: Long) { viewModelScope.launch { repository.deleteSkill(id) } }
    fun saveNeed(need: MinistryNeedEntity) { viewModelScope.launch { repository.saveNeed(need) } }
    fun deleteNeed(id: Long) { viewModelScope.launch { repository.deleteNeed(id) } }
    fun saveRecord(record: ServiceRecordEntity) { viewModelScope.launch { repository.saveServiceRecord(record) } }
    fun deleteRecord(id: Long) { viewModelScope.launch { repository.deleteServiceRecord(id) } }
}
