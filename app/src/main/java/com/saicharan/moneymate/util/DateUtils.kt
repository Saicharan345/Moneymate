package com.saicharan.moneymate.util

import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

private val DISPLAY_DATE: DateTimeFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH)
private val MONTH_LONG: DateTimeFormatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH)
private val MONTH_SHORT: DateTimeFormatter = DateTimeFormatter.ofPattern("MMM", Locale.ENGLISH)

fun LocalDate.toDisplayDate(today: LocalDate = LocalDate.now()): String = when (this) {
    today -> "Today"
    today.minusDays(1) -> "Yesterday"
    else -> format(DISPLAY_DATE)
}

fun YearMonth.toLongLabel(): String = format(MONTH_LONG)

fun YearMonth.toShortLabel(): String = format(MONTH_SHORT)

/** Material DatePicker works in UTC-midnight millis. */
fun LocalDate.toPickerMillis(): Long = atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

fun Long.fromPickerMillis(): LocalDate =
    java.time.Instant.ofEpochMilli(this).atZone(ZoneOffset.UTC).toLocalDate()
