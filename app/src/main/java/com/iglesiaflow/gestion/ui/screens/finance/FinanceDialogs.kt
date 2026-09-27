package com.iglesiaflow.gestion.ui.screens.finance

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.iglesiaflow.gestion.core.util.DateTimeUtils
import com.iglesiaflow.gestion.core.util.Formatters
import com.iglesiaflow.gestion.data.local.entity.DepositEntity
import com.iglesiaflow.gestion.data.local.entity.DonationEntity
import com.iglesiaflow.gestion.data.local.entity.EnvelopeEntity
import com.iglesiaflow.gestion.data.local.entity.ExpenseEntity
import com.iglesiaflow.gestion.data.local.entity.FundEntity
import com.iglesiaflow.gestion.data.local.entity.MemberEntity
import com.iglesiaflow.gestion.data.local.entity.PledgeEntity
import com.iglesiaflow.gestion.domain.model.DonationMethod
import com.iglesiaflow.gestion.domain.model.DonationType
import com.iglesiaflow.gestion.domain.model.PledgeFrequency
import com.iglesiaflow.gestion.ui.components.DateField
import com.iglesiaflow.gestion.ui.components.DropdownField
import com.iglesiaflow.gestion.ui.components.FormDialog
import com.iglesiaflow.gestion.ui.components.FormTextField
import com.iglesiaflow.gestion.ui.components.SwitchRow

@Composable
fun DonationFormDialog(
    state: FinanceUiState,
    onDismiss: () -> Unit,
    onSave: (DonationEntity) -> Unit
) {
    var amount by remember { mutableStateOf("") }
    var anonymous by remember { mutableStateOf(false) }
    var member by remember { mutableStateOf<MemberEntity?>(null) }
    var fund by remember { mutableStateOf<FundEntity?>(state.funds.firstOrNull()) }
    var type by remember { mutableStateOf(DonationType.OFRENDA) }
    var method by remember { mutableStateOf(DonationMethod.EFECTIVO) }
    var date by remember { mutableStateOf(System.currentTimeMillis()) }
    var envelope by remember { mutableStateOf("") }
    var reference by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

    FormDialog(
        title = "Registrar donación",
        onDismiss = onDismiss,
        confirmEnabled = amount.toDoubleOrNull() != null,
        onConfirm = {
            onSave(
                DonationEntity(
                    memberId = if (anonymous) null else member?.id,
                    familyId = if (anonymous) null else member?.familyId,
                    anonymous = anonymous,
                    amount = amount.replace(',', '.').toDoubleOrNull() ?: 0.0,
                    type = type,
                    method = method,
                    fundId = fund?.id,
                    envelopeNumber = envelope.toIntOrNull(),
                    date = date,
                    reference = reference.trim(),
                    note = note.trim()
                )
            )
        }
    ) {
        FormTextField("Importe", amount, { amount = it }, keyboardType = KeyboardType.Decimal)
        SwitchRow("Donación anónima", null, anonymous) { anonymous = it }
        if (!anonymous) {
            DropdownField("Miembro", state.members, member, { it.fullName }, { member = it })
        }
        DropdownField("Tipo", DonationType.entries, type, { it.label }, { type = it })
        DropdownField("Método", DonationMethod.entries, method, { it.label }, { method = it })
        DropdownField("Fondo", state.funds, fund, { it.name }, { fund = it })
        DateField("Fecha", date, { date = it })
        FormTextField("Nº de sobre", envelope, { envelope = it }, keyboardType = KeyboardType.Number)
        FormTextField("Referencia", reference, { reference = it })
        FormTextField("Nota", note, { note = it }, singleLine = false)
    }
}

@Composable
fun PledgeFormDialog(
    state: FinanceUiState,
    onDismiss: () -> Unit,
    onSave: (PledgeEntity) -> Unit
) {
    var member by remember { mutableStateOf<MemberEntity?>(state.members.firstOrNull()) }
    var fund by remember { mutableStateOf<FundEntity?>(state.funds.firstOrNull()) }
    var campaign by remember { mutableStateOf("") }
    var total by remember { mutableStateOf("") }
    var frequency by remember { mutableStateOf(PledgeFrequency.MENSUAL) }
    var start by remember { mutableStateOf(System.currentTimeMillis()) }
    var end by remember { mutableStateOf<Long?>(null) }

    FormDialog(
        title = "Nueva promesa de fe",
        onDismiss = onDismiss,
        confirmEnabled = member != null && total.toDoubleOrNull() != null,
        onConfirm = {
            onSave(
                PledgeEntity(
                    memberId = member?.id ?: 0L,
                    fundId = fund?.id,
                    campaign = campaign.trim(),
                    totalAmount = total.replace(',', '.').toDoubleOrNull() ?: 0.0,
                    frequency = frequency,
                    startDate = start,
                    endDate = end
                )
            )
        }
    ) {
        DropdownField("Miembro", state.members, member, { it.fullName }, { member = it })
        DropdownField("Fondo", state.funds, fund, { it.name }, { fund = it })
        FormTextField("Campaña", campaign, { campaign = it })
        FormTextField("Importe comprometido", total, { total = it }, keyboardType = KeyboardType.Decimal)
        DropdownField("Frecuencia", PledgeFrequency.entries, frequency, { it.label }, { frequency = it })
        DateField("Inicio", start, { start = it })
        DateField("Fin (opcional)", end, { end = it })
    }
}

