package com.iglesiaflow.gestion.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.iglesiaflow.gestion.domain.model.EventType

@Entity(tableName = "events", indices = [Index("startAt"), Index("groupId")])
data class EventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String = "",
    val type: EventType = EventType.CULTO,
    val location: String = "",
    val startAt: Long,
    val endAt: Long,
    val allDay: Boolean = false,
    /** Regla simple de recurrencia: NONE | DAILY | WEEKLY | BIWEEKLY | MONTHLY */
    val recurrence: String = "NONE",
    val timezoneId: String = "UTC",
    val requiresCheckIn: Boolean = false,
    val groupId: Long? = null,
    val capacity: Int? = null,
    val colorArgb: Long? = null,
    val reminderMinutesBefore: Int = 60,
    val createdBy: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val remoteId: String? = null,
    val pendingSync: Boolean = true
)

@Entity(
    tableName = "attendance",
    indices = [Index("eventId"), Index("memberId"), Index(value = ["eventId", "memberId"], unique = true)]
)
data class AttendanceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val eventId: Long,
    val memberId: Long,
    val present: Boolean = true,
    val registeredAt: Long = System.currentTimeMillis(),
    val note: String = "",
    val remoteId: String? = null,
    val pendingSync: Boolean = true,
    val updatedAt: Long = System.currentTimeMillis()
)

/** Check-in/out del ministerio infantil con código de seguridad. */
@Entity(tableName = "check_ins", indices = [Index("eventId"), Index("childMemberId")])
data class CheckInEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val eventId: Long,
    val childMemberId: Long,
    val childName: String,
    val guardianName: String = "",
    val guardianPhone: String = "",
    val securityCode: String,
    val room: String = "",
    val allergies: String = "",
    val checkInAt: Long = System.currentTimeMillis(),
    val checkOutAt: Long? = null,
    val checkedInBy: String = "",
    val checkedOutBy: String = "",
    val guardianNotified: Boolean = false
)
