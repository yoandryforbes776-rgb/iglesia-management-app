package com.iglesiaflow.gestion.data.remote

import android.util.Log
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.iglesiaflow.gestion.core.config.SettingsRepository
import com.iglesiaflow.gestion.data.local.dao.SyncDao
import com.iglesiaflow.gestion.data.local.entity.AttendanceEntity
import com.iglesiaflow.gestion.data.local.entity.DonationEntity
import com.iglesiaflow.gestion.data.local.entity.EventEntity
import com.iglesiaflow.gestion.data.local.entity.FamilyEntity
import com.iglesiaflow.gestion.data.local.entity.FundEntity
import com.iglesiaflow.gestion.data.local.entity.GroupEntity
import com.iglesiaflow.gestion.data.local.entity.GroupMemberEntity
import com.iglesiaflow.gestion.data.local.entity.GroupMessageEntity
import com.iglesiaflow.gestion.data.local.entity.MemberEntity
import com.iglesiaflow.gestion.data.local.entity.PrayerRequestEntity
import com.iglesiaflow.gestion.data.local.entity.SyncStateEntity
import com.iglesiaflow.gestion.domain.model.DonationMethod
import com.iglesiaflow.gestion.domain.model.DonationType
import com.iglesiaflow.gestion.domain.model.EventType
import com.iglesiaflow.gestion.domain.model.FamilyRole
import com.iglesiaflow.gestion.domain.model.Gender
import com.iglesiaflow.gestion.domain.model.GroupRole
import com.iglesiaflow.gestion.domain.model.GroupType
import com.iglesiaflow.gestion.domain.model.MaritalStatus
import com.iglesiaflow.gestion.domain.model.MemberStatus
import com.iglesiaflow.gestion.domain.model.PrayerStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull
import java.util.UUID
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

/** Estado observable de la sincronización, mostrado en la barra superior. */
data class RealtimeStatus(
    val enabled: Boolean = false,
    val available: Boolean = false,
    val connected: Boolean = false,
    val syncing: Boolean = false,
    val lastSyncAt: Long? = null,
    val error: String? = null
)

/**
 * Sincronización bidireccional en tiempo real sobre Firebase Realtime Database.
 *
 * - **Bajada**: un `ChildEventListener` por colección aplica en Room cada cambio
 *   en cuanto ocurre (1-2 s), y como toda la UI observa Room con Flows, las
 *   pantallas se refrescan solas.
 * - **Subida**: los registros con `pendingSync = true` se empujan en orden de
 *   dependencia; los borrados viajan como lápidas (`sync_tombstones`).
 * - **Identidad**: cada registro recibe un `remoteId` (UUID) que es la clave en
 *   la nube, de modo que dos dispositivos nunca se pisan aunque coincidan sus
 *   identificadores locales autoincrementales.
 * - **Conflictos**: gana la última escritura (`updatedAt` mayor).
 *
 * Funciona dentro de la cuota gratuita (plan Spark) porque solo se descargan los
 * nodos con `updatedAt` posterior a la última sincronización aplicada.
 */
