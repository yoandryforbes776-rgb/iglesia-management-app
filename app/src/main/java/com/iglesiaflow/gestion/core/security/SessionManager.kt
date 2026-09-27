package com.iglesiaflow.gestion.core.security

import com.iglesiaflow.gestion.core.audit.AuditLogger
import com.iglesiaflow.gestion.data.local.dao.AdminDao
import com.iglesiaflow.gestion.data.local.entity.UserEntity
import com.iglesiaflow.gestion.domain.model.Permission
import com.iglesiaflow.gestion.domain.model.UserRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

data class SessionUser(
    val id: Long,
    val name: String,
    val email: String,
    val role: UserRole,
    val memberId: Long?,
    val permissions: Set<Permission>
) {
    fun can(permission: Permission): Boolean = permissions.contains(permission)
}

sealed interface LoginResult {
    data object Success : LoginResult
    data object RequiresTwoFactor : LoginResult
    data class Failure(val message: String) : LoginResult
}

@Singleton
class SessionManager @Inject constructor(
    private val adminDao: AdminDao,
    private val auditLogger: AuditLogger
) {

    private val _currentUser = MutableStateFlow<SessionUser?>(null)
    val currentUser: StateFlow<SessionUser?> = _currentUser.asStateFlow()

    private var pendingUser: UserEntity? = null
    private var lastActivityAt: Long = System.currentTimeMillis()

    suspend fun login(email: String, password: String): LoginResult {
        val user = adminDao.findByEmail(email.trim().lowercase())
            ?: return LoginResult.Failure("Usuario no encontrado")
        if (!user.active) return LoginResult.Failure("La cuenta está desactivada")
        if (!PasswordHasher.verify(password, user.salt, user.passwordHash)) {
            auditLogger.log("LOGIN_FALLIDO", "users", user.id, "Contraseña incorrecta", user.id, user.displayName)
            return LoginResult.Failure("Credenciales incorrectas")
        }
        if (user.twoFactorEnabled && !user.totpSecret.isNullOrBlank()) {
            pendingUser = user
            return LoginResult.RequiresTwoFactor
        }
        establishSession(user)
        return LoginResult.Success
    }

    suspend fun verifyTwoFactor(code: String): LoginResult {
        val user = pendingUser ?: return LoginResult.Failure("No hay inicio de sesión pendiente")
        val secret = user.totpSecret ?: return LoginResult.Failure("2FA no configurado")
        if (!TotpGenerator.verify(secret, code)) {
            auditLogger.log("2FA_FALLIDO", "users", user.id, "Código incorrecto", user.id, user.displayName)
            return LoginResult.Failure("Código de verificación incorrecto")
        }
        pendingUser = null
        establishSession(user)
        return LoginResult.Success
    }

    private suspend fun establishSession(user: UserEntity) {
        val permissions = resolvePermissions(user.role)
        _currentUser.value = SessionUser(
            id = user.id,
            name = user.displayName,
            email = user.email,
            role = user.role,
            memberId = user.memberId,
            permissions = permissions
        )
        lastActivityAt = System.currentTimeMillis()
        adminDao.updateUser(user.copy(lastLoginAt = System.currentTimeMillis()))
        auditLogger.log("LOGIN", "users", user.id, "Inicio de sesión correcto", user.id, user.displayName)
    }

    /** Combina la matriz por defecto con las anulaciones configuradas por el administrador. */
    suspend fun resolvePermissions(role: UserRole): Set<Permission> {
        val result = Rbac.defaultsFor(role).toMutableSet()
        adminDao.allRolePermissionsOnce().filter { it.role == role }.forEach { override ->
            if (override.granted) result.add(override.permission) else result.remove(override.permission)
        }
        return result
    }

    suspend fun refreshPermissions() {
        val current = _currentUser.value ?: return
        _currentUser.value = current.copy(permissions = resolvePermissions(current.role))
    }

    fun logout() {
        val user = _currentUser.value
        auditLogger.log("LOGOUT", "users", user?.id, "Cierre de sesión", user?.id, user?.name ?: "sistema")
        _currentUser.value = null
        pendingUser = null
    }

    fun touch() { lastActivityAt = System.currentTimeMillis() }

    fun isSessionExpired(timeoutMinutes: Int): Boolean =
        _currentUser.value != null &&
            System.currentTimeMillis() - lastActivityAt > timeoutMinutes * 60_000L

    fun has(permission: Permission): Boolean = _currentUser.value?.can(permission) == true

    fun requireUserName(): String = _currentUser.value?.name ?: "sistema"

    fun currentUserId(): Long? = _currentUser.value?.id
}
