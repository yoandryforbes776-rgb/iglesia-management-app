package com.iglesiaflow.gestion.data.repository

import android.content.Context
import android.net.Uri
import com.iglesiaflow.gestion.core.audit.AuditLogger
import com.iglesiaflow.gestion.data.local.AppDatabase
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Copia de seguridad y restauración completa de la base de datos
 * (incluye el contenido cifrado tal cual está en disco).
 */
@Singleton
class BackupManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: AppDatabase,
    private val auditLogger: AuditLogger
) {

    private fun backupsDir(): File = File(context.filesDir, "backups").apply { mkdirs() }

    fun listBackups(): List<File> =
        backupsDir().listFiles()?.filter { it.extension == "db" }?.sortedByDescending { it.lastModified() }.orEmpty()

    fun createBackup(): File {
        database.openHelper.writableDatabase // asegura que exista y esté inicializada
        checkpoint()
        val source = context.getDatabasePath(AppDatabase.NAME)
        val stamp = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss").format(LocalDateTime.now())
        val target = File(backupsDir(), "iglesiaflow_backup_$stamp.db")
        source.copyTo(target, overwrite = true)
        auditLogger.log("BACKUP_CREADO", "database", null, target.name)
        return target
    }

    /** Restaura un backup; la app debe reiniciarse después. */
    fun restoreBackup(file: File): Boolean = runCatching {
        checkpoint()
        database.close()
        val target = context.getDatabasePath(AppDatabase.NAME)
        file.copyTo(target, overwrite = true)
        File(target.path + "-wal").takeIf { it.exists() }?.delete()
        File(target.path + "-shm").takeIf { it.exists() }?.delete()
        auditLogger.log("BACKUP_RESTAURADO", "database", null, file.name)
        true
    }.getOrDefault(false)

    fun importBackupFrom(uri: Uri): File? = runCatching {
        val stamp = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss").format(LocalDateTime.now())
        val target = File(backupsDir(), "importado_$stamp.db")
        context.contentResolver.openInputStream(uri)?.use { input ->
            target.outputStream().use { output -> input.copyTo(output) }
        }
        target
    }.getOrNull()

    fun deleteBackup(file: File): Boolean = file.delete()

    private fun checkpoint() = runCatching {
        database.openHelper.writableDatabase.query("PRAGMA wal_checkpoint(FULL)").use { it.moveToFirst() }
    }
}
