package com.iglesiaflow.gestion.data.repository

import com.iglesiaflow.gestion.core.audit.AuditLogger
import com.iglesiaflow.gestion.core.security.SessionManager
import com.iglesiaflow.gestion.data.local.dao.GroupDao
import com.iglesiaflow.gestion.data.local.dao.GroupMemberRow
import com.iglesiaflow.gestion.data.local.dao.GroupSummaryRow
import com.iglesiaflow.gestion.data.local.entity.GroupEntity
import com.iglesiaflow.gestion.data.local.entity.GroupMemberEntity
import com.iglesiaflow.gestion.data.local.entity.GroupMessageEntity
import com.iglesiaflow.gestion.domain.model.GroupRole
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GroupRepository @Inject constructor(
    private val dao: GroupDao,
    private val auditLogger: AuditLogger,
    private val session: SessionManager
) {
    fun groups(): Flow<List<GroupEntity>> = dao.observeGroups()
    fun group(id: Long): Flow<GroupEntity?> = dao.observeGroup(id)
    fun activeGroups(): Flow<Int> = dao.countActiveGroups()
    fun members(groupId: Long): Flow<List<GroupMemberRow>> = dao.observeGroupMembers(groupId)
    fun sizes(): Flow<List<GroupSummaryRow>> = dao.observeGroupSizes()
    fun messages(groupId: Long): Flow<List<GroupMessageEntity>> = dao.observeGroupMessages(groupId)

    suspend fun allGroups(): List<GroupEntity> = dao.allGroupsOnce()

    suspend fun save(group: GroupEntity): Long {
        val id = if (group.id == 0L) dao.insertGroup(group) else { dao.updateGroup(group); group.id }
        auditLogger.log(
            if (group.id == 0L) "GRUPO_CREADO" else "GRUPO_ACTUALIZADO",
            "church_groups", id, group.name, session.currentUserId(), session.requireUserName()
        )
        return id
    }

    suspend fun delete(id: Long) {
        dao.deleteMembersOfGroup(id)
        dao.deleteGroup(id)
        auditLogger.log("GRUPO_ELIMINADO", "church_groups", id, "", session.currentUserId(), session.requireUserName())
    }

    suspend fun addMember(groupId: Long, memberId: Long, role: GroupRole) {
        dao.insertGroupMember(GroupMemberEntity(groupId = groupId, memberId = memberId, role = role))
    }

    suspend fun removeMember(membershipId: Long) = dao.deleteGroupMember(membershipId)

    suspend fun postMessage(groupId: Long, content: String) {
        dao.insertGroupMessage(
            GroupMessageEntity(
                groupId = groupId,
                authorId = session.currentUserId(),
                authorName = session.requireUserName(),
                content = content
            )
        )
    }

    suspend fun deleteMessage(id: Long) = dao.deleteGroupMessage(id)
}
