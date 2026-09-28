package com.iglesiaflow.gestion.data.repository

import com.iglesiaflow.gestion.core.audit.AuditLogger
import com.iglesiaflow.gestion.core.security.SessionManager
import com.iglesiaflow.gestion.data.local.dao.SyncDao
import com.iglesiaflow.gestion.data.local.dao.FinanceDao
import com.iglesiaflow.gestion.data.local.dao.LabeledTotal
import com.iglesiaflow.gestion.data.local.dao.PeriodTotal
import com.iglesiaflow.gestion.data.local.entity.DepositEntity
import com.iglesiaflow.gestion.data.local.entity.DonationEntity
import com.iglesiaflow.gestion.data.local.entity.EnvelopeEntity
import com.iglesiaflow.gestion.data.local.entity.ExpenseEntity
import com.iglesiaflow.gestion.data.local.entity.FundEntity
import com.iglesiaflow.gestion.data.local.entity.PledgeEntity
import com.iglesiaflow.gestion.data.remote.RealtimeSyncManager
import com.iglesiaflow.gestion.data.remote.SyncManager
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

data class PledgeProgress(
    val pledge: PledgeEntity,
    val memberName: String,
    val contributed: Double
) {
    val progress: Float
        get() = if (pledge.totalAmount <= 0) 0f else (contributed / pledge.totalAmount).toFloat().coerceIn(0f, 1f)
}

@Singleton
class FinanceRepository @Inject constructor(
    private val dao: FinanceDao,
    private val auditLogger: AuditLogger,
    private val session: SessionManager,
    private val syncManager: SyncManager,
    private val syncDao: SyncDao
) {
    fun donations(): Flow<List<DonationEntity>> = dao.observeDonations()
    fun donationsBetween(from: Long, to: Long): Flow<List<DonationEntity>> = dao.observeDonationsBetween(from, to)
    fun donationsByMember(memberId: Long): Flow<List<DonationEntity>> = dao.observeDonationsByMember(memberId)
    fun totalBetween(from: Long, to: Long): Flow<Double> = dao.sumDonationsBetween(from, to)
    fun totalAll(): Flow<Double> = dao.sumAllDonations()
    fun byMonth(from: Long, to: Long): Flow<List<PeriodTotal>> = dao.donationsByMonth(from, to)
    fun byFund(): Flow<List<LabeledTotal>> = dao.donationsByFund()
    fun byType(): Flow<List<LabeledTotal>> = dao.donationsByType()
    fun topDonors(from: Long, to: Long, limit: Int = 10): Flow<List<LabeledTotal>> = dao.topDonors(from, to, limit)
    fun funds(): Flow<List<FundEntity>> = dao.observeFunds()
    fun pledges(): Flow<List<PledgeEntity>> = dao.observePledges()
    fun envelopes(year: Int): Flow<List<EnvelopeEntity>> = dao.observeEnvelopes(year)
    fun deposits(): Flow<List<DepositEntity>> = dao.observeDeposits()
    fun expenses(): Flow<List<ExpenseEntity>> = dao.observeExpenses()
    fun expensesBetween(from: Long, to: Long): Flow<Double> = dao.sumExpensesBetween(from, to)
    fun expensesByCategory(): Flow<List<LabeledTotal>> = dao.expensesByCategory()

    suspend fun allDonations(): List<DonationEntity> = dao.allDonationsOnce()
    suspend fun allFunds(): List<FundEntity> = dao.allFundsOnce()

    suspend fun saveDonation(donation: DonationEntity): Long {
        val stamped = donation.copy(
            createdBy = donation.createdBy.ifBlank { session.requireUserName() },
            updatedAt = System.currentTimeMillis(),
            pendingSync = true
        )
        val id = if (donation.id == 0L) dao.insertDonation(stamped) else { dao.updateDonation(stamped); donation.id }
        auditLogger.log(
            if (donation.id == 0L) "DONACION_REGISTRADA" else "DONACION_ACTUALIZADA",
            "donations", id, "${donation.amount} (${donation.type.name})",
            session.currentUserId(), session.requireUserName()
        )
        syncManager.requestSync()
        return id
    }

    suspend fun deleteDonation(id: Long) {
        val remoteId = syncDao.donationRemoteById(id)
        dao.deleteDonation(id)
        syncManager.notifyDeleted(RealtimeSyncManager.DONATIONS, remoteId)
        auditLogger.log("DONACION_ELIMINADA", "donations", id, "", session.currentUserId(), session.requireUserName())
    }

    suspend fun saveFund(fund: FundEntity): Long {
        val id = dao.insertFund(fund.copy(updatedAt = System.currentTimeMillis(), pendingSync = true))
        syncManager.requestSync()
        return id
    }

    suspend fun deleteFund(id: Long) {
        val remoteId = syncDao.fundRemoteById(id)
        dao.deleteFund(id)
        syncManager.notifyDeleted(RealtimeSyncManager.FUNDS, remoteId)
    }

    suspend fun savePledge(pledge: PledgeEntity): Long {
        val id = dao.insertPledge(pledge)
        auditLogger.log("PROMESA_GUARDADA", "pledges", id, pledge.campaign,
            session.currentUserId(), session.requireUserName())
        return id
    }

    suspend fun deletePledge(id: Long) = dao.deletePledge(id)

    suspend fun pledgeProgress(pledge: PledgeEntity): Double = dao.sumDonationsForPledge(pledge.memberId, pledge.fundId)

    suspend fun saveEnvelope(envelope: EnvelopeEntity): Long = dao.insertEnvelope(envelope)
    suspend fun deleteEnvelope(id: Long) = dao.deleteEnvelope(id)
    suspend fun envelopeTotal(number: Int): Double = dao.sumByEnvelope(number)

    suspend fun saveDeposit(deposit: DepositEntity): Long =
        if (deposit.id == 0L) dao.insertDeposit(deposit) else { dao.updateDeposit(deposit); deposit.id }

    suspend fun deleteDeposit(id: Long) = dao.deleteDeposit(id)

    suspend fun assignToDeposit(depositId: Long, donationIds: List<Long>) {
        dao.assignDonationsToDeposit(depositId, donationIds)
        auditLogger.log("DEPOSITO_ACTUALIZADO", "deposits", depositId, "${donationIds.size} donaciones",
            session.currentUserId(), session.requireUserName())
    }

    fun depositTotal(depositId: Long): Flow<Double> = dao.sumByDeposit(depositId)

    suspend fun saveExpense(expense: ExpenseEntity): Long {
        val id = dao.insertExpense(expense.copy(createdBy = expense.createdBy.ifBlank { session.requireUserName() }))
        auditLogger.log("GASTO_REGISTRADO", "expenses", id, expense.category,
            session.currentUserId(), session.requireUserName())
        return id
    }

    suspend fun deleteExpense(id: Long) = dao.deleteExpense(id)
}