@Singleton
class RealtimeSyncManager @Inject constructor(
    private val gateway: FirebaseGateway,
    private val settingsRepository: SettingsRepository,
    private val syncDaoProvider: Provider<SyncDao>
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val pushMutex = Mutex()
    private val listeners = mutableMapOf<Query, ChildEventListener>()
    private var connectionListener: ValueEventListener? = null
    private var connectionRef: DatabaseReference? = null
    private var currentRoot: DatabaseReference? = null
    private var lastChurchId: String = DEFAULT_CHURCH
    private var lastDatabaseUrl: String = ""
    private var retryJob: Job? = null

    private val _status = MutableStateFlow(RealtimeStatus())
    val status: StateFlow<RealtimeStatus> = _status.asStateFlow()

    private val syncDao: SyncDao get() = syncDaoProvider.get()

    val pendingCount get() = syncDaoProvider.get().pendingCount()

    /** Arranca (o detiene) la sincronización según la configuración de la iglesia. */
    fun bind() {
        scope.launch {
            settingsRepository.settings
                .map { Triple(it.cloudSyncEnabled, it.cloudChurchId, it.cloudDatabaseUrl) }
                .distinctUntilChanged()
                .collect { (enabled, churchId, databaseUrl) ->
                    lastChurchId = churchId.ifBlank { DEFAULT_CHURCH }
                    lastDatabaseUrl = databaseUrl
                    detach()
                    _status.value = _status.value.copy(
                        enabled = enabled,
                        available = gateway.isAvailable,
                        error = null
                    )
                    if (enabled && gateway.isAvailable) {
                        connect()
                        startRetryLoop()
                    } else {
                        retryJob?.cancel()
                    }
                }
        }
    }

    /**
     * Comprobación guiada de la conexión: informa paso a paso de qué falla
     * (archivo de configuración, URL, permisos, reglas...) en lenguaje claro.
     */
    suspend fun diagnose(): List<String> {
        val lines = mutableListOf<String>()
        if (!gateway.isAvailable) {
            lines += "❌ Esta APK no incluye google-services.json: vuelve a compilarla con el archivo de tu proyecto."
            return lines
        }
        lines += "✅ La APK incluye la configuración de Firebase."

        val settings = settingsRepository.settings.first()
        val database = runCatching { databaseFor(settings.cloudDatabaseUrl) }.getOrElse { error ->
            lines += "❌ No hay dirección de Realtime Database: ${error.message}. " +
                "Crea la base de datos en la consola y vuelve a descargar google-services.json, " +
                "o pega la URL en el campo de abajo."
            return lines
        }
        lines += "✅ Base de datos: ${database.reference}"

        val uid = runCatching {
            val auth = Firebase.auth
            if (auth.currentUser == null) auth.signInAnonymously().await()
            auth.currentUser?.uid
        }.getOrElse { error ->
            lines += "❌ No se pudo iniciar sesión anónima: ${error.message}. " +
                "Activa Authentication → Sign-in method → Anónimo en la consola de Firebase."
            null
        }
        if (uid != null) lines += "✅ Sesión anónima iniciada (${uid.take(8)}…)."

        val churchId = settings.cloudChurchId.ifBlank { DEFAULT_CHURCH }
        val probe = database.reference.child(CHURCHES).child(churchId).child("_health").child("probe")
        val stamp = System.currentTimeMillis()
        val written = withTimeoutOrNull(PROBE_TIMEOUT_MS) {
            runCatching { probe.setValue(mapOf(FIELD_UPDATED_AT to stamp)).await() }
        }
        when {
            written == null -> lines += "❌ La escritura de prueba no respondió en 10 s: revisa la conexión a internet."
            written.isFailure -> lines += "❌ Escritura rechazada: ${written.exceptionOrNull()?.message}. " +
                "Suele significar que las reglas de la base de datos no permiten escribir (publica las reglas de docs/FIREBASE.md)."
            else -> lines += "✅ Escritura de prueba correcta."
        }

        val read = withTimeoutOrNull(PROBE_TIMEOUT_MS) {
            runCatching { probe.get().await().child(FIELD_UPDATED_AT).getValue(Long::class.java) }
        }
        when {
            read == null -> lines += "❌ La lectura de prueba no respondió en 10 s."
            read.isFailure -> lines += "❌ Lectura rechazada: ${read.exceptionOrNull()?.message}."
            read.getOrNull() == stamp -> lines += "✅ Lectura de prueba correcta: la nube responde bien."
            else -> lines += "⚠️ La lectura devolvió un valor distinto al escrito."
        }

        lines += if (settings.cloudSyncEnabled) {
            "✅ El interruptor de sincronización está encendido (código de iglesia: $churchId)."
        } else {
            "⚠️ El interruptor de sincronización está apagado: enciéndelo para sincronizar."
        }
        return lines
    }

    /** Fuerza una subida inmediata; si había error, reintenta la conexión entera. */
    fun syncNow() {
        scope.launch {
            if (_status.value.error != null && _status.value.enabled && gateway.isAvailable) {
                connect()
            }
            pushPending()
        }
    }

    private suspend fun connect() {
        detach()
        runCatching { attach(lastChurchId, lastDatabaseUrl) }
            .onSuccess { _status.value = _status.value.copy(error = null) }
            .onFailure { failure ->
                Log.w(TAG, "No se pudo iniciar la sincronización", failure)
                _status.value = _status.value.copy(error = failure.message)
            }
    }

    /**
     * Si Firebase rechaza la conexión (reglas sin publicar, acceso anónimo sin
     * activar...), se reintenta cada minuto: en cuanto se corrija en la consola
     * la app se engancha sola, sin reinstalar ni reiniciar.
     */
    private fun startRetryLoop() {
        retryJob?.cancel()
        retryJob = scope.launch {
            while (true) {
                delay(RETRY_INTERVAL_MS)
                val current = _status.value
                if (current.enabled && current.available && current.error != null) {
                    Log.i(TAG, "Reintentando la conexión con Firebase…")
                    connect()
                    if (_status.value.error == null) pushPending()
                }
            }
        }
    }

    /**
     * Las reglas recomendadas exigen `auth != null`, así que abrimos una sesión
     * anónima de Firebase Auth (no pide datos al usuario; la autenticación real
     * de la app sigue siendo local). Si el proveedor anónimo está desactivado se
     * continúa igualmente por si las reglas son públicas.
     */
    private suspend fun ensureSignedIn() {
        runCatching {
            val auth = Firebase.auth
            if (auth.currentUser == null) auth.signInAnonymously().await()
        }.onFailure { Log.w(TAG, "Sin sesión anónima de Firebase", it) }
    }

    /**
     * Instancia de Realtime Database. Normalmente la URL viene en
     * `google-services.json`, pero si ese archivo se descargó antes de crear la
     * base de datos la iglesia puede pegarla a mano en los ajustes.
     */
    private fun databaseFor(url: String): FirebaseDatabase =
        if (url.isBlank()) Firebase.database else FirebaseDatabase.getInstance(url.trim())

    private suspend fun attach(churchId: String, databaseUrl: String) {
        ensureSignedIn()
        val database = databaseFor(databaseUrl)
        runCatching { database.setPersistenceEnabled(true) }
        val root = database.reference.child(CHURCHES).child(churchId)
        currentRoot = root

        connectionRef = database.getReference(".info/connected")
        connectionListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val connected = snapshot.getValue(Boolean::class.java) ?: false
                _status.value = _status.value.copy(connected = connected)
                if (connected) syncNow()
            }

            override fun onCancelled(error: DatabaseError) = Unit
        }
        connectionRef?.addValueEventListener(connectionListener!!)

        COLLECTIONS.forEach { collection ->
            val since = syncDao.lastPulledAt(collection) ?: 0L
            val query = root.child(collection).orderByChild(FIELD_UPDATED_AT).startAt(since.toDouble())
            val listener = object : ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) =
                    handle(collection, snapshot)

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) =
                    handle(collection, snapshot)

                override fun onChildRemoved(snapshot: DataSnapshot) {
                    val key = snapshot.key ?: return
                    scope.launch { deleteLocal(collection, key) }
                }

                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) = Unit

                override fun onCancelled(error: DatabaseError) {
                    Log.w(TAG, "Escucha cancelada en $collection: ${error.message}")
                    _status.value = _status.value.copy(error = error.message)
                }
            }
            query.addChildEventListener(listener)
            listeners[query] = listener
        }

        pushPending()
    }

    private fun detach() {
        listeners.forEach { (query, listener) -> query.removeEventListener(listener) }
        listeners.clear()
        connectionListener?.let { connectionRef?.removeEventListener(it) }
        connectionListener = null
        connectionRef = null
        currentRoot = null
        _status.value = _status.value.copy(connected = false)
    }

    private fun handle(collection: String, snapshot: DataSnapshot) {
        val key = snapshot.key ?: return
        @Suppress("UNCHECKED_CAST")
        val data = snapshot.value as? Map<String, Any?> ?: return
        scope.launch {
            runCatching { applyRemote(collection, key, data) }
                .onFailure { Log.w(TAG, "No se pudo aplicar $collection/$key", it) }
        }
    }

    // ------------------------------------------------------------------
    // Bajada: nube -> Room
    // ------------------------------------------------------------------

    private suspend fun applyRemote(collection: String, remoteId: String, data: Map<String, Any?>) {
        if (data.bool(FIELD_DELETED)) {
            deleteLocal(collection, remoteId)
            advance(collection, data.long(FIELD_UPDATED_AT))
            return
        }
        val updatedAt = data.long(FIELD_UPDATED_AT)
        val applied = when (collection) {
            FAMILIES -> applyFamily(remoteId, data, updatedAt)
            MEMBERS -> applyMember(remoteId, data, updatedAt)
            FUNDS -> applyFund(remoteId, data, updatedAt)
            DONATIONS -> applyDonation(remoteId, data, updatedAt)
            EVENTS -> applyEvent(remoteId, data, updatedAt)
            ATTENDANCE -> applyAttendance(remoteId, data, updatedAt)
            GROUPS -> applyGroup(remoteId, data, updatedAt)
            GROUP_MEMBERS -> applyGroupMember(remoteId, data, updatedAt)
            GROUP_MESSAGES -> applyGroupMessage(remoteId, data, updatedAt)
            PRAYERS -> applyPrayer(remoteId, data, updatedAt)
            else -> true
        }
        // Si faltaba una relación (el padre aún no ha llegado) no avanzamos el
        // cursor: el registro se reintentará en la siguiente sincronización.
        if (applied) advance(collection, updatedAt)
        _status.value = _status.value.copy(lastSyncAt = System.currentTimeMillis(), error = null)
    }

    private suspend fun advance(collection: String, updatedAt: Long) {
        val current = syncDao.lastPulledAt(collection) ?: 0L
        if (updatedAt > current) syncDao.saveState(SyncStateEntity(collection, updatedAt))
    }

    private suspend fun applyFamily(remoteId: String, data: Map<String, Any?>, updatedAt: Long): Boolean {
        val existing = syncDao.familyByRemote(remoteId)
        if (existing != null && existing.updatedAt >= updatedAt) return true
        val entity = (existing ?: FamilyEntity(name = "")).copy(
            name = data.str("name"),
            address = data.str("address"),
            city = data.str("city"),
            phone = data.str("phone"),
            email = data.str("email"),
            notes = data.str("notes"),
            updatedAt = updatedAt,
            remoteId = remoteId,
            pendingSync = false
        )
        if (existing == null) syncDao.insertFamily(entity) else syncDao.updateFamily(entity)
        return true
    }

    private suspend fun applyMember(remoteId: String, data: Map<String, Any?>, updatedAt: Long): Boolean {
        val existing = syncDao.memberByRemote(remoteId)
        if (existing != null && existing.updatedAt >= updatedAt) return true
        val familyRemote = data.strOrNull("familyRemoteId")
        val familyId = familyRemote?.let { syncDao.familyIdByRemote(it) }
        if (familyRemote != null && familyId == null) return false
        val entity = (existing ?: MemberEntity(firstName = "", lastName = "")).copy(
            firstName = data.str("firstName"),
            lastName = data.str("lastName"),
            email = data.str("email"),
            phone = data.str("phone"),
            birthDate = data.longOrNull("birthDate"),
            gender = data.enum("gender", Gender.NO_ESPECIFICA),
            maritalStatus = data.enum("maritalStatus", MaritalStatus.SOLTERO),
            status = data.enum("status", MemberStatus.ACTIVO),
            churchRole = data.str("churchRole"),
            familyId = familyId,
            familyRole = data.enum("familyRole", FamilyRole.OTRO),
            address = data.str("address"),
            city = data.str("city"),
            baptized = data.bool("baptized"),
            baptismDate = data.longOrNull("baptismDate"),
            joinedAt = data.long("joinedAt", System.currentTimeMillis()),
            propertiesCsv = data.str("propertiesCsv"),
            notes = data.str("notes"),
            isChild = data.bool("isChild"),
            guardianPhone = data.str("guardianPhone"),
            allowsContact = data.bool("allowsContact", true),
            updatedAt = updatedAt,
            remoteId = remoteId,
            pendingSync = false
        )
        if (existing == null) syncDao.insertMember(entity) else syncDao.updateMember(entity)
        return true
    }

    private suspend fun applyFund(remoteId: String, data: Map<String, Any?>, updatedAt: Long): Boolean {
        val existing = syncDao.fundByRemote(remoteId)
        if (existing != null && existing.updatedAt >= updatedAt) return true
        val entity = (existing ?: FundEntity(name = "")).copy(
            name = data.str("name"),
            description = data.str("description"),
            active = data.bool("active", true),
            updatedAt = updatedAt,
            remoteId = remoteId,
            pendingSync = false
        )
        if (existing == null) syncDao.insertFund(entity) else syncDao.updateFund(entity)
        return true
    }

    private suspend fun applyDonation(remoteId: String, data: Map<String, Any?>, updatedAt: Long): Boolean {
        val existing = syncDao.donationByRemote(remoteId)
        if (existing != null && existing.updatedAt >= updatedAt) return true
        val memberRemote = data.strOrNull("memberRemoteId")
        val memberId = memberRemote?.let { syncDao.memberIdByRemote(it) }
        if (memberRemote != null && memberId == null) return false
        val fundRemote = data.strOrNull("fundRemoteId")
        val fundId = fundRemote?.let { syncDao.fundIdByRemote(it) }
        if (fundRemote != null && fundId == null) return false
        val entity = (existing ?: DonationEntity(amount = 0.0)).copy(
            memberId = memberId,
            anonymous = data.bool("anonymous"),
            amount = data.dbl("amount"),
            type = data.enum("type", DonationType.OFRENDA),
            method = data.enum("method", DonationMethod.EFECTIVO),
            fundId = fundId,
            envelopeNumber = data.intOrNull("envelopeNumber"),
            date = data.long("date", System.currentTimeMillis()),
            reference = data.str("reference"),
            note = data.str("note"),
            createdBy = data.str("createdBy"),
            updatedAt = updatedAt,
            remoteId = remoteId,
            pendingSync = false
        )
        if (existing == null) syncDao.insertDonation(entity) else syncDao.updateDonation(entity)
        return true
    }

    private suspend fun applyEvent(remoteId: String, data: Map<String, Any?>, updatedAt: Long): Boolean {
        val existing = syncDao.eventByRemote(remoteId)
        if (existing != null && existing.updatedAt >= updatedAt) return true
        val groupRemote = data.strOrNull("groupRemoteId")
        val groupId = groupRemote?.let { syncDao.groupIdByRemote(it) }
        if (groupRemote != null && groupId == null) return false
        val entity = (existing ?: EventEntity(title = "", startAt = 0, endAt = 0)).copy(
            title = data.str("title"),
            description = data.str("description"),
            type = data.enum("type", EventType.CULTO),
            location = data.str("location"),
            startAt = data.long("startAt"),
            endAt = data.long("endAt"),
            allDay = data.bool("allDay"),
            recurrence = data.str("recurrence", "NONE"),
            timezoneId = data.str("timezoneId", "UTC"),
            requiresCheckIn = data.bool("requiresCheckIn"),
            groupId = groupId,
            capacity = data.intOrNull("capacity"),
            reminderMinutesBefore = data.int("reminderMinutesBefore", 60),
            createdBy = data.str("createdBy"),
            updatedAt = updatedAt,
            remoteId = remoteId,
            pendingSync = false
        )
        if (existing == null) syncDao.insertEvent(entity) else syncDao.updateEvent(entity)
        return true
    }

    private suspend fun applyAttendance(remoteId: String, data: Map<String, Any?>, updatedAt: Long): Boolean {
        val eventId = data.strOrNull("eventRemoteId")?.let { syncDao.eventIdByRemote(it) } ?: return false
        val memberId = data.strOrNull("memberRemoteId")?.let { syncDao.memberIdByRemote(it) } ?: return false
        val existing = syncDao.attendanceByRemote(remoteId) ?: syncDao.attendanceByPair(eventId, memberId)
        if (existing != null && existing.updatedAt >= updatedAt) return true
        val entity = (existing ?: AttendanceEntity(eventId = eventId, memberId = memberId)).copy(
            eventId = eventId,
            memberId = memberId,
            present = data.bool("present", true),
            registeredAt = data.long("registeredAt", System.currentTimeMillis()),
            note = data.str("note"),
            updatedAt = updatedAt,
            remoteId = remoteId,
            pendingSync = false
        )
        if (existing == null) syncDao.insertAttendance(entity) else syncDao.updateAttendance(entity)
        return true
    }

    private suspend fun applyGroup(remoteId: String, data: Map<String, Any?>, updatedAt: Long): Boolean {
        val existing = syncDao.groupByRemote(remoteId)
        if (existing != null && existing.updatedAt >= updatedAt) return true
        val leaderRemote = data.strOrNull("leaderRemoteId")
        val leaderId = leaderRemote?.let { syncDao.memberIdByRemote(it) }
        if (leaderRemote != null && leaderId == null) return false
        val entity = (existing ?: GroupEntity(name = "")).copy(
            name = data.str("name"),
            type = data.enum("type", GroupType.CELULA),
            description = data.str("description"),
            leaderId = leaderId,
            meetingDay = data.str("meetingDay"),
            meetingTime = data.str("meetingTime"),
            location = data.str("location"),
            active = data.bool("active", true),
            updatedAt = updatedAt,
            remoteId = remoteId,
            pendingSync = false
        )
        if (existing == null) syncDao.insertGroup(entity) else syncDao.updateGroup(entity)
        return true
    }

    private suspend fun applyGroupMember(remoteId: String, data: Map<String, Any?>, updatedAt: Long): Boolean {
        val groupId = data.strOrNull("groupRemoteId")?.let { syncDao.groupIdByRemote(it) } ?: return false
        val memberId = data.strOrNull("memberRemoteId")?.let { syncDao.memberIdByRemote(it) } ?: return false
        val existing = syncDao.groupMemberByRemote(remoteId) ?: syncDao.groupMemberByPair(groupId, memberId)
        if (existing != null && existing.updatedAt >= updatedAt) return true
        val entity = (existing ?: GroupMemberEntity(groupId = groupId, memberId = memberId)).copy(
            groupId = groupId,
            memberId = memberId,
            role = data.enum("role", GroupRole.MIEMBRO),
            joinedAt = data.long("joinedAt", System.currentTimeMillis()),
            updatedAt = updatedAt,
            remoteId = remoteId,
            pendingSync = false
        )
        if (existing == null) syncDao.insertGroupMember(entity) else syncDao.updateGroupMember(entity)
        return true
    }

    private suspend fun applyGroupMessage(remoteId: String, data: Map<String, Any?>, updatedAt: Long): Boolean {
        val groupId = data.strOrNull("groupRemoteId")?.let { syncDao.groupIdByRemote(it) } ?: return false
        val existing = syncDao.groupMessageByRemote(remoteId)
        if (existing != null && existing.updatedAt >= updatedAt) return true
        val entity = (existing ?: GroupMessageEntity(groupId = groupId, authorName = "", content = "")).copy(
            groupId = groupId,
            authorName = data.str("authorName"),
            content = data.str("content"),
            createdAt = data.long("createdAt", System.currentTimeMillis()),
            updatedAt = updatedAt,
            remoteId = remoteId,
            pendingSync = false
        )
        if (existing == null) syncDao.insertGroupMessage(entity) else syncDao.updateGroupMessage(entity)
        return true
    }

    private suspend fun applyPrayer(remoteId: String, data: Map<String, Any?>, updatedAt: Long): Boolean {
        val existing = syncDao.prayerByRemote(remoteId)
        if (existing != null && existing.updatedAt >= updatedAt) return true
        val entity = (existing ?: PrayerRequestEntity(title = "")).copy(
            title = data.str("title"),
            detail = data.str("detail"),
            requesterName = data.str("requesterName"),
            isPrivate = data.bool("isPrivate"),
            status = data.enum("status", PrayerStatus.ABIERTA),
            prayerCount = data.int("prayerCount"),
            createdAt = data.long("createdAt", System.currentTimeMillis()),
            answeredAt = data.longOrNull("answeredAt"),
            answerNote = data.str("answerNote"),
            updatedAt = updatedAt,
            remoteId = remoteId,
            pendingSync = false
        )
        if (existing == null) syncDao.insertPrayer(entity) else syncDao.updatePrayer(entity)
        return true
    }

    private suspend fun deleteLocal(collection: String, remoteId: String) {
        when (collection) {
            FAMILIES -> syncDao.deleteFamilyByRemote(remoteId)
            MEMBERS -> syncDao.deleteMemberByRemote(remoteId)
            FUNDS -> syncDao.deleteFundByRemote(remoteId)
            DONATIONS -> syncDao.deleteDonationByRemote(remoteId)
            EVENTS -> syncDao.deleteEventByRemote(remoteId)
            ATTENDANCE -> syncDao.deleteAttendanceByRemote(remoteId)
            GROUPS -> syncDao.deleteGroupByRemote(remoteId)
            GROUP_MEMBERS -> syncDao.deleteGroupMemberByRemote(remoteId)
            GROUP_MESSAGES -> syncDao.deleteGroupMessageByRemote(remoteId)
            PRAYERS -> syncDao.deletePrayerByRemote(remoteId)
        }
    }

    // ------------------------------------------------------------------
    // Subida: Room -> nube
    // ------------------------------------------------------------------

    suspend fun pushPending() {
        val settings = settingsRepository.settings.first()
        if (!settings.cloudSyncEnabled || !gateway.isAvailable) return
        val root = currentRoot ?: Firebase.database.reference
            .child(CHURCHES)
            .child(settings.cloudChurchId.ifBlank { DEFAULT_CHURCH })

        pushMutex.withLock {
            _status.value = _status.value.copy(syncing = true)
            val result = runCatching {
                pushFamilies(root)
                pushMembers(root)
                pushFunds(root)
                pushGroups(root)
                pushEvents(root)
                pushDonations(root)
                pushAttendance(root)
                pushGroupMembers(root)
                pushGroupMessages(root)
                pushPrayers(root)
                pushTombstones(root)
            }
            _status.value = if (result.isSuccess) {
                _status.value.copy(syncing = false, lastSyncAt = System.currentTimeMillis(), error = null)
            } else {
                Log.w(TAG, "Error subiendo cambios", result.exceptionOrNull())
                _status.value.copy(syncing = false, error = result.exceptionOrNull()?.message)
            }
        }
    }

    private suspend fun upload(root: DatabaseReference, collection: String, key: String, data: Map<String, Any?>) {
        root.child(collection).child(key).setValue(data).await()
    }

    private suspend fun pushFamilies(root: DatabaseReference) {
        syncDao.pendingFamilies().forEach { family ->
            val key = family.remoteId ?: UUID.randomUUID().toString()
            upload(
                root, FAMILIES, key,
                mapOf(
                    "name" to family.name,
                    "address" to family.address,
                    "city" to family.city,
                    "phone" to family.phone,
                    "email" to family.email,
                    "notes" to family.notes,
                    FIELD_UPDATED_AT to family.updatedAt
                )
            )
            syncDao.updateFamily(family.copy(remoteId = key, pendingSync = false))
        }
    }

    private suspend fun pushMembers(root: DatabaseReference) {
        syncDao.pendingMembers().forEach { member ->
            val key = member.remoteId ?: UUID.randomUUID().toString()
            upload(
                root, MEMBERS, key,
                mapOf(
                    "firstName" to member.firstName,
                    "lastName" to member.lastName,
                    "email" to member.email,
                    "phone" to member.phone,
                    "birthDate" to member.birthDate,
                    "gender" to member.gender.name,
                    "maritalStatus" to member.maritalStatus.name,
                    "status" to member.status.name,
                    "churchRole" to member.churchRole,
                    "familyRemoteId" to member.familyId?.let { syncDao.familyRemoteById(it) },
                    "familyRole" to member.familyRole.name,
                    "address" to member.address,
                    "city" to member.city,
                    "baptized" to member.baptized,
                    "baptismDate" to member.baptismDate,
                    "joinedAt" to member.joinedAt,
                    "propertiesCsv" to member.propertiesCsv,
                    "notes" to member.notes,
                    "isChild" to member.isChild,
                    "guardianPhone" to member.guardianPhone,
                    "allowsContact" to member.allowsContact,
                    FIELD_UPDATED_AT to member.updatedAt
                )
            )
            syncDao.updateMember(member.copy(remoteId = key, pendingSync = false))
        }
    }

    private suspend fun pushFunds(root: DatabaseReference) {
        syncDao.pendingFunds().forEach { fund ->
            val key = fund.remoteId ?: UUID.randomUUID().toString()
            upload(
                root, FUNDS, key,
                mapOf(
                    "name" to fund.name,
                    "description" to fund.description,
                    "active" to fund.active,
                    FIELD_UPDATED_AT to fund.updatedAt
                )
            )
            syncDao.updateFund(fund.copy(remoteId = key, pendingSync = false))
        }
    }

    private suspend fun pushGroups(root: DatabaseReference) {
        syncDao.pendingGroups().forEach { group ->
            val key = group.remoteId ?: UUID.randomUUID().toString()
            upload(
                root, GROUPS, key,
                mapOf(
                    "name" to group.name,
                    "type" to group.type.name,
                    "description" to group.description,
                    "leaderRemoteId" to group.leaderId?.let { syncDao.memberRemoteById(it) },
                    "meetingDay" to group.meetingDay,
                    "meetingTime" to group.meetingTime,
                    "location" to group.location,
                    "active" to group.active,
                    FIELD_UPDATED_AT to group.updatedAt
                )
            )
            syncDao.updateGroup(group.copy(remoteId = key, pendingSync = false))
        }
    }

    private suspend fun pushEvents(root: DatabaseReference) {
        syncDao.pendingEvents().forEach { event ->
            val key = event.remoteId ?: UUID.randomUUID().toString()
            upload(
                root, EVENTS, key,
                mapOf(
                    "title" to event.title,
                    "description" to event.description,
                    "type" to event.type.name,
                    "location" to event.location,
                    "startAt" to event.startAt,
                    "endAt" to event.endAt,
                    "allDay" to event.allDay,
                    "recurrence" to event.recurrence,
                    "timezoneId" to event.timezoneId,
                    "requiresCheckIn" to event.requiresCheckIn,
                    "groupRemoteId" to event.groupId?.let { syncDao.groupRemoteById(it) },
                    "capacity" to event.capacity,
                    "reminderMinutesBefore" to event.reminderMinutesBefore,
                    "createdBy" to event.createdBy,
                    FIELD_UPDATED_AT to event.updatedAt
                )
            )
            syncDao.updateEvent(event.copy(remoteId = key, pendingSync = false))
        }
    }

    private suspend fun pushDonations(root: DatabaseReference) {
        syncDao.pendingDonations().forEach { donation ->
            val key = donation.remoteId ?: UUID.randomUUID().toString()
            upload(
                root, DONATIONS, key,
                mapOf(
                    "memberRemoteId" to donation.memberId?.let { syncDao.memberRemoteById(it) },
                    "anonymous" to donation.anonymous,
                    "amount" to donation.amount,
                    "type" to donation.type.name,
                    "method" to donation.method.name,
                    "fundRemoteId" to donation.fundId?.let { syncDao.fundRemoteById(it) },
                    "envelopeNumber" to donation.envelopeNumber,
                    "date" to donation.date,
                    "reference" to donation.reference,
                    "note" to donation.note,
                    "createdBy" to donation.createdBy,
                    FIELD_UPDATED_AT to donation.updatedAt
                )
            )
            syncDao.updateDonation(donation.copy(remoteId = key, pendingSync = false))
        }
    }

    private suspend fun pushAttendance(root: DatabaseReference) {
        syncDao.pendingAttendance().forEach { attendance ->
            val eventRemote = syncDao.eventRemoteById(attendance.eventId) ?: return@forEach
            val memberRemote = syncDao.memberRemoteById(attendance.memberId) ?: return@forEach
            val key = attendance.remoteId ?: UUID.randomUUID().toString()
            upload(
                root, ATTENDANCE, key,
                mapOf(
                    "eventRemoteId" to eventRemote,
                    "memberRemoteId" to memberRemote,
                    "present" to attendance.present,
                    "registeredAt" to attendance.registeredAt,
                    "note" to attendance.note,
                    FIELD_UPDATED_AT to attendance.updatedAt
                )
            )
            syncDao.updateAttendance(attendance.copy(remoteId = key, pendingSync = false))
        }
    }

    private suspend fun pushGroupMembers(root: DatabaseReference) {
        syncDao.pendingGroupMembers().forEach { membership ->
            val groupRemote = syncDao.groupRemoteById(membership.groupId) ?: return@forEach
            val memberRemote = syncDao.memberRemoteById(membership.memberId) ?: return@forEach
            val key = membership.remoteId ?: UUID.randomUUID().toString()
            upload(
                root, GROUP_MEMBERS, key,
                mapOf(
                    "groupRemoteId" to groupRemote,
                    "memberRemoteId" to memberRemote,
                    "role" to membership.role.name,
                    "joinedAt" to membership.joinedAt,
                    FIELD_UPDATED_AT to membership.updatedAt
                )
            )
            syncDao.updateGroupMember(membership.copy(remoteId = key, pendingSync = false))
        }
    }

    private suspend fun pushGroupMessages(root: DatabaseReference) {
        syncDao.pendingGroupMessages().forEach { message ->
            val groupRemote = syncDao.groupRemoteById(message.groupId) ?: return@forEach
            val key = message.remoteId ?: UUID.randomUUID().toString()
            upload(
                root, GROUP_MESSAGES, key,
                mapOf(
                    "groupRemoteId" to groupRemote,
                    "authorName" to message.authorName,
                    "content" to message.content,
                    "createdAt" to message.createdAt,
                    FIELD_UPDATED_AT to message.updatedAt
                )
            )
            syncDao.updateGroupMessage(message.copy(remoteId = key, pendingSync = false))
        }
    }

    private suspend fun pushPrayers(root: DatabaseReference) {
        syncDao.pendingPrayers().forEach { prayer ->
            val key = prayer.remoteId ?: UUID.randomUUID().toString()
            upload(
                root, PRAYERS, key,
                mapOf(
                    "title" to prayer.title,
                    "detail" to prayer.detail,
                    "requesterName" to prayer.requesterName,
                    "isPrivate" to prayer.isPrivate,
                    "status" to prayer.status.name,
                    "prayerCount" to prayer.prayerCount,
                    "createdAt" to prayer.createdAt,
                    "answeredAt" to prayer.answeredAt,
                    "answerNote" to prayer.answerNote,
                    FIELD_UPDATED_AT to prayer.updatedAt
                )
            )
            syncDao.updatePrayer(prayer.copy(remoteId = key, pendingSync = false))
        }
    }

    private suspend fun pushTombstones(root: DatabaseReference) {
        syncDao.pendingTombstones().forEach { tombstone ->
            upload(
                root, tombstone.collection, tombstone.remoteId,
                mapOf(FIELD_DELETED to true, FIELD_UPDATED_AT to tombstone.deletedAt)
            )
            syncDao.markTombstoneSynced(tombstone.id)
        }
    }

    companion object {
        private const val TAG = "RealtimeSync"
        private const val CHURCHES = "churches"
        private const val PROBE_TIMEOUT_MS = 10_000L
        private const val RETRY_INTERVAL_MS = 60_000L
        const val DEFAULT_CHURCH = "principal"
        const val FIELD_UPDATED_AT = "updatedAt"
        const val FIELD_DELETED = "deleted"

        const val FAMILIES = "families"
        const val MEMBERS = "members"
        const val FUNDS = "funds"
        const val DONATIONS = "donations"
        const val EVENTS = "events"
        const val ATTENDANCE = "attendance"
        const val GROUPS = "groups"
        const val GROUP_MEMBERS = "group_members"
        const val GROUP_MESSAGES = "group_messages"
        const val PRAYERS = "prayers"

        val COLLECTIONS = listOf(
            FAMILIES, MEMBERS, FUNDS, GROUPS, EVENTS,
            DONATIONS, ATTENDANCE, GROUP_MEMBERS, GROUP_MESSAGES, PRAYERS
        )
    }
}

