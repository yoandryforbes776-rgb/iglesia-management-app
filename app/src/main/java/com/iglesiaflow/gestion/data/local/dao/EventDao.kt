package com.iglesiaflow.gestion.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.iglesiaflow.gestion.data.local.entity.AttendanceEntity
import com.iglesiaflow.gestion.data.local.entity.CheckInEntity
import com.iglesiaflow.gestion.data.local.entity.EventEntity
import kotlinx.coroutines.flow.Flow

data class AttendanceRow(
    val memberId: Long,
    val firstName: String,
    val lastName: String,
    val present: Boolean,
    val attendanceId: Long?
)

@Dao
interface EventDao {

    @Query("SELECT * FROM events ORDER BY startAt DESC")
    fun observeEvents(): Flow<List<EventEntity>>

    @Query("SELECT * FROM events WHERE startAt >= :from ORDER BY startAt ASC LIMIT :limit")
    fun observeUpcoming(from: Long, limit: Int): Flow<List<EventEntity>>

    @Query("SELECT * FROM events WHERE startAt BETWEEN :from AND :to ORDER BY startAt ASC")
    fun observeEventsBetween(from: Long, to: Long): Flow<List<EventEntity>>

    @Query("SELECT * FROM events WHERE id = :id")
    fun observeEvent(id: Long): Flow<EventEntity?>

    @Query("SELECT * FROM events WHERE id = :id")
    suspend fun getEvent(id: Long): EventEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: EventEntity): Long

    @Update
    suspend fun updateEvent(event: EventEntity)

    @Query("DELETE FROM events WHERE id = :id")
    suspend fun deleteEvent(id: Long)

    @Query("SELECT * FROM events")
    suspend fun allEventsOnce(): List<EventEntity>

    // ---- Asistencia ----
    @Query(
        """
        SELECT m.id AS memberId, m.firstName AS firstName, m.lastName AS lastName,
               IFNULL(a.present, 0) AS present, a.id AS attendanceId
        FROM members m LEFT JOIN attendance a ON a.memberId = m.id AND a.eventId = :eventId
        ORDER BY m.lastName COLLATE NOCASE, m.firstName COLLATE NOCASE
        """
    )
    fun observeAttendanceRows(eventId: Long): Flow<List<AttendanceRow>>

    @Query("SELECT * FROM attendance WHERE eventId = :eventId")
    fun observeAttendance(eventId: Long): Flow<List<AttendanceEntity>>

    @Query("SELECT * FROM attendance WHERE eventId = :eventId AND memberId = :memberId LIMIT 1")
    suspend fun findAttendance(eventId: Long, memberId: Long): AttendanceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendance(attendance: AttendanceEntity): Long

    @Query("DELETE FROM attendance WHERE id = :id")
    suspend fun deleteAttendance(id: Long)

    @Query("SELECT COUNT(*) FROM attendance WHERE eventId = :eventId AND present = 1")
    fun countPresent(eventId: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM attendance WHERE present = 1 AND registeredAt >= :since")
    fun countAttendanceSince(since: Long): Flow<Int>

    @Query(
        """
        SELECT strftime('%Y-%m', e.startAt / 1000, 'unixepoch') AS period, COUNT(a.id) AS total
        FROM attendance a INNER JOIN events e ON e.id = a.eventId
        WHERE a.present = 1 AND e.startAt BETWEEN :from AND :to
        GROUP BY period ORDER BY period
        """
    )
    fun attendanceByMonth(from: Long, to: Long): Flow<List<PeriodTotal>>

    @Query(
        """
        SELECT e.title AS label, COUNT(a.id) * 1.0 AS total
        FROM events e LEFT JOIN attendance a ON a.eventId = e.id AND a.present = 1
        GROUP BY e.id ORDER BY e.startAt DESC LIMIT :limit
        """
    )
    fun attendanceByEvent(limit: Int): Flow<List<LabeledTotal>>

    // ---- Check-in infantil ----
    @Query("SELECT * FROM check_ins WHERE eventId = :eventId ORDER BY checkInAt DESC")
    fun observeCheckIns(eventId: Long): Flow<List<CheckInEntity>>

    @Query("SELECT * FROM check_ins WHERE checkOutAt IS NULL ORDER BY checkInAt DESC")
    fun observeActiveCheckIns(): Flow<List<CheckInEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCheckIn(checkIn: CheckInEntity): Long

    @Update
    suspend fun updateCheckIn(checkIn: CheckInEntity)

    @Query("SELECT * FROM check_ins WHERE id = :id")
    suspend fun getCheckIn(id: Long): CheckInEntity?

    @Query("SELECT COUNT(*) FROM check_ins WHERE checkOutAt IS NULL")
    fun countActiveCheckIns(): Flow<Int>
}
