package com.iglesiaflow.gestion.core.util

import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

/** Exportación de reportes a CSV y PDF + compartición mediante FileProvider. */
@Singleton
class ExportManager @Inject constructor(@ApplicationContext private val context: Context) {

    private fun exportsDir(): File = File(context.filesDir, "exports").apply { mkdirs() }

    private fun stamp(): String =
        DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss").format(LocalDateTime.now())

    fun writeCsv(baseName: String, headers: List<String>, rows: List<List<String>>): File {
        val file = File(exportsDir(), "${baseName}_${stamp()}.csv")
        val content = buildString {
            append(headers.joinToString(";") { escape(it) })
            append('\n')
            rows.forEach { row ->
                append(row.joinToString(";") { escape(it) })
                append('\n')
            }
        }
        file.writeText(content, Charsets.UTF_8)
        return file
    }

    fun writePdf(
        baseName: String,
        title: String,
        subtitle: String,
        headers: List<String>,
        rows: List<List<String>>
    ): File {
        val file = File(exportsDir(), "${baseName}_${stamp()}.pdf")
        val document = PdfDocument()
        val pageWidth = 595
        val pageHeight = 842
        val margin = 32f
        val titlePaint = Paint().apply { textSize = 18f; isFakeBoldText = true }
        val subtitlePaint = Paint().apply { textSize = 11f; color = 0xFF555555.toInt() }
        val headerPaint = Paint().apply { textSize = 10f; isFakeBoldText = true }
        val bodyPaint = Paint().apply { textSize = 10f }
        val columnWidth = (pageWidth - margin * 2) / headers.size.coerceAtLeast(1)

        var pageNumber = 1
        var page = document.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create())
        var canvas = page.canvas
        var y = margin + 20f
        canvas.drawText(title, margin, y, titlePaint)
        y += 18f
        canvas.drawText(subtitle, margin, y, subtitlePaint)
        y += 24f
        headers.forEachIndexed { index, header ->
            canvas.drawText(header.take(22), margin + columnWidth * index, y, headerPaint)
        }
        y += 14f

        rows.forEach { row ->
            if (y > pageHeight - margin) {
                document.finishPage(page)
                pageNumber += 1
                page = document.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create())
                canvas = page.canvas
                y = margin + 20f
                headers.forEachIndexed { index, header ->
                    canvas.drawText(header.take(22), margin + columnWidth * index, y, headerPaint)
                }
                y += 14f
            }
            row.forEachIndexed { index, cell ->
                canvas.drawText(cell.take(24), margin + columnWidth * index, y, bodyPaint)
            }
            y += 14f
        }
        document.finishPage(page)
        file.outputStream().use { document.writeTo(it) }
        document.close()
        return file
    }

    fun uriFor(file: File): Uri =
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)

    fun shareIntent(file: File, mimeType: String): Intent {
        val uri = uriFor(file)
        return Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    fun share(file: File) {
        val mime = if (file.extension.equals("pdf", true)) "application/pdf" else "text/csv"
        context.startActivity(Intent.createChooser(shareIntent(file, mime), "Compartir ${file.name}").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
    }

    private fun escape(value: String): String =
        if (value.contains(';') || value.contains('"') || value.contains('\n')) {
            "\"" + value.replace("\"", "\"\"") + "\""
        } else value
}
