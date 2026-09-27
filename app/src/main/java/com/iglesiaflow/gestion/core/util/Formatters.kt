package com.iglesiaflow.gestion.core.util

import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

object Formatters {

    fun money(amount: Double, currencyCode: String = "EUR"): String = runCatching {
        NumberFormat.getCurrencyInstance(Locale.getDefault()).apply {
            currency = Currency.getInstance(currencyCode)
            maximumFractionDigits = 2
        }.format(amount)
    }.getOrDefault(String.format(Locale.getDefault(), "%.2f %s", amount, currencyCode))

    fun percent(value: Double): String = String.format(Locale.getDefault(), "%.0f%%", value * 100)

    fun decimal(value: Double): String = String.format(Locale.getDefault(), "%.1f", value)

    fun initials(name: String): String = name.trim().split(" ")
        .filter { it.isNotBlank() }
        .take(2)
        .joinToString("") { it.first().uppercase() }
        .ifBlank { "?" }
}
