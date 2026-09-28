package com.iglesiaflow.gestion.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.iglesiaflow.gestion.data.local.entity.AttendanceEntity
import com.iglesiaflow.gestion.data.local.entity.DonationEntity
import com.iglesiaflow.gestion.data.local.entity.EventEntity
import com.iglesiaflow.gestion.data.local.entity.FamilyEntity
import com.iglesiaflow.gestion.data.local.entity.FundEntity
import com.iglesiaflow.gestion.data.local.entity.GroupEntity
import com.iglesiaflow.gestion.data.local.entity.GroupMemberEntity
import com.iglesiaflow.gestion.data.local.entity.GroupMessageEntity
import com.iglesiaflow.gestion.data.local.entity.MemberEntity
import com.iglesiaflow.gestion.data.local.entity.PrayerRequestEntity
import com.iglesiaflow.gestion.data.local.entity.SyncStateEntity
import com.iglesiaflow.gestion.data.local.entity.TombstoneEntity
import kotlinx.coroutines.flow.Flow

/**
 * Acceso transversal usado por la sincronización en tiempo real:
 * cola de salida (`pendingSync`), búsqueda por identificador global
 * (`remoteId`) y resolución de relaciones entre dispositivos.
 */
@Dao
interface SyncDao {

    // ---------- estado y lápidas ----------

