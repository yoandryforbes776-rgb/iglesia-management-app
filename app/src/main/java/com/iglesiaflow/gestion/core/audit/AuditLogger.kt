package com.iglesiaflow.gestion.core.audit

import com.iglesiaflow.gestion.data.local.dao.AdminDao
import com.iglesiaflow.gestion.data.local.entity.AuditLogEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/** Registro de auditoría: quién hizo qué y cuándo. */
@Singleton
class AuditLogger @Inject constructor(private val adminDao: AdminDao) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun log(
        action: String,
        entityType: String = "",
        entityId: Long? = null,
        detail: String = "",
        userId: Long? = null,
        userName: String = "sistema"
    ) {
        scope.launch {
            runCatching {
                adminDao.insertAudit(
                    AuditLogEntity(
                        userId = userId,
                        userName = userName,
                        action = action,
                        entityType = entityType,
                        entityId = entityId,
                        detail = detail
                    )
                )
            }
        }
    }

    suspend fun logSuspend(entry: AuditLogEntity) {
        runCatching { adminDao.insertAudit(entry) }
    }
}
