package com.iglesiaflow.gestion.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.iglesiaflow.gestion.domain.model.Permission
import com.iglesiaflow.gestion.domain.model.UserRole

@Entity(tableName = "users", indices = [Index(value = ["email"], unique = true)])
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val displayName: String,
    val email: String,
    val passwordHash: String,
    val salt: String,
    val role: UserRole = UserRole.MIEMBRO,
    val memberId: Long? = null,
    val active: Boolean = true,
    val twoFactorEnabled: Boolean = false,
    val totpSecret: String? = null,
    val lastLoginAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)

/** Sobrescribe la matriz RBAC por defecto (configurable sin recompilar). */
@Entity(tableName = "role_permissions", primaryKeys = ["role", "permission"])
data class RolePermissionEntity(
    val role: UserRole,
    val permission: Permission,
    val granted: Boolean
)

@Entity(tableName = "audit_log", indices = [Index("timestamp"), Index("userId")])
data class AuditLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long? = null,
    val userName: String = "sistema",
    val action: String,
    val entityType: String = "",
    val entityId: Long? = null,
    val detail: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
