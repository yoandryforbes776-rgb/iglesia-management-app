package com.iglesiaflow.gestion

import android.app.Application
import com.iglesiaflow.gestion.core.config.SettingsRepository
import com.iglesiaflow.gestion.core.locale.LocaleManager
import com.iglesiaflow.gestion.core.util.NotificationHelper
import com.google.firebase.FirebaseApp
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.iglesiaflow.gestion.data.local.DatabaseSeeder
import com.iglesiaflow.gestion.data.remote.RealtimeSyncManager
import com.iglesiaflow.gestion.data.repository.AttendanceRepository
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class IglesiaFlowApp : Application() {

    @Inject lateinit var seeder: DatabaseSeeder
    @Inject lateinit var notificationHelper: NotificationHelper
    @Inject lateinit var settingsRepository: SettingsRepository
    @Inject lateinit var localeManager: LocaleManager
    @Inject lateinit var realtimeSyncManager: RealtimeSyncManager
    @Inject lateinit var attendanceRepository: AttendanceRepository

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        notificationHelper.createChannels()
        enableRealtimeCache()
        realtimeSyncManager.bind()
        scope.launch {
            runCatching { seeder.seedIfNeeded() }
            // Aviso a los líderes si alguien acumula faltas seguidas.
            runCatching {
                val alerts = attendanceRepository.absenceAlerts().first()
                attendanceRepository.notifyLeaders(alerts)
            }
            val settings = settingsRepository.settings.first()
            kotlinx.coroutines.withContext(Dispatchers.Main) {
                localeManager.apply(settings.languageTag)
            }
        }
    }

    /**
     * Cache en disco de Realtime Database: permite seguir leyendo y escribiendo
     * sin conexión; los cambios se envían solos al recuperar cobertura.
     */
    private fun enableRealtimeCache() {
        runCatching {
            if (FirebaseApp.getApps(this).isNotEmpty()) {
                Firebase.database.setPersistenceEnabled(true)
            }
        }
    }
}
