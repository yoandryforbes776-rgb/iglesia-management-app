package com.iglesiaflow.gestion.ui.screens.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iglesiaflow.gestion.core.config.AppSettings
import com.iglesiaflow.gestion.core.config.SettingsRepository
import com.iglesiaflow.gestion.core.locale.LocaleManager
import com.iglesiaflow.gestion.domain.model.AppModule
import com.iglesiaflow.gestion.domain.model.ThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val localeManager: LocaleManager
) : ViewModel() {

    val settings: StateFlow<AppSettings> = settingsRepository.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppSettings())

    private fun update(transform: (AppSettings) -> AppSettings) {
        viewModelScope.launch { settingsRepository.update(transform) }
    }

    fun setChurchName(value: String) = update { it.copy(churchName = value) }
    fun setMotto(value: String) = update { it.copy(churchMotto = value) }
    fun setLogo(uri: String?) = update { it.copy(logoUri = uri) }
    fun setPrimaryColor(argb: Long) = update { it.copy(primaryColorArgb = argb) }
    fun setSecondaryColor(argb: Long) = update { it.copy(secondaryColorArgb = argb) }
    fun setDynamicColor(enabled: Boolean) = update { it.copy(useDynamicColor = enabled) }
    fun setThemeMode(mode: ThemeMode) = update { it.copy(themeMode = mode) }
    fun setCurrency(code: String) = update { it.copy(currencyCode = code.uppercase()) }
    fun setKioskMode(enabled: Boolean) = update { it.copy(kioskModeEnabled = enabled) }
    fun setCloudSync(enabled: Boolean) = update { it.copy(cloudSyncEnabled = enabled) }
    fun setRequireTwoFactor(enabled: Boolean) = update { it.copy(requireTwoFactorForAdmins = enabled) }
    fun setSessionTimeout(minutes: Int) = update { it.copy(sessionTimeoutMinutes = minutes) }
    fun setPrivacyConsent(accepted: Boolean) = update { it.copy(privacyConsentAccepted = accepted) }

    fun setNotification(key: String, enabled: Boolean) = update {
        when (key) {
            "events" -> it.copy(notifyEvents = enabled)
            "birthdays" -> it.copy(notifyBirthdays = enabled)
            "checkin" -> it.copy(notifyCheckIn = enabled)
            "donations" -> it.copy(notifyDonations = enabled)
            else -> it.copy(notifyPrayer = enabled)
        }
    }

    fun toggleModule(module: AppModule, enabled: Boolean) {
        viewModelScope.launch { settingsRepository.toggleModule(module, enabled) }
    }

    fun setLanguage(tag: String) {
        viewModelScope.launch {
            settingsRepository.update { it.copy(languageTag = tag) }
            localeManager.apply(tag)
        }
    }
}
