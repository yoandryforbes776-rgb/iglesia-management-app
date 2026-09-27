package com.iglesiaflow.gestion.ui.screens.members

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.net.Uri
import com.iglesiaflow.gestion.core.config.SettingsRepository
import com.iglesiaflow.gestion.core.security.SessionManager
import com.iglesiaflow.gestion.core.util.DateTimeUtils
import com.iglesiaflow.gestion.core.util.ExportManager
import com.iglesiaflow.gestion.data.local.entity.CustomFieldDefEntity
import com.iglesiaflow.gestion.data.local.entity.FamilyEntity
import com.iglesiaflow.gestion.data.local.entity.MemberEntity
import com.iglesiaflow.gestion.data.repository.CsvImporter
import com.iglesiaflow.gestion.data.repository.MemberRepository
import com.iglesiaflow.gestion.domain.model.CustomFieldEntity
import com.iglesiaflow.gestion.domain.model.MemberStatus
import com.iglesiaflow.gestion.domain.model.Permission
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MembersUiState(
    val query: String = "",
    val statusFilter: MemberStatus? = null,
    val members: List<MemberEntity> = emptyList(),
    val families: List<FamilyEntity> = emptyList(),
    val customFields: List<CustomFieldDefEntity> = emptyList(),
    val canEdit: Boolean = false,
    val canDelete: Boolean = false,
    val message: String? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class MembersViewModel @Inject constructor(
    private val repository: MemberRepository,
    private val exportManager: ExportManager,
    private val csvImporter: CsvImporter,
    private val sessionManager: SessionManager,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val query = MutableStateFlow("")
    private val statusFilter = MutableStateFlow<MemberStatus?>(null)
    private val message = MutableStateFlow<String?>(null)

    private val filteredMembers = combine(
        query.flatMapLatest { repository.search(it) },
        statusFilter
    ) { members, status -> if (status == null) members else members.filter { it.status == status } }

    val uiState: StateFlow<MembersUiState> = combine(
        filteredMembers,
        repository.families(),
        repository.customFields(CustomFieldEntity.MIEMBRO),
        combine(query, statusFilter) { q, s -> q to s },
        message
    ) { members, families, fields, filters, msg ->
        MembersUiState(
            query = filters.first,
            statusFilter = filters.second,
            members = members,
            families = families,
            customFields = fields,
            canEdit = sessionManager.has(Permission.MEMBERS_EDIT),
            canDelete = sessionManager.has(Permission.MEMBERS_DELETE),
            message = msg
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MembersUiState())

    fun onQueryChange(value: String) { query.value = value }
    fun onStatusFilter(status: MemberStatus?) { statusFilter.value = status }
    fun consumeMessage() { message.value = null }

    fun save(member: MemberEntity, customValues: Map<Long, String> = emptyMap()) {
        if (!sessionManager.has(Permission.MEMBERS_EDIT)) {
            message.value = "No tienes permisos para editar miembros"
            return
        }
        viewModelScope.launch {
            val id = repository.save(member)
            customValues.forEach { (fieldId, value) ->
                repository.setCustomValue(fieldId, CustomFieldEntity.MIEMBRO, id, value)
            }
            message.value = "Miembro guardado"
        }
    }

    fun delete(member: MemberEntity) {
        if (!sessionManager.has(Permission.MEMBERS_DELETE)) {
            message.value = "No tienes permisos para eliminar miembros"
            return
        }
        viewModelScope.launch {
            repository.delete(member)
            message.value = "Miembro eliminado"
        }
    }

    fun exportCsv() {
        viewModelScope.launch {
            val members = repository.allMembers()
            val file = exportManager.writeCsv(
                baseName = "directorio_miembros",
                headers = listOf("Nombre", "Apellidos", "Email", "Teléfono", "Estado", "Nacimiento", "Ciudad", "Rol"),
                rows = members.map {
                    listOf(
                        it.firstName, it.lastName, it.email, it.phone, it.status.label,
                        DateTimeUtils.formatDate(it.birthDate), it.city, it.churchRole
                    )
                }
            )
            exportManager.share(file)
            message.value = "Directorio exportado (${file.name})"
        }
    }

    fun exportPdf() {
        viewModelScope.launch {
            val members = repository.allMembers()
            val file = exportManager.writePdf(
                baseName = "directorio_miembros",
                title = "Directorio de miembros",
                subtitle = "Generado el ${DateTimeUtils.formatDateTime(System.currentTimeMillis())}",
                headers = listOf("Nombre", "Teléfono", "Email", "Estado"),
                rows = members.map { listOf(it.fullName, it.phone, it.email, it.status.label) }
            )
            exportManager.share(file)
            message.value = "PDF generado (${file.name})"
        }
    }

    fun importCsv(uri: Uri) {
        viewModelScope.launch {
            val (members, errors) = csvImporter.parseMembers(uri)
            if (members.isNotEmpty()) repository.saveAll(members)
            message.value = buildString {
                append("${members.size} miembros importados")
                if (errors.isNotEmpty()) append(" · ${errors.size} con errores")
            }
        }
    }
}
