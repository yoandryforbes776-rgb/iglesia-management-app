package com.iglesiaflow.gestion.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.iglesiaflow.gestion.domain.model.DonationMethod
import com.iglesiaflow.gestion.domain.model.DonationType
import com.iglesiaflow.gestion.domain.model.PledgeFrequency

@Entity(tableName = "funds")
data class FundEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String = "",
    val active: Boolean = true,
    val remoteId: String? = null,
    val pendingSync: Boolean = true,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "donations",
    indices = [Index("memberId"), Index("fundId"), Index("date"), Index("depositId")]
)
data class DonationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val memberId: Long? = null,
    val familyId: Long? = null,
    val anonymous: Boolean = false,
    val amount: Double,
    val type: DonationType = DonationType.OFRENDA,
    val method: DonationMethod = DonationMethod.EFECTIVO,
    val fundId: Long? = null,
    val envelopeNumber: Int? = null,
    val depositId: Long? = null,
    val date: Long = System.currentTimeMillis(),
    val reference: String = "",
    val note: String = "",
    val createdBy: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val remoteId: String? = null,
    val pendingSync: Boolean = true
)

/** Promesas de fe: compromisos plurianuales con seguimiento. */
@Entity(tableName = "pledges", indices = [Index("memberId")])
data class PledgeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val memberId: Long,
    val fundId: Long? = null,
    val campaign: String = "",
    val totalAmount: Double,
    val frequency: PledgeFrequency = PledgeFrequency.MENSUAL,
    val startDate: Long = System.currentTimeMillis(),
    val endDate: Long? = null,
    val note: String = ""
)

@Entity(tableName = "envelopes", indices = [Index(value = ["number", "year"], unique = true)])
data class EnvelopeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val number: Int,
    val memberId: Long? = null,
    val year: Int,
    val active: Boolean = true
)

@Entity(tableName = "deposits", indices = [Index("date")])
data class DepositEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val date: Long = System.currentTimeMillis(),
    val bankAccount: String = "",
    val closed: Boolean = false,
    val note: String = ""
)

@Entity(tableName = "expenses", indices = [Index("date")])
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val category: String,
    val amount: Double,
    val date: Long = System.currentTimeMillis(),
    val description: String = "",
    val fundId: Long? = null,
    val supplier: String = "",
    val createdBy: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
