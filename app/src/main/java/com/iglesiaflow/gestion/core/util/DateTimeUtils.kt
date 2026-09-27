package com.iglesiaflow.gestion.core.util

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

object DateTimeUtils {

    private val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    private val dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    private val monthFormatter = DateTimeFormatter.ofPattern("MMMM yyyy")

    fun formatDate(millis: Long?): String = millis?.let {
        dateFormatter.format(localDateTime(it))
    } ?: "—"

    fun formatDateTime(millis: Long?): String = millis?.let {
        dateTimeFormatter.format(localDateTime(it))
    } ?: "—"

    fun formatTime(millis: Long?): String = millis?.let {
        timeFormatter.format(localDateTime(it))
    } ?: "—"

    fun formatMonth(millis: Long): String =
        monthFormatter.format(localDateTime(millis)).replaceFirstChar { it.uppercase() }

    fun formatPeriod(period: String): String = runCatching {
        val parts = period.split("-")
        val month = java.time.Month.of(parts[1].toInt())
            .getDisplayName(java.time.format.TextStyle.SHORT, Locale.getDefault())
        "${month.replaceFirstChar { it.uppercase() }} ${parts[0].takeLast(2)}"
    }.getOrDefault(period)

    fun localDateTime(millis: Long): LocalDateTime =
        Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDateTime()

    fun toMillis(date: LocalDate): Long =
        date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

    fun toMillis(dateTime: LocalDateTime): Long =
        dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

    fun startOfToday(): Long = toMillis(LocalDate.now())

    fun startOfMonth(): Long = toMillis(LocalDate.now().withDayOfMonth(1))

    fun startOfYear(): Long = toMillis(LocalDate.now().withDayOfYear(1))

    fun monthsAgo(months: Long): Long = toMillis(LocalDate.now().minusMonths(months).withDayOfMonth(1))

    fun endOfToday(): Long = toMillis(LocalDate.now().plusDays(1)) - 1

    fun currentYear(): Int = LocalDate.now().year

    fun currentMonth(): Int = LocalDate.now().monthValue

    fun age(birthMillis: Long?): Int? = birthMillis?.let {
        ChronoUnit.YEARS.between(localDateTime(it).toLocalDate(), LocalDate.now()).toInt()
    }

    fun daysUntilBirthday(birthMillis: Long?): Long? = birthMillis?.let {
        val birth = localDateTime(it).toLocalDate()
        val today = LocalDate.now()
        var next = birth.withYear(today.year)
        if (next.isBefore(today)) next = next.plusYears(1)
        ChronoUnit.DAYS.between(today, next)
    }

    fun nextOccurrence(startAt: Long, recurrence: String): Long {
        val date = localDateTime(startAt)
        val next = when (recurrence.uppercase()) {
            "DAILY" -> date.plusDays(1)
            "WEEKLY" -> date.plusWeeks(1)
            "BIWEEKLY" -> date.plusWeeks(2)
            "MONTHLY" -> date.plusMonths(1)
            else -> return startAt
        }
        return toMillis(next)
    }
}
