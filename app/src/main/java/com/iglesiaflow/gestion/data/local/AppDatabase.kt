package com.iglesiaflow.gestion.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.iglesiaflow.gestion.data.local.dao.AdminDao
import com.iglesiaflow.gestion.data.local.dao.SyncDao
import com.iglesiaflow.gestion.data.local.dao.CommunicationDao
import com.iglesiaflow.gestion.data.local.dao.EventDao
import com.iglesiaflow.gestion.data.local.dao.FinanceDao
import com.iglesiaflow.gestion.data.local.dao.GroupDao
import com.iglesiaflow.gestion.data.local.dao.MemberDao
import com.iglesiaflow.gestion.data.local.dao.VolunteerDao
import com.iglesiaflow.gestion.data.local.entity.AttendanceEntity
import com.iglesiaflow.gestion.data.local.entity.AuditLogEntity
import com.iglesiaflow.gestion.data.local.entity.SyncStateEntity
import com.iglesiaflow.gestion.data.local.entity.TombstoneEntity
import com.iglesiaflow.gestion.data.local.entity.CampaignEntity
import com.iglesiaflow.gestion.data.local.entity.CheckInEntity
import com.iglesiaflow.gestion.data.local.entity.CustomFieldDefEntity
import com.iglesiaflow.gestion.data.local.entity.CustomFieldValueEntity
import com.iglesiaflow.gestion.data.local.entity.DepositEntity
import com.iglesiaflow.gestion.data.local.entity.DonationEntity
import com.iglesiaflow.gestion.data.local.entity.EnvelopeEntity
import com.iglesiaflow.gestion.data.local.entity.EventEntity
import com.iglesiaflow.gestion.data.local.entity.ExpenseEntity
import com.iglesiaflow.gestion.data.local.entity.FamilyEntity
import com.iglesiaflow.gestion.data.local.entity.FundEntity
import com.iglesiaflow.gestion.data.local.entity.GroupEntity
import com.iglesiaflow.gestion.data.local.entity.GroupMemberEntity
import com.iglesiaflow.gestion.data.local.entity.GroupMessageEntity
import com.iglesiaflow.gestion.data.local.entity.MemberEntity
import com.iglesiaflow.gestion.data.local.entity.MinistryNeedEntity
import com.iglesiaflow.gestion.data.local.entity.NoteEntity
import com.iglesiaflow.gestion.data.local.entity.PledgeEntity
import com.iglesiaflow.gestion.data.local.entity.PrayerRequestEntity
import com.iglesiaflow.gestion.data.local.entity.RolePermissionEntity
import com.iglesiaflow.gestion.data.local.entity.ServiceRecordEntity
import com.iglesiaflow.gestion.data.local.entity.UserEntity
import com.iglesiaflow.gestion.data.local.entity.VolunteerSkillEntity

@Database(
    entities = [
        MemberEntity::class,
        FamilyEntity::class,
        CustomFieldDefEntity::class,
        CustomFieldValueEntity::class,
        NoteEntity::class,
        FundEntity::class,
        DonationEntity::class,
        PledgeEntity::class,
        EnvelopeEntity::class,
        DepositEntity::class,
        ExpenseEntity::class,
        EventEntity::class,
        AttendanceEntity::class,
        CheckInEntity::class,
        GroupEntity::class,
        GroupMemberEntity::class,
        GroupMessageEntity::class,
        VolunteerSkillEntity::class,
        MinistryNeedEntity::class,
        ServiceRecordEntity::class,
        CampaignEntity::class,
        PrayerRequestEntity::class,
        UserEntity::class,
        RolePermissionEntity::class,
        AuditLogEntity::class,
        SyncStateEntity::class,
        TombstoneEntity::class
    ],
    version = 2,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun memberDao(): MemberDao
    abstract fun financeDao(): FinanceDao
    abstract fun eventDao(): EventDao
    abstract fun groupDao(): GroupDao
    abstract fun volunteerDao(): VolunteerDao
    abstract fun communicationDao(): CommunicationDao
    abstract fun adminDao(): AdminDao
    abstract fun syncDao(): SyncDao

    companion object {
        const val NAME = "iglesiaflow.db"
    }
}
