package com.iglesiaflow.gestion.ui.screens.communication

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iglesiaflow.gestion.core.config.AppSettings
import com.iglesiaflow.gestion.core.config.SettingsRepository
import com.iglesiaflow.gestion.core.security.SessionManager
import com.iglesiaflow.gestion.core.util.NotificationHelper
import com.iglesiaflow.gestion.data.local.entity.CampaignEntity
import com.iglesiaflow.gestion.data.local.entity.GroupEntity
import com.iglesiaflow.gestion.data.local.entity.MemberEntity
import com.iglesiaflow.gestion.data.repository.CommunicationRepository
import com.iglesiaflow.gestion.data.repository.GroupRepository
import com.iglesiaflow.gestion.data.repository.MemberRepository
import com.iglesiaflow.gestion.domain.model.CampaignChannel
import com.iglesiaflow.gestion.domain.model.Permission
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CommunicationUiState(
    val campaigns: List<CampaignEntity> = emptyList(),
    val members: List<MemberEntity> = emptyList(),
    val groups: List<GroupEntity> = emptyList(),
    val openPrayers: Int = 0,
    val settings: AppSettings = AppSettings(),
    val canSend: Boolean = false,
    val message: String? = null
) {
    /** Destinatarios según la audiencia seleccionada y el consentimiento del miembro. */
    fun recipients(audience: String, channel: CampaignChannel): List<String> {
        val base = members.filter { it.allowsContact }
        val filtered = when {
            audience.startsWith("GROUP") -> base
            audience == "LEADERS" -> base.filter { it.churchRole.isNotBlank() }
            audience == "ACTIVE" -> base.filter { it.status.name == "ACTIVO" }
            else -> base
        }
        return when (channel) {
            CampaignChannel.EMAIL -> filtered.mapNotNull { it.email.takeIf { mail -> mail.isNotBlank() } }
            CampaignChannel.SMS -> filtered.mapNotNull { it.phone.takeIf { phone -> phone.isNotBlank() } }
            CampaignChannel.PUSH -> filtered.map { it.fullName }
        }
    }
}

@HiltViewModel
class CommunicationViewModel @Inject constructor(
    private val repository: CommunicationRepository,
    memberRepository: MemberRepository,
    groupRepository: GroupRepository,
    private val notificationHelper: NotificationHelper,
    private val sessionManager: SessionManager,
    settingsRepository: SettingsRepository
) : ViewModel() {

    private val message = MutableStateFlow<String?>(null)

    val uiState: StateFlow<CommunicationUiState> = combine(
        repository.campaigns(),
        memberRepository.members(),
        groupRepository.groups(),
        repository.openPrayerCount(),
        combine(settingsRepository.settings, message) { settings, msg -> settings to msg }
    ) { campaigns, members, groups, prayers, settingsAndMessage ->
        CommunicationUiState(
            campaigns = campaigns,
            members = members,
            groups = groups,
            openPrayers = prayers,
            settings = settingsAndMessage.first,
            canSend = sessionManager.has(Permission.COMMUNICATION_SEND),
            message = settingsAndMessage.second
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CommunicationUiState())

    fun consumeMessage() { message.value = null }

    fun saveDraft(campaign: CampaignEntity) {
        if (!sessionManager.has(Permission.COMMUNICATION_SEND)) {
            message.value = "No tienes permisos para enviar comunicaciones"
            return
        }
        viewModelScope.launch {
            repository.saveCampaign(campaign)
            message.value = "Campaña guardada"
        }
    }

    fun markSent(campaign: CampaignEntity, recipients: Int) {
        viewModelScope.launch {
            val id = if (campaign.id == 0L) repository.saveCampaign(campaign) else campaign.id
            repository.markSent(id, recipients)
            if (campaign.channel == CampaignChannel.PUSH) {
                notificationHelper.notify(
                    NotificationHelper.CHANNEL_GENERAL,
                    campaign.subject,
                    campaign.body
                )
            }
            message.value = "Campaña enviada a $recipients destinatarios"
        }
    }

    fun delete(id: Long) {
        viewModelScope.launch { repository.deleteCampaign(id) }
    }
}
