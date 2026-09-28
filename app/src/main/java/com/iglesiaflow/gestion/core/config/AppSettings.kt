package com.iglesiaflow.gestion.core.config

import com.iglesiaflow.gestion.domain.model.AppModule
import com.iglesiaflow.gestion.domain.model.ThemeMode

/**
 * Configuración modificable en caliente (sin recompilar la APK).
 * Persistida en DataStore y observada por toda la UI.
 */
data class AppSettings(
    val churchName: String = "Iglesia Central",
    val churchMotto: String = "Gestión y control con excelencia",
    val logoUri: String? = null,
    val primaryColorArgb: Long = 0xFF4C57A9,
    val secondaryColorArgb: Long = 0xFF5C5D72,
    val useDynamicColor: Boolean = true,
    val themeMode: ThemeMode = ThemeMode.SISTEMA,
    val languageTag: String = LANGUAGE_SYSTEM,
    val currencyCode: String = "EUR",
    val enabledModules: Set<String> = AppModule.entries.map { it.key }.toSet(),
    val notifyEvents: Boolean = true,
    val notifyBirthdays: Boolean = true,
    val notifyCheckIn: Boolean = true,
    val notifyDonations: Boolean = false,
    val notifyPrayer: Boolean = true,
    val cloudSyncEnabled: Boolean = false,
    /** Nodo compartido en la nube; todos los dispositivos de la misma iglesia usan el mismo código. */
    val cloudChurchId: String = "principal",
    /** URL de la Realtime Database; vacío = la que indique google-services.json. */
    val cloudDatabaseUrl: String = "",
    val requireTwoFactorForAdmins: Boolean = true,
    val sessionTimeoutMinutes: Int = 30,
    val privacyConsentAccepted: Boolean = false,
    val kioskModeEnabled: Boolean = false,
    val lastBackupAt: Long? = null
) {
    fun isModuleEnabled(module: AppModule): Boolean =
        module in AppModule.alwaysOn || enabledModules.contains(module.key)

    companion object {
        const val LANGUAGE_SYSTEM = "system"
    }
}
