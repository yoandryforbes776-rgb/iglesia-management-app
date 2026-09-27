package com.iglesiaflow.gestion.ui.screens.communication

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iglesiaflow.gestion.core.security.SessionManager
import com.iglesiaflow.gestion.data.local.entity.PrayerRequestEntity
import com.iglesiaflow.gestion.data.repository.CommunicationRepository
import com.iglesiaflow.gestion.domain.model.Permission
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PrayerUiState(
    val requests: List<PrayerRequestEntity> = emptyList(),
    val canModerate: Boolean = false
)

@HiltViewModel
class PrayerViewModel @Inject constructor(
    private val repository: CommunicationRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val canModerate = sessionManager.has(Permission.PRAYER_MODERATE)

    val uiState: StateFlow<PrayerUiState> =
        (if (canModerate) repository.prayerRequests() else repository.publicPrayerRequests())
            .map { PrayerUiState(requests = it, canModerate = canModerate) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PrayerUiState())

    fun save(request: PrayerRequestEntity) {
        viewModelScope.launch { repository.savePrayer(request) }
    }

    fun pray(id: Long) {
        viewModelScope.launch { repository.prayFor(id) }
    }

    fun answer(id: Long, note: String) {
        viewModelScope.launch { repository.answerPrayer(id, note) }
    }

    fun delete(id: Long) {
        viewModelScope.launch { repository.deletePrayer(id) }
    }
}
