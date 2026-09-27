package com.iglesiaflow.gestion.core.locale

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.iglesiaflow.gestion.core.config.AppSettings
import javax.inject.Inject
import javax.inject.Singleton

/** Aplica el idioma elegido en Administración o el del dispositivo. */
@Singleton
class LocaleManager @Inject constructor() {

    fun apply(languageTag: String) {
        val locales = if (languageTag == AppSettings.LANGUAGE_SYSTEM) {
            LocaleListCompat.getEmptyLocaleList()
        } else {
            LocaleListCompat.forLanguageTags(languageTag)
        }
        AppCompatDelegate.setApplicationLocales(locales)
    }

    val supportedLanguages: List<Pair<String, String>> = listOf(
        AppSettings.LANGUAGE_SYSTEM to "Automático (sistema)",
        "es" to "Español",
        "en" to "English",
        "pt" to "Português"
    )
}
