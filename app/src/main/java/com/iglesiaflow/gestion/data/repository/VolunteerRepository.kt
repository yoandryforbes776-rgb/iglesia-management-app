package com.iglesiaflow.gestion.data.repository

import com.iglesiaflow.gestion.core.audit.AuditLogger
import com.iglesiaflow.gestion.core.security.SessionManager
import com.iglesiaflow.gestion.data.local.dao.VolunteerDao
import com.iglesiaflow.gestion.data.local.dao.VolunteerHours
import com.iglesiaflow.gestion.data.local.entity.MinistryNeedEntity
import com.iglesiaflow.gestion.data.local.entity.ServiceRecordEntity
import com.iglesiaflow.gestion.data.local.entity.VolunteerSkillEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/** Resultado del matching automático necesidad <-> voluntario. */
data class VolunteerMatch(
    val need: MinistryNeedEntity,
    val candidates: List<VolunteerSkillEntity>
)

@Singleton
class VolunteerRepository @Inject constructor(
    private val dao: VolunteerDao,
    private val auditLogger: AuditLogger,
    private val session: SessionManager
) {
    fun skills(): Flow<List<VolunteerSkillEntity>> = dao.observeSkills()
    fun skillsFor(memberId: Long): Flow<List<VolunteerSkillEntity>> = dao.observeSkillsForMember(memberId)
    fun needs(): Flow<List<MinistryNeedEntity>> = dao.observeNeeds()
    fun serviceRecords(): Flow<List<ServiceRecordEntity>> = dao.observeServiceRecords()
    fun topVolunteers(since: Long, limit: Int = 10): Flow<List<VolunteerHours>> = dao.topVolunteers(since, limit)
    fun totalHours(since: Long): Flow<Double> = dao.totalHoursSince(since)
    fun volunteerCount(): Flow<Int> = dao.countVolunteers()

    suspend fun saveSkill(skill: VolunteerSkillEntity): Long {
        val id = if (skill.id == 0L) dao.insertSkill(skill) else { dao.updateSkill(skill); skill.id }
        auditLogger.log("HABILIDAD_GUARDADA", "volunteer_skills", id, skill.skill,
            session.currentUserId(), session.requireUserName())
        return id
    }

    suspend fun deleteSkill(id: Long) = dao.deleteSkill(id)
    suspend fun saveNeed(need: MinistryNeedEntity): Long = dao.insertNeed(need)
    suspend fun deleteNeed(id: Long) = dao.deleteNeed(id)

    suspend fun saveServiceRecord(record: ServiceRecordEntity): Long {
        val id = dao.insertServiceRecord(record)
        auditLogger.log("SERVICIO_REGISTRADO", "service_records", id, record.ministry,
            session.currentUserId(), session.requireUserName())
        return id
    }

    suspend fun deleteServiceRecord(id: Long) = dao.deleteServiceRecord(id)

    /**
     * Matching automático: empareja cada necesidad con los voluntarios cuya
     * habilidad y disponibilidad encajan.
     */
    fun match(needs: List<MinistryNeedEntity>, skills: List<VolunteerSkillEntity>): List<VolunteerMatch> =
        needs.map { need ->
            val candidates = skills.filter { skill ->
                val skillMatch = skill.skill.contains(need.skillRequired, ignoreCase = true) ||
                    need.skillRequired.contains(skill.skill, ignoreCase = true)
                val dayMatch = need.dayOfWeek.isBlank() ||
                    skill.availability.isBlank() ||
                    skill.availability.contains(need.dayOfWeek, ignoreCase = true)
                skillMatch && dayMatch && skill.active
            }.sortedByDescending { it.level.ordinal }
            VolunteerMatch(need, candidates)
        }
}
