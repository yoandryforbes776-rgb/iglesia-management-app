package com.iglesiaflow.gestion.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.iglesiaflow.gestion.data.local.entity.GroupEntity
import com.iglesiaflow.gestion.data.local.entity.GroupMemberEntity
import com.iglesiaflow.gestion.data.local.entity.GroupMessageEntity
import com.iglesiaflow.gestion.domain.model.GroupRole
import kotlinx.coroutines.flow.Flow

data class GroupMemberRow(
    val membershipId: Long,
    val memberId: Long,
    val role: GroupRole,
    val firstName: String,
    val lastName: String,
    val phone: String,
    val email: String
)

data class GroupSummaryRow(val groupId: Long, val total: Int)

@Dao
interface GroupDao {

    @Query("SELECT * FROM church_groups ORDER BY name COLLATE NOCASE")
    fun observeGroups(): Flow<List<GroupEntity>>

    @Query("SELECT * FROM church_groups WHERE id = :id")
    fun observeGroup(id: Long): Flow<GroupEntity?>

    @Query("SELECT COUNT(*) FROM church_groups WHERE active = 1")
    fun countActiveGroups(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroup(group: GroupEntity): Long

    @Update
    suspend fun updateGroup(group: GroupEntity)

    @Query("DELETE FROM church_groups WHERE id = :id")
    suspend fun deleteGroup(id: Long)

    @Query("SELECT * FROM church_groups")
    suspend fun allGroupsOnce(): List<GroupEntity>

    @Query(
        """
        SELECT gm.id AS membershipId, gm.memberId AS memberId, gm.role AS role,
               m.firstName AS firstName, m.lastName AS lastName, m.phone AS phone, m.email AS email
        FROM group_members gm INNER JOIN members m ON m.id = gm.memberId
        WHERE gm.groupId = :groupId
        ORDER BY gm.role, m.lastName COLLATE NOCASE
        """
    )
    fun observeGroupMembers(groupId: Long): Flow<List<GroupMemberRow>>

    @Query("SELECT groupId AS groupId, COUNT(*) AS total FROM group_members GROUP BY groupId")
    fun observeGroupSizes(): Flow<List<GroupSummaryRow>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroupMember(member: GroupMemberEntity): Long

    @Query("DELETE FROM group_members WHERE id = :id")
    suspend fun deleteGroupMember(id: Long)

    @Query("DELETE FROM group_members WHERE groupId = :groupId")
    suspend fun deleteMembersOfGroup(groupId: Long)

    @Query("SELECT * FROM group_messages WHERE groupId = :groupId ORDER BY createdAt ASC")
    fun observeGroupMessages(groupId: Long): Flow<List<GroupMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroupMessage(message: GroupMessageEntity): Long

    @Query("DELETE FROM group_messages WHERE id = :id")
    suspend fun deleteGroupMessage(id: Long)
}
