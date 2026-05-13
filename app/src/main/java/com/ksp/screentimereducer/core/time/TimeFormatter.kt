package com.ksp.screentimereducer.core.time

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit
import kotlin.math.max

/**
 * Helpers for formatting durations / dates consistently across the UI.
 *
 * All durations are in milliseconds. We do not depend on Joda or Kotlinx-datetime
 * to keep the binary lean — `java.time` (desugared on minSdk 26+ here) is enough.
 */
object TimeFormatter {

    fun durationCompact(ms: Long): String {
        val safe = max(0L, ms)
        val hours = TimeUnit.MILLISECONDS.toHours(safe)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(safe) % 60
        return when {
            hours > 0 -> "${hours}h ${minutes}m"
            minutes > 0 -> "${minutes}m"
            else -> "${TimeUnit.MILLISECONDS.toSeconds(safe)}s"
        }
    }

    fun durationVerbose(ms: Long): String {
        val safe = max(0L, ms)
        val hours = TimeUnit.MILLISECONDS.toHours(safe)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(safe) % 60
        val sec = TimeUnit.MILLISECONDS.toSeconds(safe) % 60
        val parts = buildList {
            if (hours > 0) add("$hours hr")
            if (minutes > 0) add("$minutes min")
            if (hours == 0L && minutes == 0L) add("$sec sec")
        }
        return parts.joinToString(" ")
    }

    fun hourLabel(hour: Int): String = "%02d".format(hour)

    fun shortDate(date: LocalDate): String =
        date.format(DateTimeFormatter.ofPattern("MMM d"))

    fun dayOfWeek(date: LocalDate): String =
        date.format(DateTimeFormatter.ofPattern("EEE"))

    fun timestampToLocalDate(timestamp: Long, zone: ZoneId = ZoneId.systemDefault()): LocalDate =
        Instant.ofEpochMilli(timestamp).atZone(zone).toLocalDate()

    fun timestampToLocalDateTime(timestamp: Long, zone: ZoneId = ZoneId.systemDefault()): LocalDateTime =
        Instant.ofEpochMilli(timestamp).atZone(zone).toLocalDateTime()
}
