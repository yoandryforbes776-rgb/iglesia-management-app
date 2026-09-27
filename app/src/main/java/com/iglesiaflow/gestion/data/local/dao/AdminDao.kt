package com.iglesiaflow.gestion.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.iglesiaflow.gestion.data.local.entity.AuditLogEntity
import com.iglesiaflow.gestion.data.local.entity.RolePermissionEntity
import com.iglesiaflow.gestion.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AdminDao {

    @Query("SELECT * FROM users ORDER BY displayName COLLATE NOCASE")
    fun observeUsers(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun findByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users WHERE id = :id")
    suspend fun getUser(id: Long): UserEntity?

    @Query("SELECT COUNT(*) FROM users")
    suspend fun countUsers(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity): Long

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("DELETE FROM users WHERE id = :id")
    suspend fun deleteUser(id: Long)

    @Query("SELECT * FROM role_permissions")
    fun observeRolePermissions(): Flow<List<RolePermissionEntity>>

    @Query("SELECT * FROM role_permissions")
    suspend fun allRolePermissionsOnce(): List<RolePermissionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertRolePermission(entity: RolePermissionEntity)

    @Query("DELETE FROM role_permissions")
    suspend fun clearRolePermissions()

    @Query("SELECT * FROM audit_log ORDER BY timestamp DESC LIMIT :limit")
    fun observeAuditLog(limit: Int): Flow<List<AuditLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAudit(entry: AuditLogEntity): Long

    @Query("DELETE FROM audit_log WHERE timestamp < :before")
    suspend fun purgeAuditBefore(before: Long)

    @Query("SELECT * FROM audit_log ORDER BY timestamp DESC")
    suspend fun allAuditOnce(): List<AuditLogEntity>
}
