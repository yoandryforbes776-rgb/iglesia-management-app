@file:OptIn(ExperimentalMaterial3Api::class)

package com.iglesiaflow.gestion.ui.screens.finance

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.item
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.iglesiaflow.gestion.core.util.DateTimeUtils
import com.iglesiaflow.gestion.core.util.Formatters
import com.iglesiaflow.gestion.data.local.entity.DepositEntity
import com.iglesiaflow.gestion.data.local.entity.DonationEntity
import com.iglesiaflow.gestion.data.local.entity.EnvelopeEntity
import com.iglesiaflow.gestion.data.local.entity.ExpenseEntity
import com.iglesiaflow.gestion.data.local.entity.PledgeEntity
import com.iglesiaflow.gestion.ui.components.DataTable
import com.iglesiaflow.gestion.ui.components.HorizontalBreakdown
import com.iglesiaflow.gestion.ui.components.LineChart
import com.iglesiaflow.gestion.ui.components.SectionCard
import com.iglesiaflow.gestion.ui.components.StatCard

private val tabs = listOf("Resumen", "Donaciones", "Promesas", "Sobres", "Depósitos", "Gastos")

@Composable
fun FinanceScreen(viewModel: FinanceViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var tabIndex by remember { mutableStateOf(0) }
    var showDonationForm by remember { mutableStateOf(false) }
    var showPledgeForm by remember { mutableStateOf(false) }
    var showExpenseForm by remember { mutableStateOf(false) }
    var showDepositForm by remember { mutableStateOf(false) }
    var showEnvelopeForm by remember { mutableStateOf(false) }
    val currency = state.settings.currencyCode

    LaunchedEffect(state.message) {
        state.message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.consumeMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            if (state.canEdit) {
                FloatingActionButton(onClick = {
                    when (tabIndex) {
                        2 -> showPledgeForm = true
                        3 -> showEnvelopeForm = true
                        4 -> showDepositForm = true
                        5 -> showExpenseForm = true
                        else -> showDonationForm = true
                    }
                }) { Icon(Icons.Filled.Add, contentDescription = "Añadir") }
            }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            ScrollableTabRow(selectedTabIndex = tabIndex, edgePadding = 12.dp) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = tabIndex == index,
                        onClick = { tabIndex = index },
                        text = { Text(title) }
                    )
                }
            }
            Row(
                Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FinancePeriod.entries.forEach { period ->
                    FilterChip(
                        selected = state.period == period,
                        onClick = { viewModel.onPeriodChange(period) },
                        label = { Text(period.label) }
                    )
                }
            }

            when (tabIndex) {
                0 -> SummaryTab(state)
                1 -> DonationsTab(state, viewModel)
                2 -> PledgesTab(state)
                3 -> EnvelopesTab(state)
                4 -> DepositsTab(state)
                else -> ExpensesTab(state, viewModel)
            }
        }
    }

    if (showDonationForm) {
        DonationFormDialog(
            state = state,
            onDismiss = { showDonationForm = false },
            onSave = { donation -> viewModel.saveDonation(donation); showDonationForm = false }
        )
    }
    if (showPledgeForm) {
        PledgeFormDialog(
            state = state,
            onDismiss = { showPledgeForm = false },
            onSave = { pledge -> viewModel.savePledge(pledge); showPledgeForm = false }
        )
    }
    if (showExpenseForm) {
        ExpenseFormDialog(
            state = state,
            onDismiss = { showExpenseForm = false },
            onSave = { expense -> viewModel.saveExpense(expense); showExpenseForm = false }
        )
    }
    if (showDepositForm) {
        DepositFormDialog(
            state = state,
            onDismiss = { showDepositForm = false },
            onSave = { deposit, ids -> viewModel.saveDeposit(deposit, ids); showDepositForm = false }
        )
    }
    if (showEnvelopeForm) {
        EnvelopeFormDialog(
            state = state,
            onDismiss = { showEnvelopeForm = false },
            onSave = { envelope -> viewModel.saveEnvelope(envelope); showEnvelopeForm = false }
        )
    }
}

@Composable
private fun SummaryTab(state: FinanceUiState) {
    val currency = state.settings.currencyCode
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(
                    title = "Ingresos",
                    value = Formatters.money(state.totalIncome, currency),
                    subtitle = state.period.label,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Gastos",
                    value = Formatters.money(state.totalExpenses, currency),
                    subtitle = "Balance ${Formatters.money(state.totalIncome - state.totalExpenses, currency)}",
                    modifier = Modifier.weight(1f)
                )
            }
        }
        item { SectionCard(title = "Tendencia de ingresos") { LineChart(state.trend) } }
        item {
            SectionCard(title = "Por fondo") {
                HorizontalBreakdown(state.byFund, { Formatters.money(it, currency) })
            }
        }
        item {
            SectionCard(title = "Por tipo de donación") {
                HorizontalBreakdown(state.byType, { Formatters.money(it, currency) })
            }
        }
        item { Spacer(Modifier.height(80.dp)) }
    }
}

