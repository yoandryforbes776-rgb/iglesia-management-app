package com.iglesiaflow.gestion.data.repository

import com.iglesiaflow.gestion.core.audit.AuditLogger
import com.iglesiaflow.gestion.core.security.SessionManager
import com.iglesiaflow.gestion.data.local.dao.CommunicationDao
import com.iglesiaflow.gestion.data.local.entity.CampaignEntity
import com.iglesiaflow.gestion.data.local.entity.PrayerRequestEntity
import com.iglesiaflow.gestion.domain.model.CampaignStatus
import com.iglesiaflow.gestion.domain.model.PrayerStatus
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CommunicationRepository @Inject constructor(
    private val dao: CommunicationDao,
    private val auditLogger: AuditLogger,
    private val session: SessionManager
) {
    fun campaigns(): Flow<List<CampaignEntity>> = dao.observeCampaigns()
    fun prayerRequests(): Flow<List<PrayerRequestEntity>> = dao.observePrayerRequests()
    fun publicPrayerRequests(): Flow<List<PrayerRequestEntity>> = dao.observePublicPrayerRequests()
    fun openPrayerCount(): Flow<Int> = dao.countOpenPrayers()

    suspend fun saveCampaign(campaign: CampaignEntity): Long {
        val stamped = campaign.copy(createdBy = campaign.createdBy.ifBlank { session.requireUserName() })
        val id = if (campaign.id == 0L) dao.insertCampaign(stamped) else { dao.updateCampaign(stamped); campaign.id }
        auditLogger.log("CAMPANA_GUARDADA", "campaigns", id, campaign.subject,
            session.currentUserId(), session.requireUserName())
        return id
    }

    suspend fun markSent(campaignId: Long, recipients: Int) {
        val campaign = dao.getCampaign(campaignId) ?: return
        dao.updateCampaign(
            campaign.copy(
                status = CampaignStatus.ENVIADA,
                sentAt = System.currentTimeMillis(),
                recipientCount = recipients
            )
        )
        auditLogger.log("CAMPANA_ENVIADA", "campaigns", campaignId, "$recipients destinatarios",
            session.currentUserId(), session.requireUserName())
    }

    suspend fun deleteCampaign(id: Long) = dao.deleteCampaign(id)

    suspend fun savePrayer(request: PrayerRequestEntity): Long {
        val id = if (request.id == 0L) dao.insertPrayerRequest(request) else {
            dao.updatePrayerRequest(request); request.id
        }
        return id
    }

    suspend fun prayFor(id: Long) {
        val request = dao.getPrayerRequest(id) ?: return
        dao.updatePrayerRequest(request.copy(prayerCount = request.prayerCount + 1))
    }

    suspend fun answerPrayer(id: Long, note: String) {
        val request = dao.getPrayerRequest(id) ?: return
        dao.updatePrayerRequest(
            request.copy(
                status = PrayerStatus.RESPONDIDA,
                answeredAt = System.currentTimeMillis(),
                answerNote = note
            )
        )
        auditLogger.log("ORACION_RESPONDIDA", "prayer_requests", id, request.title,
            session.currentUserId(), session.requireUserName())
    }

    suspend fun deletePrayer(id: Long) = dao.deletePrayerRequest(id)
}