    @Query("SELECT lastPulledAt FROM sync_state WHERE collection = :collection")
    suspend fun lastPulledAt(collection: String): Long?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveState(state: SyncStateEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addTombstone(tombstone: TombstoneEntity)

    @Query("SELECT * FROM sync_tombstones WHERE pendingSync = 1")
    suspend fun pendingTombstones(): List<TombstoneEntity>

    @Query("UPDATE sync_tombstones SET pendingSync = 0 WHERE id = :id")
    suspend fun markTombstoneSynced(id: Long)

    @Query("SELECT COUNT(*) FROM sync_tombstones WHERE pendingSync = 1")
    fun pendingTombstoneCount(): Flow<Int>

    // ---------- familias ----------

    @Query("SELECT * FROM families WHERE pendingSync = 1")
    suspend fun pendingFamilies(): List<FamilyEntity>

    @Query("SELECT * FROM families WHERE remoteId = :remoteId LIMIT 1")
    suspend fun familyByRemote(remoteId: String): FamilyEntity?

    @Query("SELECT id FROM families WHERE remoteId = :remoteId LIMIT 1")
    suspend fun familyIdByRemote(remoteId: String): Long?

    @Query("SELECT remoteId FROM families WHERE id = :id")
    suspend fun familyRemoteById(id: Long): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFamily(entity: FamilyEntity): Long

    @Update
    suspend fun updateFamily(entity: FamilyEntity)

    @Query("DELETE FROM families WHERE remoteId = :remoteId")
    suspend fun deleteFamilyByRemote(remoteId: String)

    // ---------- miembros ----------

    @Query("SELECT * FROM members WHERE pendingSync = 1")
    suspend fun pendingMembers(): List<MemberEntity>

    @Query("SELECT * FROM members WHERE remoteId = :remoteId LIMIT 1")
    suspend fun memberByRemote(remoteId: String): MemberEntity?

    @Query("SELECT id FROM members WHERE remoteId = :remoteId LIMIT 1")
    suspend fun memberIdByRemote(remoteId: String): Long?

    @Query("SELECT remoteId FROM members WHERE id = :id")
    suspend fun memberRemoteById(id: Long): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMember(entity: MemberEntity): Long

    @Update
    suspend fun updateMember(entity: MemberEntity)

    @Query("DELETE FROM members WHERE remoteId = :remoteId")
    suspend fun deleteMemberByRemote(remoteId: String)

    // ---------- fondos ----------

    @Query("SELECT * FROM funds WHERE pendingSync = 1")
    suspend fun pendingFunds(): List<FundEntity>

    @Query("SELECT * FROM funds WHERE remoteId = :remoteId LIMIT 1")
    suspend fun fundByRemote(remoteId: String): FundEntity?

    @Query("SELECT id FROM funds WHERE remoteId = :remoteId LIMIT 1")
    suspend fun fundIdByRemote(remoteId: String): Long?

    @Query("SELECT remoteId FROM funds WHERE id = :id")
    suspend fun fundRemoteById(id: Long): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFund(entity: FundEntity): Long

    @Update
    suspend fun updateFund(entity: FundEntity)

    @Query("DELETE FROM funds WHERE remoteId = :remoteId")
    suspend fun deleteFundByRemote(remoteId: String)

    // ---------- donaciones ----------

    @Query("SELECT * FROM donations WHERE pendingSync = 1")
    suspend fun pendingDonations(): List<DonationEntity>

    @Query("SELECT * FROM donations WHERE remoteId = :remoteId LIMIT 1")
    suspend fun donationByRemote(remoteId: String): DonationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDonation(entity: DonationEntity): Long

    @Update
    suspend fun updateDonation(entity: DonationEntity)

    @Query("SELECT remoteId FROM donations WHERE id = :id")
    suspend fun donationRemoteById(id: Long): String?

    @Query("DELETE FROM donations WHERE remoteId = :remoteId")
    suspend fun deleteDonationByRemote(remoteId: String)

    // ---------- eventos ----------

    @Query("SELECT * FROM events WHERE pendingSync = 1")
    suspend fun pendingEvents(): List<EventEntity>

    @Query("SELECT * FROM events WHERE remoteId = :remoteId LIMIT 1")
    suspend fun eventByRemote(remoteId: String): EventEntity?

    @Query("SELECT id FROM events WHERE remoteId = :remoteId LIMIT 1")
    suspend fun eventIdByRemote(remoteId: String): Long?

    @Query("SELECT remoteId FROM events WHERE id = :id")
    suspend fun eventRemoteById(id: Long): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(entity: EventEntity): Long

    @Update
    suspend fun updateEvent(entity: EventEntity)

    @Query("DELETE FROM events WHERE remoteId = :remoteId")
    suspend fun deleteEventByRemote(remoteId: String)

    // ---------- asistencia ----------

    @Query("SELECT * FROM attendance WHERE pendingSync = 1")
    suspend fun pendingAttendance(): List<AttendanceEntity>

    @Query("SELECT * FROM attendance WHERE remoteId = :remoteId LIMIT 1")
    suspend fun attendanceByRemote(remoteId: String): AttendanceEntity?

    @Query("SELECT * FROM attendance WHERE eventId = :eventId AND memberId = :memberId LIMIT 1")
    suspend fun attendanceByPair(eventId: Long, memberId: Long): AttendanceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendance(entity: AttendanceEntity): Long

    @Update
    suspend fun updateAttendance(entity: AttendanceEntity)

    @Query("DELETE FROM attendance WHERE remoteId = :remoteId")
    suspend fun deleteAttendanceByRemote(remoteId: String)

    // ---------- grupos ----------

    @Query("SELECT * FROM church_groups WHERE pendingSync = 1")
    suspend fun pendingGroups(): List<GroupEntity>

    @Query("SELECT * FROM church_groups WHERE remoteId = :remoteId LIMIT 1")
    suspend fun groupByRemote(remoteId: String): GroupEntity?

    @Query("SELECT id FROM church_groups WHERE remoteId = :remoteId LIMIT 1")
    suspend fun groupIdByRemote(remoteId: String): Long?

    @Query("SELECT remoteId FROM church_groups WHERE id = :id")
    suspend fun groupRemoteById(id: Long): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroup(entity: GroupEntity): Long

    @Update
    suspend fun updateGroup(entity: GroupEntity)

    @Query("DELETE FROM church_groups WHERE remoteId = :remoteId")
    suspend fun deleteGroupByRemote(remoteId: String)

    // ---------- integrantes de grupo ----------

    @Query("SELECT * FROM group_members WHERE pendingSync = 1")
    suspend fun pendingGroupMembers(): List<GroupMemberEntity>

    @Query("SELECT * FROM group_members WHERE remoteId = :remoteId LIMIT 1")
    suspend fun groupMemberByRemote(remoteId: String): GroupMemberEntity?

    @Query("SELECT * FROM group_members WHERE groupId = :groupId AND memberId = :memberId LIMIT 1")
    suspend fun groupMemberByPair(groupId: Long, memberId: Long): GroupMemberEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroupMember(entity: GroupMemberEntity): Long

    @Update
    suspend fun updateGroupMember(entity: GroupMemberEntity)

    @Query("SELECT remoteId FROM group_members WHERE id = :id")
    suspend fun groupMemberRemoteById(id: Long): String?

    @Query("DELETE FROM group_members WHERE remoteId = :remoteId")
    suspend fun deleteGroupMemberByRemote(remoteId: String)

    // ---------- mensajes de grupo ----------

    @Query("SELECT * FROM group_messages WHERE pendingSync = 1")
    suspend fun pendingGroupMessages(): List<GroupMessageEntity>

    @Query("SELECT * FROM group_messages WHERE remoteId = :remoteId LIMIT 1")
    suspend fun groupMessageByRemote(remoteId: String): GroupMessageEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroupMessage(entity: GroupMessageEntity): Long

    @Update
    suspend fun updateGroupMessage(entity: GroupMessageEntity)

    @Query("SELECT remoteId FROM group_messages WHERE id = :id")
    suspend fun groupMessageRemoteById(id: Long): String?

    @Query("DELETE FROM group_messages WHERE remoteId = :remoteId")
    suspend fun deleteGroupMessageByRemote(remoteId: String)

    // ---------- peticiones de oración ----------

    @Query("SELECT * FROM prayer_requests WHERE pendingSync = 1")
    suspend fun pendingPrayers(): List<PrayerRequestEntity>

    @Query("SELECT * FROM prayer_requests WHERE remoteId = :remoteId LIMIT 1")
    suspend fun prayerByRemote(remoteId: String): PrayerRequestEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrayer(entity: PrayerRequestEntity): Long

    @Update
    suspend fun updatePrayer(entity: PrayerRequestEntity)

    @Query("SELECT remoteId FROM prayer_requests WHERE id = :id")
    suspend fun prayerRemoteById(id: Long): String?

    @Query("DELETE FROM prayer_requests WHERE remoteId = :remoteId")
    suspend fun deletePrayerByRemote(remoteId: String)

    // ---------- contadores de cola ----------

    @Query(
        """
        SELECT (SELECT COUNT(*) FROM members WHERE pendingSync = 1) +
               (SELECT COUNT(*) FROM families WHERE pendingSync = 1) +
               (SELECT COUNT(*) FROM funds WHERE pendingSync = 1) +
               (SELECT COUNT(*) FROM donations WHERE pendingSync = 1) +
               (SELECT COUNT(*) FROM events WHERE pendingSync = 1) +
               (SELECT COUNT(*) FROM attendance WHERE pendingSync = 1) +
               (SELECT COUNT(*) FROM church_groups WHERE pendingSync = 1) +
               (SELECT COUNT(*) FROM group_members WHERE pendingSync = 1) +
               (SELECT COUNT(*) FROM group_messages WHERE pendingSync = 1) +
               (SELECT COUNT(*) FROM prayer_requests WHERE pendingSync = 1) +
               (SELECT COUNT(*) FROM sync_tombstones WHERE pendingSync = 1)
        """
    )
    fun pendingCount(): Flow<Int>
}
