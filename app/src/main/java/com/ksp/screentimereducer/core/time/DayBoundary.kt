package com.ksp.screentimereducer.core.time

import java.time.LocalDate
import java.time.ZoneId

/** Resolves the start/end-of-day timestamps for the device's current zone. */
object DayBoundary {

    fun startOfToday(zone: ZoneId = ZoneId.systemDefault()): Long =
        LocalDate.now(zone).atStartOfDay(zone).toInstant().toEpochMilli()

    fun endOfToday(zone: ZoneId = ZoneId.systemDefault()): Long =
        LocalDate.now(zone).plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli() - 1

    fun startOf(date: LocalDate, zone: ZoneId = ZoneId.systemDefault()): Long =
        date.atStartOfDay(zone).toInstant().toEpochMilli()

    fun startOfWeek(zone: ZoneId = ZoneId.systemDefault()): Long {
        val today = LocalDate.now(zone)
        // Sunday-start; rotate to Monday by subtracting (DOW.value - 1)
        val monday = today.minusDays(((today.dayOfWeek.value + 6) % 7).toLong())
        return monday.atStartOfDay(zone).toInstant().toEpochMilli()
    }
}
