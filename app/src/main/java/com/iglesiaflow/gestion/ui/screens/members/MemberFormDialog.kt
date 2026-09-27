package com.iglesiaflow.gestion.ui.screens.members

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.KeyboardType
import com.iglesiaflow.gestion.data.local.entity.CustomFieldDefEntity
import com.iglesiaflow.gestion.data.local.entity.FamilyEntity
import com.iglesiaflow.gestion.data.local.entity.MemberEntity
import com.iglesiaflow.gestion.domain.model.CustomFieldType
import com.iglesiaflow.gestion.domain.model.FamilyRole
import com.iglesiaflow.gestion.domain.model.Gender
import com.iglesiaflow.gestion.domain.model.MaritalStatus
import com.iglesiaflow.gestion.domain.model.MemberStatus
import com.iglesiaflow.gestion.ui.components.DateField
import com.iglesiaflow.gestion.ui.components.DropdownField
import com.iglesiaflow.gestion.ui.components.FormDialog
import com.iglesiaflow.gestion.ui.components.FormTextField
import com.iglesiaflow.gestion.ui.components.SwitchRow

/** Formulario de alta/edición de miembro, incluidos los campos personalizados. */
@Composable
fun MemberFormDialog(
    member: MemberEntity?,
    families: List<FamilyEntity>,
    customFields: List<CustomFieldDefEntity>,
    initialCustomValues: Map<Long, String> = emptyMap(),
    onDismiss: () -> Unit,
    onSave: (MemberEntity, Map<Long, String>) -> Unit
) {
    var firstName by remember { mutableStateOf(member?.firstName.orEmpty()) }
    var lastName by remember { mutableStateOf(member?.lastName.orEmpty()) }
    var email by remember { mutableStateOf(member?.email.orEmpty()) }
    var phone by remember { mutableStateOf(member?.phone.orEmpty()) }
    var birthDate by remember { mutableStateOf(member?.birthDate) }
    var gender by remember { mutableStateOf(member?.gender ?: Gender.NO_ESPECIFICA) }
    var marital by remember { mutableStateOf(member?.maritalStatus ?: MaritalStatus.SOLTERO) }
    var status by remember { mutableStateOf(member?.status ?: MemberStatus.ACTIVO) }
    var churchRole by remember { mutableStateOf(member?.churchRole.orEmpty()) }
    var family by remember { mutableStateOf(families.firstOrNull { it.id == member?.familyId }) }
    var familyRole by remember { mutableStateOf(member?.familyRole ?: FamilyRole.OTRO) }
    var address by remember { mutableStateOf(member?.address.orEmpty()) }
    var city by remember { mutableStateOf(member?.city.orEmpty()) }
    var properties by remember { mutableStateOf(member?.propertiesCsv.orEmpty()) }
    var notes by remember { mutableStateOf(member?.notes.orEmpty()) }
    var isChild by remember { mutableStateOf(member?.isChild ?: false) }
    var guardianPhone by remember { mutableStateOf(member?.guardianPhone.orEmpty()) }
    var baptized by remember { mutableStateOf(member?.baptized ?: false) }
    var allowsContact by remember { mutableStateOf(member?.allowsContact ?: true) }
    val customValues = remember { mutableStateMapOf(initialCustomValues) }

    FormDialog(
        title = if (member == null) "Nuevo miembro" else "Editar miembro",
        onDismiss = onDismiss,
        confirmEnabled = firstName.isNotBlank() || lastName.isNotBlank(),
        onConfirm = {
            val updated = (member ?: MemberEntity(firstName = "", lastName = "")).copy(
                firstName = firstName.trim(),
                lastName = lastName.trim(),
                email = email.trim(),
                phone = phone.trim(),
                birthDate = birthDate,
                gender = gender,
                maritalStatus = marital,
                status = status,
                churchRole = churchRole.trim(),
                familyId = family?.id,
                familyRole = familyRole,
                address = address.trim(),
                city = city.trim(),
                propertiesCsv = properties.trim(),
                notes = notes.trim(),
                isChild = isChild,
                guardianPhone = guardianPhone.trim(),
                baptized = baptized,
                allowsContact = allowsContact
            )
            onSave(updated, customValues.toMap())
        }
    ) {
        FormTextField("Nombre", firstName, { firstName = it })
        FormTextField("Apellidos", lastName, { lastName = it })
        FormTextField("Correo", email, { email = it }, keyboardType = KeyboardType.Email)
        FormTextField("Teléfono", phone, { phone = it }, keyboardType = KeyboardType.Phone)
        DateField("Fecha de nacimiento", birthDate, { birthDate = it })
        DropdownField("Género", Gender.entries, gender, { it.label }, { gender = it })
        DropdownField("Estado civil", MaritalStatus.entries, marital, { it.label }, { marital = it })
        DropdownField("Estado", MemberStatus.entries, status, { it.label }, { status = it })
        FormTextField("Rol en la iglesia", churchRole, { churchRole = it })
        DropdownField(
            label = "Familia",
            options = families,
            selected = family,
            optionLabel = { it.name },
            onSelected = { family = it }
        )
        DropdownField("Rol en la familia", FamilyRole.entries, familyRole, { it.label }, { familyRole = it })
        FormTextField("Dirección", address, { address = it })
        FormTextField("Ciudad", city, { city = it })
        FormTextField(
            "Propiedades (separadas por coma)", properties, { properties = it },
            supportingText = "Ej.: Miembro de coro, Voluntario"
        )
        FormTextField("Notas", notes, { notes = it }, singleLine = false)
        SwitchRow("Bautizado", null, baptized) { baptized = it }
        SwitchRow("Es menor (ministerio infantil)", null, isChild) { isChild = it }
        if (isChild) {
            FormTextField("Teléfono del tutor", guardianPhone, { guardianPhone = it }, keyboardType = KeyboardType.Phone)
        }
        SwitchRow("Autoriza recibir comunicaciones", "Consentimiento RGPD", allowsContact) { allowsContact = it }

        customFields.forEach { field ->
            val value = customValues[field.id].orEmpty()
            when (field.fieldType) {
                CustomFieldType.BOOLEANO -> SwitchRow(
                    title = field.label,
                    checked = value == "true",
                    onCheckedChange = { customValues[field.id] = it.toString() }
                )
                CustomFieldType.SELECCION -> DropdownField(
                    label = field.label,
                    options = field.options,
                    selected = field.options.firstOrNull { it == value },
                    optionLabel = { it },
                    onSelected = { customValues[field.id] = it }
                )
                CustomFieldType.FECHA -> DateField(
                    label = field.label,
                    millis = value.toLongOrNull(),
                    onDateSelected = { customValues[field.id] = it.toString() }
                )
                CustomFieldType.NUMERO -> FormTextField(
                    field.label, value, { customValues[field.id] = it }, keyboardType = KeyboardType.Number
                )
                CustomFieldType.TEXTO -> FormTextField(field.label, value, { customValues[field.id] = it })
            }
        }
    }
}

private fun <K, V> mutableStateMapOf(initial: Map<K, V>): androidx.compose.runtime.snapshots.SnapshotStateMap<K, V> =
    androidx.compose.runtime.mutableStateMapOf<K, V>().apply { putAll(initial) }
