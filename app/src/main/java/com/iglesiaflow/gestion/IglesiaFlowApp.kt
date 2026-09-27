package com.iglesiaflow.gestion

import android.app.Application
import com.iglesiaflow.gestion.core.config.SettingsRepository
import com.iglesiaflow.gestion.core.locale.LocaleManager
import com.iglesiaflow.gestion.core.util.NotificationHelper
import com.iglesiaflow.gestion.data.local.DatabaseSeeder
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

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        notificationHelper.createChannels()
        scope.launch {
            runCatching { seeder.seedIfNeeded() }
            val settings = settingsRepository.settings.first()
            kotlinx.coroutines.withContext(Dispatchers.Main) {
                localeManager.apply(settings.languageTag)
            }
        }
    }
}
