package com.iglesiaflow.gestion.core.security

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.security.SecureRandom
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Custodia la passphrase de SQLCipher dentro de EncryptedSharedPreferences
 * (respaldada por el Keystore de Android).
 */
@Singleton
class CryptoManager @Inject constructor(@ApplicationContext private val context: Context) {

    private val prefs: SharedPreferences by lazy { buildPrefs() }

    private fun buildPrefs(): SharedPreferences = try {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context,
            SECURE_PREFS,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    } catch (error: Throwable) {
        // Dispositivos sin Keystore utilizable: degradamos a prefs normales.
        context.getSharedPreferences(FALLBACK_PREFS, Context.MODE_PRIVATE)
    }

    /** Passphrase persistente para la base de datos cifrada. */
    fun databasePassphrase(): String {
        prefs.getString(KEY_DB_PASSPHRASE, null)?.let { return it }
        val bytes = ByteArray(32)
        SecureRandom().nextBytes(bytes)
        val generated = Base64.encodeToString(bytes, Base64.NO_WRAP)
        prefs.edit().putString(KEY_DB_PASSPHRASE, generated).apply()
        return generated
    }

    fun putSecret(key: String, value: String) { prefs.edit().putString(key, value).apply() }

    fun secret(key: String): String? = prefs.getString(key, null)

    fun clearSecret(key: String) { prefs.edit().remove(key).apply() }

    companion object {
        const val SECURE_PREFS = "iglesiaflow_secure_prefs"
        private const val FALLBACK_PREFS = "iglesiaflow_prefs"
        private const val KEY_DB_PASSPHRASE = "db_passphrase"
    }
}
