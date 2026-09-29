package com.iglesiaflow.gestion.data.remote

import com.iglesiaflow.gestion.data.local.dao.SyncDao
import com.iglesiaflow.gestion.data.local.entity.TombstoneEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

/**
 * Fachada que usan los repositorios para avisar de cambios locales.
 * Delega en [RealtimeSyncManager], que mantiene la réplica en tiempo real.
 */
@Singleton
class SyncManager @Inject constructor(
    private val realtime: RealtimeSyncManager,
    private val syncDaoProvider: Provider<SyncDao>
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val status: StateFlow<RealtimeStatus> get() = realtime.status
    val pendingCount: Flow<Int> get() = realtime.pendingCount

    /** Empuja la cola de salida en segundo plano. */
    fun requestSync() {
        realtime.syncNow()
    }

    suspend fun syncNow() = realtime.pushPending()

    /** Comprobación guiada de la conexión con Firebase. */
    suspend fun diagnose(): List<String> = realtime.diagnose()

    /**
     * Registra una lápida para que el borrado llegue al resto de dispositivos.
     * Si el registro nunca llegó a subirse no hace falta propagar nada.
     */
    suspend fun notifyDeleted(collection: String, remoteId: String?) {
        if (!remoteId.isNullOrBlank()) {
            syncDaoProvider.get().addTombstone(
                TombstoneEntity(collection = collection, remoteId = remoteId)
            )
        }
        realtime.syncNow()
    }

    fun notifyDeletedAsync(collection: String, remoteId: String?) {
        scope.launch { notifyDeleted(collection, remoteId) }
    }
}
