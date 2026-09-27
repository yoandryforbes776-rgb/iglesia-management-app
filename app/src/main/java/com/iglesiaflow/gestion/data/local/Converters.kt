package com.iglesiaflow.gestion.data.local

import androidx.room.TypeConverter
import com.iglesiaflow.gestion.domain.model.CampaignChannel
import com.iglesiaflow.gestion.domain.model.CampaignStatus
import com.iglesiaflow.gestion.domain.model.CustomFieldEntity
import com.iglesiaflow.gestion.domain.model.CustomFieldType
import com.iglesiaflow.gestion.domain.model.DonationMethod
import com.iglesiaflow.gestion.domain.model.DonationType
import com.iglesiaflow.gestion.domain.model.EventType
import com.iglesiaflow.gestion.domain.model.FamilyRole
import com.iglesiaflow.gestion.domain.model.Gender
import com.iglesiaflow.gestion.domain.model.GroupRole
import com.iglesiaflow.gestion.domain.model.GroupType
import com.iglesiaflow.gestion.domain.model.MaritalStatus
import com.iglesiaflow.gestion.domain.model.MemberStatus
import com.iglesiaflow.gestion.domain.model.Permission
import com.iglesiaflow.gestion.domain.model.PledgeFrequency
import com.iglesiaflow.gestion.domain.model.PrayerStatus
import com.iglesiaflow.gestion.domain.model.SkillLevel
import com.iglesiaflow.gestion.domain.model.UserRole

/** Conversores de enums <-> texto para Room. */
class Converters {
    @TypeConverter fun genderToString(value: Gender): String = value.name
    @TypeConverter fun stringToGender(value: String): Gender = enumOrDefault(value, Gender.NO_ESPECIFICA)

    @TypeConverter fun maritalToString(value: MaritalStatus): String = value.name
    @TypeConverter fun stringToMarital(value: String): MaritalStatus = enumOrDefault(value, MaritalStatus.OTRO)

    @TypeConverter fun memberStatusToString(value: MemberStatus): String = value.name
    @TypeConverter fun stringToMemberStatus(value: String): MemberStatus = enumOrDefault(value, MemberStatus.ACTIVO)

    @TypeConverter fun familyRoleToString(value: FamilyRole): String = value.name
    @TypeConverter fun stringToFamilyRole(value: String): FamilyRole = enumOrDefault(value, FamilyRole.OTRO)

    @TypeConverter fun fieldTypeToString(value: CustomFieldType): String = value.name
    @TypeConverter fun stringToFieldType(value: String): CustomFieldType = enumOrDefault(value, CustomFieldType.TEXTO)

    @TypeConverter fun fieldEntityToString(value: CustomFieldEntity): String = value.name
    @TypeConverter fun stringToFieldEntity(value: String): CustomFieldEntity = enumOrDefault(value, CustomFieldEntity.MIEMBRO)

    @TypeConverter fun donationMethodToString(value: DonationMethod): String = value.name
    @TypeConverter fun stringToDonationMethod(value: String): DonationMethod = enumOrDefault(value, DonationMethod.EFECTIVO)

    @TypeConverter fun donationTypeToString(value: DonationType): String = value.name
    @TypeConverter fun stringToDonationType(value: String): DonationType = enumOrDefault(value, DonationType.OFRENDA)

    @TypeConverter fun pledgeFrequencyToString(value: PledgeFrequency): String = value.name
    @TypeConverter fun stringToPledgeFrequency(value: String): PledgeFrequency = enumOrDefault(value, PledgeFrequency.MENSUAL)

    @TypeConverter fun eventTypeToString(value: EventType): String = value.name
    @TypeConverter fun stringToEventType(value: String): EventType = enumOrDefault(value, EventType.CULTO)

    @TypeConverter fun groupTypeToString(value: GroupType): String = value.name
    @TypeConverter fun stringToGroupType(value: String): GroupType = enumOrDefault(value, GroupType.CELULA)

    @TypeConverter fun groupRoleToString(value: GroupRole): String = value.name
    @TypeConverter fun stringToGroupRole(value: String): GroupRole = enumOrDefault(value, GroupRole.MIEMBRO)

    @TypeConverter fun skillLevelToString(value: SkillLevel): String = value.name
    @TypeConverter fun stringToSkillLevel(value: String): SkillLevel = enumOrDefault(value, SkillLevel.BASICO)

    @TypeConverter fun channelToString(value: CampaignChannel): String = value.name
    @TypeConverter fun stringToChannel(value: String): CampaignChannel = enumOrDefault(value, CampaignChannel.EMAIL)

    @TypeConverter fun campaignStatusToString(value: CampaignStatus): String = value.name
    @TypeConverter fun stringToCampaignStatus(value: String): CampaignStatus = enumOrDefault(value, CampaignStatus.BORRADOR)

    @TypeConverter fun prayerStatusToString(value: PrayerStatus): String = value.name
    @TypeConverter fun stringToPrayerStatus(value: String): PrayerStatus = enumOrDefault(value, PrayerStatus.ABIERTA)

    @TypeConverter fun roleToString(value: UserRole): String = value.name
    @TypeConverter fun stringToRole(value: String): UserRole = enumOrDefault(value, UserRole.MIEMBRO)

    @TypeConverter fun permissionToString(value: Permission): String = value.name
    @TypeConverter fun stringToPermission(value: String): Permission = enumOrDefault(value, Permission.MEMBERS_VIEW)

    private inline fun <reified T : Enum<T>> enumOrDefault(value: String, fallback: T): T =
        runCatching { enumValueOf<T>(value) }.getOrDefault(fallback)
}
