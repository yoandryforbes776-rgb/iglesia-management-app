package com.iglesiaflow.gestion.core.security

import java.nio.ByteBuffer
import java.security.SecureRandom
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlin.experimental.and

/**
 * Generador TOTP (RFC 6238) para el segundo factor de administradores.
 * Compatible con Google Authenticator / Authy (SHA1, 6 dígitos, 30 s).
 */
object TotpGenerator {

    private const val BASE32 = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567"
    private const val DIGITS = 6
    private const val PERIOD_SECONDS = 30L

    fun newSecret(length: Int = 16): String {
        val random = SecureRandom()
        return (1..length).map { BASE32[random.nextInt(BASE32.length)] }.joinToString("")
    }

    fun otpAuthUri(secret: String, account: String, issuer: String = "IglesiaFlow"): String =
        "otpauth://totp/$issuer:$account?secret=$secret&issuer=$issuer&algorithm=SHA1&digits=$DIGITS&period=$PERIOD_SECONDS"

    fun code(secret: String, timeMillis: Long = System.currentTimeMillis()): String {
        val counter = timeMillis / 1000L / PERIOD_SECONDS
        val key = SecretKeySpec(base32Decode(secret), "HmacSHA1")
        val mac = Mac.getInstance("HmacSHA1")
        mac.init(key)
        val data = ByteBuffer.allocate(8).putLong(counter).array()
        val hmac = mac.doFinal(data)
        val offset = (hmac[hmac.size - 1] and 0x0f).toInt()
        val binary = ((hmac[offset].toInt() and 0x7f) shl 24) or
            ((hmac[offset + 1].toInt() and 0xff) shl 16) or
            ((hmac[offset + 2].toInt() and 0xff) shl 8) or
            (hmac[offset + 3].toInt() and 0xff)
        val otp = binary % 1_000_000
        return otp.toString().padStart(DIGITS, '0')
    }

    /** Acepta la ventana anterior y siguiente para tolerar desfases de reloj. */
    fun verify(secret: String, candidate: String, timeMillis: Long = System.currentTimeMillis()): Boolean {
        if (candidate.isBlank()) return false
        val clean = candidate.trim()
        return (-1..1).any { window ->
            code(secret, timeMillis + window * PERIOD_SECONDS * 1000L) == clean
        }
    }

    private fun base32Decode(secret: String): ByteArray {
        val clean = secret.trim().replace("=", "").uppercase()
        var buffer = 0
        var bitsLeft = 0
        val output = ArrayList<Byte>()
        for (char in clean) {
            val value = BASE32.indexOf(char)
            if (value < 0) continue
            buffer = (buffer shl 5) or value
            bitsLeft += 5
            if (bitsLeft >= 8) {
                output.add(((buffer shr (bitsLeft - 8)) and 0xff).toByte())
                bitsLeft -= 8
            }
        }
        return output.toByteArray()
    }
}