@Composable
fun ExpenseFormDialog(
    state: FinanceUiState,
    onDismiss: () -> Unit,
    onSave: (ExpenseEntity) -> Unit
) {
    var category by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var supplier by remember { mutableStateOf("") }
    var fund by remember { mutableStateOf<FundEntity?>(null) }
    var date by remember { mutableStateOf(System.currentTimeMillis()) }

    FormDialog(
        title = "Registrar gasto",
        onDismiss = onDismiss,
        confirmEnabled = category.isNotBlank() && amount.toDoubleOrNull() != null,
        onConfirm = {
            onSave(
                ExpenseEntity(
                    category = category.trim(),
                    amount = amount.replace(',', '.').toDoubleOrNull() ?: 0.0,
                    date = date,
                    description = description.trim(),
                    fundId = fund?.id,
                    supplier = supplier.trim()
                )
            )
        }
    ) {
        FormTextField("Categoría", category, { category = it })
        FormTextField("Importe", amount, { amount = it }, keyboardType = KeyboardType.Decimal)
        FormTextField("Descripción", description, { description = it }, singleLine = false)
        FormTextField("Proveedor", supplier, { supplier = it })
        DropdownField("Fondo", state.funds, fund, { it.name }, { fund = it })
        DateField("Fecha", date, { date = it })
    }
}

@Composable
fun DepositFormDialog(
    state: FinanceUiState,
    onDismiss: () -> Unit,
    onSave: (DepositEntity, List<Long>) -> Unit
) {
    var name by remember { mutableStateOf("Depósito ${DateTimeUtils.formatDate(System.currentTimeMillis())}") }
    var bank by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(System.currentTimeMillis()) }
    val selected = remember { mutableStateListOf<Long>() }
    val pending = state.donations.filter { it.depositId == null }

    FormDialog(
        title = "Nuevo depósito bancario",
        onDismiss = onDismiss,
        confirmEnabled = name.isNotBlank(),
        onConfirm = {
            onSave(
                DepositEntity(name = name.trim(), date = date, bankAccount = bank.trim()),
                selected.toList()
            )
        }
    ) {
        FormTextField("Nombre", name, { name = it })
        FormTextField("Cuenta bancaria", bank, { bank = it })
        DateField("Fecha", date, { date = it })
        Text(
            "Donaciones sin depositar (${pending.size})",
            style = MaterialTheme.typography.titleSmall
        )
        Column(
            Modifier
                .fillMaxWidth()
                .heightIn(max = 220.dp)
                .verticalScroll(rememberScrollState())
        ) {
            pending.forEach { donation ->
                SwitchRow(
                    title = "${Formatters.money(donation.amount, state.settings.currencyCode)} · " +
                        if (donation.anonymous) "Anónimo" else state.memberName(donation.memberId),
                    subtitle = DateTimeUtils.formatDate(donation.date),
                    checked = selected.contains(donation.id),
                    onCheckedChange = { checked ->
                        if (checked) selected.add(donation.id) else selected.remove(donation.id)
                    }
                )
            }
        }
    }
}

@Composable
fun EnvelopeFormDialog(
    state: FinanceUiState,
    onDismiss: () -> Unit,
    onSave: (EnvelopeEntity) -> Unit
) {
    var number by remember { mutableStateOf("") }
    var member by remember { mutableStateOf<MemberEntity?>(null) }
    var year by remember { mutableStateOf(DateTimeUtils.currentYear().toString()) }

    FormDialog(
        title = "Asignar sobre",
        onDismiss = onDismiss,
        confirmEnabled = number.toIntOrNull() != null,
        onConfirm = {
            onSave(
                EnvelopeEntity(
                    number = number.toIntOrNull() ?: 0,
                    memberId = member?.id,
                    year = year.toIntOrNull() ?: DateTimeUtils.currentYear()
                )
            )
        }
    ) {
        FormTextField("Número de sobre", number, { number = it }, keyboardType = KeyboardType.Number)
        DropdownField("Miembro", state.members, member, { it.fullName }, { member = it })
        FormTextField("Año", year, { year = it }, keyboardType = KeyboardType.Number)
    }
}
