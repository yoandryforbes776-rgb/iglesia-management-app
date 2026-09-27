package com.iglesiaflow.gestion.data.repository

import com.iglesiaflow.gestion.core.audit.AuditLogger
import com.iglesiaflow.gestion.core.security.PasswordHasher
import com.iglesiaflow.gestion.core.security.Rbac
import com.iglesiaflow.gestion.core.security.SessionManager
import com.iglesiaflow.gestion.core.security.TotpGenerator
import com.iglesiaflow.gestion.data.local.dao.AdminDao
import com.iglesiaflow.gestion.data.local.entity.AuditLogEntity
import com.iglesiaflow.gestion.data.local.entity.RolePermissionEntity
import com.iglesiaflow.gestion.data.local.entity.UserEntity
import com.iglesiaflow.gestion.domain.model.Permission
import com.iglesiaflow.gestion.domain.model.UserRole
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdminRepository @Inject constructor(
    private val dao: AdminDao,
    private val auditLogger: AuditLogger,
    private val session: SessionManager
) {
    fun users(): Flow<List<UserEntity>> = dao.observeUsers()
    fun auditLog(limit: Int = 200): Flow<List<AuditLogEntity>> = dao.observeAuditLog(limit)

    /** Matriz efectiva de permisos por rol (defaults + anulaciones). */
    fun permissionMatrix(): Flow<Map<UserRole, Set<Permission>>> =
        dao.observeRolePermissions().map { overrides ->
            UserRole.entries.associateWith { role ->
                val effective = Rbac.defaultsFor(role).toMutableSet()
                overrides.filter { it.role == role }.forEach {
                    if (it.granted) effective.add(it.permission) else effective.remove(it.permission)
                }
                effective
            }
        }

    suspend fun setPermission(role: UserRole, permission: Permission, granted: Boolean) {
        dao.upsertRolePermission(RolePermissionEntity(role, permission, granted))
        session.refreshPermissions()
        auditLogger.log(
            "PERMISO_ACTUALIZADO", "role_permissions", null,
            "${role.name}:${permission.name}=$granted", session.currentUserId(), session.requireUserName()
        )
    }

    suspend fun resetPermissions() {
        dao.clearRolePermissions()
        session.refreshPermissions()
    }

    suspend fun createUser(
        displayName: String,
        email: String,
        password: String,
        role: UserRole,
        memberId: Long? = null,
        twoFactor: Boolean = false
    ): Long {
        val salt = PasswordHasher.newSalt()
        val user = UserEntity(
            displayName = displayName,
            email = email.trim().lowercase(),
            passwordHash = PasswordHasher.hash(password, salt),
            salt = salt,
            role = role,
            memberId = memberId,
            twoFactorEnabled = twoFactor,
            totpSecret = if (twoFactor) TotpGenerator.newSecret() else null
        )
        val id = dao.insertUser(user)
        auditLogger.log("USUARIO_CREADO", "users", id, email, session.currentUserId(), session.requireUserName())
        return id
    }

    suspend fun updateUser(user: UserEntity) {
        dao.updateUser(user)
        auditLogger.log("USUARIO_ACTUALIZADO", "users", user.id, user.email,
            session.currentUserId(), session.requireUserName())
    }

    suspend fun changePassword(user: UserEntity, newPassword: String) {
        val salt = PasswordHasher.newSalt()
        dao.updateUser(user.copy(salt = salt, passwordHash = PasswordHasher.hash(newPassword, salt)))
        auditLogger.log("PASSWORD_CAMBIADA", "users", user.id, user.email,
            session.currentUserId(), session.requireUserName())
    }

    suspend fun toggleTwoFactor(user: UserEntity, enabled: Boolean): String? {
        val secret = if (enabled) user.totpSecret ?: TotpGenerator.newSecret() else null
        dao.updateUser(user.copy(twoFactorEnabled = enabled, totpSecret = secret))
        auditLogger.log("2FA_ACTUALIZADO", "users", user.id, "enabled=$enabled",
            session.currentUserId(), session.requireUserName())
        return secret
    }

    suspend fun deleteUser(id: Long) {
        dao.deleteUser(id)
        auditLogger.log("USUARIO_ELIMINADO", "users", id, "", session.currentUserId(), session.requireUserName())
    }

    suspend fun getUser(id: Long): UserEntity? = dao.getUser(id)
    suspend fun findByEmail(email: String): UserEntity? = dao.findByEmail(email.trim().lowercase())
    suspend fun userCount(): Int = dao.countUsers()
    suspend fun allAudit(): List<AuditLogEntity> = dao.allAuditOnce()
    suspend fun purgeAuditBefore(timestamp: Long) = dao.purgeAuditBefore(timestamp)
}
