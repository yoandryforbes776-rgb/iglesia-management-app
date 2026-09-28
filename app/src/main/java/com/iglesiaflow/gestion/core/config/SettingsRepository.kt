package com.iglesiaflow.gestion.core.config

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.iglesiaflow.gestion.domain.model.AppModule
import com.iglesiaflow.gestion.domain.model.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "iglesiaflow_settings")

@Singleton
class SettingsRepository @Inject constructor(@ApplicationContext private val context: Context) {

    val settings: Flow<AppSettings> = context.settingsDataStore.data.map { prefs -> prefs.toSettings() }

    suspend fun update(transform: (AppSettings) -> AppSettings) {
        context.settingsDataStore.edit { prefs ->
            val updated = transform(prefs.toSettings())
            prefs[Keys.churchName] = updated.churchName
            prefs[Keys.churchMotto] = updated.churchMotto
            updated.logoUri?.let { prefs[Keys.logoUri] = it } ?: prefs.remove(Keys.logoUri)
            prefs[Keys.primaryColor] = updated.primaryColorArgb
            prefs[Keys.secondaryColor] = updated.secondaryColorArgb
            prefs[Keys.dynamicColor] = updated.useDynamicColor
            prefs[Keys.themeMode] = updated.themeMode.name
            prefs[Keys.language] = updated.languageTag
            prefs[Keys.currency] = updated.currencyCode
            prefs[Keys.modules] = updated.enabledModules
            prefs[Keys.notifyEvents] = updated.notifyEvents
            prefs[Keys.notifyBirthdays] = updated.notifyBirthdays
            prefs[Keys.notifyCheckIn] = updated.notifyCheckIn
            prefs[Keys.notifyDonations] = updated.notifyDonations
            prefs[Keys.notifyPrayer] = updated.notifyPrayer
            prefs[Keys.cloudSync] = updated.cloudSyncEnabled
            prefs[Keys.cloudChurchId] = updated.cloudChurchId
            prefs[Keys.require2fa] = updated.requireTwoFactorForAdmins
            prefs[Keys.sessionTimeout] = updated.sessionTimeoutMinutes
            prefs[Keys.privacyConsent] = updated.privacyConsentAccepted
            prefs[Keys.kioskMode] = updated.kioskModeEnabled
            updated.lastBackupAt?.let { prefs[Keys.lastBackup] = it }
        }
    }

    suspend fun toggleModule(module: AppModule, enabled: Boolean) = update { current ->
        if (module in AppModule.alwaysOn) return@update current
        val modules = current.enabledModules.toMutableSet()
        if (enabled) modules.add(module.key) else modules.remove(module.key)
        current.copy(enabledModules = modules)
    }

    private fun Preferences.toSettings(): AppSettings {
        val defaults = AppSettings()
        return AppSettings(
            churchName = this[Keys.churchName] ?: defaults.churchName,
            churchMotto = this[Keys.churchMotto] ?: defaults.churchMotto,
            logoUri = this[Keys.logoUri],
            primaryColorArgb = this[Keys.primaryColor] ?: defaults.primaryColorArgb,
            secondaryColorArgb = this[Keys.secondaryColor] ?: defaults.secondaryColorArgb,
            useDynamicColor = this[Keys.dynamicColor] ?: defaults.useDynamicColor,
            themeMode = runCatching { ThemeMode.valueOf(this[Keys.themeMode] ?: "") }.getOrDefault(defaults.themeMode),
            languageTag = this[Keys.language] ?: defaults.languageTag,
            currencyCode = this[Keys.currency] ?: defaults.currencyCode,
            enabledModules = this[Keys.modules] ?: defaults.enabledModules,
            notifyEvents = this[Keys.notifyEvents] ?: defaults.notifyEvents,
            notifyBirthdays = this[Keys.notifyBirthdays] ?: defaults.notifyBirthdays,
            notifyCheckIn = this[Keys.notifyCheckIn] ?: defaults.notifyCheckIn,
            notifyDonations = this[Keys.notifyDonations] ?: defaults.notifyDonations,
            notifyPrayer = this[Keys.notifyPrayer] ?: defaults.notifyPrayer,
            cloudSyncEnabled = this[Keys.cloudSync] ?: defaults.cloudSyncEnabled,
            cloudChurchId = this[Keys.cloudChurchId] ?: defaults.cloudChurchId,
            requireTwoFactorForAdmins = this[Keys.require2fa] ?: defaults.requireTwoFactorForAdmins,
            sessionTimeoutMinutes = this[Keys.sessionTimeout] ?: defaults.sessionTimeoutMinutes,
            privacyConsentAccepted = this[Keys.privacyConsent] ?: defaults.privacyConsentAccepted,
            kioskModeEnabled = this[Keys.kioskMode] ?: defaults.kioskModeEnabled,
            lastBackupAt = this[Keys.lastBackup]
        )
    }

    private object Keys {
        val churchName = stringPreferencesKey("church_name")
        val churchMotto = stringPreferencesKey("church_motto")
        val logoUri = stringPreferencesKey("logo_uri")
        val primaryColor = longPreferencesKey("primary_color")
        val secondaryColor = longPreferencesKey("secondary_color")
        val dynamicColor = booleanPreferencesKey("dynamic_color")
        val themeMode = stringPreferencesKey("theme_mode")
        val language = stringPreferencesKey("language")
        val currency = stringPreferencesKey("currency")
        val modules = stringSetPreferencesKey("enabled_modules")
        val notifyEvents = booleanPreferencesKey("notify_events")
        val notifyBirthdays = booleanPreferencesKey("notify_birthdays")
        val notifyCheckIn = booleanPreferencesKey("notify_checkin")
        val notifyDonations = booleanPreferencesKey("notify_donations")
        val notifyPrayer = booleanPreferencesKey("notify_prayer")
        val cloudSync = booleanPreferencesKey("cloud_sync")
        val cloudChurchId = stringPreferencesKey("cloud_church_id")
        val require2fa = booleanPreferencesKey("require_2fa")
        val sessionTimeout = intPreferencesKey("session_timeout")
        val privacyConsent = booleanPreferencesKey("privacy_consent")
        val kioskMode = booleanPreferencesKey("kiosk_mode")
        val lastBackup = longPreferencesKey("last_backup")
    }
}
