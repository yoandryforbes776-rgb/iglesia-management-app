package com.iglesiaflow.gestion.ui.screens.admin

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iglesiaflow.gestion.core.config.SettingsRepository
import com.iglesiaflow.gestion.core.security.SessionManager
import com.iglesiaflow.gestion.core.util.DateTimeUtils
import com.iglesiaflow.gestion.core.util.ExportManager
import com.iglesiaflow.gestion.data.local.entity.AuditLogEntity
import com.iglesiaflow.gestion.data.local.entity.CustomFieldDefEntity
import com.iglesiaflow.gestion.data.local.entity.UserEntity
import com.iglesiaflow.gestion.data.repository.AdminRepository
import com.iglesiaflow.gestion.data.repository.BackupManager
import com.iglesiaflow.gestion.data.repository.CsvImporter
import com.iglesiaflow.gestion.data.repository.MemberRepository
import com.iglesiaflow.gestion.domain.model.Permission
import com.iglesiaflow.gestion.domain.model.UserRole
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

data class AdminUiState(
    val users: List<UserEntity> = emptyList(),
    val matrix: Map<UserRole, Set<Permission>> = emptyMap(),
    val audit: List<AuditLogEntity> = emptyList(),
    val backups: List<File> = emptyList(),
    val canManageUsers: Boolean = false,
    val canBackup: Boolean = false,
    val message: String? = null,
    val totpSecret: String? = null
)

