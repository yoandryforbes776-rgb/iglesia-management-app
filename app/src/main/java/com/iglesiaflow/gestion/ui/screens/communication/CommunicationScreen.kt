@file:OptIn(ExperimentalMaterial3Api::class)

package com.iglesiaflow.gestion.ui.screens.communication

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.item
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.iglesiaflow.gestion.core.util.DateTimeUtils
import com.iglesiaflow.gestion.data.local.entity.CampaignEntity
import com.iglesiaflow.gestion.domain.model.CampaignChannel
import com.iglesiaflow.gestion.domain.model.CampaignStatus
import com.iglesiaflow.gestion.ui.components.DropdownField
import com.iglesiaflow.gestion.ui.components.FormDialog
import com.iglesiaflow.gestion.ui.components.FormTextField
import com.iglesiaflow.gestion.ui.components.InfoBanner

private val audiences = listOf(
    "ALL" to "Toda la iglesia",
    "ACTIVE" to "Solo miembros activos",
    "LEADERS" to "Líderes y equipo"
)

@Composable
fun CommunicationScreen(
    onOpenPrayer: () -> Unit,
    viewModel: CommunicationViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    var showForm by remember { mutableStateOf(false) }

    LaunchedEffect(state.message) {
        state.message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.consumeMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            if (state.canSend) {
                FloatingActionButton(onClick = { showForm = true }) {
                    Icon(Icons.Filled.Add, contentDescription = "Nueva campaña")
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp, 12.dp, 16.dp, 96.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Card(Modifier.fillMaxWidth().clickable { onOpenPrayer() }) {
                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.VolunteerActivism, contentDescription = null)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Peticiones de oración", style = MaterialTheme.typography.titleSmall)
                            Text(
                                "${state.openPrayers} abiertas",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        AssistChip(onClick = onOpenPrayer, label = { Text("Abrir") })
                    }
                }
            }

            item {
                InfoBanner(
                    "Los envíos de email y SMS se realizan a través de la app de correo o mensajería del " +
                        "dispositivo. Las notificaciones push utilizan Firebase Cloud Messaging cuando está configurado."
                )
            }

            item { Text("Campañas", style = MaterialTheme.typography.titleMedium) }

            items(state.campaigns, key = { it.id }) { campaign ->
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(campaign.subject, style = MaterialTheme.typography.titleSmall)
                                Text(
                                    "${campaign.channel.label} · ${campaign.status.label} · " +
                                        DateTimeUtils.formatDate(campaign.sentAt ?: campaign.createdAt),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (state.canSend && campaign.status != CampaignStatus.ENVIADA) {
                                IconButton(onClick = {
                                    val recipients = state.recipients(campaign.audience, campaign.channel)
                                    launchCampaign(context, campaign, recipients)
                                    viewModel.markSent(campaign, recipients.size)
                                }) { Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Enviar") }
                            }
                            if (state.canSend) {
                                IconButton(onClick = { viewModel.delete(campaign.id) }) {
                                    Icon(Icons.Filled.Delete, contentDescription = "Eliminar")
                                }
                            }
                        }
                        Spacer(Modifier.padding(top = 6.dp))
                        Text(campaign.body, style = MaterialTheme.typography.bodyMedium)
                        if (campaign.recipientCount > 0) {
                            Text(
                                "${campaign.recipientCount} destinatarios",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }

            if (state.campaigns.isEmpty()) {
                item { Text("Todavía no hay campañas creadas.") }
            }
        }
    }

    if (showForm) {
        var subject by remember { mutableStateOf("") }
        var body by remember { mutableStateOf("") }
        var channel by remember { mutableStateOf(CampaignChannel.EMAIL) }
        var audience by remember { mutableStateOf(audiences.first()) }
        FormDialog(
            title = "Nueva comunicación",
            onDismiss = { showForm = false },
            confirmEnabled = subject.isNotBlank() && body.isNotBlank(),
            confirmLabel = "Guardar",
            onConfirm = {
                viewModel.saveDraft(
                    CampaignEntity(
                        subject = subject.trim(),
                        body = body.trim(),
                        channel = channel,
                        audience = audience.first
                    )
                )
                showForm = false
            }
        ) {
            FormTextField("Asunto", subject, { subject = it })
            FormTextField("Mensaje", body, { body = it }, singleLine = false)
            DropdownField("Canal", CampaignChannel.entries, channel, { it.label }, { channel = it })
            DropdownField("Audiencia", audiences, audience, { it.second }, { audience = it })
        }
    }
}

private fun launchCampaign(
    context: android.content.Context,
    campaign: CampaignEntity,
    recipients: List<String>
) {
    when (campaign.channel) {
        CampaignChannel.EMAIL -> {
            val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:")).apply {
                putExtra(Intent.EXTRA_BCC, recipients.toTypedArray())
                putExtra(Intent.EXTRA_SUBJECT, campaign.subject)
                putExtra(Intent.EXTRA_TEXT, campaign.body)
            }
            runCatching { context.startActivity(Intent.createChooser(intent, "Enviar boletín")) }
        }
        CampaignChannel.SMS -> {
            val uri = Uri.parse("smsto:${recipients.joinToString(";")}")
            val intent = Intent(Intent.ACTION_SENDTO, uri).apply {
                putExtra("sms_body", "${campaign.subject}\n${campaign.body}")
            }
            runCatching { context.startActivity(intent) }
        }
        CampaignChannel.PUSH -> Unit
    }
}
