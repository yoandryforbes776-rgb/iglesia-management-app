package com.iglesiaflow.gestion.data.repository

import android.content.Context
import android.net.Uri
import com.iglesiaflow.gestion.core.util.DateTimeUtils
import com.iglesiaflow.gestion.data.local.entity.MemberEntity
import com.iglesiaflow.gestion.domain.model.Gender
import com.iglesiaflow.gestion.domain.model.MemberStatus
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

data class ImportResult(val imported: Int, val skipped: Int, val errors: List<String>)

/**
 * Importación de miembros desde CSV (compatible con exportaciones de ChurchCRM
 * y con hojas de cálculo separadas por ';' o ',').
 */
@Singleton
class CsvImporter @Inject constructor(@ApplicationContext private val context: Context) {

    fun parseMembers(uri: Uri): Pair<List<MemberEntity>, List<String>> {
        val text = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
            ?: return emptyList<MemberEntity>() to listOf("No se pudo leer el archivo")
        return parseMembersFromText(text)
    }

    fun parseMembersFromText(text: String): Pair<List<MemberEntity>, List<String>> {
        val errors = mutableListOf<String>()
        val lines = text.lines().filter { it.isNotBlank() }
        if (lines.isEmpty()) return emptyList<MemberEntity>() to listOf("Archivo vacío")

        val separator = if (lines.first().count { it == ';' } >= lines.first().count { it == ',' }) ';' else ','
        val headers = splitLine(lines.first(), separator).map { it.trim().lowercase() }
        val members = mutableListOf<MemberEntity>()

        lines.drop(1).forEachIndexed { index, line ->
            val cells = splitLine(line, separator)
            runCatching {
                fun value(vararg keys: String): String {
                    keys.forEach { key ->
                        val position = headers.indexOfFirst { it == key || it.contains(key) }
                        if (position >= 0 && position < cells.size) return cells[position].trim()
                    }
                    return ""
                }
                val firstName = value("nombre", "firstname", "first name")
                val lastName = value("apellido", "lastname", "last name")
                if (firstName.isBlank() && lastName.isBlank()) {
                    errors.add("Línea ${index + 2}: sin nombre")
                    return@runCatching
                }
                members.add(
                    MemberEntity(
                        firstName = firstName,
                        lastName = lastName,
                        email = value("email", "correo"),
                        phone = value("phone", "telefono", "teléfono", "móvil"),
                        birthDate = parseDate(value("birth", "nacimiento", "fecha")),
                        gender = when (value("gender", "genero", "género").lowercase().firstOrNull()) {
                            'm', 'h' -> Gender.MASCULINO
                            'f' -> Gender.FEMENINO
                            else -> Gender.NO_ESPECIFICA
                        },
                        status = MemberStatus.ACTIVO,
                        address = value("address", "direccion", "dirección"),
                        city = value("city", "ciudad")
                    )
                )
            }.onFailure { errors.add("Línea ${index + 2}: ${it.message}") }
        }
        return members to errors
    }

    private fun splitLine(line: String, separator: Char): List<String> {
        val result = mutableListOf<String>()
        val current = StringBuilder()
        var inQuotes = false
        line.forEach { char ->
            when {
                char == '"' -> inQuotes = !inQuotes
                char == separator && !inQuotes -> { result.add(current.toString()); current.clear() }
                else -> current.append(char)
            }
        }
        result.add(current.toString())
        return result
    }

    private fun parseDate(raw: String): Long? {
        if (raw.isBlank()) return null
        val patterns = listOf("yyyy-MM-dd", "dd/MM/yyyy", "MM/dd/yyyy", "dd-MM-yyyy")
        patterns.forEach { pattern ->
            runCatching {
                return DateTimeUtils.toMillis(LocalDate.parse(raw, DateTimeFormatter.ofPattern(pattern)))
            }
        }
        return null
    }
}
