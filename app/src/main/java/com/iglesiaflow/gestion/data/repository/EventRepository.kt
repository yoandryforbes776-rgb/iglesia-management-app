package com.iglesiaflow.gestion.data.repository

import com.iglesiaflow.gestion.core.audit.AuditLogger
import com.iglesiaflow.gestion.core.security.SessionManager
import com.iglesiaflow.gestion.data.local.dao.SyncDao
import com.iglesiaflow.gestion.core.util.DateTimeUtils
import com.iglesiaflow.gestion.data.local.dao.AttendanceRow
import com.iglesiaflow.gestion.data.local.dao.EventDao
import com.iglesiaflow.gestion.data.local.dao.LabeledTotal
import com.iglesiaflow.gestion.data.local.dao.PeriodTotal
import com.iglesiaflow.gestion.data.local.entity.AttendanceEntity
import com.iglesiaflow.gestion.data.local.entity.CheckInEntity
import com.iglesiaflow.gestion.data.local.entity.EventEntity
import com.iglesiaflow.gestion.data.remote.RealtimeSyncManager
import com.iglesiaflow.gestion.data.remote.SyncManager
import kotlinx.coroutines.flow.Flow
import kotlin.random.Random
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EventRepository @Inject constructor(
    private val dao: EventDao,
    private val auditLogger: AuditLogger,
    private val session: SessionManager,
    private val syncManager: SyncManager,
    private val syncDao: SyncDao
) {
    fun events(): Flow<List<EventEntity>> = dao.observeEvents()
    fun upcoming(limit: Int = 5): Flow<List<EventEntity>> = dao.observeUpcoming(System.currentTimeMillis(), limit)
    fun between(from: Long, to: Long): Flow<List<EventEntity>> = dao.observeEventsBetween(from, to)
    fun event(id: Long): Flow<EventEntity?> = dao.observeEvent(id)
    fun attendanceRows(eventId: Long): Flow<List<AttendanceRow>> = dao.observeAttendanceRows(eventId)
    fun attendance(eventId: Long): Flow<List<AttendanceEntity>> = dao.observeAttendance(eventId)
    fun presentCount(eventId: Long): Flow<Int> = dao.countPresent(eventId)
    fun attendanceSince(since: Long): Flow<Int> = dao.countAttendanceSince(since)
    fun attendanceByMonth(from: Long, to: Long): Flow<List<PeriodTotal>> = dao.attendanceByMonth(from, to)
    fun attendanceByEvent(limit: Int = 8): Flow<List<LabeledTotal>> = dao.attendanceByEvent(limit)
    fun checkIns(eventId: Long): Flow<List<CheckInEntity>> = dao.observeCheckIns(eventId)
    fun activeCheckIns(): Flow<List<CheckInEntity>> = dao.observeActiveCheckIns()
    fun activeCheckInCount(): Flow<Int> = dao.countActiveCheckIns()

    suspend fun getEvent(id: Long): EventEntity? = dao.getEvent(id)
    suspend fun allEvents(): List<EventEntity> = dao.allEventsOnce()

    suspend fun save(event: EventEntity): Long {
        val stamped = event.copy(
            createdBy = event.createdBy.ifBlank { session.requireUserName() },
            updatedAt = System.currentTimeMillis(),
            pendingSync = true
        )
        val id = if (event.id == 0L) dao.insertEvent(stamped) else { dao.updateEvent(stamped); event.id }
        auditLogger.log(
            if (event.id == 0L) "EVENTO_CREADO" else "EVENTO_ACTUALIZADO",
            "events", id, event.title, session.currentUserId(), session.requireUserName()
        )
        syncManager.requestSync()
        return id
    }

    /** Crea las siguientes N repeticiones de un evento recurrente. */
    suspend fun generateRecurrences(event: EventEntity, times: Int = 8) {
        if (event.recurrence.equals("NONE", true)) return
        var start = event.startAt
        var end = event.endAt
        repeat(times) {
            val nextStart = DateTimeUtils.nextOccurrence(start, event.recurrence)
            val nextEnd = nextStart + (end - start)
            dao.insertEvent(event.copy(id = 0, startAt = nextStart, endAt = nextEnd, recurrence = event.recurrence))
            start = nextStart
            end = nextEnd
        }
    }

    suspend fun delete(id: Long) {
        val remoteId = syncDao.eventRemoteById(id)
        dao.deleteEvent(id)
        syncManager.notifyDeleted(RealtimeSyncManager.EVENTS, remoteId)
        auditLogger.log("EVENTO_ELIMINADO", "events", id, "", session.currentUserId(), session.requireUserName())
    }

    suspend fun toggleAttendance(eventId: Long, memberId: Long, present: Boolean) {
        val existing = dao.findAttendance(eventId, memberId)
        if (existing == null) {
            dao.insertAttendance(AttendanceEntity(eventId = eventId, memberId = memberId, present = present))
        } else {
            dao.insertAttendance(
                existing.copy(
                    present = present,
                    registeredAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis(),
                    pendingSync = true
                )
            )
        }
        syncManager.requestSync()
    }

    suspend fun checkIn(
        eventId: Long,
        childMemberId: Long,
        childName: String,
        guardianName: String,
        guardianPhone: String,
        room: String,
        allergies: String
    ): CheckInEntity {
        val code = Random.nextInt(1000, 9999).toString()
        val entity = CheckInEntity(
            eventId = eventId,
            childMemberId = childMemberId,
            childName = childName,
            guardianName = guardianName,
            guardianPhone = guardianPhone,
            securityCode = code,
            room = room,
            allergies = allergies,
            checkedInBy = session.requireUserName()
        )
        val id = dao.insertCheckIn(entity)
        auditLogger.log("CHECKIN", "check_ins", id, childName, session.currentUserId(), session.requireUserName())
        return entity.copy(id = id)
    }

    suspend fun checkOut(checkInId: Long, code: String): Boolean {
        val checkIn = dao.getCheckIn(checkInId) ?: return false
        if (checkIn.securityCode != code.trim()) return false
        dao.updateCheckIn(
            checkIn.copy(checkOutAt = System.currentTimeMillis(), checkedOutBy = session.requireUserName())
        )
        auditLogger.log("CHECKOUT", "check_ins", checkInId, checkIn.childName,
            session.currentUserId(), session.requireUserName())
        return true
    }

    suspend fun markNotified(checkIn: CheckInEntity) {
        dao.updateCheckIn(checkIn.copy(guardianNotified = true))
    }
}
