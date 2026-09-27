package com.iglesiaflow.gestion.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.iglesiaflow.gestion.data.local.entity.DepositEntity
import com.iglesiaflow.gestion.data.local.entity.DonationEntity
import com.iglesiaflow.gestion.data.local.entity.EnvelopeEntity
import com.iglesiaflow.gestion.data.local.entity.ExpenseEntity
import com.iglesiaflow.gestion.data.local.entity.FundEntity
import com.iglesiaflow.gestion.data.local.entity.PledgeEntity
import kotlinx.coroutines.flow.Flow

data class PeriodTotal(val period: String, val total: Double)
data class LabeledTotal(val label: String, val total: Double)

@Dao
interface FinanceDao {

    // ---- Donaciones ----
    @Query("SELECT * FROM donations ORDER BY date DESC")
    fun observeDonations(): Flow<List<DonationEntity>>

    @Query("SELECT * FROM donations WHERE date BETWEEN :from AND :to ORDER BY date DESC")
    fun observeDonationsBetween(from: Long, to: Long): Flow<List<DonationEntity>>

    @Query("SELECT * FROM donations WHERE memberId = :memberId ORDER BY date DESC")
    fun observeDonationsByMember(memberId: Long): Flow<List<DonationEntity>>

    @Query("SELECT IFNULL(SUM(amount), 0) FROM donations WHERE date BETWEEN :from AND :to")
    fun sumDonationsBetween(from: Long, to: Long): Flow<Double>

    @Query("SELECT IFNULL(SUM(amount), 0) FROM donations")
    fun sumAllDonations(): Flow<Double>

    @Query("SELECT IFNULL(SUM(amount), 0) FROM donations WHERE memberId = :memberId AND fundId = :fundId")
    suspend fun sumDonationsForPledge(memberId: Long, fundId: Long?): Double

    @Query(
        """
        SELECT strftime('%Y-%m', date / 1000, 'unixepoch') AS period, IFNULL(SUM(amount), 0) AS total
        FROM donations WHERE date BETWEEN :from AND :to
        GROUP BY period ORDER BY period
        """
    )
    fun donationsByMonth(from: Long, to: Long): Flow<List<PeriodTotal>>

    @Query(
        """
        SELECT IFNULL(f.name, 'Sin fondo') AS label, IFNULL(SUM(d.amount), 0) AS total
        FROM donations d LEFT JOIN funds f ON f.id = d.fundId
        GROUP BY label ORDER BY total DESC
        """
    )
    fun donationsByFund(): Flow<List<LabeledTotal>>

    @Query("SELECT type AS label, IFNULL(SUM(amount), 0) AS total FROM donations GROUP BY type ORDER BY total DESC")
    fun donationsByType(): Flow<List<LabeledTotal>>

    @Query(
        """
        SELECT IFNULL(m.firstName || ' ' || m.lastName, 'Anónimo') AS label, IFNULL(SUM(d.amount), 0) AS total
        FROM donations d LEFT JOIN members m ON m.id = d.memberId
        WHERE d.date BETWEEN :from AND :to
        GROUP BY label ORDER BY total DESC LIMIT :limit
        """
    )
    fun topDonors(from: Long, to: Long, limit: Int): Flow<List<LabeledTotal>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDonation(donation: DonationEntity): Long

    @Update
    suspend fun updateDonation(donation: DonationEntity)

    @Query("DELETE FROM donations WHERE id = :id")
    suspend fun deleteDonation(id: Long)

    @Query("SELECT * FROM donations")
    suspend fun allDonationsOnce(): List<DonationEntity>

    // ---- Fondos ----
    @Query("SELECT * FROM funds ORDER BY name")
    fun observeFunds(): Flow<List<FundEntity>>

    @Query("SELECT * FROM funds")
    suspend fun allFundsOnce(): List<FundEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFund(fund: FundEntity): Long

    @Query("DELETE FROM funds WHERE id = :id")
    suspend fun deleteFund(id: Long)

    // ---- Promesas de fe ----
    @Query("SELECT * FROM pledges ORDER BY startDate DESC")
    fun observePledges(): Flow<List<PledgeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPledge(pledge: PledgeEntity): Long

    @Query("DELETE FROM pledges WHERE id = :id")
    suspend fun deletePledge(id: Long)

    // ---- Sobres ----
    @Query("SELECT * FROM envelopes WHERE year = :year ORDER BY number")
    fun observeEnvelopes(year: Int): Flow<List<EnvelopeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEnvelope(envelope: EnvelopeEntity): Long

    @Query("DELETE FROM envelopes WHERE id = :id")
    suspend fun deleteEnvelope(id: Long)

    @Query("SELECT IFNULL(SUM(amount), 0) FROM donations WHERE envelopeNumber = :number")
    suspend fun sumByEnvelope(number: Int): Double

    // ---- Depósitos ----
    @Query("SELECT * FROM deposits ORDER BY date DESC")
    fun observeDeposits(): Flow<List<DepositEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDeposit(deposit: DepositEntity): Long

    @Update
    suspend fun updateDeposit(deposit: DepositEntity)

    @Query("DELETE FROM deposits WHERE id = :id")
    suspend fun deleteDeposit(id: Long)

    @Query("UPDATE donations SET depositId = :depositId WHERE id IN (:donationIds)")
    suspend fun assignDonationsToDeposit(depositId: Long, donationIds: List<Long>)

    @Query("SELECT IFNULL(SUM(amount), 0) FROM donations WHERE depositId = :depositId")
    fun sumByDeposit(depositId: Long): Flow<Double>

    // ---- Gastos ----
    @Query("SELECT * FROM expenses ORDER BY date DESC")
    fun observeExpenses(): Flow<List<ExpenseEntity>>

    @Query("SELECT IFNULL(SUM(amount), 0) FROM expenses WHERE date BETWEEN :from AND :to")
    fun sumExpensesBetween(from: Long, to: Long): Flow<Double>

    @Query("SELECT category AS label, IFNULL(SUM(amount), 0) AS total FROM expenses GROUP BY category ORDER BY total DESC")
    fun expensesByCategory(): Flow<List<LabeledTotal>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpenseEntity): Long

    @Query("DELETE FROM expenses WHERE id = :id")
    suspend fun deleteExpense(id: Long)
}
