package com.iglesiaflow.gestion.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.iglesiaflow.gestion.domain.model.CustomFieldEntity
import com.iglesiaflow.gestion.domain.model.CustomFieldType
import com.iglesiaflow.gestion.domain.model.FamilyRole
import com.iglesiaflow.gestion.domain.model.Gender
import com.iglesiaflow.gestion.domain.model.MaritalStatus
import com.iglesiaflow.gestion.domain.model.MemberStatus

@Entity(tableName = "families")
data class FamilyEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val address: String = "",
    val city: String = "",
    val phone: String = "",
    val email: String = "",
    val photoUri: String? = null,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val remoteId: String? = null,
    val pendingSync: Boolean = true
)

@Entity(
    tableName = "members",
    indices = [Index("familyId"), Index("lastName"), Index("status")]
)
data class MemberEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val firstName: String,
    val lastName: String,
    val email: String = "",
    val phone: String = "",
    val birthDate: Long? = null,
    val gender: Gender = Gender.NO_ESPECIFICA,
    val maritalStatus: MaritalStatus = MaritalStatus.SOLTERO,
    val status: MemberStatus = MemberStatus.ACTIVO,
    val churchRole: String = "",
    val familyId: Long? = null,
    val familyRole: FamilyRole = FamilyRole.OTRO,
    val photoUri: String? = null,
    val address: String = "",
    val city: String = "",
    val baptized: Boolean = false,
    val baptismDate: Long? = null,
    val joinedAt: Long = System.currentTimeMillis(),
    val propertiesCsv: String = "",
    val notes: String = "",
    val isChild: Boolean = false,
    val guardianPhone: String = "",
    val allowsContact: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val remoteId: String? = null,
    val pendingSync: Boolean = true
) {
    val fullName: String get() = listOf(firstName, lastName).filter { it.isNotBlank() }.joinToString(" ")
    val properties: List<String>
        get() = propertiesCsv.split(",").map { it.trim() }.filter { it.isNotBlank() }
}

/** Definición de campos personalizados creada por el administrador. */
@Entity(tableName = "custom_field_defs", indices = [Index("entityType")])
data class CustomFieldDefEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val entityType: CustomFieldEntity,
    val label: String,
    val fieldType: CustomFieldType = CustomFieldType.TEXTO,
    val optionsCsv: String = "",
    val required: Boolean = false,
    val position: Int = 0,
    val active: Boolean = true
) {
    val options: List<String>
        get() = optionsCsv.split(",").map { it.trim() }.filter { it.isNotBlank() }
}

@Entity(tableName = "custom_field_values", indices = [Index("fieldId"), Index(value = ["entityType", "entityId"])])
data class CustomFieldValueEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fieldId: Long,
    val entityType: CustomFieldEntity,
    val entityId: Long,
    val value: String = ""
)

/** Timeline de notas e interacciones (privadas o compartidas). */
@Entity(tableName = "notes", indices = [Index(value = ["entityType", "entityId"])])
data class NoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val entityType: CustomFieldEntity = CustomFieldEntity.MIEMBRO,
    val entityId: Long,
    val authorId: Long? = null,
    val authorName: String = "",
    val title: String = "",
    val content: String,
    val isPrivate: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
