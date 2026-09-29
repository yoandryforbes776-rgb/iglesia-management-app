package com.iglesiaflow.gestion.data.repository

import com.iglesiaflow.gestion.core.config.SettingsRepository
import com.iglesiaflow.gestion.core.util.NotificationHelper
import com.iglesiaflow.gestion.data.local.dao.EventDao
import com.iglesiaflow.gestion.data.local.dao.MemberAttendanceRow
import com.iglesiaflow.gestion.data.local.dao.MemberDao
import com.iglesiaflow.gestion.data.local.entity.EventEntity
import com.iglesiaflow.gestion.data.local.entity.MemberEntity
import com.iglesiaflow.gestion.domain.model.MemberStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/** Miembro que acumula ausencias consecutivas a los últimos cultos/eventos. */
data class AbsenceAlert(
    val memberId: Long,
    val name: String,
    val phone: String,
    val missedCount: Int,
    val lastAttendedAt: Long?
)

/** Resumen de asistencia de un miembro en un periodo. */
data class MemberAttendanceSummary(
    val member: MemberEntity?,
    val rows: List<MemberAttendanceRow> = emptyList(),
    val attended: Int = 0,
    val total: Int = 0
) {
    val percentage: Int get() = if (total == 0) 0 else (attended * 100) / total
}

/**
 * Módulo de asistencia: histórico por miembro (calendario) y detección de
 * ausencias reiteradas para avisar a los líderes.
 */
@Singleton
class AttendanceRepository @Inject constructor(
    private val eventDao: EventDao,
    private val memberDao: MemberDao,
    private val settingsRepository: SettingsRepository,
    private val notificationHelper: NotificationHelper
) {

    fun member(memberId: Long): Flow<MemberEntity?> = memberDao.observeMember(memberId)

    fun members(): Flow<List<MemberEntity>> = memberDao.observeMembers()

    /** Eventos del rango con la marca de presencia del miembro. */
    fun memberAttendance(memberId: Long, from: Long, to: Long): Flow<List<MemberAttendanceRow>> =
        eventDao.observeMemberAttendance(memberId, from, to)

    fun memberSummary(memberId: Long, from: Long, to: Long): Flow<MemberAttendanceSummary> =
        combine(
            memberDao.observeMember(memberId),
            eventDao.observeMemberAttendance(memberId, from, to)
        ) { member, rows ->
            MemberAttendanceSummary(
                member = member,
                rows = rows,
                attended = rows.count { it.present },
                total = rows.size
            )
        }

    /** Porcentaje de asistencia de cada miembro sobre los últimos [window] eventos. */
    fun attendanceOverview(window: Int = ALERT_WINDOW): Flow<List<Pair<MemberEntity, Int>>> =
        combine(
            memberDao.observeMembers(),
            eventDao.observePastEvents(System.currentTimeMillis(), window),
            eventDao.observeAttendanceFacts()
        ) { members, events, facts ->
            val present = facts.filter { it.present }.map { it.eventId to it.memberId }.toSet()
            members.map { member ->
                val attended = events.count { (it.id to member.id) in present }
                member to if (events.isEmpty()) 0 else attended * 100 / events.size
            }
        }

    /**
     * Miembros activos que han faltado [minAbsences] veces seguidas a los
     * últimos eventos ya celebrados. Solo se tienen en cuenta los eventos
     * posteriores a su alta, para no penalizar a quien acaba de llegar.
     */
    fun absenceAlerts(minAbsences: Int = MIN_ABSENCES, window: Int = ALERT_WINDOW): Flow<List<AbsenceAlert>> =
        combine(
            memberDao.observeMembers(),
            eventDao.observePastEvents(System.currentTimeMillis(), window),
            eventDao.observeAttendanceFacts()
        ) { members, events, facts ->
            computeAlerts(members, events, facts.filter { it.present }.map { it.eventId to it.memberId }.toSet(), minAbsences)
        }

    private fun computeAlerts(
        members: List<MemberEntity>,
        events: List<EventEntity>,
        present: Set<Pair<Long, Long>>,
        minAbsences: Int
    ): List<AbsenceAlert> {
        if (events.size < minAbsences) return emptyList()
        return members.asSequence()
            .filter { it.status == MemberStatus.ACTIVO }
            .mapNotNull { member ->
                val visible = events.filter { it.startAt >= member.joinedAt }
                if (visible.size < minAbsences) return@mapNotNull null
                var missed = 0
                var lastAttendedAt: Long? = null
                for (event in visible) {
                    if ((event.id to member.id) in present) {
                        lastAttendedAt = event.startAt
                        break
                    }
                    missed++
                }
                if (missed >= minAbsences) {
                    AbsenceAlert(
                        memberId = member.id,
                        name = member.fullName,
                        phone = member.phone,
                        missedCount = missed,
                        lastAttendedAt = lastAttendedAt
                    )
                } else {
                    null
                }
            }
            .sortedByDescending { it.missedCount }
            .toList()
    }

    /**
     * Lanza una notificación local al líder si hay ausencias nuevas. Se avisa
     * como máximo una vez cada 12 horas para no resultar molesto.
     */
    suspend fun notifyLeaders(alerts: List<AbsenceAlert>) {
        if (alerts.isEmpty()) return
        val settings = settingsRepository.settings.first()
        if (!settings.notifyAbsences) return
        val now = System.currentTimeMillis()
        if (now - settings.lastAbsenceAlertAt < NOTIFY_INTERVAL_MS) return
        val names = alerts.take(3).joinToString(", ") { "${it.name} (${it.missedCount})" }
        val extra = if (alerts.size > 3) " y ${alerts.size - 3} más" else ""
        notificationHelper.notify(
            channelId = NotificationHelper.CHANNEL_ATTENDANCE,
            title = "Seguimiento pastoral: ${alerts.size} miembro(s) faltando",
            message = "Han faltado 2 o más veces seguidas: $names$extra. Toca para revisar la asistencia.",
            id = ABSENCE_NOTIFICATION_ID
        )
        settingsRepository.update { it.copy(lastAbsenceAlertAt = now) }
    }

    fun pastEventCount(): Flow<Int> = eventDao.countPastEvents(System.currentTimeMillis())

    fun alertCount(): Flow<Int> = absenceAlerts().map { it.size }

    companion object {
        const val MIN_ABSENCES = 2
        const val ALERT_WINDOW = 8
        private const val NOTIFY_INTERVAL_MS = 12 * 60 * 60 * 1000L
        private const val ABSENCE_NOTIFICATION_ID = 90201
    }
}
