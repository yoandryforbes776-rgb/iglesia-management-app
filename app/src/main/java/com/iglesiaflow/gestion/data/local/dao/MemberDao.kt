package com.iglesiaflow.gestion.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.iglesiaflow.gestion.data.local.entity.CustomFieldDefEntity
import com.iglesiaflow.gestion.data.local.entity.CustomFieldValueEntity
import com.iglesiaflow.gestion.data.local.entity.FamilyEntity
import com.iglesiaflow.gestion.data.local.entity.MemberEntity
import com.iglesiaflow.gestion.data.local.entity.NoteEntity
import com.iglesiaflow.gestion.domain.model.CustomFieldEntity
import kotlinx.coroutines.flow.Flow

data class StatusCount(val status: String, val total: Int)

@Dao
interface MemberDao {

    @Query("SELECT * FROM members ORDER BY lastName COLLATE NOCASE, firstName COLLATE NOCASE")
    fun observeMembers(): Flow<List<MemberEntity>>

    @Query(
        """
        SELECT * FROM members
        WHERE (:query = '' OR firstName LIKE '%' || :query || '%' OR lastName LIKE '%' || :query || '%'
               OR email LIKE '%' || :query || '%' OR phone LIKE '%' || :query || '%')
        ORDER BY lastName COLLATE NOCASE, firstName COLLATE NOCASE
        """
    )
    fun searchMembers(query: String): Flow<List<MemberEntity>>

    @Query("SELECT * FROM members WHERE id = :id")
    fun observeMember(id: Long): Flow<MemberEntity?>

    @Query("SELECT * FROM members WHERE id = :id")
    suspend fun getMember(id: Long): MemberEntity?

    @Query("SELECT * FROM members WHERE familyId = :familyId ORDER BY familyRole")
    fun observeFamilyMembers(familyId: Long): Flow<List<MemberEntity>>

    @Query("SELECT * FROM members WHERE isChild = 1 ORDER BY firstName")
    fun observeChildren(): Flow<List<MemberEntity>>

    @Query("SELECT COUNT(*) FROM members")
    fun countMembers(): Flow<Int>

    @Query("SELECT COUNT(*) FROM members WHERE status = 'ACTIVO'")
    fun countActiveMembers(): Flow<Int>

    @Query("SELECT COUNT(*) FROM members WHERE joinedAt >= :since")
    fun countNewMembersSince(since: Long): Flow<Int>

    @Query("SELECT status AS status, COUNT(*) AS total FROM members GROUP BY status")
    fun countByStatus(): Flow<List<StatusCount>>

    @Query(
        """
        SELECT * FROM members
        WHERE birthDate IS NOT NULL
          AND CAST(strftime('%m', birthDate / 1000, 'unixepoch') AS INTEGER) = :month
        ORDER BY CAST(strftime('%d', birthDate / 1000, 'unixepoch') AS INTEGER)
        """
    )
    fun observeBirthdaysInMonth(month: Int): Flow<List<MemberEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMember(member: MemberEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMembers(members: List<MemberEntity>): List<Long>

    @Update
    suspend fun updateMember(member: MemberEntity)

    @Delete
    suspend fun deleteMember(member: MemberEntity)

    @Query("DELETE FROM members WHERE id = :id")
    suspend fun deleteMemberById(id: Long)

    // ---- Familias ----
    @Query("SELECT * FROM families ORDER BY name COLLATE NOCASE")
    fun observeFamilies(): Flow<List<FamilyEntity>>

    @Query("SELECT * FROM families WHERE id = :id")
    suspend fun getFamily(id: Long): FamilyEntity?

    @Query("SELECT COUNT(*) FROM families")
    fun countFamilies(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFamily(family: FamilyEntity): Long

    @Update
    suspend fun updateFamily(family: FamilyEntity)

    @Query("DELETE FROM families WHERE id = :id")
    suspend fun deleteFamilyById(id: Long)

    @Query("UPDATE members SET familyId = NULL WHERE familyId = :familyId")
    suspend fun detachMembersFromFamily(familyId: Long)

    // ---- Campos personalizados ----
    @Query("SELECT * FROM custom_field_defs WHERE entityType = :entityType AND active = 1 ORDER BY position")
    fun observeCustomFields(entityType: CustomFieldEntity): Flow<List<CustomFieldDefEntity>>

    @Query("SELECT * FROM custom_field_defs ORDER BY entityType, position")
    fun observeAllCustomFields(): Flow<List<CustomFieldDefEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomField(def: CustomFieldDefEntity): Long

    @Update
    suspend fun updateCustomField(def: CustomFieldDefEntity)

    @Query("DELETE FROM custom_field_defs WHERE id = :id")
    suspend fun deleteCustomField(id: Long)

    @Query("SELECT * FROM custom_field_values WHERE entityType = :entityType AND entityId = :entityId")
    fun observeCustomValues(entityType: CustomFieldEntity, entityId: Long): Flow<List<CustomFieldValueEntity>>

    @Query("DELETE FROM custom_field_values WHERE fieldId = :fieldId AND entityId = :entityId")
    suspend fun deleteCustomValue(fieldId: Long, entityId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomValue(value: CustomFieldValueEntity): Long

    // ---- Notas / timeline ----
    @Query("SELECT * FROM notes WHERE entityType = :entityType AND entityId = :entityId ORDER BY createdAt DESC")
    fun observeNotes(entityType: CustomFieldEntity, entityId: Long): Flow<List<NoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteEntity): Long

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun deleteNote(id: Long)

    @Query("SELECT * FROM members")
    suspend fun allMembersOnce(): List<MemberEntity>

    @Query("SELECT * FROM families")
    suspend fun allFamiliesOnce(): List<FamilyEntity>
}
