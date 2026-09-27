package com.iglesiaflow.gestion.data.repository

import com.iglesiaflow.gestion.core.audit.AuditLogger
import com.iglesiaflow.gestion.core.security.SessionManager
import com.iglesiaflow.gestion.data.local.dao.MemberDao
import com.iglesiaflow.gestion.data.local.entity.CustomFieldDefEntity
import com.iglesiaflow.gestion.data.local.entity.CustomFieldValueEntity
import com.iglesiaflow.gestion.data.local.entity.FamilyEntity
import com.iglesiaflow.gestion.data.local.entity.MemberEntity
import com.iglesiaflow.gestion.data.local.entity.NoteEntity
import com.iglesiaflow.gestion.data.remote.SyncManager
import com.iglesiaflow.gestion.domain.model.CustomFieldEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MemberRepository @Inject constructor(
    private val dao: MemberDao,
    private val auditLogger: AuditLogger,
    private val session: SessionManager,
    private val syncManager: SyncManager
) {
    fun members(): Flow<List<MemberEntity>> = dao.observeMembers()
    fun search(query: String): Flow<List<MemberEntity>> = dao.searchMembers(query.trim())
    fun member(id: Long): Flow<MemberEntity?> = dao.observeMember(id)
    fun children(): Flow<List<MemberEntity>> = dao.observeChildren()
    fun families(): Flow<List<FamilyEntity>> = dao.observeFamilies()
    fun familyMembers(familyId: Long): Flow<List<MemberEntity>> = dao.observeFamilyMembers(familyId)
    fun birthdays(month: Int): Flow<List<MemberEntity>> = dao.observeBirthdaysInMonth(month)
    fun totalMembers(): Flow<Int> = dao.countMembers()
    fun activeMembers(): Flow<Int> = dao.countActiveMembers()
    fun newMembersSince(since: Long): Flow<Int> = dao.countNewMembersSince(since)
    fun totalFamilies(): Flow<Int> = dao.countFamilies()
    fun membersByStatus() = dao.countByStatus()

    suspend fun getMember(id: Long): MemberEntity? = dao.getMember(id)
    suspend fun getFamily(id: Long): FamilyEntity? = dao.getFamily(id)
    suspend fun allMembers(): List<MemberEntity> = dao.allMembersOnce()
    suspend fun allFamilies(): List<FamilyEntity> = dao.allFamiliesOnce()

    suspend fun save(member: MemberEntity): Long {
        val stamped = member.copy(updatedAt = System.currentTimeMillis(), pendingSync = true)
        val id = if (member.id == 0L) dao.insertMember(stamped) else {
            dao.updateMember(stamped); member.id
        }
        auditLogger.log(
            action = if (member.id == 0L) "MIEMBRO_CREADO" else "MIEMBRO_ACTUALIZADO",
            entityType = "members", entityId = id, detail = stamped.fullName,
            userId = session.currentUserId(), userName = session.requireUserName()
        )
        syncManager.requestSync()
        return id
    }

    suspend fun saveAll(members: List<MemberEntity>): Int {
        val ids = dao.insertMembers(members)
        auditLogger.log("MIEMBROS_IMPORTADOS", "members", null, "${ids.size} registros",
            session.currentUserId(), session.requireUserName())
        return ids.size
    }

    suspend fun delete(member: MemberEntity) {
        dao.deleteMember(member)
        auditLogger.log("MIEMBRO_ELIMINADO", "members", member.id, member.fullName,
            session.currentUserId(), session.requireUserName())
    }

    suspend fun saveFamily(family: FamilyEntity): Long {
        val stamped = family.copy(updatedAt = System.currentTimeMillis(), pendingSync = true)
        val id = if (family.id == 0L) dao.insertFamily(stamped) else { dao.updateFamily(stamped); family.id }
        auditLogger.log(
            if (family.id == 0L) "FAMILIA_CREADA" else "FAMILIA_ACTUALIZADA",
            "families", id, family.name, session.currentUserId(), session.requireUserName()
        )
        return id
    }

    suspend fun deleteFamily(id: Long) {
        dao.detachMembersFromFamily(id)
        dao.deleteFamilyById(id)
        auditLogger.log("FAMILIA_ELIMINADA", "families", id, "", session.currentUserId(), session.requireUserName())
    }

    // ---- Campos personalizados ----
    fun customFields(entityType: CustomFieldEntity): Flow<List<CustomFieldDefEntity>> =
        dao.observeCustomFields(entityType)

    fun allCustomFields(): Flow<List<CustomFieldDefEntity>> = dao.observeAllCustomFields()

    fun customValues(entityType: CustomFieldEntity, entityId: Long): Flow<List<CustomFieldValueEntity>> =
        dao.observeCustomValues(entityType, entityId)

    suspend fun saveCustomField(def: CustomFieldDefEntity): Long {
        val id = if (def.id == 0L) dao.insertCustomField(def) else { dao.updateCustomField(def); def.id }
        auditLogger.log("CAMPO_PERSONALIZADO_GUARDADO", "custom_field_defs", id, def.label,
            session.currentUserId(), session.requireUserName())
        return id
    }

    suspend fun deleteCustomField(id: Long) = dao.deleteCustomField(id)

    suspend fun setCustomValue(fieldId: Long, entityType: CustomFieldEntity, entityId: Long, value: String) {
        dao.deleteCustomValue(fieldId, entityId)
        if (value.isNotBlank()) {
            dao.insertCustomValue(
                CustomFieldValueEntity(fieldId = fieldId, entityType = entityType, entityId = entityId, value = value)
            )
        }
    }

    // ---- Notas / timeline ----
    fun notes(entityType: CustomFieldEntity, entityId: Long): Flow<List<NoteEntity>> =
        dao.observeNotes(entityType, entityId)

    suspend fun addNote(note: NoteEntity): Long = dao.insertNote(
        note.copy(authorId = note.authorId ?: session.currentUserId(),
            authorName = note.authorName.ifBlank { session.requireUserName() })
    )

    suspend fun deleteNote(id: Long) = dao.deleteNote(id)
}
