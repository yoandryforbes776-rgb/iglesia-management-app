package com.iglesiaflow.gestion.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.iglesiaflow.gestion.data.local.entity.MinistryNeedEntity
import com.iglesiaflow.gestion.data.local.entity.ServiceRecordEntity
import com.iglesiaflow.gestion.data.local.entity.VolunteerSkillEntity
import kotlinx.coroutines.flow.Flow

data class VolunteerHours(val memberId: Long, val firstName: String, val lastName: String, val hours: Double)

@Dao
interface VolunteerDao {

    @Query("SELECT * FROM volunteer_skills WHERE active = 1 ORDER BY skill")
    fun observeSkills(): Flow<List<VolunteerSkillEntity>>

    @Query("SELECT * FROM volunteer_skills WHERE memberId = :memberId")
    fun observeSkillsForMember(memberId: Long): Flow<List<VolunteerSkillEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSkill(skill: VolunteerSkillEntity): Long

    @Update
    suspend fun updateSkill(skill: VolunteerSkillEntity)

    @Query("DELETE FROM volunteer_skills WHERE id = :id")
    suspend fun deleteSkill(id: Long)

    @Query("SELECT * FROM ministry_needs WHERE active = 1 ORDER BY ministry")
    fun observeNeeds(): Flow<List<MinistryNeedEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNeed(need: MinistryNeedEntity): Long

    @Query("DELETE FROM ministry_needs WHERE id = :id")
    suspend fun deleteNeed(id: Long)

    @Query("SELECT * FROM service_records ORDER BY date DESC")
    fun observeServiceRecords(): Flow<List<ServiceRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertServiceRecord(record: ServiceRecordEntity): Long

    @Query("DELETE FROM service_records WHERE id = :id")
    suspend fun deleteServiceRecord(id: Long)

    @Query(
        """
        SELECT m.id AS memberId, m.firstName AS firstName, m.lastName AS lastName,
               IFNULL(SUM(s.hours), 0) AS hours
        FROM service_records s INNER JOIN members m ON m.id = s.memberId
        WHERE s.date >= :since
        GROUP BY m.id ORDER BY hours DESC LIMIT :limit
        """
    )
    fun topVolunteers(since: Long, limit: Int): Flow<List<VolunteerHours>>

    @Query("SELECT IFNULL(SUM(hours), 0) FROM service_records WHERE date >= :since")
    fun totalHoursSince(since: Long): Flow<Double>

    @Query("SELECT COUNT(DISTINCT memberId) FROM volunteer_skills WHERE active = 1")
    fun countVolunteers(): Flow<Int>
}
