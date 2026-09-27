package com.iglesiaflow.gestion.data.remote

import android.util.Log
import com.iglesiaflow.gestion.core.config.SettingsRepository
import com.iglesiaflow.gestion.data.local.dao.EventDao
import com.iglesiaflow.gestion.data.local.dao.FinanceDao
import com.iglesiaflow.gestion.data.local.dao.MemberDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

enum class SyncState { IDLE, RUNNING, SUCCESS, DISABLED, ERROR }

/**
 * Sincronización multi-dispositivo: empuja a Firestore los registros marcados
 * como `pendingSync`. Se ejecuta solo si la nube está configurada y activada.
 */
@Singleton
class SyncManager @Inject constructor(
    private val gateway: FirebaseGateway,
    private val settingsRepository: SettingsRepository,
    private val memberDao: Provider<MemberDao>,
    private val financeDao: Provider<FinanceDao>,
    private val eventDao: Provider<EventDao>
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val _state = MutableStateFlow(SyncState.IDLE)
    val state: StateFlow<SyncState> = _state.asStateFlow()

    private val _lastSyncAt = MutableStateFlow<Long?>(null)
    val lastSyncAt: StateFlow<Long?> = _lastSyncAt.asStateFlow()

    fun requestSync() {
        scope.launch { syncNow() }
    }

    suspend fun syncNow(): SyncState {
        val settings = settingsRepository.settings.first()
        if (!settings.cloudSyncEnabled || !gateway.isAvailable) {
            _state.value = SyncState.DISABLED
            return SyncState.DISABLED
        }
        _state.value = SyncState.RUNNING
        val result = runCatching {
            memberDao.get().allMembersOnce().filter { it.pendingSync }.forEach { member ->
                gateway.upsert(
                    "members", member.id.toString(),
                    mapOf(
                        "firstName" to member.firstName,
                        "lastName" to member.lastName,
                        "email" to member.email,
                        "phone" to member.phone,
                        "status" to member.status.name,
                        "familyId" to member.familyId,
                        "updatedAt" to member.updatedAt
                    )
                )
            }
            financeDao.get().allDonationsOnce().filter { it.pendingSync }.forEach { donation ->
                gateway.upsert(
                    "donations", donation.id.toString(),
                    mapOf(
                        "memberId" to donation.memberId,
                        "amount" to donation.amount,
                        "type" to donation.type.name,
                        "method" to donation.method.name,
                        "date" to donation.date
                    )
                )
            }
            eventDao.get().allEventsOnce().filter { it.pendingSync }.forEach { event ->
                gateway.upsert(
                    "events", event.id.toString(),
                    mapOf(
                        "title" to event.title,
                        "startAt" to event.startAt,
                        "endAt" to event.endAt,
                        "location" to event.location,
                        "type" to event.type.name
                    )
                )
            }
        }
        return if (result.isSuccess) {
            _lastSyncAt.value = System.currentTimeMillis()
            _state.value = SyncState.SUCCESS
            SyncState.SUCCESS
        } else {
            Log.w("SyncManager", "Error sincronizando", result.exceptionOrNull())
            _state.value = SyncState.ERROR
            SyncState.ERROR
        }
    }
}