@Composable
private fun DonationsTab(state: FinanceUiState, viewModel: FinanceViewModel) {
    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "${state.donations.size} donaciones · ${Formatters.money(state.totalIncome, state.settings.currencyCode)}",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = viewModel::exportDonationsCsv) {
                Icon(Icons.Filled.FileDownload, contentDescription = "CSV")
            }
            IconButton(onClick = viewModel::exportDonationsPdf) {
                Icon(Icons.Filled.PictureAsPdf, contentDescription = "PDF")
            }
        }
        LazyColumn(
            contentPadding = PaddingValues(16.dp, 8.dp, 16.dp, 96.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(state.donations, key = { it.id }) { donation ->
                Card(Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                if (donation.anonymous) "Anónimo" else state.memberName(donation.memberId),
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text(
                                "${DateTimeUtils.formatDate(donation.date)} · ${donation.type.label} · ${donation.method.label}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                "${state.fundName(donation.fundId)}" +
                                    (donation.envelopeNumber?.let { " · sobre #$it" } ?: ""),
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                        Text(
                            Formatters.money(donation.amount, state.settings.currencyCode),
                            style = MaterialTheme.typography.titleMedium
                        )
                        if (state.canEdit) {
                            IconButton(onClick = { viewModel.deleteDonation(donation.id) }) {
                                Icon(Icons.Filled.Delete, contentDescription = "Eliminar")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PledgesTab(state: FinanceUiState) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp, 8.dp, 16.dp, 96.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(state.pledges, key = { it.pledge.id }) { progress ->
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp)) {
                    Text(progress.pledge.campaign.ifBlank { "Promesa de fe" }, style = MaterialTheme.typography.titleSmall)
                    Text(
                        "${progress.memberName} · ${progress.pledge.frequency.label}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { progress.progress },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "${Formatters.money(progress.contributed, state.settings.currencyCode)} de " +
                            Formatters.money(progress.pledge.totalAmount, state.settings.currencyCode) +
                            " (${Formatters.percent(progress.progress.toDouble())})",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
        if (state.pledges.isEmpty()) {
            item { Text("Sin promesas registradas", Modifier.padding(16.dp)) }
        }
    }
}

@Composable
private fun EnvelopesTab(state: FinanceUiState) {
    val rows = state.envelopes.map { envelope ->
        listOf(
            "#${envelope.number}",
            state.memberName(envelope.memberId),
            envelope.year.toString(),
            if (envelope.active) "Activo" else "Inactivo"
        )
    }
    Column(Modifier.padding(16.dp)) {
        DataTable(headers = listOf("Sobre", "Asignado a", "Año", "Estado"), rows = rows)
    }
}

@Composable
private fun DepositsTab(state: FinanceUiState) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp, 8.dp, 16.dp, 96.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(state.deposits, key = { it.id }) { deposit ->
            val total = state.donations.filter { it.depositId == deposit.id }.sumOf { it.amount }
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp)) {
                    Text(deposit.name, style = MaterialTheme.typography.titleSmall)
                    Text(
                        "${DateTimeUtils.formatDate(deposit.date)} · ${deposit.bankAccount}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "Total agrupado: ${Formatters.money(total, state.settings.currencyCode)}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
        if (state.deposits.isEmpty()) {
            item { Text("Sin depósitos", Modifier.padding(16.dp)) }
        }
    }
}

@Composable
private fun ExpensesTab(state: FinanceUiState, viewModel: FinanceViewModel) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp, 8.dp, 16.dp, 96.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(state.expenses, key = { it.id }) { expense ->
            Card(Modifier.fillMaxWidth()) {
                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(expense.category, style = MaterialTheme.typography.titleSmall)
                        Text(
                            "${DateTimeUtils.formatDate(expense.date)} · ${expense.description}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        Formatters.money(expense.amount, state.settings.currencyCode),
                        style = MaterialTheme.typography.titleMedium
                    )
                    if (state.canEdit) {
                        IconButton(onClick = { viewModel.deleteExpense(expense.id) }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Eliminar")
                        }
                    }
                }
            }
        }
        if (state.expenses.isEmpty()) {
            item { Text("Sin gastos en el periodo", Modifier.padding(16.dp)) }
        }
    }
}
