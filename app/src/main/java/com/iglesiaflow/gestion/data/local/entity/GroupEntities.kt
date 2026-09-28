package com.iglesiaflow.gestion.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.iglesiaflow.gestion.domain.model.GroupRole
import com.iglesiaflow.gestion.domain.model.GroupType

@Entity(tableName = "church_groups")
data class GroupEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: GroupType = GroupType.CELULA,
    val description: String = "",
    val leaderId: Long? = null,
    val meetingDay: String = "",
    val meetingTime: String = "",
    val location: String = "",
    val active: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val remoteId: String? = null,
    val pendingSync: Boolean = true
)

@Entity(
    tableName = "group_members",
    indices = [Index("groupId"), Index("memberId"), Index(value = ["groupId", "memberId"], unique = true)]
)
data class GroupMemberEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val groupId: Long,
    val memberId: Long,
    val role: GroupRole = GroupRole.MIEMBRO,
    val joinedAt: Long = System.currentTimeMillis(),
    val remoteId: String? = null,
    val pendingSync: Boolean = true,
    val updatedAt: Long = System.currentTimeMillis()
)

/** Muro/chat interno de cada grupo. */
@Entity(tableName = "group_messages", indices = [Index("groupId")])
data class GroupMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val groupId: Long,
    val authorId: Long? = null,
    val authorName: String,
    val content: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val remoteId: String? = null,
    val pendingSync: Boolean = true
)
