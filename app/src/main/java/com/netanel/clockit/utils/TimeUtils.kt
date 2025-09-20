package com.netanel.clockit.utils

import java.util.Locale
import kotlin.math.min

object TimeUtils {

    fun minutesToHHmm(minutes: Int): String {
        val h = (minutes / 60).coerceAtLeast(0)
        val m = (minutes % 60).coerceAtLeast(0)
        return String.format(Locale.getDefault(), "%02d:%02d", h, m)
    }

    /**
     * קלט גמיש:
     *  - "HH:mm"
     *  - "H.mm" או "HH.mm"  => דקות (לא עשרוניות)
     *  - "HHmm" (3-4 ספרות) => HH:mm
     */
    fun parseToMinutes(input: String): Int? {
        val s = input.trim()
        if (s.isEmpty()) return null

        // HH:mm
        if (s.contains(":")) {
            val parts = s.split(":")
            if (parts.size == 2) {
                val h = parts[0].toIntOrNull() ?: return null
                val m = parts[1].toIntOrNull() ?: return null
                if (m in 0..59) return h * 60 + m
            }
            return null
        }

        // H.mm / HH.mm -> דקות
        if (s.contains(".") || s.contains(",")) {
            val sep = if (s.contains(".")) "." else ","
            val parts = s.split(sep)
            if (parts.size == 2) {
                val h = parts[0].toIntOrNull() ?: return null
                val mmRaw = parts[1].padEnd(2, '0').take(2) // "1" -> "10"
                val m = mmRaw.toIntOrNull() ?: return null
                val clamped = min(m, 59)
                return h * 60 + clamped
            }
            return null
        }

        // HHmm (815 -> 08:15)
        if (s.length in 3..4 && s.all { it.isDigit() }) {
            val (hPart, mPart) = s.dropLast(2) to s.takeLast(2)
            val h = hPart.toIntOrNull() ?: return null
            val m = mPart.toIntOrNull() ?: return null
            if (m in 0..59) return h * 60 + m
            return null
        }

        // רק שעות (למשל "8")
        return s.toIntOrNull()?.let { it * 60 }
    }
}
