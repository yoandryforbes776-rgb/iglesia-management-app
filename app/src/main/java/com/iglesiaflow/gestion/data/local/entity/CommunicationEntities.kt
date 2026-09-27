package com.iglesiaflow.gestion.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.iglesiaflow.gestion.domain.model.CampaignChannel
import com.iglesiaflow.gestion.domain.model.CampaignStatus
import com.iglesiaflow.gestion.domain.model.PrayerStatus

@Entity(tableName = "campaigns", indices = [Index("status")])
data class CampaignEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subject: String,
    val body: String,
    val channel: CampaignChannel = CampaignChannel.EMAIL,
    /** ALL | MEMBERS | LEADERS | GROUP:{id} | VOLUNTEERS */
    val audience: String = "ALL",
    val status: CampaignStatus = CampaignStatus.BORRADOR,
    val scheduledAt: Long? = null,
    val sentAt: Long? = null,
    val recipientCount: Int = 0,
    val createdBy: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "prayer_requests", indices = [Index("status")])
data class PrayerRequestEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val detail: String = "",
    val requesterId: Long? = null,
    val requesterName: String = "",
    val isPrivate: Boolean = false,
    val status: PrayerStatus = PrayerStatus.ABIERTA,
    val prayerCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val answeredAt: Long? = null,
    val answerNote: String = ""
)