@HiltViewModel
class AdminViewModel @Inject constructor(
    private val repository: AdminRepository,
    private val memberRepository: MemberRepository,
    private val backupManager: BackupManager,
    private val csvImporter: CsvImporter,
    private val exportManager: ExportManager,
    private val settingsRepository: SettingsRepository,
    private val session: SessionManager
) : ViewModel() {

    private val message = MutableStateFlow<String?>(null)
    private val totpSecret = MutableStateFlow<String?>(null)
    private val backups = MutableStateFlow(backupManager.listBackups())

    val uiState: StateFlow<AdminUiState> = combine(
        repository.users(),
        repository.permissionMatrix(),
        repository.auditLog(),
        backups,
        combine(message, totpSecret) { msg, secret -> msg to secret }
    ) { users, matrix, audit, backupFiles, messageAndSecret ->
        AdminUiState(
            users = users,
            matrix = matrix,
            audit = audit,
            backups = backupFiles,
            canManageUsers = session.has(Permission.ADMIN_USERS),
            canBackup = session.has(Permission.ADMIN_BACKUP),
            message = messageAndSecret.first,
            totpSecret = messageAndSecret.second
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AdminUiState())

    fun consumeMessage() { message.value = null }
    fun consumeSecret() { totpSecret.value = null }

    // ---------- Usuarios ----------

    fun createUser(name: String, email: String, password: String, role: UserRole, twoFactor: Boolean) {
        viewModelScope.launch {
            if (repository.findByEmail(email) != null) {
                message.value = "Ya existe un usuario con ese email"
                return@launch
            }
            repository.createUser(name, email, password, role, twoFactor = twoFactor)
            message.value = "Usuario creado"
        }
    }

    fun updateUser(user: UserEntity) {
        viewModelScope.launch {
            repository.updateUser(user)
            message.value = "Usuario actualizado"
        }
    }

    fun changePassword(user: UserEntity, password: String) {
        viewModelScope.launch {
            repository.changePassword(user, password)
            message.value = "Contraseña actualizada"
        }
    }

    fun toggleTwoFactor(user: UserEntity, enabled: Boolean) {
        viewModelScope.launch {
            val secret = repository.toggleTwoFactor(user, enabled)
            totpSecret.value = secret
            message.value = if (enabled) "2FA activado" else "2FA desactivado"
        }
    }

    fun deleteUser(user: UserEntity) {
        viewModelScope.launch {
            if (user.id == session.currentUserId()) {
                message.value = "No puedes eliminar tu propio usuario"
                return@launch
            }
            repository.deleteUser(user.id)
            message.value = "Usuario eliminado"
        }
    }

    // ---------- RBAC ----------

    fun setPermission(role: UserRole, permission: Permission, granted: Boolean) {
        viewModelScope.launch { repository.setPermission(role, permission, granted) }
    }

    fun resetPermissions() {
        viewModelScope.launch {
            repository.resetPermissions()
            message.value = "Permisos restablecidos"
        }
    }

    // ---------- Backup / import / export ----------

    fun createBackup() {
        viewModelScope.launch {
            val file = backupManager.createBackup()
            settingsRepository.update { it.copy(lastBackupAt = System.currentTimeMillis()) }
            backups.value = backupManager.listBackups()
            message.value = "Copia creada: ${file.name}"
        }
    }

    fun shareBackup(file: File) {
        exportManager.share(file)
    }

    fun restoreBackup(file: File) {
        viewModelScope.launch {
            val ok = backupManager.restoreBackup(file)
            message.value = if (ok) {
                "Copia restaurada. Reinicia la aplicación para aplicar los cambios."
            } else {
                "No se pudo restaurar la copia"
            }
        }
    }

    fun deleteBackup(file: File) {
        backupManager.deleteBackup(file)
        backups.value = backupManager.listBackups()
        message.value = "Copia eliminada"
    }

    fun importBackup(uri: Uri) {
        viewModelScope.launch {
            val file = backupManager.importBackupFrom(uri)
            backups.value = backupManager.listBackups()
            message.value = if (file != null) "Copia importada: ${file.name}" else "Archivo no válido"
        }
    }

    fun importMembersCsv(uri: Uri) {
        viewModelScope.launch {
            val (members, errors) = csvImporter.parseMembers(uri)
            val imported = memberRepository.saveAll(members)
            message.value = buildString {
                append("$imported miembros importados")
                if (errors.isNotEmpty()) append(" · ${errors.size} filas con errores")
            }
        }
    }

    fun exportMembersCsv() {
        viewModelScope.launch {
            val members = memberRepository.allMembers()
            val file = exportManager.writeCsv(
                baseName = "miembros_backup",
                headers = listOf(
                    "Nombre", "Apellidos", "Email", "Teléfono", "Nacimiento",
                    "Estado", "Rol", "Dirección", "Ciudad"
                ),
                rows = members.map {
                    listOf(
                        it.firstName, it.lastName, it.email, it.phone,
                        DateTimeUtils.formatDate(it.birthDate), it.status.name, it.churchRole, it.address, it.city
                    )
                }
            )
            exportManager.share(file)
            message.value = "Miembros exportados"
        }
    }

    fun exportAudit() {
        viewModelScope.launch {
            val entries = repository.allAudit()
            val file = exportManager.writeCsv(
                baseName = "auditoria",
                headers = listOf("Fecha", "Usuario", "Acción", "Entidad", "Detalle"),
                rows = entries.map {
                    listOf(
                        DateTimeUtils.formatDateTime(it.timestamp),
                        it.userName, it.action, it.entityType, it.detail
                    )
                }
            )
            exportManager.share(file)
            message.value = "Auditoría exportada"
        }
    }

    fun purgeAudit() {
        viewModelScope.launch {
            repository.purgeAuditBefore(DateTimeUtils.monthsAgo(6))
            message.value = "Auditoría anterior a 6 meses eliminada"
        }
    }
}

/** ViewModel de campos personalizados (configurables sin recompilar). */
@HiltViewModel
class CustomFieldsViewModel @Inject constructor(
    private val memberRepository: MemberRepository
) : ViewModel() {

    private val message = MutableStateFlow<String?>(null)

    val fields: StateFlow<List<CustomFieldDefEntity>> = memberRepository.allCustomFields()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val notice: StateFlow<String?> = message.map { it }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun consumeMessage() { message.value = null }

    fun save(def: CustomFieldDefEntity) {
        viewModelScope.launch {
            memberRepository.saveCustomField(def)
            message.value = "Campo guardado"
        }
    }

    fun delete(id: Long) {
        viewModelScope.launch {
            memberRepository.deleteCustomField(id)
            message.value = "Campo eliminado"
        }
    }
}
