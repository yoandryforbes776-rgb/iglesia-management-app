package com.iglesiaflow.gestion.ui.screens.members

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iglesiaflow.gestion.core.config.AppSettings
import com.iglesiaflow.gestion.core.config.SettingsRepository
import com.iglesiaflow.gestion.core.security.SessionManager
import com.iglesiaflow.gestion.data.local.entity.CustomFieldDefEntity
import com.iglesiaflow.gestion.data.local.entity.FamilyEntity
import com.iglesiaflow.gestion.data.local.entity.MemberEntity
import com.iglesiaflow.gestion.data.local.entity.NoteEntity
import com.iglesiaflow.gestion.data.local.entity.VolunteerSkillEntity
import com.iglesiaflow.gestion.data.repository.MemberRepository
import com.iglesiaflow.gestion.data.repository.VolunteerRepository
import com.iglesiaflow.gestion.domain.model.CustomFieldEntity
import com.iglesiaflow.gestion.domain.model.Permission
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MemberDetailUiState(
    val member: MemberEntity? = null,
    val family: FamilyEntity? = null,
    val notes: List<NoteEntity> = emptyList(),
    val customFields: List<CustomFieldDefEntity> = emptyList(),
    val customValues: Map<Long, String> = emptyMap(),
    val skills: List<VolunteerSkillEntity> = emptyList(),
    val families: List<FamilyEntity> = emptyList(),
    val settings: AppSettings = AppSettings(),
    val canEdit: Boolean = false,
    val canSeeAttendance: Boolean = true
)

@HiltViewModel
class MemberDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: MemberRepository,
    volunteerRepository: VolunteerRepository,
    settingsRepository: SettingsRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val memberId: Long = savedStateHandle.get<Long>("memberId") ?: 0L
    private val familyState = MutableStateFlow<FamilyEntity?>(null)

    private val profile = combine(
        repository.member(memberId),
        repository.notes(CustomFieldEntity.MIEMBRO, memberId),
        repository.customFields(CustomFieldEntity.MIEMBRO),
        repository.customValues(CustomFieldEntity.MIEMBRO, memberId),
        repository.families()
    ) { member, notes, fields, values, families ->
        member?.familyId?.let { id -> familyState.value = families.firstOrNull { it.id == id } }
        Profile(member, notes, fields, values.associate { it.fieldId to it.value }, families)
    }

    private data class Profile(
        val member: MemberEntity?,
        val notes: List<NoteEntity>,
        val fields: List<CustomFieldDefEntity>,
        val values: Map<Long, String>,
        val families: List<FamilyEntity>
    )

    val uiState: StateFlow<MemberDetailUiState> = combine(
        profile,
        volunteerRepository.skillsFor(memberId),
        settingsRepository.settings,
        familyState
    ) { profile, skills, settings, family ->
        MemberDetailUiState(
            member = profile.member,
            family = family,
            notes = profile.notes,
            customFields = profile.fields,
            customValues = profile.values,
            skills = skills,
            families = profile.families,
            settings = settings,
            canEdit = sessionManager.has(Permission.MEMBERS_EDIT),
            canSeeAttendance = sessionManager.has(Permission.ATTENDANCE_VIEW)
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MemberDetailUiState())


    fun save(member: MemberEntity, customValues: Map<Long, String>) {
        viewModelScope.launch {
            repository.save(member)
            customValues.forEach { (fieldId, value) ->
                repository.setCustomValue(fieldId, CustomFieldEntity.MIEMBRO, member.id, value)
            }
        }
    }

    fun addNote(title: String, content: String, isPrivate: Boolean) {
        viewModelScope.launch {
            repository.addNote(
                NoteEntity(
                    entityType = CustomFieldEntity.MIEMBRO,
                    entityId = memberId,
                    title = title,
                    content = content,
                    isPrivate = isPrivate
                )
            )
        }
    }

    fun deleteNote(id: Long) {
        viewModelScope.launch { repository.deleteNote(id) }
    }
}