// ---------------------------------------------------------------------
// Lectura defensiva de los mapas que llegan de la nube
// ---------------------------------------------------------------------

private fun Map<String, Any?>.str(key: String, default: String = ""): String =
    (this[key] as? String) ?: default

private fun Map<String, Any?>.strOrNull(key: String): String? = this[key] as? String

private fun Map<String, Any?>.long(key: String, default: Long = 0L): Long =
    (this[key] as? Number)?.toLong() ?: default

private fun Map<String, Any?>.longOrNull(key: String): Long? = (this[key] as? Number)?.toLong()

private fun Map<String, Any?>.int(key: String, default: Int = 0): Int =
    (this[key] as? Number)?.toInt() ?: default

private fun Map<String, Any?>.intOrNull(key: String): Int? = (this[key] as? Number)?.toInt()

private fun Map<String, Any?>.dbl(key: String, default: Double = 0.0): Double =
    (this[key] as? Number)?.toDouble() ?: default

private fun Map<String, Any?>.bool(key: String, default: Boolean = false): Boolean =
    (this[key] as? Boolean) ?: default

private inline fun <reified T : Enum<T>> Map<String, Any?>.enum(key: String, default: T): T =
    runCatching { enumValueOf<T>(str(key, default.name)) }.getOrDefault(default)
