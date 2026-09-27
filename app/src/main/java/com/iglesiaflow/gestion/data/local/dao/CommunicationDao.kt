package com.iglesiaflow.gestion.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.iglesiaflow.gestion.data.local.entity.CampaignEntity
import com.iglesiaflow.gestion.data.local.entity.PrayerRequestEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CommunicationDao {

    @Query("SELECT * FROM campaigns ORDER BY createdAt DESC")
    fun observeCampaigns(): Flow<List<CampaignEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCampaign(campaign: CampaignEntity): Long

    @Update
    suspend fun updateCampaign(campaign: CampaignEntity)

    @Query("SELECT * FROM campaigns WHERE id = :id")
    suspend fun getCampaign(id: Long): CampaignEntity?

    @Query("DELETE FROM campaigns WHERE id = :id")
    suspend fun deleteCampaign(id: Long)

    @Query("SELECT * FROM prayer_requests ORDER BY createdAt DESC")
    fun observePrayerRequests(): Flow<List<PrayerRequestEntity>>

    @Query("SELECT * FROM prayer_requests WHERE isPrivate = 0 ORDER BY createdAt DESC")
    fun observePublicPrayerRequests(): Flow<List<PrayerRequestEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrayerRequest(request: PrayerRequestEntity): Long

    @Update
    suspend fun updatePrayerRequest(request: PrayerRequestEntity)

    @Query("SELECT * FROM prayer_requests WHERE id = :id")
    suspend fun getPrayerRequest(id: Long): PrayerRequestEntity?

    @Query("DELETE FROM prayer_requests WHERE id = :id")
    suspend fun deletePrayerRequest(id: Long)

    @Query("SELECT COUNT(*) FROM prayer_requests WHERE status IN ('ABIERTA', 'EN_ORACION')")
    fun countOpenPrayers(): Flow<Int>
}
