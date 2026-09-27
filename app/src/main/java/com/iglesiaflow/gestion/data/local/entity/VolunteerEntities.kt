package com.iglesiaflow.gestion.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.iglesiaflow.gestion.domain.model.SkillLevel

@Entity(tableName = "volunteer_skills", indices = [Index("memberId")])
data class VolunteerSkillEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val memberId: Long,
    val skill: String,
    val level: SkillLevel = SkillLevel.BASICO,
    /** Disponibilidad en texto libre o días separados por coma. */
    val availability: String = "",
    val notes: String = "",
    val active: Boolean = true
)

/** Necesidades de cada ministerio para el matching automático. */
@Entity(tableName = "ministry_needs")
data class MinistryNeedEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val ministry: String,
    val skillRequired: String,
    val slots: Int = 1,
    val dayOfWeek: String = "",
    val note: String = "",
    val active: Boolean = true
)

@Entity(tableName = "service_records", indices = [Index("memberId"), Index("date")])
data class ServiceRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val memberId: Long,
    val ministry: String,
    val eventId: Long? = null,
    val hours: Double = 1.0,
    val date: Long = System.currentTimeMillis(),
    val note: String = ""
)
